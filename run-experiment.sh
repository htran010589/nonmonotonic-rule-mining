mvn compile
mvn package
mvn exec:java -Dexec.mainClass="com.mpii.saarland.germany.experiment.Conductor" -Dexec.args="$1 $2"