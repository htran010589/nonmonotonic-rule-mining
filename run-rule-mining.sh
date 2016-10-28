mvn compile
if [ $# -eq 3 ]
then
	mvn exec:java -Dexec.mainClass="com.mpii.saarland.germany.rulemining.nonmonotonicrule.ExceptionRanker" -Dexec.args="$1 $2 $3"
else
	mvn exec:java -Dexec.mainClass="com.mpii.saarland.germany.rulemining.nonmonotonicrule.ExceptionRanker" -Dexec.args="$1 $2 $3 $4"
fi
