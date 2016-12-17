RUMIS - Nonmonotonic Rule Mining System
=============


Tool Description
------------

RUMIS is a system for mining nonmonotonic rules from a knowledge graph (KG) under the Open World Assumption (OWA). It exploits association rule learning techniques. It first extracts frequent conjunctive queries from a KG and then casts them into rules based on conviction measure. The rules are revised by incorporating exceptions in the form of negated atoms into the rule bodies to improve their predictive quality. For example, based on given facts and a positive rule livesIn(X, Z) :- isMarriedTo(X, Y), livesIn(Y, Z); the tool may find out that the rule is not true if X is a researcher. As a result, livesIn(X, Z) :- isMarriedTo(X, Y), livesIn(Y, Z), not Researcher(X) can be mined in the final result. Researcher(X) in this case is an exception for the Horn rule.

### Knowledge Graph Format

The KG is presented in a tab seperated file containing triples in the format &lt;subject&gt; &lt;predicate&gt; &lt;object&gt; (format 1). You can see this format after uncompressing data/sample.imdb.txt.zip file in the repository.

### Horn Rule Format

Each line in this file represents a positive rule of the form
h(X, Z) :- p(X, Y), q(Y, Z) (format 2). Rules are sorted in the decreasing order based on their absolute support [1]. You can see this format in the output of positive rule mining function below.

### Exception Ranking

Exceptions for each positive Horn rule are ranked based on a particular ranking criteria. There are three kinds of ranking: Naive, PM and OPM. For the their definitions, please refer to our paper in [1].

### DLV Tool
DLV [2] is a system that extends a KG given a set of Horn or nonmonotonic rules at hand. RUMIS exploits DLV in its experiment function, i.e, a RUMIS' feature for evaluating result.

### Predicate Ratio and Learning KG
RUMIS provides a function for creating a learning KG based on a predicate ratio. For example, given the KG and 0.8 as the ratio, then 80% facts of every binary predicate are retained in the learning KG. The original KG is called the approximated ideal one in [1].

### Working Folder
Working folder is a location for experiment where we have learning and approximated ideal KGs in format 1 (training.data.txt and ideal.data.txt, resp.), positive rule and sampled positive rule files (horn-rules.txt and selected.horn-rules.txt, resp.) in format 2. Besides, DLV binary file [3] should be downloaded and renamed to dlv.bin in this folder. The selected.horn-rules.txt containing a subset of rules in horn-rules.txt, it is only necessary if we want to revise some rules in this file.

### Command Line Options

Users can choose some of the following options to get a suitable mode for RUMIS:

 -d: This flag is to enable DLV in order to extend KG.

 -e=[execution function]: This requires a string as a function for execution, i.e., new, pos and neg, exp are corresponding to creating a new learning KG, positve and nonmonotonic rule mining and conducting experiment, resp.

 -f=[working folder path]: This requires a string as an experiment folder path.

 -h: This is the command line interface description.

 -l=[KG file path]: This requires a string as a KG file path in format 1.

 -o=[predicate ratio]: This requires a percentage as a predicate ratio to create a new learning KG.

 -p=[Horn rule file path]: This requires a string as an Horn rule file path in format 2.

 -r=[ranking]: This requires an integer as a ranking type, i.e., 0, 1, 2 for Naive, PM, OPM ranking, resp.

 -s: This flag is to enable sampling positive rules.

 -t=[number of top Horn rules]: This requires an integer as a number of positive rules with top absolute support.

Operating System and Required Softwares
------------

This tool is developed and currently tested in Linux, we may extend it to Windows in the future. Besides, Java 8 should be installed before running the first three features such as creating learning data, positive and revised rule mining. As regards experiment function, the required environment and library are Linux with the DLV binary file.

Usage
------------

First of all, please download the repository and uncompress data/sample.imdb.txt.zip in the repository to get sample.imdb.txt file. In the next step, the repository folder should be located:

```
$ cd nonmonotonic-rule-mining
```

### Generating Learning KG

Please generate the learning data in the format 1 with the following command:

```
$ java -jar rumis-1.0.jar -e=new -l=[path to KG file] -o=[ratio] 1>[training KG file path]
```

#### Example

A learning KG of the IMDB sample dataset can be generated with predicate ratio being 80%:

```
$ java -jar rumis-1.0.jar -e=new -l=data/sample.imdb.txt -o=0.8 1>training.sample.imdb.txt
```

Then training.sample.imdb.txt file is the learning KG that we want to generate.

### Horn Rule Mining

Please run the Horn rule mining with the following command:

```
$ java -jar rumis-1.0.jar -e=pos -l=[path to KG file]
```

This will generate two files, horn-rules.txt for the positive rules and horn-rules-stats.txt with the presence of absolute support. Both rules in two files are sorted by decreasing order of the support measure.

#### Example:

Please run the following command for executing IMDB Horn rule mining:

```
$ java -jar rumis-1.0.jar -e=pos -l=data/sample.imdb.txt
```

Two generated files horn-rules.txt and horn-rules-stats.txt can be seen in the same folder with RUMIS jar file.

### Nonmonotonic Rule Mining

Please run the nonmonotonic rule mining with the following command:

```
$ java -jar rumis-1.0.jar -e=neg -p=[path to positive rule file] -l=[path to KG file] -r=[ranking option] -t=[top Horn rules]
```

Revised rules can be seen from generated result file revised-rules.txt in the same folder with RUMIS jar file. Note that horn-rules.txt is generated by using above Horn rule mining function, or by another software. However, RUMIS currently only supports Horn rules in format 2.

The file revised-rules.txt lists the revisions of Horn rules from horn-rules.txt with the ranking option being specified in the command. Two main sections are presented in revised-rules.txt, the first one describes exceptions ranked for each Horn rule and the second one lists final selected revisions. As regards the first section, Naive ranking subsection is always presented on top of the file, followed by [PM | OPM] one if [PM | OPM] is selected, resp.

Every ranking subsection contains many parts separated by a blank line, each of them describes rules with exceptions. The first line of each part is a positive rule and its measure values with the format: &lt;rule&gt; &lt;Conv&gt; &lt;Conf&gt;. Conv and Conf are abbreviations of conviction and confidence measures, resp. Please refer [1] for definition of conviction and confidence measures.

The rest of each above part is top 10 negated atoms for each Horn rule which are sorted according to the decreasing order of the positive-negative conviction (PosNegConv) of the corresponding revision achieved by inserting the negated atom to the rule [1]. If two revisions have the same positive-negative conviction, the one with higher conviction has a higer rank. The format for describing each negated atom with its measures is: &lt;not exception&gt; &lt;PosNegConv&gt; &lt;Conv&gt;.

The second section of the revised-rules.txt lists chosen revisions for all the Horn rules. They are corresponding to the best exceptions of positive rules in the subsection of given ranking option.

#### Example:

Please run the following command for executing IMDB nonmonotonic rule mining with OPM ranking:

```
$ java -jar rumis-1.0.jar -e=neg -p=horn-rules.txt -l=data/sample.imdb.txt -r=2
```

If you just want to revise top 10 Horn rules, please use -t option:

```
$ java -jar rumis-1.0.jar -e=neg -p=horn-rules.txt -l=data/sample.imdb.txt -r=2 -t=10
```

### Experiment

First of all, please create the working folder indicated above, and then run the following command for the experiment:

```
java -XX:-UseGCOverheadLimit -Xmx[max memory]G -jar rumis-1.0.jar -e=exp -f=[working folder] -r=[ranking] -t=[top positive rules] -d -s 1>experiment.txt 2>log
```

Where -XX, -Xmx, -t, -d, -s are optional. -XX and -Xmx are used when we want to allocate more memory for RUMIS, and, if the option -t is not used, all the rules in horn-rules.txt are revised. Besides, -s option should only be added to the command if we just want to care about revisions of some selected rules in selected.horn-rules.txt file.

The first two lines of the generated file experiment.txt present the average conviction of selected Horn rules and their final revisions. These statistics are described in the Table 1 in [1] with different number of top Horn rules. Besides, the rest two parts of the file show predicates extended from the learning KG for each positive and revised rules. Format of every tab separated line in each part is &lt;relation&gt; &lt;inferred facts&gt; &lt;good facts&gt; &lt;other facts&gt; where inferred facts means total number of predicted triples over the relation. Besides, good and other facts indicate quantity of inferred facts that are in and not in the ideal KG, resp.

The command outputs a file encode.txt and a DLV directory in the working folder. The former is a tab separated file that maps from entities and predicates of ideal KG to their encoded IDs. The latter provides some files as follows. First, training.data.kg is the DLV format version of training.data.txt. Second, files chosen.rules.[naive | pm | opm].txt.[pos | neg].[number of top rules] list chosen rules in encoded DLV format, i.e, DLV rule format s.t. predicated and exceptions are encoded. Third, files extension.[naive | pm | opm].txt.[pos | neg].[number of top rules] describe KGs extended from learning data in DLV format and corresponding rules. The terms pos, neg correspond to positive and revised rule sets which are exploited to extend KG. Finally, For each of these, there are decode, needcheck, good, conflict extension files which present KG in format 1, facts not in the ideal KG, facts in the ideal KG and conflicts, resp [1].

#### Example

The working folder can be built by the following steps. First, please create a directory data/experiment/IMDB, rename sample.imdb.txt file to ideal.data.txt in the directory. Second, please create learning data as training.data.txt in the same location with ideal KG file. Third, one should generate horn-rules.txt as an output of positive rule mining function applied to the learning data and put it in the working folder. If only some positive rules need to be revised, we can list them in selected.horn-rules.txt of the same location. Finally, DLV binary file should be downloaded to the directory.

The command that executes experiment with OPM ranking and top 10 positive rules (without DLV) is as follows.

```
java -jar rumis-1.0.jar -e=exp -f=data/experiment/IMDB/ -r=2 -t=10 1>experiment.txt 2>log
```

If we want to expand memory, enable DLV and selected rule option, the following command can be used.

```
java -XX:-UseGCOverheadLimit -Xmx300G -jar rumis-1.0.jar -e=exp -f=data/experiment/IMDB/ -r=2 -t=10 -d -s 1>experiment.txt 2>log
```

References
----------
[1] Hai Dang Tran, Daria Stepanova, Mohamed H. Gad-Elrab, Francesca A. Lisi, Gerhard Weikum. Towards Non-monotonic Relational Learning from Knowledge Graphs. In 26th International Conference on Inductive Logic Programming (ILP-16), London, England, 2016.

[2] DLV User Manual. http://www.dlvsystem.com/html/DLV_User_Manual.html

[3] DLV Binary File. http://www.dlvsystem.com/files/dlv.x86-64-linux-elf-static.bin