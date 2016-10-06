This tool requires maven, linux and can execute on Ganymede server of MPI Informatics which has 400G RAM. You can test the system by executing rule mining or the experiment.

1. How to run rule mining?
Step 1: move to nonmonotonic-rule-mining folder and chmod a+x run-rule-mining.sh
Step 2: ./run-rule-mining.sh [path to pattern file] [path to knowledge graph file]

Note that the pattern file contains positive rule on each line. The positive rule is in a format: h\tabp\tabq (format 1), that means h(X, Z) <- p(X, Y) ^ q(Y, Z). This is the only form that the system currently supports.

The knowlege graph file is in RDF format (format 2), that is, <X> <p> <Y> on each line. This file also contains unary fact with the form <X> <type> <Y>.

2. How to run experiment?
Step 1: create a working folder, for example: data/experiment/IMDB
Step 2: copy ideal.data.txt (ideal knowledge graph file), patterns.txt (pattern file), training.data.txt (training graph file), dlv.bin to this folder
Step 3: move to nonmonotonic-rule-mining folder and chmod a+x run-experiment.sh
Step 4: ./run-experiment.sh [path to working folder] [ranking option]

Note that ideal and training knowledge graph file are in format 2 while pattern file is in format 1. You can download dlv.bin file from http://www.dlvsystem.com/, make sure to rename the binary file to dlv.bin in the working folder.

As regards the ranking option, it can be 0, 1, 2 which stand for naive, pm and opm ranking, resp.
