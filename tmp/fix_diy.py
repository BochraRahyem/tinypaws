import re
import sys

def get_id(text):
    clean = re.sub(r'[^a-zA-Z0-9]', '_', text.lower())
    clean = re.sub(r'_+', '_', clean).strip('_')
    return clean

def transform_file(file_path):
    with open(file_path, 'r') as f:
        content = f.read()

    # Define common resource mappings
    difficulties = {
        "Very Easy": "difficulty_easiest",
        "Easy": "difficulty_easy",
        "Medium": "difficulty_medium",
        "Hard": "difficulty_hard",
        "Very Hard": "difficulty_hardest"
    }
    
    costs = {
        "$0": "cost_0",
        "$2": "cost_2",
        "$3": "cost_3",
        "$5": "cost_5",
        "$10": "cost_10",
        "$15": "cost_15",
        "$20": "cost_20",
        "$25": "cost_25",
        "$30": "cost_30",
        "$50": "cost_50"
    }

    times = {
        "5 mins": "time_5m",
        "10 mins": "time_10m",
        "15 mins": "time_15m",
        "20 mins": "time_20m",
        "30 mins": "time_30m",
        "45 mins": "time_45m",
        "1 hour": "time_1h",
        "2 hours": "time_2h",
        "3 hours": "time_3h",
        "4 hours": "time_4h"
    }

    new_strings = {}

    def replace_project(match):
        project_id = match.group(1)
        p_num = project_id.split('_')[-1]
        
        title = match.group(2)
        diff = match.group(3)
        cost = match.group(4)
        time = match.group(5)
        materials_str = match.group(6)
        desc = match.group(7)
        steps_str = match.group(8)
        alts_str = match.group(9)

        new_strings[f"diy_s{p_num}_title"] = title
        new_strings[f"diy_s{p_num}_desc"] = desc
        
        # Difficulties, Costs, Times
        diff_res = difficulties.get(diff, f"difficulty_{get_id(diff)}")
        cost_res = costs.get(cost, f"cost_{get_id(cost)}")
        time_res = times.get(time, f"time_{get_id(time)}")
        
        # Materials
        materials = [s.strip().strip('"') for s in materials_str.split(',')]
        mat_res_list = []
        for m in materials:
            m_id = f"mat_{get_id(m)}"
            new_strings[m_id] = m
            mat_res_list.append(f"R.string.{m_id}")
        
        # Steps
        steps = [s.strip().strip('"') for s in re.findall(r'"([^"]*)"', steps_str)]
        step_res_list = []
        for i, s in enumerate(steps):
            s_id = f"diy_s{p_num}_step{i+1}"
            new_strings[s_id] = s
            step_res_list.append(f"R.string.{s_id}")
            
        # Alt Texts
        alts = [s.strip().strip('"') for s in re.findall(r'"([^"]*)"', alts_str)]
        alt_res_list = []
        for i, s in enumerate(alts):
            a_id = f"diy_s{p_num}_alt{i+1}"
            new_strings[a_id] = s
            alt_res_list.append(f"R.string.{a_id}")

        return f"""        DiyProjectDetailsData(
            id = "{project_id}",
            titleRes = R.string.diy_s{p_num}_title,
            category = "shelter",
            difficultyRes = R.string.{diff_res},
            costRes = R.string.{cost_res},
            timeRes = R.string.{time_res},
            materialsRes = listOf({", ".join(mat_res_list)}),
            descriptionRes = R.string.diy_s{p_num}_desc,
            stepsRes = listOf({", ".join(step_res_list)}),
            altTextsRes = listOf({", ".join(alt_res_list)})
        )"""

    # Regex to find projects 4-10
    project_pattern = r'DiyProjectDetailsData\(\s*id = "(shelter_(?:4|5|6|7|8|9|10))",\s*titleRes = "([^"]+)",\s*category = "([^"]+)",\s*difficultyRes = "([^"]+)",\s*costRes = "([^"]+)",\s*timeRes = "([^"]+)",\s*materialsRes = listOf\(([^)]+)\),\s*descriptionRes = "([^"]+)",\s*stepsRes = listOf\(([^)]+)\),\s*altTextsRes = listOf\(([^)]+)\)\s*\)'
    
    # Wait, the above regex might be too strict. Let's simplify.
    # I'll do it manually for projects 4-10 if the script is too risky.
    # Actually, I can just use a simpler one.
    
    # Let's just output the strings for now.
    return new_strings

# I'll just write the strings.xml manually. It's safer.
