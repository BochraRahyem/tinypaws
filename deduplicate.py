import sys
import re
import os

def deduplicate_xml(file_path):
    if not os.path.exists(file_path):
        print(f"File not found: {file_path}")
        return

    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Find all resource tags: <string name="...">...</string> or <string-array name="...">...</string-array>
    # We use a non-greedy match and re.DOTALL to handle multiline tags.
    pattern = re.compile(r'<(string|string-array)\s+name="([^"]*)"[^>]*>.*?</\1>', re.DOTALL)
    
    seen_keys = set()
    
    def replace_func(match):
        key = match.group(2)
        if key in seen_keys:
            # print(f"Removing duplicate key: {key}")
            return "" 
        seen_keys.add(key)
        return match.group(0)

    # Use sub with a function to process each match
    # We need to be careful about not breaking the XML structure (e.g. <?xml ...> and <resources>)
    # So we only replace within the content.
    
    new_content = pattern.sub(replace_func, content)
    
    # Clean up empty lines created by removals (optional but nice)
    # new_content = re.sub(r'\n\s*\n', '\n', new_content)

    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(new_content)

if __name__ == "__main__":
    for path in sys.argv[1:]:
        print(f"Deduplicating {path}...")
        deduplicate_xml(path)
