import sys
import re
import os

def clean_xml(file_path):
    if not os.path.exists(file_path):
        print(f"Not found: {file_path}")
        return

    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    def fix_body(body):
        # 1. Escape unescaped apostrophes
        body = re.sub(r"(?<!\\)'", r"\'", body)
        
        def escape_fixer(match):
            seq = match.group(0)
            char = match.group(1)
            if char in "ntr'\"\\":
                return seq
            if char == 'u':
                if re.match(r'\\u[0-9a-fA-F]{4}', match.string[match.start():match.start()+6]):
                    return match.string[match.start():match.start()+6]
                else:
                    return 'u'
            return char

        body = re.sub(r'\\(.)', escape_fixer, body)
        return body

    def replace_string(match):
        start = match.group(1)
        body = match.group(2)
        end = match.group(3)
        return start + fix_body(body) + end

    new_content = re.sub(r'(<string\s+name="[^"]*"[^>]*>)(.*?)(</string>)', replace_string, content, flags=re.DOTALL)
    new_content = re.sub(r'(<item>)(.*?)(</item>)', replace_string, new_content, flags=re.DOTALL)

    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(new_content)

if __name__ == "__main__":
    for path in sys.argv[1:]:
        print(f"Cleaning {path}...")
        clean_xml(path)
