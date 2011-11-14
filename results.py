import MySQLdb
import operator
from collections import defaultdict

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
	

def getScores(skittles, color, player, mean):
	score_results = defaultdict(int)
	
	cursor.execute("select job_id, player_num, score from result, jobs where player_num = "+str(player)+" and color_num = "+str(color)+" and skittles_num = "+str(skittles)+" and taste_mean = "+str(mean))
	results_length = int(cursor.rawcount)
	for i in xrange(0, results_length):
	    job_id, player_num, score = cursor.fetchone()
	    score_results[(job_id, player_num)] += score
	
	return score_results
			
def playersInJobId(job_id):
	player_list = []
	cursor.execute("select player_name from result where job_id = "+str(job_id))
	results_length = int(cursor.rawcount)
	
	for i in xrange(0, results_length):
	    player_list.append(cursor.fetchone()[0])
	
	return player_list
	
def sortResults(toSort, reverse):
	sorted_total_results = sorted(toSort.iteritems(), key=operator.itemgetter(1), reverse=reverse)
	return sorted_total_results

def getJobIds(skittles, colors, num, mean):
    cursor.execute("select job_id from result, jobs where player_num = "+str(num)+" and color_num = "+str(colors)+" and skittles_num = "+str(skittles)+" and taste_mean = "+str(taste_mean))
    results = []
    results_length = int(cursor.rawcount)
    for i in xrange(0, results_length):
        results.append(cursor.fetchone()[0])
    return results

#Pairs Rankings
def groups(skittles, colors, num, mean):
	#data = []
	ranking_dict = defaultdict(int)
	scores_for_players = {}
	score_results = sortResults(getScores(skittles, colors, num, mean), True)
	# get job ids for this config
	job_ids = getJobIds(*args)
	# get players for each job id
	for job_id in job_ids:
	    players = sorted(playersInJobId(job_id))
	    scores = []
	    # get scores for players
	    for player in players:
	        scores.append(score_results[(job_id, player)])
	    scores_for_players[(set(players),)] = scores
	     
	return scores_for_players
	
	#for i, score in enumerate(score_results):
	#	players_in_results = list(set(playersInJobId(score[0][0])))
	#	#data.append([score[0], score[1], players_in_results, i])
	#	formatted_players = replacePlayers(str(player_in_results))
	#	
	#	ranking_dict[formatted_players] += float(score[1])	
	#return sortResults(ranking_dict, True)

def print_groups(pairs):
	for pair in pairs:
		f.write(str(pair) + "\n")

def all_groups(num, mean):
	print "Another run"
	f.write(str(num) + " players\n")
	f.write("mean is " + str(mean) + "\n")
	print_groups(groups(1000, 20, num, mean))
	f.write("************************************\n")
	f.write("Pairs with 100, 3\n")
	print_groups(groups(100, 3, num, mean))
	f.write("************************************\n")
	f.write("Pairs with 20, 2\n")
	print_groups(groups(20, 2, num, mean))
	f.write("************************************\n")
	f.write("Pairs with 2, 4\n")
	print_groups(groups(2, 4, num, mean))
	f.write("************************************\n")
	f.write("Pairs with 20, 4\n")
	print_groups(groups(20, 4, num, mean))
	f.write("************************************\n")
	f.write("Pairs with 40, 20\n")
	print_groups(groups(40, 20, num, mean))
	f.write("************************************\n")
	f.write("Pairs with 20, 20\n")
	print_groups(groups(20, 20, num, mean))
	f.write("************************************\n")
	f.write("Pairs with 50, 1\n")
	print_groups(groups(50, 1, num, mean))
	f.write("************************************\n")

def pairs_by_mean(mean):
	all_groups(2, mean)
	all_groups(3, mean)
	all_groups(5, mean)
	all_groups(7, mean)

def full_print():
	pairs_by_mean(0.01)
	pairs_by_mean(0.5)

full_print()

f.close()


#End of File
