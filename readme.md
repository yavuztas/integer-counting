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

### Comparison (Java 25 GraalVM — JIT Version)
| SOLUTION                                                        | 1M   | 10M   | 100M  | 1B    |
|-----------------------------------------------------------------|------|-------|-------|-------|
| Single-Threaded - using MappedByteArray                         | 53.8 | 79.9  | 348.9 | x     |
| Multi-Threaded - lockless synchronization (CAS)                 | 82.2 | 122.3 | 625.2 | 5682  |
| Multi-Threaded - lock-free via actor-model approach             | 76.9 | 79.3  | 117.4 | 499.9 |
| Single-Threaded - using Java Memory Api                         | 52.6 | 84.0  | 406.5 | 3996  |
| Single-Threaded - branchless version, scan four bytes at a time | 55.3 | 89.4  | 429.1 | x     |

* Showing elapsed time in ms. 
* (x) terminated with error, possibly needs refactoring (against integer overflow, etc.)

### Comparison (Java 25 GraalVM — Compiled Native)
| SOLUTION                                                        | 1M    | 10M   | 100M  | 1B   |
|-----------------------------------------------------------------|-------|-------|-------|------|
| Single-Threaded - using MappedByteArray                         | 7.1   | 33.5  | 313.8 | x    |
| Multi-Threaded - lockless synchronization (CAS)                 | 9.9   | 56.7  | 517.1 | 5115 |
| Multi-Threaded - lock-free via actor-model approach             | 5.0   | 10.5  | 59.4  | 529  |
| Single-Threaded - using Java Memory Api                         | 107.1 | 917.7 | oom   | oom  |
| Single-Threaded - branchless version, scan four bytes at a time | 8.1   | 43.0  | 386.3 | x    |

* Showing elapsed time in ms.
* (x) terminated with an error, possibly needs refactoring (against integer overflow, etc.).
* (oom) Ouf of Memory error, Java Memory Api doesn't play nice with GraalVM Native since memory usage increases excessively.
