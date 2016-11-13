mvn compile
export MAVEN_OPTS="-Xmx400G"
mvn exec:java -Dexec.mainClass="com.mpii.saarland.germany.experiment.Conductor" -Dexec.args="$1 $2 $3 $4"
