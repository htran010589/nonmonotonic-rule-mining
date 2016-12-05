mvn compile
#export MAVEN_OPTS="-Xmx400G"
mvn exec:java -Dexec.mainClass="com.mpii.saarland.germany.rulemining.nonmonotonicrule.ExceptionRanker" -Dexec.args="$$@"