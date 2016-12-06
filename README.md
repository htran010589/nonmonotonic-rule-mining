RUMIS - Nonmonotonic Rule Mining System
=============


Tool Description
------------

RUMIS is a system for mining nonmonotonic rules from a knowledge graph (KG) under the Open World Assumption (OWA). It exploits association rule learning techniques. It first extracts frequent conjunctive queries from a KG and then casts them into rules based on conviction measure. The rules are revised by incorporating exceptions in the form of negated atoms into the rule bodies to improve their predictive quality. For example, based on given facts and a positive rule livesIn(X, Z) :- isMarriedTo(X, Y), livesIn(Y, Z); the tool may find out that the rule is not true if X is a researcher. As a result, livesIn(X, Z) :- isMarriedTo(X, Y), livesIn(Y, Z), not Researcher(X) can be mined in the final result. Researcher(X) in this case is an exception for the Horn rule.

### Knowledge Graph Format

The knowlege graph is presented in a tab seperated file containing triples in the format <subject predicate object> (format 1). You can see this format in data/experiment/IMDB/ideal.data.txt or data/experiment/IMDB/training.data.txt of the repository.

### Horn Rule Format

Each line in this file represents a positive rule of the form
h(X, Z) :- p(X, Y), q(Y, Z) and its absolute support s (format 2), i.e., the number of substitutions X/c, Y/d, Z/e that
satisfy the above rule. Rules are sorted in the decreasing order based on their absolute support. You can see this format in data/experiment/IMDB/patterns.txt of the repository.

### Exception Ranking

Exceptions for each positive Horn rule are ranked based on a particular ranking criteria. There are three kinds of ranking: Naive, PM and OPM. For the their definitions, please refer to our paper in [1].

### Command Line Options

Users can choose some of the following options to get a suitable mode for RUMIS:

 -e,--exe <arg>      this requires a function for execution, i.e., pos and neg are corresponding to positve and nonmonotonic rule mining, resp.
 -h,--help           command line interface description.
 -l,--learn <arg>    this requires a learning graph file path in format 1.
 -p,--pos <arg>      this requires an Horn rule file in format 2.
 -r,--rank <arg>     this requires a ranking type, i.e., 0, 1, 2 for Naive, PM, OPM ranking, resp.
 -t,--top <arg>      this requires number of positive rules with top absolute support.

Operating System and Required Softwares
------------

This tool is developed and currently tested in Linux, we may extend it to Windows in the future. Besides, Apache Maven should be installed before running the system.

Installation
------------

```
Download Apache Maven from https://maven.apache.org and install.
```

Usage
------------

Firat of all, please locate to the repository folder and set run.sh as executable file:

```
$ cd nonmonotonic-rule-mining
$ chmod a+x run-pattern-mining.sh
```

### Horn Rule Mining

Please run the Horn rule mining with the following command:

```
$ ./run.sh -e=pos -l=[path to knowledge graph file]
```

Note that current system only supports positive rules with format 2.

#### Example:

Please download the repository and run the following command for executing IMDB Horn rule mining:

```
$ ./run.sh -e=pos -l=data/experiment/IMDB/ideal.data.txt
```

### Nonmonotonic Rule Mining

Please run the nonmonotonic rule mining with the following command:

```
$ ./run.sh -e=neg -p=[path to pattern file] -l=[path to knowledge graph file] -r=[ranking option]
```

#### Example:

Please download the repository and run the following command for executing IMDB nonmonotonic rule mining with OPM ranking:

```
$ ./run.sh -e=neg -p=data/experiment/IMDB/patterns.txt -l=data/experiment/IMDB/training.data.txt -r=2
```

References
----------
[1] Hai Dang Tran, Daria Stepanova, Mohamed H. Gad-Elrab, Francesca A. Lisi, Gerhard Weikum. Towards Non-monotonic Relational Learning from Knowledge Graphs. In 26th International Conference on Inductive Logic Programming (ILP-16), London, England, 2016.
