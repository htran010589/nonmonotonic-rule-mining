mvn compile
#export MAVEN_OPTS="-Xmx400G"
if [ $# -eq 1 ]
then
	mvn exec:java -Dexec.mainClass="com.mpii.saarland.germany.rulemining.patternmining.PatternForm1Miner" -Dexec.args="$1"
else
	mvn exec:java -Dexec.mainClass="com.mpii.saarland.germany.rulemining.patternmining.PatternForm1Miner" -Dexec.args="$1 $2"
fi
