RUMIS - Nonmonotonic Rule Mining System
=============


Tool Description
------------

This system is used to mine nonmonotonic rules from a knowledge graph and a list of positive Horn rules. For example, based on given facts and a positive rule livesIn(X, Z) :- isMarriedTo(X, Y), livesIn(Y, Z); the tool may find out that the rule is not true if X is a researcher. As a result, livesIn(X, Z) :- isMarriedTo(X, Y), livesIn(Y, Z), not Researcher(X) can be mined in the final result.

### Knowledge Graph File

The knowlege graph file is in RDF format (format 1). This file also contains unary fact with predicate being "type". You can see this format in data/experiment/IMDB/ideal.data.txt or data/experiment/IMDB/training.data.txt of the repository.

### Pattern File

The pattern file contains positive rule on each line. The positive rule is in a format: h\tabp\tabq (format 2), that means h(X, Z) :- p(X, Y), q(Y, Z). This is the only form that the system currently supports. You can see this format in data/experiment/IMDB/patterns.txt of the repository.

### Ranking Option

As regards the ranking option, it can be 0, 1, 2 which stands for naive, pm and opm ranking, respectively. For the definition of naive, pm and opm ranking, please refer to our paper in [1].

### DLV Option

With the DLV option, it can be 0, 1 which stands for without and with DLV, respectively. More specifically, option 1 means DLV is enabled and choosen rules are applied to the learning graph. This option is also mentioned in experiment section.

Operating System and Required Softwares
------------

This tool is developed and currently tested in Linux, we may extend it to Windows in the future. Besides, Apache Maven and DLV should be installed before running the system.

Installation
------------

```
Download Apache Maven from https://maven.apache.org and install.
Create an experiment folder, copy ideal.data.txt (ideal knowledge graph file), patterns.txt (pattern file), training.data.txt (training graph file) to this folder.
(Note that ideal and training knowledge graph file are in format 1 while pattern file is in format 2)
Download DLV from http://www.dlvsystem.com, choose a version for Linux, rename it to dlv.bin and copy to the experiment folder.
Download the repository of the system from https://github.com/htran010589/nonmonotonic-rule-mining/archive/master.zip, then uncompress it.
```

Usage
------------

### Pattern Mining

```
cd nonmonotonic-rule-mining
chmod a+x run-pattern-mining.sh
./run-pattern-mining.sh [path to knowledge graph file]
```

Note that current system only supports patterns or positive rules with format 2.

#### Example with IMDB Pattern Mining

Please download the repository and run the following command for executing IMDB pattern mining:

```
./run-pattern-mining.sh data/experiment/IMDB/ideal.data.txt
```

### Rule Mining

```
cd nonmonotonic-rule-mining
chmod a+x run-rule-mining.sh
./run-rule-mining.sh [path to pattern file] [path to knowledge graph file] [ranking option]
```

Or if you just concern top k patterns, the following commands can be used:

```
cd nonmonotonic-rule-mining
chmod a+x run-rule-mining.sh
./run-rule-mining.sh [path to pattern file] [path to knowledge graph file] [ranking option] [top k patterns]
```

#### Example with IMDB Rule Mining

Please download the repository and run the following command for executing IMDB rule mining with opm ranking:

```
./run-rule-mining.sh data/experiment/IMDB/patterns.txt data/experiment/IMDB/training.data.txt 2
```

Or with opm ranking and top 10 patterns:

```
./run-rule-mining.sh data/experiment/IMDB/patterns.txt data/experiment/IMDB/training.data.txt 2 10
```

### Experiment

```
cd nonmonotonic-rule-mining folder
chmod a+x run-experiment.sh
./run-experiment.sh [path to working folder] [ranking option] [top k patterns] [dlv option]
```

#### Example with IMDB Experiment

Please download the repository and run the following command for executing IMDB experiment with opm ranking, top 10 patterns and enable DLV option:

```
./run-experiment.sh data/experiment/IMDB/ 2 10 1
```

References
----------
[1] Hai Dang Tran, Daria Stepanova, Mohamed H. Gad-Elrab, Francesca A. Lisi, Gerhard Weikum. Towards Non-monotonic Relational Learning from Knowledge Graphs. In 26th International Conference on Inductive Logic Programming (ILP-16), London, England, 2016.
