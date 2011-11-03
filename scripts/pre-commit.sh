results_path="scripts/output/"


java -classpath bin/:scripts/ skittles.sim.BatchSkittles > ${results_path}results.txt
[ $? -eq 1 ] && echo "Sim crashed!" && exit 1
ruby scripts/results_analyzer.rb
[ $? -eq 1 ] && echo "Failed regression test!" && exit 1
mv ${results_path}results.txt ${results_path}old_results.txt
