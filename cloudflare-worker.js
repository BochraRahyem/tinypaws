export default {
  async fetch(request, env) {
    if (request.method === "OPTIONS") {
      return new Response(null, {
        headers: {
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Methods": "POST, OPTIONS",
          "Access-Control-Allow-Headers": "Content-Type, Authorization"
        }
      });
    }

    if (request.method !== "POST") {
      return new Response("Method Not Allowed", { status: 405 });
    }

    // ---------------------------------------------------------------
    // Photo upload relay: POST /upload
    // Authenticated with a Firebase ID token; forwards the image to imgbb
    // using the IMGBB_API_KEY Worker secret (never shipped in the APK).
    // Free-tier replacement for Firebase Storage.
    // ---------------------------------------------------------------
    const reqUrl = new URL(request.url);
    if (reqUrl.pathname === "/upload") {
      try {
        const authHeader = request.headers.get("Authorization") || "";
        const idToken = authHeader.replace("Bearer ", "").trim();
        if (!idToken) {
          return new Response(JSON.stringify({ error: "Missing Authorization header" }), { status: 401, headers: { "Content-Type": "application/json" } });
        }

        const FIREBASE_API_KEY = "AIzaSyBmSigm47Rn7JwOyGtUm1Rv3sqQET8Gbr0"; // public client key
        const verifyRes = await fetch(`https://identitytoolkit.googleapis.com/v1/accounts:lookup?key=${FIREBASE_API_KEY}`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ idToken })
        });
        if (!verifyRes.ok) {
          return new Response(JSON.stringify({ error: "Invalid or expired Firebase ID token" }), { status: 401, headers: { "Content-Type": "application/json" } });
        }

        if (!env.IMGBB_API_KEY) {
          return new Response(JSON.stringify({ error: "Image hosting not configured yet" }), { status: 503, headers: { "Content-Type": "application/json" } });
        }

        const imgBuffer = await request.arrayBuffer();
        if (imgBuffer.byteLength === 0) {
          return new Response(JSON.stringify({ error: "Empty image body" }), { status: 400, headers: { "Content-Type": "application/json" } });
        }
        if (imgBuffer.byteLength > 10 * 1024 * 1024) {
          return new Response(JSON.stringify({ error: "Image too large (max 10 MB)" }), { status: 413, headers: { "Content-Type": "application/json" } });
        }

        const form = new FormData();
        form.append("image", new Blob([imgBuffer], { type: request.headers.get("Content-Type") || "image/jpeg" }));
        const imgbbRes = await fetch(`https://api.imgbb.com/1/upload?key=${encodeURIComponent(env.IMGBB_API_KEY)}`, {
          method: "POST",
          body: form
        });

        const imgbbData = await imgbbRes.json().catch(() => null);
        if (!imgbbRes.ok || !imgbbData || !imgbbData.success) {
          return new Response(JSON.stringify({ error: "Image host rejected upload", details: JSON.stringify(imgbbData).slice(0, 300) }), { status: 502, headers: { "Content-Type": "application/json" } });
        }

        return new Response(JSON.stringify({
          success: true,
          url: imgbbData.data.display_url || imgbbData.data.url,
          deleteUrl: imgbbData.data.delete_url || null
        }), { status: 200, headers: { "Content-Type": "application/json", "Access-Control-Allow-Origin": "*" } });
      } catch (e) {
        return new Response(JSON.stringify({ error: "Upload failed", message: e.message }), { status: 500, headers: { "Content-Type": "application/json" } });
      }
    }

    try {
      // 1. Authenticate Request via Firebase ID Token
      const authHeader = request.headers.get("Authorization") || "";
      const idToken = authHeader.replace("Bearer ", "").trim();
      
      if (!idToken) {
        return new Response(JSON.stringify({ error: "Missing Authorization header" }), { status: 401, headers: { "Content-Type": "application/json" } });
      }

      const body = await request.json();
      const targetEmail = body.to;
      const lang = body.lang || "en";

      if (!targetEmail) {
        return new Response(JSON.stringify({ error: "Missing 'to' email" }), { status: 400, headers: { "Content-Type": "application/json" } });
      }

      // Verify the ID token using Google Identity Toolkit REST API
      const FIREBASE_API_KEY = "AIzaSyBmSigm47Rn7JwOyGtUm1Rv3sqQET8Gbr0"; // Extracted from your google-services.json (safe for public client, safe here)
      const verifyRes = await fetch(`https://identitytoolkit.googleapis.com/v1/accounts:lookup?key=${FIREBASE_API_KEY}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ idToken: idToken })
      });

      if (!verifyRes.ok) {
        return new Response(JSON.stringify({ error: "Invalid or expired Firebase ID token" }), { status: 401, headers: { "Content-Type": "application/json" } });
      }

      const verifyData = await verifyRes.json();
      const verifiedEmail = verifyData.users[0].email;

      // Ensure users can only trigger welcome emails to their own authenticated email address
      if (verifiedEmail !== targetEmail) {
        return new Response(JSON.stringify({ error: "Unauthorized email target" }), { status: 403, headers: { "Content-Type": "application/json" } });
      }

      // Extract displayName securely from the verified Firebase token
      const displayName = verifyData.users[0].displayName || "there";

      // 2. Build the Email HTML
      const emailHtml = buildEmailHtml("welcome", lang, { displayName });
      
      // 3. Send email using Resend
      const resendRes = await fetch("https://api.resend.com/emails", {
        method: "POST",
        headers: {
          "Authorization": `Bearer ${env.RESEND_API_KEY}`,
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          from: "TinyPaws Team <support@tinypaws.tn>",
          to: targetEmail,
          subject: emailHtml.subject,
          html: emailHtml.html
        })
      });

      if (!resendRes.ok) {
        const errorText = await resendRes.text();
        return new Response(JSON.stringify({ error: "Resend API error", details: errorText }), { status: 500, headers: { "Content-Type": "application/json" } });
      }

      return new Response(JSON.stringify({ success: true }), { status: 200, headers: { "Content-Type": "application/json" } });

    } catch (e) {
      return new Response(JSON.stringify({ error: "Internal Server Error", message: e.message }), { status: 500, headers: { "Content-Type": "application/json" } });
    }
  }
};

// ---------------------------------------------------
// HTML Template Builder (Ported from existing functions)
// ---------------------------------------------------
const emailTemplates = {
  welcome: {
    en: {
      subject: "Welcome to TinyPaws 🐾",
      title: "",
      intro: "",
      body: (vars) => `Hi ${vars.displayName},<br><br>
Welcome to TinyPaws. 🎀🐱<br><br>
I’m so happy you’re here.<br><br>
TinyPaws started with a small idea and a big feeling: the feeling that no cat should be invisible.<br><br>
My name is Bochra, and I’m a 17-year-old founder from Tunisia. I created TinyPaws because I wanted to build a place where caring for stray cats could become easier, where people who want to help could find a way to take action, and where every cat’s story could have a chance to be heard.<br><br>
The idea behind TinyPaws came from noticing something simple: there are so many stray cats around us who need help, and there are so many people with kind hearts who want to help them, but sometimes they don’t know where to start or how to reach them. I wanted to create the bridge between those two sides.<br><br>
TinyPaws is not just about reporting cats. It is about creating a community where a small action can become part of something bigger. A person sharing the location of a cat, someone reaching a reported case, someone filling a feeding station, or someone learning how to care for animals better, every action becomes a piece of a bigger story. 🐾<br><br>
I believe kindness does not have a minimum age. A simple idea created by a teenager in Tunisia can grow into something that helps animals and connects people who care.<br><br>
Through TinyPaws, my mission is to make stray cats seen, supported, and remembered. I want to create a world where helping an animal is not a difficult task, but a simple action that anyone can be part of.<br><br>
Our values are at the heart of everything we do:<br><br>
🎀 Kindness — because every life deserves care.<br>
🐾 Compassion — because noticing a problem is the first step toward changing it.<br>
🌸 Community — because together, we can help more than we ever could alone.<br>
💗 Action — because even the smallest gesture can change a story.<br><br>
Thank you for joining TinyPaws. You are now part of a journey that started with one idea, one purpose, and a lot of love for the little paws that need us. 🎀<br><br>
Every cat has a story. Together, we can help more of them be seen.<br><br>
With love,<br><br>
Bochra Rhayem<br>
Founder of TinyPaws 🐾🎀`,
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
      buttonText: "Ouvrir l'application TinyPaws",
      footer: "Ensemble, nous sauvons des vies, une petite patte à la fois. ❤️"
    },
    es: {
      subject: "¡Bienvenido a TinyPaws! 🐾",
      title: "¡Bienvenido a TinyPaws! 🐾",
      intro: "¡Gracias por unirte a nuestra comunidad!",
      body: "TinyPaws se dedica a ayudar a los gatos callejeros a encontrar cuidado a través de informes de la comunidad, seguimiento de estaciones de alimentación e historias de rescate.",
      buttonText: "Abrir la aplicación TinyPaws",
      footer: "Juntos salvamos vidas, una patita a la vez. ❤️"
    }
  }
};

function escapeHtml(unsafe) {
  return (unsafe || "").replace(/&/g, "&amp;")
                       .replace(/</g, "&lt;")
                       .replace(/>/g, "&gt;")
                       .replace(/"/g, "&quot;")
                       .replace(/'/g, "&#039;");
}

function buildEmailHtml(templateName, lang, vars = {}) {
  const tCategory = emailTemplates[templateName] || emailTemplates.welcome;
  const t = tCategory[lang] || tCategory.en;
  
  if (vars.displayName) {
    vars.displayName = escapeHtml(vars.displayName);
  }

  const subject = typeof t.subject === "function" ? t.subject(vars) : t.subject;
  const title = typeof t.title === "function" ? t.title(vars) : t.title;
  const intro = typeof t.intro === "function" ? t.intro(vars) : t.intro;
  const body = typeof t.body === "function" ? t.body(vars) : t.body;
  const buttonText = typeof t.buttonText === "function" ? t.buttonText(vars) : t.buttonText;
  const footer = typeof t.footer === "function" ? t.footer(vars) : t.footer;
  
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
        body { background-color: #FFF8F6; margin: 0; padding: 0; font-family: 'Quicksand', 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; -webkit-font-smoothing: antialiased; }
        .container { max-width: 600px; margin: 40px auto; background: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 12px rgba(96, 24, 39, 0.08); border: 1px solid #F4D3D7; }
        .header { background-color: #601827; color: #ffffff; text-align: center; padding: 30px 20px; }
        .header h1 { margin: 0; font-size: 26px; font-weight: 700; letter-spacing: 0.5px; }
        .content { padding: 40px 30px; color: #2D1B1E; line-height: 1.6; text-align: ${textAlign}; }
        .content h2 { color: #8C4F5E; font-size: 22px; margin-top: 0; margin-bottom: 12px; }
        .content p { font-size: 16px; margin-bottom: 24px; }
        .card { background-color: #FFF5F6; border: 1px solid #FADCE0; border-radius: 12px; padding: 20px; margin-bottom: 30px; font-size: 16px; font-weight: 500; color: #601827; }
        .btn { display: inline-block; background-color: #8C4F5E; color: #ffffff !important; text-decoration: none; padding: 14px 28px; font-size: 16px; font-weight: bold; border-radius: 30px; box-shadow: 0 4px 6px rgba(140, 79, 94, 0.2); transition: background-color 0.2s ease; }
        .btn:hover { background-color: #601827; }
        .footer { background-color: #FFF0F2; border-top: 1px solid #FADCE0; padding: 24px; text-align: center; color: #7A5861; font-size: 13px; }
      </style>
    </head>
    <body>
      <div class="container">
        <div class="header">
          <h1>TinyPaws 🐾</h1>
        </div>
        <div class="content">
          ${title ? `<h2>${title}</h2>` : ""}
          ${intro ? `<p style="font-size: 18px; font-weight: 600; color: #8C4F5E;">${intro}</p>` : ""}
          <div class="card" style="font-weight: normal;">${body}</div>
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
