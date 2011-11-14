import MySQLdb
import operator

#Connection Variables
host = "localhost"
user = "root"
passwd="letmein"
db="pps3"

f = open("dump", 'w')

#Jobs Constants
JOB_ID = 0
ROUND_ID = 1
CONFIG_ID = 2
MULTIPLAYER = 3
PLAYER_NUM = 4
SKITTLES_NUM = 5
COLOR_NUM = 6
TASTE_MEAN = 7
PLAYERS = 8
TASTES = 9
INHANDS = 10
OWNED = 11
#Results Constants
PLAYER_NAME = 1
SCORE = 2

jobs = []
results = []
players = []

# connect
db = MySQLdb.connect(host=host, user=user, passwd=passwd,db=db)
cursor = db.cursor()

#Jobs Table
cursor.execute("SELECT * from jobs")
jobs_length = int(cursor.rowcount)
for i in range(0, jobs_length):
	jobs.append(cursor.fetchone())

#Results Table
cursor.execute("SELECT * from result")
results_length = int(cursor.rowcount)
for i in range(0, results_length):
	results.append(cursor.fetchone())

for result in results:
	if result[PLAYER_NAME] not in players:
		players.append(result[PLAYER_NAME])

def replacePlayers(s):
	for i, player in enumerate(players):
		s = s.replace(str(player), str(i+1))
	return s
	

def getScores(skittle, color, player, mean):
	jobs_list = []
	score_results = {}
	for job in jobs:
		if job[PLAYER_NUM] == player and job[COLOR_NUM] == color and job[SKITTLES_NUM] == skittle and job[TASTE_MEAN] == mean:
			jobs_list.append(job[JOB_ID])
	for result in results:
		if result[JOB_ID] in jobs_list:
			if result[JOB_ID] not in score_results:
				score_results[result[JOB_ID]] = 0
			score_results[result[JOB_ID]] += result[SCORE]
	return score_results
			
def playersInJobId(job_id):
	player_list = []
	for result in results:
		if result[JOB_ID] == job_id:
			player_list.append(result[PLAYER_NAME])
	return player_list
	
def sortResults(toSort, reverse):
	sorted_total_results = sorted(toSort.iteritems(), key=operator.itemgetter(1), reverse=reverse)
	return sorted_total_results

#Pairs Rankings
def pairs(skittles, colors, num, mean):
	data = []
	ranking_dict = {}
	score_results = sortResults(getScores(skittles, colors, num, mean), True)
	for i, score in enumerate(score_results):
		player_in_results = list(set(playersInJobId(score[0])))
		data.append([score[0], score[1], player_in_results, i])
		formated_players = replacePlayers(str(player_in_results))
		if formated_players not in ranking_dict:
			ranking_dict[formated_players] = 0
		ranking_dict[formated_players] += float(score[1])			
	return sortResults(ranking_dict, True)

def print_pairs(pairs):
	for pair in pairs:
		f.write(str(pair) + "\n")

def all_pairs(num, mean):
	print "Another run"
	f.write(str(num) + " players\n")
	f.write("mean is " + str(mean) + "\n")
	f.write("************************************\n")
	f.write("Pairs with 1000, 20\n")
	print_pairs(pairs(1000, 20, num, mean))
	f.write("************************************\n")
	f.write("Pairs with 100, 3\n")
	print_pairs(pairs(100, 3, num, mean))
	f.write("************************************\n")
	f.write("Pairs with 20, 2\n")
	print_pairs(pairs(20, 2, num, mean))
	f.write("************************************\n")
	f.write("Pairs with 2, 4\n")
	print_pairs(pairs(2, 4, num, mean))
	f.write("************************************\n")
	f.write("Pairs with 20, 4\n")
	print_pairs(pairs(20, 4, num, mean))
	f.write("************************************\n")
	f.write("Pairs with 40, 20\n")
	print_pairs(pairs(40, 20, num, mean))
	f.write("************************************\n")
	f.write("Pairs with 20, 20\n")
	print_pairs(pairs(20, 20, num, mean))
	f.write("************************************\n")
	f.write("Pairs with 50, 1\n")
	print_pairs(pairs(50, 1, num, mean))
	f.write("************************************\n")

def pairs_by_mean(mean):
	all_pairs(2, mean)
	all_pairs(3, mean)
	all_pairs(5, mean)
	all_pairs(7, mean)

def full_print():
	pairs_by_mean(0.01)
	pairs_by_mean(0.5)

full_print()

f.close()


#End of File
