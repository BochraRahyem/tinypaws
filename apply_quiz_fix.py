import json
import xml.etree.ElementTree as ET

# Options for Q1 to Q50 in EN, ES, FR, AR
options_db = {
    "en": {
        1: ['4-6 hrs', '8-10 hrs', '12-16 hrs', 'Basically never'],
        2: ['Only when happy', 'Only when hungry', 'When happy, stressed, or healing', 'To clean their ears'],
        3: ['Super happy', 'Threatened or very angry', 'Bored', 'Sleepy'],
        4: ['They are tired', 'They are plotting', 'They trust and love you', 'Dust in their eyes'],
        5: ['A clowder', 'A squad', 'A pack', 'A meow-b'],
        6: ['Cheek rubbing & scent glands', 'Running at full speed', 'Staring into your eyes', 'Loud clapping noises'],
        7: ['Tail', 'Whiskers', 'Eyelashes', 'Claws'],
        8: ['Angry and aggressive', 'Terrified', 'Happy & friendly greeting', 'Extremely sleepy'],
        9: ['1-2 days', '7-14 days', '1 month', '6 weeks'],
        10: ['Hygiene, cooling & stress relief', 'To lose hair', 'To get dirty', 'To attract insects'],
        11: ['Yes, daily', 'No, chocolate is toxic', 'Only dark chocolate', 'Only white chocolate'],
        12: ['Yes, it is essential', 'No, most are lactose intolerant', 'Only for adult cats', 'Yes, all milk is safe'],
        13: ['Vegan diet', 'Strict fruit diet', 'Meat-based (obligate carnivore)', 'High carbohydrate diet'],
        14: ['It tastes bad', 'Contains dangerous parasites & bacteria', 'It is too cold', 'Cats dislike water'],
        15: ['Raw onions', 'Grapes', 'Plain cooked chicken', 'Chocolate'],
        16: ['Once every 3 days', '1-2 scheduled meals daily', '10 times a day', 'Whenever they meow'],
        17: ['Vitamin C', 'Taurine', 'Calcium', 'Iron'],
        18: ['Yes, always', 'No, use Kitten Milk Replacer (KMR)', 'Only skim milk', 'Yes, mixed with water'],
        19: ['Force drink with spoon', 'Feed wet cat food', 'Give fruit juice', 'Add salt to water'],
        20: ['Yes, always', 'No, wet food provides vital hydration', 'Dry food is harmful', 'Cats do not need water'],
        21: ['Trap-Neuter-Return', 'Train-Nourish-Rescue', 'Treat-Name-Relocate', 'Tag-Number-Register'],
        22: ['Collared tag', 'Tipped ear (ear tip)', 'Shaved back', 'Forehead tattoo'],
        23: ['95.0°F - 97.5°F', '100.5°F - 102.5°F', '105.0°F - 108.0°F', '98.6°F exactly'],
        24: ['Purring loudly', 'Matted fur, discharge & lethargy', 'High energy', 'Shiny coat'],
        25: ['Pet it gently', 'Stay away & call Animal Control/Vet', 'Feed it by hand', 'Bring it inside'],
        26: ['It makes them sleepy', 'Extremely toxic & destroys red blood cells', 'Makes fur fall out', 'Has no effect'],
        27: ['From eating grass', 'Contact with infested environments/animals', 'From drinking milk', 'Sleeping too much'],
        28: ['Every 5 years', 'At least once a year', 'Only when severely ill', 'Never'],
        29: ['Hot & dry', 'Cool & slightly damp', 'Bleeding', 'Bright yellow'],
        30: ['To annoy humans', 'Shed nail sheaths & stretch muscles', 'To sharpen teeth', 'To sleep on'],
        31: ['Run towards it shouting', 'Move slowly, crouch low, let it sniff', 'Grab it by tail', 'Corner it quickly'],
        32: ['Take them immediately', 'Observe from distance for 2-4 hours', 'Leave them forever', 'Give them cow\'s milk'],
        33: ['Thick towel, blanket or humane trap', 'Bare hands', 'Cardboard box with no lid', 'Leash and collar'],
        34: ['Ignore it completely', 'Wash with soap/water & seek medical advice', 'Apply perfume', 'Put ice only'],
        35: ['Noon under direct sun', 'Dawn and dusk (crepuscular hours)', 'Midnight only', '3 AM'],
        36: ['Citrus and lemon', 'Feline pheromones (Feliway)', 'Vinegar', 'Ammonia'],
        37: ['It looks nice', 'Builds routine for easy TNR trapping', 'Keeps birds away', 'Makes cats fat'],
        38: ['Extremely relaxed', 'High adrenaline (excited or scared)', 'Half asleep', 'Blind'],
        39: ['Yes, always carry by scruff', 'Avoid if possible; use towel wrap', 'Scruff hard and swing', 'Never touch neck'],
        40: ['Patience, space & consistent feeding schedule', 'Force physical contact', 'Make loud noises', 'Follow them around'],
        41: ['Give cold bath', 'Immediate warmth & sugar/glucose on gums + vet', 'Put in dark closet', 'Feed solid kibble'],
        42: ['Cotton blankets', 'Clean dry straw', 'Wet towels', 'Shredded paper'],
        43: ['Check mouth carefully & gentle compressions + vet', 'Shake upside down', 'Give water', 'Wait 1 hour'],
        44: ['Cat grass', 'Lilies', 'Spider plant', 'Boston fern'],
        45: ['Just constipation', 'Life-threatening urinary blockage emergency', 'Playing', 'Normal behavior'],
        46: ['Pour hydrogen peroxide', 'Clean saline/water & vet treatment', 'Cover with dirt', 'Ignore it'],
        47: ['Chemical dog flea spray', 'Warm bath with Blue Dawn dish soap', 'Alcohol rub', 'Shave all fur'],
        48: ['They smell too strong', 'Cats lack liver enzymes to metabolize oils', 'Oils make cats sneeze', 'No risk'],
        49: ['Hold in arms', 'Closed carrier wrapped in dark towel', 'Cardboard box', 'Open laundry basket'],
        50: ['Save animal at any cost', 'Ensure human safety first', 'Take photos', 'Act quickly without planning']
    },
    "es": {
        1: ['4-6 horas', '8-10 horas', '12-16 horas', 'Prácticamente nunca'],
        2: ['Solo cuando están felices', 'Solo cuando tienen hambre', 'Cuando están felices, estresados o sanando', 'Para limpiarse las orejas'],
        3: ['Súper feliz', 'Amenazado o muy enojado', 'Aburrido', 'Somnoliento'],
        4: ['Están cansados', 'Están tramando algo', 'Confían en ti y te aman', 'Tienen polvo en los ojos'],
        5: ['Una manada (clowder)', 'Un escuadrón', 'Un grupo', 'Un maullido'],
        6: ['Frotar mejillas y glándulas de olor', 'Correr a toda velocidad', 'Mirarte fijamente a los ojos', 'Hacer aplausos fuertes'],
        7: ['Cola', 'Bigotes', 'Pestañas', 'Garras'],
        8: ['Enojado y agresivo', 'Aterrorizado', 'Saludo feliz y amigable', 'Extremadamente somnoliento'],
        9: ['1-2 días', '7-14 días', '1 mes', '6 semanas'],
        10: ['Higiene, enfriamiento y alivio del estrés', 'Para perder pelo', 'Para ensuciarse', 'Para atraer insectos'],
        11: ['Sí, a diario', 'No, el chocolate es tóxico', 'Solo chocolate negro', 'Solo chocolate blanco'],
        12: ['Sí, es esencial', 'No, la mayoría es intolerante a la lactosa', 'Solo para gatos adultos', 'Sí, toda la leche es segura'],
        13: ['Dieta vegana', 'Dieta estricta de frutas', 'A base de carne (carnívoro obligado)', 'Dieta alta en carbohidratos'],
        14: ['Sabe mal', 'Contiene parásitos y bacterias peligrosos', 'Está demasiado fría', 'A los gatos no les gusta el agua'],
        15: ['Cebollas crudas', 'Uvas', 'Pollo cocido simple', 'Chocolate'],
        16: ['Una vez cada 3 días', '1-2 comidas programadas al día', '10 veces al día', 'Cada vez que maúllan'],
        17: ['Vitamina C', 'Taurina', 'Calcio', 'Hierro'],
        18: ['Sí, siempre', 'No, usar sustituto de leche para gatitos (KMR)', 'Solo leche desnatada', 'Sí, mezclada con agua'],
        19: ['Forzar a beber con cuchara', 'Alimentar con comida húmeda', 'Dar jugo de frutas', 'Agregar sal al agua'],
        20: ['Sí, siempre', 'No, la comida húmeda proporciona hidratación vital', 'El pienso seco es dañino', 'Los gatos no necesitan agua'],
        21: ['Captura-Esterilización-Retorno (TNR)', 'Entrenar-Nutrir-Rescatar', 'Tratar-Nombrar-Reubicar', 'Marcar-Numerar-Registrar'],
        22: ['Placa en collar', 'Corte en la oreja (muesca)', 'Espalda afeitada', 'Tatuaje en la frente'],
        23: ['35.0°C - 36.4°C', '38.0°C - 39.2°C', '40.5°C - 42.0°C', '37.0°C exactamente'],
        24: ['Ronronear fuerte', 'Pelo apelmazado, secreción y letargo', 'Mucha energía', 'Pelaje brillante'],
        25: ['Acariciarlo suavemente', 'Mantenerse alejado y llamar al Control de Animales/Veterinario', 'Alimentarlo con la mano', 'Llevarlo adentro'],
        26: ['Los adormece', 'Extremadamente tóxico y destruye glóbulos rojos', 'Hace que se caiga el pelo', 'No tiene efecto'],
        27: ['Por comer hierba', 'Contacto con entornos o animales infestados', 'Por beber leche', 'Por dormir demasiado'],
        28: ['Cada 5 años', 'Al menos una vez al año', 'Solo cuando esté gravemente enfermo', 'Nunca'],
        29: ['Caliente y seca', 'Fresca y ligeramente húmeda', 'Sangrando', 'Amarillo brillante'],
        30: ['Para molestar a los humanos', 'Mudar fundas de uñas y estirar músculos', 'Para afilar dientes', 'Para dormir sobre ellos'],
        31: ['Correr hacia él gritando', 'Moverse despacio, agacharse y dejar que olfatee', 'Agarrarlo por la cola', 'Acorralarlo rápidamente'],
        32: ['Tomarlos inmediatamente', 'Observar a distancia durante 2-4 horas', 'Dejarlos para siempre', 'Darles leche de vaca'],
        33: ['Toalla gruesa, manta o trampa humana', 'Manos descubiertas', 'Caja de cartón sin tapa', 'Correa y collar'],
        34: ['Ignorarlo por completo', 'Lavar con agua y jabón y buscar atención médica', 'Aplicar perfume', 'Poner solo hielo'],
        35: ['Mediodía bajo el sol', 'Amanecer y atardecer (horas crepusculares)', 'Solo a medianoche', '3 AM'],
        36: ['Cítricos y limón', 'Feromonas felinas (Feliway)', 'Vinagre', 'Amoníaco'],
        37: ['Se ve bien', 'Crea una rutina para facilitar la captura TNR', 'Aleja a los pájaros', 'Engorda a los gatos'],
        38: ['Extremadamente relajado', 'Adrenalina alta (excitado o asustado)', 'Medio dormido', 'Ciego'],
        39: ['Sí, siempre llevar por el pellejo', 'Evitar si es posible; usar envoltorio de toalla', 'Agarrar fuerte el pellejo y balancear', 'Nunca tocar el cuello'],
        40: ['Paciencia, espacio y horario constante de alimentación', 'Forzar contacto físico', 'Hacer ruidos fuertes', 'Seguirlos por todas partes'],
        41: ['Dar baño frío', 'Calor inmediato y azúcar/glucosa en encías + veterinario', 'Poner en armario oscuro', 'Alimentar con pienso sólido'],
        42: ['Mantas de algodón', 'Paja seca y limpia', 'Toallas mojadas', 'Papel triturado'],
        43: ['Revisar la boca con cuidado y compresiones suaves + veterinario', 'Sacudir boca abajo', 'Dar agua', 'Esperar 1 hora'],
        44: ['Hierba para gatos', 'Lirios', 'Planta cinta', 'Helecho de Boston'],
        45: ['Solo estreñimiento', 'Emergencia mortal de bloqueo urinario', 'Jugando', 'Comportamiento normal'],
        46: ['Vertir agua oxigenada', 'Solución salina/agua limpia y tratamiento veterinario', 'Cubrir con tierra', 'Ignorarlo'],
        47: ['Insecticida químico para perros', 'Baño tibio con jabón lavavajillas Dawn', 'Frotar con alcohol', 'Afeitar todo el pelo'],
        48: ['Huelen demasiado fuerte', 'Los gatos carecen de enzimas hepáticas para metabolizar aceites', 'Los aceites hacen estornudar a los gatos', 'Sin riesgo'],
        49: ['Sostener en brazos', 'Transportín cerrado envuelto en toalla oscura', 'Caja de cartón', 'Cesta de lavandería abierta'],
        50: ['Salvar al animal a toda costa', 'Garantizar la seguridad humana primero', 'Tomar fotos', 'Actuar rápidamente sin planificar']
    },
    "fr": {
        1: ['4-6h', '8-10h', '12-16h', 'Presque jamais'],
        2: ['Uniquement quand ils sont heureux', 'Uniquement quand ils ont faim', 'Quand ils sont heureux, stressés ou en guérison', 'Pour se nettoyer les oreilles'],
        3: ['Super heureux', 'Menacé ou très en colère', 'Ennuyé', 'Endormi'],
        4: ['Ils sont fatigués', 'Ils complotent', 'Ils vous font confiance et vous aiment', 'De la poussière dans les yeux'],
        5: ['Un clowder', 'Une escouade', 'Une meute', 'Un miaou-b'],
        6: ['Frottement des joues et glandes odorantes', 'Courir à toute vitesse', 'Vous fixer dans les yeux', 'Bruits de mains forts'],
        7: ['Queue', 'Moustaches', 'Cils', 'Griffes'],
        8: ['En colère et agressif', 'Terrifié', 'Salut amical et heureux', 'Extrêmement endormi'],
        9: ['1-2 jours', '7-14 jours', '1 mois', '6 semaines'],
        10: ['Hygiène, refroidissement et soulagement du stress', 'Pour perdre des poils', 'Pour se salir', 'Pour attirer les insectes'],
        11: ['Oui, tous les jours', 'Non, le chocolat est toxique', 'Seulement le chocolat noir', 'Seulement le chocolat blanc'],
        12: ['Oui, c\'est essentiel', 'Non, la plupart sont intolérants au lactose', 'Seulement pour les chats adultes', 'Oui, tout le lait est sûr'],
        13: ['Régime végétalien', 'Régime strict de fruits', 'À base de viande (carnivore strict)', 'Régime riche en glucides'],
        14: ['C\'est mauvais au goût', 'Contient des parasites et bactéries dangereux', 'C\'est trop froid', 'Les chats n\'aiment pas l\'eau'],
        15: ['Oignons crus', 'Raisins', 'Poulet cuit nature', 'Chocolat'],
        16: ['Une fois tous les 3 jours', '1-2 repas programmés par jour', '10 fois par jour', 'Chaque fois qu\'ils miaulent'],
        17: ['Vitamine C', 'Taurine', 'Calcium', 'Fer'],
        18: ['Oui, toujours', 'Non, utiliser un substitut de lait pour chatons (KMR)', 'Seulement le lait écrémé', 'Oui, mélangé avec de l\'eau'],
        19: ['Forcer à boire avec une cuillère', 'Nourrir avec de la nourriture humide', 'Donner du jus de fruit', 'Ajouter du sel à l\'eau'],
        20: ['Oui, toujours', 'Non, la nourriture humide offre une hydratation vitale', 'Les croquettes sont nocives', 'Les chats n\'ont pas besoin d\'eau'],
        21: ['Capture-Stérilisation-Retour (TNR)', 'Entraîner-Nourrir-Sauver', 'Traiter-Nommer-Relocaliser', 'Marquer-Numéroter-Enregistrer'],
        22: ['Médaille au collier', 'Oreille entaillée (coche à l\'oreille)', 'Dos rasé', 'Tatouage sur le front'],
        23: ['35,0°C - 36,4°C', '38,0°C - 39,2°C', '40,5°C - 42,0°C', '37,0°C exactement'],
        24: ['Ronronner fort', 'Poil emmêlé, écoulement et léthargie', 'Haute énergie', 'Pelage brillant'],
        25: ['Le caresser doucement', 'Rester à l\'écart et appeler le contrôle animalier/vétérinaire', 'Le nourrir à la main', 'Le faire entrer'],
        26: ['Les rend endormis', 'Extrêmement toxique et détruit les globules rouges', 'Fait tomber les poils', 'Aucun effet'],
        27: ['En mangeant de l\'herbe', 'Contact avec des environnements/animaux infestés', 'En buvant du lait', 'En dormant trop'],
        28: ['Tous les 5 ans', 'Au moins une fois par an', 'Seulement en cas de maladie grave', 'Jamais'],
        29: ['Chaud et sec', 'Frais et légèrement humide', 'Saignant', 'Jaune vif'],
        30: ['Pour énerver les humains', 'Muer les gaines d\'ongles et étirer les muscles', 'Pour aiguiser les dents', 'Pour dormir dessus'],
        31: ['Courir vers lui en criant', 'Se déplacer lentement, s\'accroupir, laisser renifler', 'Attraper par la queue', 'L\'acculer rapidement'],
        32: ['Les prendre immédiatement', 'Observer à distance pendant 2-4 heures', 'Les abandonner pour toujours', 'Leur donner du lait de vache'],
        33: ['Serviette épaisse, couverture ou piège humain', 'Mains nues', 'Boîte en carton sans couvercle', 'Laisse et collier'],
        34: ['Ignorer complètement', 'Laver à l\'eau et au savon et consulter un médecin', 'Appliquer du parfum', 'Mettre de la glace uniquement'],
        35: ['Midi sous le soleil direct', 'Aube et crépuscule (heures crépusculaires)', 'Minuit uniquement', '3h du matin'],
        36: ['Agrumes et citron', 'Phéromones félines (Feliway)', 'Vinaigre', 'Ammoniac'],
        37: ['C\'est joli', 'Crée une routine pour faciliter la capture TNR', 'Éloigne les oiseaux', 'Fait grossir les chats'],
        38: ['Extrêmement détendu', 'Haute adrénaline (excité ou effrayé)', 'À demi endormi', 'Aveugle'],
        39: ['Oui, toujours porter par la peau du cou', 'Éviter si possible ; utiliser une serviette', 'Attraper fort la peau et balancer', 'Ne jamais toucher le cou'],
        40: ['Patience, espace et calendrier de nourriture constant', 'Forcer le contact physique', 'Faire des bruits forts', 'Les suivre partout'],
        41: ['Donner un bain froid', 'Chaleur immédiate et sucre/glucose sur les gencives + vétérinaire', 'Mettre dans un placard sombre', 'Nourrir avec des croquettes solides'],
        42: ['Couvertures en coton', 'Paille propre et sèche', 'Serviettes mouillées', 'Papier broyé'],
        43: ['Vérifier la bouche avec soin & compressions douces + vétérinaire', 'Secouer la tête en bas', 'Donner de l\'eau', 'Attendre 1 heure'],
        44: ['Herbe à chat', 'Lys', 'Plante araignée', 'Fougère de Boston'],
        45: ['Simple constipation', 'Urgence vitale de blocage urinaire', 'Joue', 'Comportement normal'],
        46: ['Verser de l\'eau oxygénée', 'Sérum physiologique/eau propre et soin vétérinaire', 'Couvrir de terre', 'Ignorer'],
        47: ['Insecticide chimique pour chiens', 'Bain tiède avec du savon vaisselle Dawn', 'Friction à l\'alcool', 'Raser tous les poils'],
        48: ['Ils sentent trop fort', 'Les chats manquent d\'enzymes hépatiques pour métaboliser les huiles', 'Les huiles font éternuer les chats', 'Aucun risque'],
        49: ['Tenir dans les bras', 'Caisse fermée enveloppée dans une serviette sombre', 'Boîte en carton', 'Panier à linge ouvert'],
        50: ['Sauver l\'animal à tout prix', 'Assurer la sécurité humaine d\'abord', 'Prendre des photos', 'Agir vite sans planification']
    },
    "ar": {
        1: ['4-6 ساعات', '8-10 ساعات', '12-16 ساعة', 'تقريباً أبداً'],
        2: ['فقط عندما تكون سعيدة', 'فقط عندما تكون جائعة', 'عند السعادة أو التوتر أو التعافي', 'لتنظيف آذانها'],
        3: ['سعيدة جداً', 'تشعر بالتهديد أو غاضبة جداً', 'شاعرة بالملل', 'نعسانة'],
        4: ['متعبة', 'تخطط لأمر ما', 'تثق بك وتحبك', 'يوجد غبار في عينيها'],
        5: ['مجموعة قطط (clowder)', 'فريق', 'قطيع', 'مواء'],
        6: ['فرك الخدين والغدد العطرية', 'الجري بأقصى سرعة', 'التحديق في عينيك', 'التصفيق بصوت عالٍ'],
        7: ['الذيل', 'شارب القط (الشاربان)', 'الرموش', 'المخالب'],
        8: ['غاضبة وهجومية', 'مرعوبة', 'تحية سعيدة وودودة', 'نعسانة جداً'],
        9: ['1-2 يوم', '7-14 يوماً', 'شهر واحد', '6 أسابيع'],
        10: ['النظافة والتبريد وتخفيف التوتر', 'لتساقط الشعر', 'لتتسخ', 'لجذب الحشرات'],
        11: ['نعم، يومياً', 'لا، الشوكولاتة سامة للقطط', 'الشوكولاتة الداكنة فقط', 'الشوكولاتة البيضاء فقط'],
        12: ['نعم، إنه ضروري', 'لا، معظمها يعاني من حساسية اللاكتوز', 'للقطط البالغة فقط', 'نعم، كل الحليب آمن'],
        13: ['نظام غذائي نباتي', 'نظام فاكهة فقط', 'يعتمد على اللحوم (لاقمة لحوم زاحفة)', 'نظام عالي الكربوهيدرات'],
        14: ['طعمها سيء', 'تحتوي على طفيليات وبكتيريا خطيرة', 'باردة جداً', 'القطط تكره الماء'],
        15: ['البصل النيئ', 'العنب', 'دجاج مطبوخ سادة', 'الشوكولاتة'],
        16: ['مرة كل 3 أيام', 'وجبتان منتظمتان يومياً', '10 مرات في اليوم', 'كلما مواءت'],
        17: ['فيتامين ج', 'التورين (Taurine)', 'الكالسيوم', 'الحديد'],
        18: ['نعم، دائماً', 'لا، استخدم بديل حليب القطط (KMR)', 'حليب خالي الدسم فقط', 'نعم، مخفف بالماء'],
        19: ['الإجبار بالملعقة', 'إطعام طعام رطب (Wet food)', 'إعطاء عصير فواكه', 'إضافة ملح للماء'],
        20: ['نعم، دائماً', 'لا، الطعام الرطب يوفر ترطيباً حيوياً', 'الطعام الجاف ضار', 'القطط لا تحتاج للماء'],
        21: ['صيد-تعقيم-إعادة (TNR)', 'تدريب-تغذية-إنقاذ', 'علاج-تسمية-نقل', 'ترقيم-تسجيل'],
        22: ['طوق برقم', 'قص طرف الأذن (Ear-tip)', 'حلاقة الظهر', 'وشم على الجبهة'],
        23: ['35.0 - 36.4 مئوية', '38.0 - 39.2 مئوية', '40.5 - 42.0 مئوية', '37.0 مئوية بالضبط'],
        24: ['الخرخرة بصوت عالٍ', 'فراء متلبد، إفرازات وخمول', 'طاقة عالية', 'فراء لامع'],
        25: ['المسح عليها بلطف', 'الابتعاد والاتصال بمكافحة الحيوانات/البيطري', 'إطعامها باليد', 'إدخالها للمنزل'],
        26: ['يجعلها تنام', 'سام جداً ويدمر كريات الدم الحمراء', 'يسقط الفراء', 'لا تأثير له'],
        27: ['أكل الأعشاب', 'مخالطة حيوانات أو أماكن موبوءة بالبراغيث', 'شرب الحليب', 'النوم الكثيف'],
        28: ['كل 5 سنوات', 'مرة واحدة في السنة على الأقل', 'عند المرض الشديد فقط', 'أبداً'],
        29: ['حار وجاف', 'بارد ورطب قليلاً', 'ينزف', 'أصفر فاقع'],
        30: ['لإزعاج البشر', 'لتقشير الأظافر وتمديد العضلات', 'لشحذ الأسنان', 'لنوم عليها'],
        31: ['الجري نحوها بالصراخ', 'التحرك ببطء، الانخفاض، والسماح لها بالشم', 'الإمساك بها من الذيل', 'حصارها بسرعة'],
        32: ['أخذها فوراً', 'المراقبة عن بعد لمدة 2-4 ساعات', 'تركها للأبد', 'إعطاؤها حليب بقر'],
        33: ['منشفة سميكة، بطانية أو قفص إنقاذ آمن', 'الأيدي العارية', 'صندوق كرتوني بدون غطاء', 'حبل وطوق'],
        34: ['تجاهل الأمر تماماً', 'غسل الجرح بالماء والصابون واستشارة طبيب', 'وضع عطر', 'وضع ثلج فقط'],
        35: ['الظهيرة تحت الشمس', 'الفجر والغروب (أوقات النشاط Feline)', 'منتصف الليل فقط', '3 صباحاً'],
        36: ['الليمون والحمضيات', 'فيرومونات القطط المهدئة (Feliway)', 'الخل', 'الأمونيا'],
        37: ['منظر جميل', 'يبني روتيناً يسهل عملية صيد وتعقيم TNR', 'يبعد الطيور', 'يجعل القطط سمينة'],
        38: ['مرتاحة جداً', 'ارتفاع الأدرينالين (خوف أو إثارة)', 'نصف نائمة', 'عمياء'],
        39: ['نعم، دائماً حملها من الرقبة', 'تجنب ذلك إن أمكن؛ استخدم لفة المنشفة', 'حملها بقوة وأرجحتها', 'عدم لمس الرقبة أبداً'],
        40: ['الصبر والمسافة وجدول إطعام منتظم', 'فرض الاتصال الجسدي', 'إصدار أصوات عالية', 'ملاحقتها في كل مكان'],
        41: ['إعطاء حمام بارد', 'تدفئة فورية وسكر/جلوكوز على اللثة + البيطري', 'وضعها في خزانة مظلمة', 'إطعام طعام جاف'],
        42: ['بطانيات قطنية', 'قش (Straw) جاف ونظيف', 'مناشف مبللة', 'ورق ممزق'],
        43: ['فحص الفم بحذر وضغطات خفيفة + بيطري', 'هزها رأساً على عقب', 'إعطاؤها ماء', 'الانتظار ساعة'],
        44: ['عشب القطط', 'زنبق (Lilies)', 'نبات العنكبوت', 'سرخس بوسطن'],
        45: ['مجرد إمساك', 'حالة طوارئ انسداد مجرى البول المهددة للحياة', 'لعب', 'سلوك طبيعي'],
        46: ['صب ماء أكسجين', 'محلول ملحي/ماء نظيف وعلاج بيطري', 'تغطيتها بالتراب', 'تجاهلها'],
        47: ['مبيد براغيث الكلاب الكيميائي', 'حمام دافئ بصابون أطباق Dawn الأزرق', 'مسح بالكحول', 'حلاقة الفراء بالكامل'],
        48: ['رائحتها قوية جداً', 'تفتقر القطط لإنزيمات الكبد لاستقلاب الزيوت', 'الزيوت تجعلها تعطس', 'لا يوجد خطر'],
        49: ['حملها بين الذراعين', 'قفص مغلق ملفوف بمنشفة مظلمة', 'صندوق كرتون', 'سلة غسيل مفتوحة'],
        50: ['إنقاذ الحيوان بأي ثمن', 'ضمان السلامة البشرية أولاً', 'التقاط صور', 'العمل بسرعة بدون تخطيط']
    }
}

# 1. Update app/src/main/res/values/strings.xml
tree = ET.parse('app/src/main/res/values/strings.xml')
root = tree.getroot()

# Create a map of existing string tags
string_elements = {elem.get('name'): elem for elem in root.findall('string') if elem.get('name')}

for q in range(1, 51):
    opts = options_db['en'][q]
    for opt_idx, text in enumerate(opts):
        key = f"quiz_q{q}_opt{opt_idx}"
        if key in string_elements:
            string_elements[key].text = text
        else:
            new_elem = ET.SubElement(root, 'string', name=key)
            new_elem.text = text

ET.indent(tree, space="    ")
tree.write('app/src/main/res/values/strings.xml', encoding='utf-8', xml_declaration=True)
print("Updated strings.xml with all 50 question options!")

# 2. Update app/src/main/assets/translations.json
with open('app/src/main/assets/translations.json') as f:
    tdata = json.load(f)

for lang in ["en", "es", "fr", "ar"]:
    if lang not in tdata:
        tdata[lang] = {}
    if "quiz" not in tdata[lang]:
        tdata[lang]["quiz"] = {}
    
    for q in range(1, 51):
        opts = options_db[lang][q]
        for opt_idx, text in enumerate(opts):
            key = f"quiz_q{q}_opt{opt_idx}"
            tdata[lang]["quiz"][key] = text

with open('app/src/main/assets/translations.json', 'w', encoding='utf-8') as f:
    json.dump(tdata, f, ensure_ascii=False, indent=2)

print("Updated translations.json for EN, ES, FR, AR!")
