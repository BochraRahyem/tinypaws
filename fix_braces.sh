# Let's write a python script to count braces
cat << 'PY' > count_braces.py
import sys

content = open('app/src/main/java/com/example/ui/GameModule.kt').read()
lines = content.split('\n')

for i, line in enumerate(lines):
    if "if (!quizCompleted) {" in line:
        start_idx = i
        break

open_count = 0
for i in range(start_idx, len(lines)):
    line = lines[i]
    open_count += line.count('{') - line.count('}')
    if "else {" in line and "quizCompleted" not in line:
        pass
    if "            } else {" in line:
        print(f"Line {i+1}: 'else' reached. Open count is {open_count}")
        break
PY
python3 count_braces.py
