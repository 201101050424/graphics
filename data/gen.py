# -*- coding: UTF-8  -*-
import xml.etree.ElementTree as ET
import re
import time
import copy


def get_element(tag, text):
    ele = ET.Element(tag)
    ele.text = text
    return ele


start_team = []
women_temp = []
team_map = {}


def generate_team_info(root, file_name):
    team_name_map = {}

    team_file = open(file_name, 'r')
    for team in team_file:
        team = team.split()
        team[5] = team[5].decode('utf-8')
        team[4] = team[4].decode('utf-8')

        sss = team[5]
        sss += ' '
        sss += team[4]

        # print sss
        team_ele = ET.Element('team')
        team_ele.append(get_element('external-id', team[1]))
        team_ele.append(get_element('id', team[1]))
        team_ele.append(get_element('name', sss))
        team_ele.append(get_element('nationality', 'China'))
        team_ele.append(get_element('region', 'Asia'))
        team_ele.append(get_element('university', team[5]))

        team_map[team[1]] = team[5] + "——".decode('utf-8') + team[4]

        sss = team[5]+' '
        sss += team[4]

        # print sss
        # print '%-22s%-s' % (team[5], team[4])
        # ss=team[5].ljust(22)+team[4]
        # print ss
        # print team[5] + '——'.decode('utf-8') + team[4], len(team[5]), len(team[4])
        if team[6] == '1':
            # print team[1], ',,,', team[4]
            temp_str = team[1]
            start_team.append(temp_str)

        if team[7] == '1':
            women_temp.append(team[1])

        # print team_ele.find('name').text
        team_name_map[team[1]] = True
        root.append(team_ele)

    girlTeam = open('girlTeamFile.txt', 'w')
    for item in women_temp:
        girlTeam.write(item + '\n')

    start_team_file = open('startTeamFile.txt', 'w')
    # print start_team
    for item in start_team:
        start_team_file.write(item + '\n')

    return team_name_map


def generate_problem_info(root, file_name):
    problem_map = {}
    problem_file = open(file_name, 'r')
    for problem in problem_file:
        problem = problem.split()
        problem[1] = problem[1].decode('utf-8')

        problem_ele = ET.Element('problem')
        problem_ele.append(get_element('id', problem[0]))
        problem_ele.append(get_element('name', problem[1]))

        root.append(problem_ele)
        problem_map[problem[1]] = problem[0]
    return problem_map


def generate_run_info(root, file_name, team_name_map, problem_map):
    input_file = open(file_name, 'r')
    content_list = []
    for each_line in input_file:
        content_list.append(each_line.decode('utf-8'))
    run_number = int(re.match(r'-- (\d+) runs --', content_list[10]).group(1))

    i = 11
    total_line_number = len(content_list)
    start_time = time.mktime(time.strptime('09:00:09 2015-05-10', '%H:%M:%S %Y-%m-%d'))
    # print start_time
    while i < total_line_number - 3:
        each_run = []
        j = i
        while True:
            if content_list[j] == '\n':
                break
            each_run.append(content_list[j])
            j += 1
        i = j + 1

        match = re.match(r'run (\d+) JUDGED [\S]+? at ([\S]+?) \((.+?)\) team([\d]+) \(.+?\) ([\S]+?) ', each_run[0])
        # print match.groups()
        run_id = match.group(1)
        elaps = int(match.group(2)) * 60.0
        time_stamp = start_time + elaps

        team = match.group(4)
        problem = match.group(5)

        # print each_run
        result = ""
        for line in each_run:
            line = line.strip()
            if line[0] == '\'':
                result = re.search(r'\'(.+?)\'', line).group(1)

        # if team == "46":
        #     print run_id, time_stamp, elaps, team, problem, elaps, result

        if team not in team_name_map:
            continue

        ele_run = ET.Element("run")
        ele_run.append(get_element("id", run_id))
        ele_run.append(get_element("judged", "False"))
        ele_run.append(get_element("language", "C++"))
        ele_run.append(get_element("problem", problem_map[problem]))
        ele_run.append(get_element("status", "fresh"))
        ele_run.append(get_element("team", team))
        ele_run.append(get_element("time", str(elaps)))
        # print str(elaps), str(time_stamp), start_time
        ele_run.append(get_element("time_stamp", str(time_stamp)))

        # if team=="46":
        # ET.dump(ele_run)

        root.append(copy.deepcopy(ele_run))

        ele_run = ET.Element('run')
        ele_run.append(get_element("id", run_id))
        ele_run.append(get_element("judged", "True"))
        if result == "Yes":
            ele_run.append(get_element("penalty", "False"))
        else:
            ele_run.append(get_element("penalty", "True"))

        ele_run.append(get_element("language", "C++"))
        ele_run.append(get_element("problem", problem_map[problem]))
        if result == "Yes":
            ele_run.append(get_element("result", "AC"))
        else:
            ele_run.append(get_element("result", "CE"))

        if result == "Yes":
            ele_run.append(get_element("solved", "True"))
        else:
            ele_run.append(get_element("solved", "False"))
        ele_run.append(get_element("status", "done"))
        ele_run.append(get_element("team", team))
        ele_run.append(get_element("time", str(elaps)))
        ele_run.append(get_element("time_stamp", str(time_stamp)))

        root.append(ele_run)


def generate_award():
    tree = ET.parse('result.xml')
    root = tree.getroot()
    gold_num = 11
    silver_num = 21
    bronze_num = 32
    standings = root.findall('teamStanding')
    rank_map = {}
    for item in standings:
        rank_map[item.get('rank')] = item.get('teamId')
    # print rank_map

    gold_file = open('goldTeamFile.txt', 'w')
    count = 0
    temp_rank = 0
    while count < gold_num:
        temp_rank += 1
        if rank_map[str(temp_rank)] in start_team:
            continue
        gold_file.write(rank_map[str(temp_rank)] + '\n')
        print team_map[rank_map[str(temp_rank)]]
        count += 1

    count = 0
    silver_file = open('silverTeamFile.txt', 'w')
    while count < silver_num:
        temp_rank += 1
        if rank_map[str(temp_rank)] in start_team:
            continue
        silver_file.write(rank_map[str(temp_rank)] + '\n')
        # print team_map[rank_map[str(temp_rank)]]
        count += 1

    count = 0
    silver_file = open('bronzeTeamFile.txt', 'w')
    while count < bronze_num:
        temp_rank += 1
        if rank_map[str(temp_rank)] in start_team:
            continue
        silver_file.write(rank_map[str(temp_rank)] + '\n')
        # print team_map[rank_map[str(temp_rank)]]
        count += 1

    min_rank = 1000000
    for key in rank_map:
        if rank_map[key] in start_team:
            continue
        if int(key) < min_rank:
            min_rank = int(key)

    first_team = open('firstTeamFile.txt', 'w')
    first_team.write(rank_map[str(min_rank)])
    # print team_map[rank_map[str(min_rank)]]

    min_rank = 1000000
    for key in rank_map:
        if rank_map[key] in start_team:
            continue
        if rank_map[key] not in women_temp:
            continue
        if int(key) < min_rank:
            min_rank = int(key)

    women_file = open('bestWomen.txt', 'w')
    women_file.write(rank_map[str(min_rank)])
    # print team_map[rank_map[str(min_rank)]]


if __name__ == "__main__":
    # print("...")
    tree = ET.parse('basic_info.xml')
    root = tree.getroot()
    team_name_map = generate_team_info(root, 'team')
    # print team_name_map
    problem_map = generate_problem_info(root, 'problem')
    generate_run_info(root, 'runs', team_name_map, problem_map)
    output_file = open('contest.xml', 'w')
    output_file.write(ET.tostring(root, 'utf-8'))

    generate_award()

