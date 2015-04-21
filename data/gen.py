# -*- coding: UTF-8  -*-
import xml.etree.ElementTree as ET
import re
import time
import copy


def get_elemment(tag, text):
    ele = ET.Element(tag)
    ele.text = text
    # print tag, text
    return ele


tree = ET.parse('pro_team.xml')
root = tree.getroot()
team_file = open('team', 'r')

for team in team_file:
    team = team.split()
    team[5] = team[5].decode('utf-8')
    # print team
    team_ele = ET.Element('team')
    team_ele.append(get_elemment('external-id', team[1]))
    team_ele.append(get_elemment('id', team[1]))
    team_ele.append(get_elemment('name', team[4]))
    team_ele.append(get_elemment('nationality', 'China'))
    team_ele.append(get_elemment('region', 'Asia'))
    team_ele.append(get_elemment('university', team[5]))

    root.append(team_ele)

problem_map = {}
problems = root.findall("problem")
for problem in problems:
    problem_map[problem.find('name').text] = problem.find('id').text
print problem_map

team_name_map = {}
teams = root.findall('team')
for team in teams:
    team_name_map[team.find('name').text] = team.find('id').text
    team.find('name').text = team.find('university').text + '-' + team.find('name').text
print team_name_map

input_file = open('runs', 'r')
content_list = []
for each_line in input_file:
    content_list.append(each_line.decode('utf-8'))

run_number = int(re.match(r'-- (\d+) runs --', content_list[10]).group(1))

i = 11
total_line_number = len(content_list)
start_time = time.mktime(time.strptime('09:06:30 2015-04-15', '%H:%M:%S %Y-%m-%d'))
print start_time
while i < total_line_number - 3:
    each_run = []
    j = i
    while True:
        if content_list[j] == '\n':
            break
        each_run.append(content_list[j])
        j += 1
    i = j + 1

    match = re.match(r'run (\d+) JUDGED [\S]+? at [\S]+? \((.+?)\) [\S]+ \((.+?)\) ([\S]+?) ', each_run[0])
    run_id = match.group(1)
    time_stamp = time.mktime(time.strptime(match.group(2).split(' ')[3] + ' 2015-04-15', '%H:%M:%S %Y-%m-%d'))
    elaps = time_stamp - start_time
    team = match.group(3)
    problem = match.group(4)

    # print each_run
    result = ""
    for line in each_run:
        line = line.strip()
        if line[0] == '\'':
            result = re.search(r'\'(.+?)\'', line).group(1)

    # print run_id, time_stamp, elaps, team, problem, elaps, result

    ele_run = ET.Element("run")
    ele_run.append(get_elemment("id", run_id))
    ele_run.append(get_elemment("judged", "False"))
    ele_run.append(get_elemment("language", "C++"))
    ele_run.append(get_elemment("problem", problem_map[problem]))
    ele_run.append(get_elemment("status", "fresh"))
    ele_run.append(get_elemment("team", team_name_map[team]))
    ele_run.append(get_elemment("time", str(elaps)))
    ele_run.append(get_elemment("time_stamp", str(time_stamp)))
    ET.dump(ele_run)

    root.append(copy.deepcopy(ele_run))

    ele_run = ET.Element('run')
    ele_run.append(get_elemment("id", run_id))
    ele_run.append(get_elemment("judged", "True"))
    if result == "Yes":
        ele_run.append(get_elemment("penalty", "False"))
    else:
        ele_run.append(get_elemment("penalty", "True"))

    ele_run.append(get_elemment("language", "C++"))
    ele_run.append(get_elemment("problem", problem_map[problem]))
    if result == "Yes":
        ele_run.append(get_elemment("result", "AC"))
    else:
        ele_run.append(get_elemment("result", "CE"))

    if result == "Yes":
        ele_run.append(get_elemment("solved", "True"))
    else:
        ele_run.append(get_elemment("solved", "False"))
    ele_run.append(get_elemment("status", "done"))
    ele_run.append(get_elemment("team", team_name_map[team]))
    ele_run.append(get_elemment("time", str(elaps)))
    ele_run.append(get_elemment("time_stamp", str(time_stamp)))

    root.append(ele_run)

print ""
print ""
print ""
print ""

output_file = open('nn.xml', 'w')
output_file.write(ET.tostring(root, 'utf-8'))