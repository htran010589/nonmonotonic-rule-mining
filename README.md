RUMIS - Nonmonotonic Rule Mining System
=============


Tool Description
------------

RUMIS is a system for mining nonmonotonic rules from a knowledge graph (KG) under the Open World Assumption (OWA). It exploits association rule learning techniques. It first extracts frequent conjunctive queries from a KG and then casts them into rules based on conviction measure. The rules are revised by incorporating exceptions in the form of negated atoms into the rule bodies to improve their predictive quality. For example, based on given facts and a positive rule livesIn(X, Z) :- isMarriedTo(X, Y), livesIn(Y, Z); the tool may find out that the rule is not true if X is a researcher. As a result, livesIn(X, Z) :- isMarriedTo(X, Y), livesIn(Y, Z), not Researcher(X) can be mined in the final result. Researcher(X) in this case is an exception for the Horn rule.

### Knowledge Graph Format

The KG is presented in a tab seperated file containing triples in the format <subject predicate object> (format 1). You can see this format after uncompressing data/sample.imdb.txt.zip file in the repository.

### Horn Rule Format

Each line in this file represents a positive rule of the form
h(X, Z) :- p(X, Y), q(Y, Z). Rules are sorted in the decreasing order based on their absolute support [1]. You can see this format in the output of positive rule mining function below.

### Exception Ranking

Exceptions for each positive Horn rule are ranked based on a particular ranking criteria. There are three kinds of ranking: Naive, PM and OPM. For the their definitions, please refer to our paper in [1].

### Command Line Options

Users can choose some of the following options to get a suitable mode for RUMIS:

 -e,--exe <arg>: This requires a function for execution, i.e., pos and neg are corresponding to positve and nonmonotonic rule mining, resp.

 -h,--help: This is the command line interface description.

 -l,--learn <arg>: This requires a KG file path in format 1.

 -p,--pos <arg>: This requires an Horn rule file path in format 2.

 -r,--rank <arg>	: This requires a ranking type, i.e., 0, 1, 2 for Naive, PM, OPM ranking, resp.

 -t,--top <arg>: This requires number of positive rules with top absolute support.

Operating System and Required Softwares
------------

This tool is developed and currently tested in Linux, we may extend it to Windows in the future. Besides, Java 8 should be installed before running the system.

Usage
------------

First of all, please locate to the repository folder:

```
$ cd nonmonotonic-rule-mining
```

And then uncompress data/sample.imdb.txt.zip in the repository to get sample.imdb.txt file.

### Horn Rule Mining

Please run the Horn rule mining with the following command:

```
$ java -jar rumis-1.0.jar -e=pos -l=[path to knowledge graph file]
```

This will generate two files, horn-rules.txt for the positive rules and horn-rules-stats.txt with the presence of absolute support. Both rules in two files are sorted by decreasing order of absolute support.

#### Example:

Please download the repository and run the following command for executing IMDB Horn rule mining:

```
$ java -jar rumis-1.0.jar -e=pos -l=data/sample.imdb.txt
```

You will see two generated files horn-rules.txt and horn-rules-stats.txt in the same folder with RUMIS jar file.

### Nonmonotonic Rule Mining

Please run the nonmonotonic rule mining with the following command:

```
$ java -jar rumis-1.0.jar -e=neg -p=[path to pattern file] -l=[path to knowledge graph file] -r=[ranking option] -t=[top Horn rules]
```

You will see revised rules from generated file revised-rules.txt in the same folder with RUMIS jar file. Note that horn-rules.txt is generated by using above Horn rule mining function, or by another software. However, RUMIS currently only supports Horn rules in format 2.

The file revised-rules.txt contains rules from horn-rules.txt revised using the chosen ranking approach. There are two parts in these files, one for the ranking and the other one for the chosen revised rules at the end. The first part of every file contains the list of initial Horn rules with statistics on them in the following format (please refer [1] for definition of conviction and confidence measure):

```
<rule> <conviction> <confidence>
```

followed by their top 10 exceptions, sorted in the decreasing order of the positive-negative conviction (PosNegConv) for the respective revision obtained by adding the respective exception to the body of the rule [1]. The statistics for the revisions is given in the following format:

```
<exception> <posNegConviction> <conviction>
```

The second part of the file is a list of best revisions (rank 1) of Horn rules in horn-rules.txt file.

#### Example:

Please download the repository and run the following command for executing IMDB nonmonotonic rule mining with OPM ranking:

```
$ java -jar rumis-1.0.jar -e=neg -p=horn-rules.txt -l=data/sample.imdb.txt -r=2
```

If you just want to revise top 10 Horn rules, please use -t option:

```
$ java -jar rumis-1.0.jar -e=neg -p=horn-rules.txt -l=data/sample.imdb.txt -r=2 -t=10
```

References
----------
[1] Hai Dang Tran, Daria Stepanova, Mohamed H. Gad-Elrab, Francesca A. Lisi, Gerhard Weikum. Towards Non-monotonic Relational Learning from Knowledge Graphs. In 26th International Conference on Inductive Logic Programming (ILP-16), London, England, 2016.
