import os

strings_to_add = {
    'values': """
    <!-- Certificate Preview & Screen -->
    <string name="cert_preview_title">🏆 Future Reward</string>
    <string name="cert_preview_desc">Complete all 50 quizzes to unlock your Cat Champion Certificate!</string>
    <string name="cert_preview_btn">Keep Playing</string>
    <string name="cert_fallback_name">TinyPaws Cat Champion</string>
    <string name="cert_content_desc">Certificate of Purrfection</string>
""",
    'values-ar': """
    <string name="cert_preview_title">🏆 مكافأة المستقبل</string>
    <string name="cert_preview_desc">أكمل جميع الاختبارات الخمسين لفتح شهادة بطل القطط!</string>
    <string name="cert_preview_btn">استمر في اللعب</string>
    <string name="cert_fallback_name">بطل قطط TinyPaws</string>
    <string name="cert_content_desc">شهادة الكمال للقطط</string>
""",
    'values-fr': """
    <string name="cert_preview_title">🏆 Future Récompense</string>
    <string name="cert_preview_desc">Terminez les 50 quiz pour débloquer votre certificat de Champion des Chats !</string>
    <string name="cert_preview_btn">Continuer à Jouer</string>
    <string name="cert_fallback_name">Champion des Chats TinyPaws</string>
    <string name="cert_content_desc">Certificat de Perfection</string>
""",
    'values-es': """
    <string name="cert_preview_title">🏆 Recompensa Futura</string>
    <string name="cert_preview_desc">¡Completa los 50 cuestionarios para desbloquear tu Certificado de Campeón de Gatos!</string>
    <string name="cert_preview_btn">Seguir Jugando</string>
    <string name="cert_fallback_name">Campeón de Gatos TinyPaws</string>
    <string name="cert_content_desc">Certificado de Perfección</string>
"""
}

for folder, content in strings_to_add.items():
    path = f'app/src/main/res/{folder}/strings.xml'
    if os.path.exists(path):
        with open(path, 'r') as f:
            lines = f.readlines()
        
        # Insert before the last </resources> tag
        for i in range(len(lines)-1, -1, -1):
            if '</resources>' in lines[i]:
                lines.insert(i, content)
                break
                
        with open(path, 'w') as f:
            f.writelines(lines)
        print(f"Updated {path}")
