const functions = require("firebase-functions");
const admin = require("firebase-admin");
const geofire = require("geofire-common");
const { Resend } = require("resend");
admin.initializeApp();
const db = admin.firestore();

// ---------------------------------------------------------------
// Localized notification templates
// ---------------------------------------------------------------
const templates = {
  en: {
    newReportTitle: "A cat needs help near you 🐾",
    newReportBody: (dist) => `A stray cat was reported ${dist} km away. Can you help?`,
    helpedTitle: "Someone helped the cat you reported 💗",
    helpedBody: "A community member just stepped in to help the cat you reported!",
    adoptedTitle: "Cat Adopted! 💜",
    adoptedBody: "The cat you reported has found help and a safe home 💜",
    newStationTitle: "New feeding station nearby 🐾",
    newStationBody: (dist) => `A feeding station was added ${dist} km away.`,
  },
  es: {
    newReportTitle: "Un gato necesita ayuda cerca 🐾",
    newReportBody: (dist) => `Se reportó un gato a ${dist} km. ¿Puedes ayudar?`,
    helpedTitle: "Alguien ayudó al gato que reportaste 💗",
    helpedBody: "¡Un miembro de la comunidad acaba de ayudar al gato que reportaste!",
    adoptedTitle: "¡Gato adoptado! 💜",
    adoptedBody: "El gato que reportaste ha encontrado ayuda y un hogar seguro 💜",
    newStationTitle: "Nuevo punto de alimentación cerca 🐾",
    newStationBody: (dist) => `Se añadió un punto de alimentación a ${dist} km.`,
  },
  fr: {
    newReportTitle: "Un chat a besoin d'aide près de chez vous 🐾",
    newReportBody: (dist) => `Un chat a été signalé à ${dist} km. Pouvez-vous aider ?`,
    helpedTitle: "Quelqu'un a aidé le chat que vous avez signalé 💗",
    helpedBody: "Un membre de la communauté est intervenu pour aider le chat que vous avez signalé !",
    adoptedTitle: "Chat adopté ! 💜",
    adoptedBody: "Le chat que vous avez signalé a trouvé de l'aide et un foyer sûr 💜",
    newStationTitle: "Nouveau point de nourrissage à proximité 🐾",
    newStationBody: (dist) => `Un point de nourrissage a été ajouté à ${dist} km.`,
  },
  ar: {
    newReportTitle: "قطة تحتاج إلى مساعدة بالقرب منك 🐾",
    newReportBody: (dist) => `تم الإبلاغ عن قطة على بعد ${dist} كم. هل يمكنك المساعدة؟`,
    helpedTitle: "قام شخص ما بمساعدة القطة التي أبلغت عنها 💗",
    helpedBody: "قام أحد أعضاء المجتمع بمساعدة القطة التي أبلغت عنها للتو!",
    adoptedTitle: "تم تبني القطة! 💜",
    adoptedBody: "القطة التي أبلغت عنها وجدت المساعدة ومنزلاً آمناً 💜",
    newStationTitle: "محطة تغذية جديدة بالقرب منك 🐾",
    newStationBody: (dist) => `تمت إضافة محطة تغذية على بعد ${dist} كم.`,
  },
};

function getTemplate(lang) {
  return templates[lang] || templates.en;
}

// ---------------------------------------------------------------
// Helper to send FCM message to a single user
// ---------------------------------------------------------------
async function sendNotificationToUser(userId, title, body) {
  if (!userId) return;
  const userDoc = await db.collection("users").doc(userId).get();
  if (!userDoc.exists) return;
  const userData = userDoc.data();
  if (!userData.fcmToken) return;

  try {
    await admin.messaging().send({
      token: userData.fcmToken,
      notification: { title, body },
    });
  } catch (err) {
    console.error("Error sending FCM to user:", userId, err);
  }
}

// ---------------------------------------------------------------
// 1. New report created -> notify users within 20km + bump stats
// ---------------------------------------------------------------
exports.onReportCreated = functions.firestore
  .document("reports/{reportId}")
  .onCreate(async (snap) => {
    const report = snap.data();

    // Increment reporter's reportedCatsCount and catBadges
    if (report.reportedBy) {
      await db.collection("users").doc(report.reportedBy).set(
        {
          reportedCatsCount: admin.firestore.FieldValue.increment(1),
          catBadges: admin.firestore.FieldValue.increment(1)
        },
        { merge: true }
      );
    }

    if (!report.latitude || !report.longitude) return null;

    const center = [report.latitude, report.longitude];
    const radiusInKm = 20;
    const radiusInM = radiusInKm * 1000;

    await db.collection("statistics").doc("global").set(
      { totalCatsReported: admin.firestore.FieldValue.increment(1) },
      { merge: true }
    );

    const bounds = geofire.geohashQueryBounds(center, radiusInM);
    const messages = [];

    for (const b of bounds) {
      const q = await db.collection("users").orderBy("geohash").startAt(b[0]).endAt(b[1]).get();
      q.forEach((doc) => {
        const u = doc.data();
        if (doc.id === report.reportedBy) return; // Don't notify the reporter themselves
        if (!u.latitude || !u.longitude || !u.fcmToken) return;

        const distInKm = geofire.distanceBetween([u.latitude, u.longitude], center);
        if (distInKm <= radiusInKm) {
          const formattedDist = distInKm < 1 ? distInKm.toFixed(2) : distInKm.toFixed(1);
          const t = getTemplate(u.preferredLanguage || "en");
          messages.push({
            token: u.fcmToken,
            notification: {
              title: t.newReportTitle,
              body: t.newReportBody(formattedDist),
            },
          });
        }
      });
    }

    if (messages.length === 0) return null;
    return admin.messaging().sendEach(messages);
  });

// ---------------------------------------------------------------
// 2. Interaction added ("I Reached This Cat") -> update count
// ---------------------------------------------------------------
exports.onReachCreated = functions.firestore
  .document("reports/{reportId}/reaches/{userId}")
  .onCreate(async (snap, context) => {
    return db.collection("reports").doc(context.params.reportId).set(
      { reachedCount: admin.firestore.FieldValue.increment(1) },
      { merge: true }
    );
  });

// ---------------------------------------------------------------
// 3. Report marked as helped/adopted/rescued -> bump stats & notify reporter
// ---------------------------------------------------------------
exports.onReportUpdated = functions.firestore
  .document("reports/{reportId}")
  .onUpdate(async (change) => {
    const before = change.before.data();
    const after = change.after.data();

    const updates = {};
    const reporterId = after.reportedBy;

    if (before.status !== "helped" && after.status === "helped") {
      updates.totalCatsHelped = admin.firestore.FieldValue.increment(1);
      updates.totalCatsRescued = admin.firestore.FieldValue.increment(1);

      if (reporterId && reporterId !== after.helpedBy) {
        const reporterDoc = await db.collection("users").doc(reporterId).get();
        const lang = reporterDoc.exists ? (reporterDoc.data().preferredLanguage || "en") : "en";
        const t = getTemplate(lang);
        await sendNotificationToUser(reporterId, t.helpedTitle, t.helpedBody);

        if (reporterDoc.exists && reporterDoc.data().email) {
          await db.collection("mail").add({
            to: [reporterDoc.data().email],
            userId: reporterId,
            template: "cat_helped",
            variables: {
              catDescription: after.description || "Stray cat"
            }
          });
        }
      }
    }

    if (before.status !== "adopted" && after.status === "adopted") {
      updates.totalAdoptedCats = admin.firestore.FieldValue.increment(1);
      updates.totalCatsHelped = admin.firestore.FieldValue.increment(1);
      updates.totalCatsRescued = admin.firestore.FieldValue.increment(1);

      if (reporterId && reporterId !== after.adoptedBy) {
        const reporterDoc = await db.collection("users").doc(reporterId).get();
        const lang = reporterDoc.exists ? (reporterDoc.data().preferredLanguage || "en") : "en";
        const t = getTemplate(lang);
        await sendNotificationToUser(reporterId, t.adoptedTitle, t.adoptedBody);

        if (reporterDoc.exists && reporterDoc.data().email) {
          await db.collection("mail").add({
            to: [reporterDoc.data().email],
            userId: reporterId,
            template: "cat_adopted",
            variables: {
              catDescription: after.description || "Stray cat"
            }
          });
        }
      }
    }

    if (before.status !== "rescued" && after.status === "rescued") {
      updates.totalCatsHelped = admin.firestore.FieldValue.increment(1);
      updates.totalCatsRescued = admin.firestore.FieldValue.increment(1);
      await change.after.ref.set(
        { rescuedAt: admin.firestore.FieldValue.serverTimestamp() },
        { merge: true }
      );
    }

    // Trigger general status update email for other transitions
    if (before.status !== after.status && after.status !== "helped" && after.status !== "adopted" && after.status !== "open") {
      if (reporterId) {
        const reporterDoc = await db.collection("users").doc(reporterId).get();
        if (reporterDoc.exists && reporterDoc.data().email) {
          await db.collection("mail").add({
            to: [reporterDoc.data().email],
            userId: reporterId,
            template: "rescue_update",
            variables: {
              catDescription: after.description || "Stray cat",
              status: after.status
            }
          });
        }
      }
    }

    if (Object.keys(updates).length > 0) {
      await db.collection("statistics").doc("global").set(updates, { merge: true });
    }
    return null;
  });

// ---------------------------------------------------------------
// 4. New feeding station -> notify nearby users + bump stats
// ---------------------------------------------------------------
exports.onStationCreated = functions.firestore
  .document("feedingStations/{stationId}")
  .onCreate(async (snap) => {
    const station = snap.data();
    if (!station.latitude || !station.longitude) return null;

    const center = [station.latitude, station.longitude];
    const radiusInKm = 20;
    const radiusInM = radiusInKm * 1000;

    await db.collection("statistics").doc("global").set(
      { totalFeedingStations: admin.firestore.FieldValue.increment(1) },
      { merge: true }
    );

    const bounds = geofire.geohashQueryBounds(center, radiusInM);
    const messages = [];
    for (const b of bounds) {
      const q = await db.collection("users").orderBy("geohash").startAt(b[0]).endAt(b[1]).get();
      q.forEach((doc) => {
        const u = doc.data();
        if (doc.id === station.createdBy) return;
        if (!u.latitude || !u.longitude || !u.fcmToken) return;

        const distInKm = geofire.distanceBetween([u.latitude, u.longitude], center);
        if (distInKm <= radiusInKm) {
          const formattedDist = distInKm < 1 ? distInKm.toFixed(2) : distInKm.toFixed(1);
          const t = getTemplate(u.preferredLanguage || "en");
          messages.push({
            token: u.fcmToken,
            notification: {
              title: t.newStationTitle,
              body: t.newStationBody(formattedDist),
            },
          });
        }
      });
    }
    if (messages.length === 0) return null;
    return admin.messaging().sendEach(messages);
  });

// ---------------------------------------------------------------
// 5. Feeding station reached -> update count + stats
// ---------------------------------------------------------------
exports.onStationReachCreated = functions.firestore
  .document("feedingStations/{stationId}/reaches/{userId}")
  .onCreate(async (snap, context) => {
    await db.collection("feedingStations").doc(context.params.stationId).set(
      { reachedCount: admin.firestore.FieldValue.increment(1), lastUpdated: admin.firestore.FieldValue.serverTimestamp() },
      { merge: true }
    );
    return db.collection("statistics").doc("global").set(
      { totalFeedingStationsReached: admin.firestore.FieldValue.increment(1) },
      { merge: true }
    );
  });

// ---------------------------------------------------------------
// 6. User Action created -> calculate rewardAmount + increment user's totalStars & rescueStars
// ---------------------------------------------------------------
exports.onUserActionCreated = functions.firestore
  .document("users/{userId}/actions/{actionId}")
  .onCreate(async (snap, context) => {
    const action = snap.data();
    let rewardAmount = action.rewardAmount || action.starsEarned || 0;
    
    if (action.actionType === "feed_cat") {
      rewardAmount = 2;
    } else if (action.actionType === "vet_visit" || action.actionType === "vet") {
      rewardAmount = 3;
    } else if (action.actionType === "fill_station") {
      rewardAmount = 5;
    } else if (action.actionType === "adopt_cat" || action.actionType === "rescue") {
      rewardAmount = 10;
    }
    
    await snap.ref.set({ rewardAmount: rewardAmount }, { merge: true });
    
    if (rewardAmount > 0) {
      await db.collection("users").doc(context.params.userId).set(
        { 
          totalStars: admin.firestore.FieldValue.increment(rewardAmount),
          rescueStars: admin.firestore.FieldValue.increment(rewardAmount)
        },
        { merge: true }
      );
    }
    return null;
  });

// ---------------------------------------------------------------
// 6b. Adoption completed -> update rescue statistics
// ---------------------------------------------------------------
exports.onAdoptionCompleted = functions.firestore
  .document("reports/{reportId}")
  .onUpdate(async (change) => {
    const before = change.before.data();
    const after = change.after.data();
    if (before.status !== "adopted" && after.status === "adopted") {
      await db.collection("statistics").doc("global").set(
        {
          totalAdoptedCats: admin.firestore.FieldValue.increment(1),
          totalCatsRescued: admin.firestore.FieldValue.increment(1)
        },
        { merge: true }
      );
    }
    return null;
  });

// ---------------------------------------------------------------
// 7. New user registered -> bump registered users stat & trigger welcome email once
// ---------------------------------------------------------------
exports.onUserCreated = functions.firestore
  .document("users/{userId}")
  .onCreate(async (snap, context) => {
    const userData = snap.data() || {};
    const userId = context.params.userId;

    await db.collection("statistics").doc("global").set(
      { 
        totalUsers: admin.firestore.FieldValue.increment(1),
        totalRegisteredUsers: admin.firestore.FieldValue.increment(1)
      },
      { merge: true }
    );

    if (userData.email && userData.welcomeEmailSent !== true) {
      const userLang = userData.preferredLanguage || "en";
      await db.collection("mail").add({
        to: [userData.email.trim()],
        userId: userId,
        template: "welcome",
        createdAt: admin.firestore.FieldValue.serverTimestamp()
      });
      await snap.ref.update({ welcomeEmailSent: true });
    }
    return null;
  });

// ---------------------------------------------------------------
// 8. Secure Resend Email Pipeline
// ---------------------------------------------------------------

async function getUserPreferredLanguage(email, userId) {
  if (userId) {
    try {
      const userDoc = await db.collection("users").doc(userId).get();
      if (userDoc.exists && userDoc.data().preferredLanguage) {
        return userDoc.data().preferredLanguage;
      }
    } catch (err) {
      console.error("Error looking up user language by ID:", err);
    }
  }
  if (email) {
    try {
      const userQuery = await db.collection("users").where("email", "==", email.trim()).limit(1).get();
      if (!userQuery.empty) {
        const userData = userQuery.docs[0].data();
        if (userData.preferredLanguage) {
          return userData.preferredLanguage;
        }
      }
    } catch (err) {
      console.error("Error looking up user language by email:", err);
    }
  }
  return "en";
}

const emailTemplates = {
  welcome: {
    en: {
      subject: "Welcome to TinyPaws! 🐾",
      title: "Welcome to TinyPaws! 🐾",
      intro: "Thank you for joining our community!",
      body: "TinyPaws is dedicated to helping stray cats find care through community reporting, feeding station tracking, and rescue stories.",
      buttonText: "Open TinyPaws App",
      footer: "Together we save lives, one tiny paw at a time. ❤️"
    },
    ar: {
      subject: "مرحباً بك في تايني باوز! 🐾",
      title: "مرحباً بك في تايني باوز! 🐾",
      intro: "شكراً لانضمامك إلى مجتمعنا!",
      body: "تايني باوز مخصص لمساعدة القطط الشاردة في العثور على الرعاية من خلال بلاغات المجتمع، وتتبع محطات التغذية، وقصص الإنقاذ.",
      buttonText: "افتح تطبيق تايني باوز",
      footer: "معاً ننقذ الأرواح، مخلباً تلو الآخر. ❤️"
    },
    fr: {
      subject: "Bienvenue sur TinyPaws ! 🐾",
      title: "Bienvenue sur TinyPaws ! 🐾",
      intro: "Merci de rejoindre notre communauté !",
      body: "TinyPaws est dédié à aider les chats errants à trouver des soins grâce aux signalements de la communauté, au suivi des stations de nourrissage et aux histoires de sauvetage.",
      buttonText: "Ouvrir l'application",
      footer: "Ensemble, nous sauvons des vies, une petite patte à la fois. ❤️"
    },
    es: {
      subject: "¡Bienvenido a TinyPaws! 🐾",
      title: "¡Bienvenido a TinyPaws! 🐾",
      intro: "¡Gracias por unirte a nuestra comunidad!",
      body: "TinyPaws se dedica a ayudar a los gatos callejeros a encontrar cuidados a través de reportes comunitarios, seguimiento de puntos de alimentación e historias de rescate.",
      buttonText: "Abrir la aplicación",
      footer: "Juntos salvamos vidas, una patita a la vez. ❤️"
    }
  },
  cat_helped: {
    en: {
      subject: "Someone helped the cat you reported! 💗",
      title: "Wonderful News! 💗",
      intro: "Someone stepped in to help!",
      body: (vars) => `A compassionate community member has helped the cat you reported: "${vars.catDescription || "Stray cat"}".`,
      buttonText: "View Rescue Details",
      footer: "Thank you for being a hero. Your reports save lives! 🐾"
    },
    ar: {
      subject: "قام شخص ما بمساعدة القطة التي أبلغت عنها! 💗",
      title: "أخبار رائعة! 💗",
      intro: "تدخل شخص ما لتقديم المساعدة!",
      body: (vars) => `لقد قام أحد أعضاء المجتمع الرحيم بمساعدة القطة التي أبلغت عنها: "${vars.catDescription || "قطة شاردة"}".`,
      buttonText: "عرض تفاصيل الإنقاذ",
      footer: "شكراً لكونك بطلاً. بلاغاتك تنقذ الأرواح! 🐾"
    },
    fr: {
      subject: "Quelqu'un a aidé le chat que vous avez signalé ! 💗",
      title: "Excellente nouvelle ! 💗",
      intro: "Quelqu'un est intervenu pour aider !",
      body: (vars) => `Un membre compatissant de la communauté a aidé le chat que vous avez signalé : "${vars.catDescription || "Chat errant"}".`,
      buttonText: "Voir les détails",
      footer: "Merci d'être un héros. Vos signalements sauvent des vies ! 🐾"
    },
    es: {
      subject: "¡Alguien ayudó al gato que reportaste! 💗",
      title: "¡Maravillosas noticias! 💗",
      intro: "¡Alguien ha intervenido para ayudar!",
      body: (vars) => `Un miembro compasivo de la comunidad ha ayudado al gato que reportaste: "${vars.catDescription || "Gato callejero"}".`,
      buttonText: "Ver detalles de rescate",
      footer: "Gracias por ser un héroe. ¡Tus reportes salvan vidas! 🐾"
    }
  },
  cat_adopted: {
    en: {
      subject: "A cat you reported has been adopted! 💜",
      title: "A Happy Ending! 💜",
      intro: "Adopted and loved!",
      body: (vars) => `Great joy! The cat you reported ("${vars.catDescription || "Stray cat"}") has officially been adopted and found a safe, warm, and loving permanent home.`,
      buttonText: "See Happy Endings",
      footer: "This miracle happened because you cared. Thank you! 🐾"
    },
    ar: {
      subject: "تم تبني القطة التي أبلغت عنها! 💜",
      title: "نهاية سعيدة! 💜",
      intro: "تم التبني بحب!",
      body: (vars) => `فرحة عارمة! القطة الشاردة التي أبلغت عنها ("${vars.catDescription || "قطة شاردة"}") تم تبنيها رسمياً ووجدت منزلاً آمناً ومحباً ودائماً.`,
      buttonText: "رؤية النهايات السعيدة",
      footer: "حدثت هذه المعجزة لأنك اهتممت. شكراً لك! 🐾"
    },
    fr: {
      subject: "Un chat que vous avez signalé a été adopté ! 💜",
      title: "Une fin heureuse ! 💜",
      intro: "Adopté et choyé !",
      body: (vars) => `Grande joie ! Le chat que vous avez signalé ("${vars.catDescription || "Chat errant"}") a officiellement été adopté et a trouvé un foyer sûr, aimant et permanent.`,
      buttonText: "Voir les fins heureuses",
      footer: "Ce miracle est arrivé parce que vous vous en souciez. Merci ! 🐾"
    },
    es: {
      subject: "¡Un gato que reportaste ha sido adoptado! 💜",
      title: "¡Un final feliz! 💜",
      intro: "¡Adoptado y amado!",
      body: (vars) => `¡Gran alegría! El gato que reportaste ("${vars.catDescription || "Gato callejero"}") ha been adoptado oficialmente y ha encontrado un hogar seguro, amoroso y permanente.`,
      buttonText: "Ver finales felices",
      footer: "Este milagro ocurrió porque te importó. ¡Gracias! 🐾"
    }
  },
  rescue_update: {
    en: {
      subject: "Rescue update on TinyPaws! 🐾",
      title: "Rescue Mission Update! 🐾",
      intro: "New update on a cat rescue",
      body: (vars) => `There is a new update on the stray cat you reported ("${vars.catDescription || "Stray cat"}"). The rescue status has changed to: **${vars.status || "Updated"}**.`,
      buttonText: "Check Report Status",
      footer: "Together we make the world a better place for our feline friends. ❤️"
    },
    ar: {
      subject: "تحديث عملية الإنقاذ على تايني باوز! 🐾",
      title: "تحديث مهمة الإنقاذ! 🐾",
      intro: "تحديث جديد لإنقاذ القطة",
      body: (vars) => `هناك تحديث جديد بخصوص القطة الشاردة التي أبلغت عنها ("${vars.catDescription || "قطة شاردة"}"). تغيرت حالة الإنقاذ إلى: **${vars.status || "محدثة"}**.`,
      buttonText: "التحقق من حالة البلاغ",
      footer: "معاً نجعل العالم مكاناً أفضل لأصدقائنا الأليفين. ❤️"
    },
    fr: {
      subject: "Mise à jour du sauvetage sur TinyPaws ! 🐾",
      title: "Mise à jour de la mission ! 🐾",
      intro: "Nouveau statut pour le chat",
      body: (vars) => `Il y a une nouvelle mise à jour concernant le chat errant que vous avez signalé ("${vars.catDescription || "Chat errant"}"). Le statut du sauvetage est devenu : **${vars.status || "Mis à jour"}**.`,
      buttonText: "Vérifier le statut",
      footer: "Ensemble, nous rendons le monde meilleur pour nos amis les félins. ❤️"
    },
    es: {
      subject: "¡Actualización de rescate en TinyPaws! 🐾",
      title: "¡Actualización de la misión de rescate! 🐾",
      intro: "Nuevo estado del gato",
      body: (vars) => `Hay una nueva actualización sobre el gato callejero que reportaste ("${vars.catDescription || "Gato callejero"}"). El estado del rescate ha cambiado a: **${vars.status || "Actualizado"}**.`,
      buttonText: "Verificar estado",
      footer: "Juntos hacemos del mundo un lugar mejor para nuestros amigos felinos. ❤️"
    }
  }
};

function buildEmailHtml(templateName, lang, variables = {}) {
  const tCategory = emailTemplates[templateName] || emailTemplates.welcome;
  const t = tCategory[lang] || tCategory.en;

  const subject = typeof t.subject === "function" ? t.subject(variables) : t.subject;
  const title = typeof t.title === "function" ? t.title(variables) : t.title;
  const intro = typeof t.intro === "function" ? t.intro(variables) : t.intro;
  const body = typeof t.body === "function" ? t.body(variables) : t.body;
  const buttonText = typeof t.buttonText === "function" ? t.buttonText(variables) : t.buttonText;
  const footer = typeof t.footer === "function" ? t.footer(variables) : t.footer;

  const isRtl = lang === "ar";
  const dirAttr = isRtl ? 'dir="rtl"' : 'dir="ltr"';
  const textAlign = isRtl ? 'right' : 'left';

  const html = `
    <!DOCTYPE html>
    <html lang="${lang}" ${dirAttr}>
    <head>
      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <title>${subject}</title>
      <style>
        body {
          background-color: #FFF8F6;
          margin: 0;
          padding: 0;
          font-family: 'Quicksand', 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
          -webkit-font-smoothing: antialiased;
        }
        .container {
          max-width: 600px;
          margin: 40px auto;
          background: #ffffff;
          border-radius: 16px;
          overflow: hidden;
          box-shadow: 0 4px 12px rgba(96, 24, 39, 0.08);
          border: 1px solid #F4D3D7;
        }
        .header {
          background-color: #601827;
          color: #ffffff;
          text-align: center;
          padding: 30px 20px;
        }
        .header h1 {
          margin: 0;
          font-size: 26px;
          font-weight: 700;
          letter-spacing: 0.5px;
        }
        .content {
          padding: 40px 30px;
          color: #2D1B1E;
          line-height: 1.6;
          text-align: ${textAlign};
        }
        .content h2 {
          color: #8C4F5E;
          font-size: 22px;
          margin-top: 0;
          margin-bottom: 12px;
        }
        .content p {
          font-size: 16px;
          margin-bottom: 24px;
        }
        .card {
          background-color: #FFF5F6;
          border: 1px solid #FADCE0;
          border-radius: 12px;
          padding: 20px;
          margin-bottom: 30px;
          font-size: 16px;
          font-weight: 500;
          color: #601827;
        }
        .btn {
          display: inline-block;
          background-color: #8C4F5E;
          color: #ffffff !important;
          text-decoration: none;
          padding: 14px 28px;
          font-size: 16px;
          font-weight: bold;
          border-radius: 30px;
          box-shadow: 0 4px 6px rgba(140, 79, 94, 0.2);
          transition: background-color 0.2s ease;
        }
        .btn:hover {
          background-color: #601827;
        }
        .footer {
          background-color: #FFF0F2;
          border-top: 1px solid #FADCE0;
          padding: 24px;
          text-align: center;
          color: #7A5861;
          font-size: 13px;
        }
      </style>
    </head>
    <body>
      <div class="container">
        <div class="header">
          <h1>TinyPaws 🐾</h1>
        </div>
        <div class="content">
          <h2>${title}</h2>
          <p style="font-size: 18px; font-weight: 600; color: #8C4F5E;">${intro}</p>
          <div class="card">
            ${body}
          </div>
          <div style="text-align: center; margin-top: 30px; margin-bottom: 10px;">
            <a href="https://tinypaws.app" class="btn">${buttonText}</a>
          </div>
        </div>
        <div class="footer">
          <p style="margin: 0; font-weight: 600;">${footer}</p>
          <p style="margin: 10px 0 0 0; font-size: 11px;">© 2026 TinyPaws Stray Rescue Network. All rights reserved.</p>
        </div>
      </div>
    </body>
    </html>
  `;

  return { subject, html };
}

exports.onMailCreated = functions.runWith({ secrets: ["RESEND_API_KEY"] })
  .firestore.document("mail/{mailId}")
  .onCreate(async (snap, context) => {
    const mailId = context.params.mailId;
    const mailData = snap.data();

    if (mailData.status === "sent" || mailData.status === "failed") {
      return null;
    }

    const resendApiKey = process.env.RESEND_API_KEY;
    if (!resendApiKey) {
      console.error("RESEND_API_KEY is not configured in Firebase Secrets!");
      await snap.ref.update({
        status: "failed",
        error: "RESEND_API_KEY secret is missing on server",
        updatedAt: admin.firestore.FieldValue.serverTimestamp()
      });
      return null;
    }

    const resend = new Resend(resendApiKey);
    const fromAddress = process.env.RESEND_FROM_EMAIL || "TinyPaws Team <welcome@tinypaws.org>";

    let subject, html;

    if (mailData.template) {
      const toEmail = (mailData.to && mailData.to[0]) || "";
      const userId = mailData.userId || "";
      const lang = await getUserPreferredLanguage(toEmail, userId);

      const built = buildEmailHtml(mailData.template, lang, mailData.variables || {});
      subject = built.subject;
      html = built.html;
    } else if (mailData.message) {
      subject = mailData.message.subject || "TinyPaws Update 🐾";
      html = mailData.message.html || mailData.message.text || "";
    } else {
      console.warn("Mail document does not contain message or template!");
      await snap.ref.update({
        status: "failed",
        error: "No template or message field found",
        updatedAt: admin.firestore.FieldValue.serverTimestamp()
      });
      return null;
    }

    try {
      const response = await resend.emails.send({
        from: fromAddress,
        to: mailData.to,
        subject: subject,
        html: html
      });

      console.log("Email sent successfully via Resend:", response);
      await snap.ref.update({
        status: "sent",
        sentAt: admin.firestore.FieldValue.serverTimestamp(),
        resendId: response.data ? response.data.id : (response.id || "")
      });
    } catch (err) {
      console.error("Error sending email via Resend:", err);
      await snap.ref.update({
        status: "failed",
        error: err.message || String(err),
        updatedAt: admin.firestore.FieldValue.serverTimestamp()
      });
    }
    return null;
  });

