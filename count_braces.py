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
    # ignoring string literals or comments for a rough estimate
    if "//" in line:
        line = line.split("//")[0]
    open_count += line.count('{') - line.count('}')
    if "} else {" in line:
        print(f"Line {i+1}: '}} else {{' reached. Open count BEFORE this line is {open_count - line.count('{') + line.count('}')}. Open count AFTER is {open_count}.")
