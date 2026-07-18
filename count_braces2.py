import sys
content = open('app/src/main/java/com/example/ui/GameModule.kt').read()
lines = content.split('\n')
count = 0
for i, line in enumerate(lines):
    # simple heuristic ignoring strings
    l = line.split("//")[0]
    count += l.count('{') - l.count('}')
    if "} else {" in line and "quizCompleted" not in line:
        pass
    if "            } else {" in line:
        print(f"Line {i+1}: {line.strip()} | count BEFORE: {count - l.count('{') + l.count('}')} | AFTER: {count}")
