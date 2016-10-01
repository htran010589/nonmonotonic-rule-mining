mvn compile
mvn package
mvn exec:java -Dexec.mainClass="com.mpii.saarland.germany.rulemining.nonmonotonicrule.ExceptionRanker" -Dexec.args="data/imdb.form2.patterns.txt data/imdb.facts.tsv 2"