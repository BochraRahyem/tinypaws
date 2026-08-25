package com.example.util

import android.content.Context

object WeatherNotificationResolver {

    enum class WeatherConditionCategory {
        VERY_HOT,
        COLD,
        RAIN,
        STRONG_WIND,
        SUDDEN_TEMP_CHANGE,
        COMFORTABLE
    }

    data class NotificationMessage(
        val title: String,
        val message: String
    )

    fun determineCategory(
        currentTemp: Double,
        maxTemp: Double,
        minTemp: Double,
        weatherCode: Int,
        windSpeed: Double,
        tempChangeDelta: Double
    ): WeatherConditionCategory {
        return when {
            maxTemp >= 33.0 || currentTemp >= 33.0 -> WeatherConditionCategory.VERY_HOT
            minTemp <= 13.0 || currentTemp <= 13.0 -> WeatherConditionCategory.COLD
            weatherCode in listOf(51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82, 95, 96, 99) -> WeatherConditionCategory.RAIN
            windSpeed >= 25.0 -> WeatherConditionCategory.STRONG_WIND
            tempChangeDelta >= 6.0 -> WeatherConditionCategory.SUDDEN_TEMP_CHANGE
            else -> WeatherConditionCategory.COMFORTABLE
        }
    }

    fun getRotatingNotification(
        context: Context,
        category: WeatherConditionCategory,
        cityName: String,
        currentTemp: Double,
        maxTemp: Double,
        minTemp: Double,
        languageCode: String = "en"
    ): NotificationMessage {
        val prefs = context.getSharedPreferences("tinypaws_prefs", Context.MODE_PRIVATE)
        val messages = getMessagePool(category, cityName, currentTemp, maxTemp, minTemp, languageCode)
        
        val key = "last_weather_msg_idx_${category.name}"
        val lastIdx = prefs.getInt(key, -1)
        val nextIdx = if (messages.isEmpty()) 0 else (lastIdx + 1) % messages.size
        
        prefs.edit().putInt(key, nextIdx).apply()
        return messages[nextIdx]
    }

    private fun getMessagePool(
        category: WeatherConditionCategory,
        cityName: String,
        currentTemp: Double,
        maxTemp: Double,
        minTemp: Double,
        lang: String
    ): List<NotificationMessage> {
        val tempInt = currentTemp.toInt()
        val maxInt = maxTemp.toInt()
        val minInt = minTemp.toInt()

        return when (lang) {
            "ar" -> when (category) {
                WeatherConditionCategory.VERY_HOT -> listOf(
                    NotificationMessage(
                        "☀️ طقس شديد الحرارة في $cityName",
                        "تصل الحرارة إلى $maxInt°م اليوم. يرجى وضع أوعية ماء بارد في الظل لقطط الشوارع 💧🐾"
                    ),
                    NotificationMessage(
                        "🌡️ تنبيه حرارة عالية ($cityName)",
                        "الأسفلت والأسطح ساخنة جداً اليوم ($tempInt°م). تأكد من توفر أماكن استراحة مظللة للقطط 🐱"
                    ),
                    NotificationMessage(
                        "💧 تذكير رعاية القطط في الصيف",
                        "القطط الضالة معرضة للجفاف في هذه الحرارة. ملء وعاء ماء واحد ينقذ حياة قطط الحي 🚰"
                    ),
                    NotificationMessage(
                        "☀️ موجة حرارية: $cityName ($maxInt°م)",
                        "ضع أوعية طعام القطط بعيداً عن الشمس المباشرة لتجنب تلفه سريعاً في هذا الطقس الحار 🐟"
                    )
                )
                WeatherConditionCategory.COLD -> listOf(
                    NotificationMessage(
                        "❄️ طقس بارد في $cityName ($minInt°م)",
                        "تبحث القطط عن الدفء قرب محركات السيارات. يرجى النقر على غطاء المحرك قبل التشغيل 🚗🐾"
                    ),
                    NotificationMessage(
                        "🧣 تنبيه انخفاض درجات الحرارة",
                        "تنخفض الحرارة إلى $minInt°م. تأكد من أن بيوت وملاجئ القطط الخارجية جافة ومعزولة بالدفء 🏡"
                    ),
                    NotificationMessage(
                        "🍲 طعام إضافي لقطط الحي اليوم",
                        "تحرق القطط طاقة أكبر للتدفئة في البرد ($tempInt°م). تقديم وجبة مغذية إضافية يساعدها كثيراً ❤️"
                    ),
                    NotificationMessage(
                        "❄️ عناية بالقطط في الشتاء ($cityName)",
                        "يتجمد الماء بسرعة في هذا الطقس. يرجى تفقد أوعية المياه وتبديلها بماء فاتر بانتظام 🚰"
                    )
                )
                WeatherConditionCategory.RAIN -> listOf(
                    NotificationMessage(
                        "🌧️ أمطار وزخات متوقعة في $cityName",
                        "تأكد من أن محطات إطعام قطط الشارع مسقوفة وأن الملاجئ جافة وبعيدة عن مجاري المياه ☔🐾"
                    ),
                    NotificationMessage(
                        "☔ طقس ماطر ورطب اليوم",
                        "البلل يجعل فرار القطط عرضة لنزلات البرد. ارفع أوعية الطعام عن الأرض قليلاً 🏡"
                    ),
                    NotificationMessage(
                        "🌧️ تنبيه رعاية القطط أثناء المطر",
                        "وفر كراتين مغلفة بالبلاستيك أو زوايا جافة لتلجأ إليها قطط الحي من المطر 🐱"
                    )
                )
                WeatherConditionCategory.STRONG_WIND -> listOf(
                    NotificationMessage(
                        "💨 رياح قوية متوقعة في $cityName",
                        "ثبّت بيوت القطط الخارجية جيداً لكي لا تسقط أو تتحرك بفعل الرياح الشديدة 🍃🐾"
                    ),
                    NotificationMessage(
                        "🍃 تنبيه طقس عاصف",
                        "تبحث القطط الضالة عن زوايا هادئة ومحمية من الرياح. تفقد الزوايا الآمنة في حديقتك أو حيك 🏡"
                    )
                )
                WeatherConditionCategory.SUDDEN_TEMP_CHANGE -> listOf(
                    NotificationMessage(
                        "🌡️ تقلب مفاجئ في درجات الحرارة",
                        "تغيرات الطقس المفاجئة قد ترهق صغار القطط. تفقد القطط الضعيفة والشارردة اليوم 🐱"
                    ),
                    NotificationMessage(
                        "🔄 تغير درجات الحرارة في $cityName",
                        "فارق حراري ملحوظ اليوم ($tempInt°م). جهّز أماكن مرنة ومحمية لقططك وللقطط الضالة 🐾"
                    )
                )
                WeatherConditionCategory.COMFORTABLE -> listOf(
                    NotificationMessage(
                        "🌤️ طقس جميل ومعتدل في $cityName ($tempInt°م)",
                        "أجواء مثالية لتفقد مستعمرات قطط الحي وتجديد أوعية الطعام والمياه 🐾☀️"
                    ),
                    NotificationMessage(
                        "🐾 يوم رائع لقطط الحي ($tempInt°م)",
                        "الطقس مريح ومشمس للقطط لتستمتع بأشعة الشمس بهدوء. يوم دافئ ولطيف 🐱✨"
                    ),
                    NotificationMessage(
                        "🌿 طقس لطيف في $cityName",
                        "فرصة ممتازة لمراجعة يوميات الرعاية والتأكد من صحة أصدقائنا الفرويين 🥣"
                    )
                )
            }
            "fr" -> when (category) {
                WeatherConditionCategory.VERY_HOT -> listOf(
                    NotificationMessage(
                        "☀️ Fortes Chaleurs à $cityName",
                        "Les températures atteignent $maxInt°C. Pensez à déposer des bols d'eau fraîche à l'ombre pour les chats 💧🐾"
                    ),
                    NotificationMessage(
                        "🌡️ Alerte Chaleur ($cityName)",
                        "Le bitume est brûlant aujourd'hui ($tempInt°C). Vérifiez que les minous extérieurs ont des coins ombragés 🐱"
                    ),
                    NotificationMessage(
                        "💧 Rappel Hydratation Féline",
                        "Les chats de rue risquent la déshydratation sous ce soleil. Un geste simple avec de l'eau fraîche sauve des vies 🚰"
                    ),
                    NotificationMessage(
                        "☀️ Chaleur Estivale : $cityName ($maxInt°C)",
                        "Placez la nourriture des chats à l'abri du soleil pour éviter qu'elle ne s'abîme trop rapidement 🐟"
                    )
                )
                WeatherConditionCategory.COLD -> listOf(
                    NotificationMessage(
                        "❄️ Temps Froid à $cityName ($minInt°C)",
                        "Les chats errants se réfugient sous les capots de voiture. Tapez sur le capot avant de démarrer 🚗🐾"
                    ),
                    NotificationMessage(
                        "🧣 Alerte Baisse de Température",
                        "Il fera $minInt°C. Assurez-vous que les abris extérieurs sont bien isolés et restent au sec 🏡"
                    ),
                    NotificationMessage(
                        "🍲 Repas Énergétique pour les Chats",
                        "Par temps froid ($tempInt°C), les chats dépensent plus d'énergie. Une portion de croquettes en plus les aide énormément ❤️"
                    ),
                    NotificationMessage(
                        "❄️ Soin des Chats en Hiver ($cityName)",
                        "L'eau gèle rapidement par ce froid. Pensez à renouveler régulièrement les écuelles d'eau tiède 🚰"
                    )
                )
                WeatherConditionCategory.RAIN -> listOf(
                    NotificationMessage(
                        "🌧️ Pluie Prévue à $cityName",
                        "Assurez-vous que les postes de nourrissage sont protégés et que les abris restent bien étanches ☔🐾"
                    ),
                    NotificationMessage(
                        "☔ Météo Humide et Averses",
                        "Le pelage mouillé rend les chats vulnérables au froid. Surélevez légèrement les gamelles d'eau et de nourriture 🏡"
                    ),
                    NotificationMessage(
                        "🌧️ Protection Féline Sous la Pluie",
                        "Offrez des cartons étanches ou des recoins abrités pour aider les chats du quartier à rester au sec 🐱"
                    )
                )
                WeatherConditionCategory.STRONG_WIND -> listOf(
                    NotificationMessage(
                        "💨 Vents Soutenus à $cityName",
                        "Sécurisez et lestez les cabanes extérieures pour éviter qu'elles ne soient renversées par les rafales 🍃🐾"
                    ),
                    NotificationMessage(
                        "🍃 Alerte Météo Venteuse",
                        "Les chats de rue recherchent des recoins calmes et protégés du vent. Vérifiez les accès abrités 🏡"
                    )
                )
                WeatherConditionCategory.SUDDEN_TEMP_CHANGE -> listOf(
                    NotificationMessage(
                        "🌡️ Variation Thermique Brusque",
                        "Les changements rapides de température peuvent stresser les chatons et chats fragiles. Gardez un œil bienveillant 🐱"
                    ),
                    NotificationMessage(
                        "🔄 Changement de Météo à $cityName",
                        "Écart de température notable aujourd'hui ($tempInt°C). Prévoyez des zones confortables et modulables 🐾"
                    )
                )
                WeatherConditionCategory.COMFORTABLE -> listOf(
                    NotificationMessage(
                        "🌤️ Temps Doux et Agréable à $cityName ($tempInt°C)",
                        "Idéal pour faire un tour de surveillance des points de nourrissage de votre quartier 🐾☀️"
                    ),
                    NotificationMessage(
                        "🐾 Belle Journée pour les Chats ($tempInt°C)",
                        "Des conditions parfaites pour que les chats profitent paisiblement du soleil. Belle journée avec vos compagnons 🐱✨"
                    ),
                    NotificationMessage(
                        "🌿 Météo Clémente à $cityName",
                        "Une excellente occasion pour mettre à jour vos carnets de soins et veiller sur nos amis félins 🥣"
                    )
                )
            }
            "es" -> when (category) {
                WeatherConditionCategory.VERY_HOT -> listOf(
                    NotificationMessage(
                        "☀️ Día Muy Caluroso en $cityName",
                        "Temperaturas hasta $maxInt°C. Coloca cuencos con agua fresca en zonas de sombra para los gatos callejeros 💧🐾"
                    ),
                    NotificationMessage(
                        "🌡️ Alerta de Altas Temperaturas ($cityName)",
                        "El asfalto quema hoy ($tempInt°C). Comprueba que los gatitos del vecindario tengan rincones con sombra 🐱"
                    ),
                    NotificationMessage(
                        "💧 Recordatorio de Hidratación Felina",
                        "Los gatos de la calle sufren deshidratación con este calor. Un cuenco con agua limpia salva vidas hoy 🚰"
                    ),
                    NotificationMessage(
                        "☀️ Calor Intenso: $cityName ($maxInt°C)",
                        "Mantén la comida de los gatos resguardada del sol directo para evitar que se estropee rápidamente 🐟"
                    )
                )
                WeatherConditionCategory.COLD -> listOf(
                    NotificationMessage(
                        "❄️ Clima Frío en $cityName ($minInt°C)",
                        "Los gatos buscan calor cerca del motor de los coches. ¡Golpea suavemente el capó antes de arrancar! 🚗🐾"
                    ),
                    NotificationMessage(
                        "🧣 Alerta por Bajas Temperaturas",
                        "Mínimas de $minInt°C. Revisa que los refugios exteriores estén secos y bien aislados 🏡"
                    ),
                    NotificationMessage(
                        "🍲 Comida Extra para Días Fríos",
                        "Los gatos queman más calorías para mantenerse calientes con $tempInt°C. Una ración extra nutritiva les ayuda mucho ❤️"
                    ),
                    NotificationMessage(
                        "❄️ Cuidado de Gatos en Invierno ($cityName)",
                        "El agua se enfría o congela rápido. Renueva los bebederos con agua tibia regularmente 🚰"
                    )
                )
                WeatherConditionCategory.RAIN -> listOf(
                    NotificationMessage(
                        "🌧️ Lluvia Prevista en $cityName",
                        "Revisa que los puntos de alimentación estén techados y que los refugios no acumulen agua ☔🐾"
                    ),
                    NotificationMessage(
                        "☔ Día Lluvioso y Húmedo",
                        "El pelaje mojado enfría a los gatos. Eleva ligeramente los comederos del suelo 🏡"
                    ),
                    NotificationMessage(
                        "🌧️ Cuidado Felino Bajo la Lluvia",
                        "Coloca cajas protegidas o esquinas secas donde los gatos comunitarios puedan resguardarse 🐱"
                    )
                )
                WeatherConditionCategory.STRONG_WIND -> listOf(
                    NotificationMessage(
                        "💨 Viento Fuerte en $cityName",
                        "Asegura bien los refugios exteriores para que las ráfagas no los vuelquen 🍃🐾"
                    ),
                    NotificationMessage(
                        "🍃 Alerta por Ráfagas de Viento",
                        "Los gatos callejeros buscarán rincones protegidos del viento. Revisa zonas seguras en tu zona 🏡"
                    )
                )
                WeatherConditionCategory.SUDDEN_TEMP_CHANGE -> listOf(
                    NotificationMessage(
                        "🌡️ Cambio Brusco de Temperatura",
                        "Los cambios rápidos de clima pueden afectar a los gatos más vulnerables. Préstales atención extra hoy 🐱"
                    ),
                    NotificationMessage(
                        "🔄 Variación Térmica en $cityName",
                        "Oscilación de temperatura notable hoy ($tempInt°C). Asegura refugios cómodos y adaptables 🐾"
                    )
                )
                WeatherConditionCategory.COMFORTABLE -> listOf(
                    NotificationMessage(
                        "🌤️ Clima Suave y Agradable en $cityName ($tempInt°C)",
                        "Condiciones perfectas para revisar los puntos de alimentación comunitaria 🐾☀️"
                    ),
                    NotificationMessage(
                        "🐾 Gran Día para los Gatos ($tempInt°C)",
                        "Tiempo ideal para que los gatos disfruten del sol tranquilamente. ¡Que tengas un día sereno! 🐱✨"
                    ),
                    NotificationMessage(
                        "🌿 Clima Templado en $cityName",
                        "Buen momento para registrar rutinas de cuidado y mimar a nuestros amigos peludos 🥣"
                    )
                )
            }
            else -> when (category) {
                WeatherConditionCategory.VERY_HOT -> listOf(
                    NotificationMessage(
                        "☀️ Hot Day Ahead in $cityName",
                        "Temperatures reach $maxInt°C today. Place bowls of cool, clean water in shaded spots for neighborhood cats 💧🐾"
                    ),
                    NotificationMessage(
                        "🌡️ High Heat Alert ($cityName)",
                        "Pavements get scorching today ($tempInt°C). Ensure outdoor and stray kitties have shaded cooling spots 🐱"
                    ),
                    NotificationMessage(
                        "💧 Cat Hydration Reminder",
                        "Community cats easily dehydrate in this summer heat. A quick water bowl refill saves lives today 🚰"
                    ),
                    NotificationMessage(
                        "☀️ Heatwave Safety: $cityName ($maxInt°C)",
                        "Keep outdoor food bowls out of direct sunlight so meals stay fresh and safe for street cats 🐟"
                    )
                )
                WeatherConditionCategory.COLD -> listOf(
                    NotificationMessage(
                        "❄️ Chilly Weather in $cityName ($minInt°C)",
                        "Outdoor cats seek warmth near warm car engines. Please tap your hood before starting your car! 🚗🐾"
                    ),
                    NotificationMessage(
                        "🧣 Cold Weather Care Alert",
                        "Temperatures drop to $minInt°C. Ensure outdoor cat shelters stay dry and insulated with straw or blankets 🏡"
                    ),
                    NotificationMessage(
                        "🍲 Extra Nourishment in the Cold",
                        "Cats burn extra calories staying warm at $tempInt°C. An extra nutritious portion helps them thrive ❤️"
                    ),
                    NotificationMessage(
                        "❄️ Winter Cat Care ($cityName)",
                        "Water freezes quickly in freezing weather. Check water bowls regularly and refill with lukewarm water 🚰"
                    )
                )
                WeatherConditionCategory.RAIN -> listOf(
                    NotificationMessage(
                        "🌧️ Rainy Weather in $cityName",
                        "Check that community cat feeding stations are covered and shelters remain completely dry ☔🐾"
                    ),
                    NotificationMessage(
                        "☔ Wet Weather & Showers Expected",
                        "Damp fur makes cats vulnerable to chills. Elevate feeding bowls slightly above ground puddles 🏡"
                    ),
                    NotificationMessage(
                        "🌧️ Rain Protection Reminder",
                        "Provide weatherproof boxes or dry overhang spots for neighborhood cats seeking cover from the rain 🐱"
                    )
                )
                WeatherConditionCategory.STRONG_WIND -> listOf(
                    NotificationMessage(
                        "💨 High Winds Expected in $cityName",
                        "Secure outdoor shelters with weights so strong wind gusts don't tip them over 🍃🐾"
                    ),
                    NotificationMessage(
                        "🍃 Breezy Weather Alert",
                        "Outdoor cats will seek calm, wind-protected corners today. Check cozy sheltered nooks nearby 🏡"
                    )
                )
                WeatherConditionCategory.SUDDEN_TEMP_CHANGE -> listOf(
                    NotificationMessage(
                        "🌡️ Sudden Temperature Swing",
                        "Abrupt weather changes can stress young kittens and outdoor cats. Keep an extra eye on local strays 🐱"
                    ),
                    NotificationMessage(
                        "🔄 Temperature Shift in $cityName",
                        "Noticeable temperature swing today ($tempInt°C). Ensure adaptable and cozy shelter spots 🐾"
                    )
                )
                WeatherConditionCategory.COMFORTABLE -> listOf(
                    NotificationMessage(
                        "🌤️ Pleasant & Mild Weather in $cityName ($tempInt°C)",
                        "Perfect conditions to check in on local community cat colonies and refill feeding stations 🐾☀️"
                    ),
                    NotificationMessage(
                        "🐾 Lovely Day for Outdoor Cats ($tempInt°C)",
                        "Gentle, sunny weather for cats to enjoy the day comfortably. Wishing you and your feline friends a great day! 🐱✨"
                    ),
                    NotificationMessage(
                        "🌿 Mild Weather in $cityName",
                        "A wonderful time to update your care logs and spend quality moments caring for furry friends 🥣"
                    )
                )
            }
        }
    }
}
