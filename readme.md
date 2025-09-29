# Stackoverflow Challenge - Integer Counting
This is a challenge focusing on performance and optimization. 
Details are on the site: https://stackoverflow.com/beta/challenges/79766578/code-challenge-6-integer-counting

## How to run?
Install JDK using sdk man:
```
sdk install java 25-graal
```
Install hyperfine and coreutils:
```
brew install hyperfine
brew install coreutils # for gtimeout
```
To run different solutions use the class name as parameter in `evaluate.sh <classname>`:
```
./evaluate.sh Solution1
```
Solutions are pre-configured to run on 1M record set. See `evaluate.sh`:
```shell
#!/bin/bash

JAVA_VERSION=25
JAVA_VERSION_ID="25-graal"
INPUT_ARGS="./data/1M_random_numbers.txt" # <-- change here if needed
#...
```
Alternatively, you can generate larger record sets using the provided script, `generate.sh`:
```shell
./generate.sh 100000000 > ./data/100M_random_numbers.txt
```
## Solutions

### Single-Threaded - using MappedByteArray
Run `evaluate.sh Solution1`.

### Multi-Threaded - lockless synchronization (CAS) 
Run `evaluate.sh Solution2`.

### Multi-Threaded - lock-free via actor-model approach
Run `evaluate.sh Solution3`.

### Single-Threaded - using Java Memory Api (preview in Java 21, released in 25)
Run `evaluate.sh Solution4`.

### Single-Threaded - branchless version, scan four bytes at a time
Run `evaluate.sh Solution5`.
