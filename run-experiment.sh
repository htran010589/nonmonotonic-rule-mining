mvn compile
if [ $# -eq 2 ]
then
	mvn exec:java -Dexec.mainClass="com.mpii.saarland.germany.experiment.Conductor" -Dexec.args="$1 $2"
else
	mvn exec:java -Dexec.mainClass="com.mpii.saarland.germany.experiment.Conductor" -Dexec.args="$1 $2 $3"
fi
