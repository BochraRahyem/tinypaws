
import xml.etree.ElementTree as ET
import os

def get_keys(file_path):
    if not os.path.exists(file_path):
        return set()
    tree = ET.parse(file_path)
    root = tree.getroot()
    keys = set()
    for child in root:
        if 'name' in child.attrib:
            keys.add(child.attrib['name'])
    return keys

base_file = 'app/src/main/res/values/strings.xml'
base_keys = get_keys(base_file)

for lang in ['ar', 'fr', 'es']:
    lang_file = f'app/src/main/res/values-{lang}/strings.xml'
    lang_keys = get_keys(lang_file)
    missing = base_keys - lang_keys
    print(f"Missing keys in {lang}: {len(missing)}")
    if missing:
        print(sorted(list(missing)))
