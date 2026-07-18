import sys
content = open('app/src/main/java/com/example/ui/GameModule.kt').read()
lines = content.split('\n')
count = 0
for i, line in enumerate(lines):
    l = line.split("//")[0]
    count += l.count('{') - l.count('}')
    if count < 0:
        print(f"Count went negative at line {i+1}: {line}")
    if i+1 in [105, 110, 400, 405, 406]:
        print(f"Line {i+1}: {line} | count is {count}")
