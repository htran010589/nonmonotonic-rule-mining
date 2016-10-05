mvn compile
mvn package
mvn exec:java -Dexec.mainClass="com.mpii.saarland.germany.rulemining.nonmonotonicrule.ExceptionRanker" -Dexec.args="$1 $2"
