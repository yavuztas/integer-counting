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
To run different solutions use classname as parameter in `evaluate.sh <classname>`:
```
./evaluate.sh Main
```
## Solutions

### Using MappedByteArray
Run `evaluate.sh Solution1`.

### Using Java Memory Api (preview in Java 21)
Run `evaluate.sh Solution2`.

### Using UNSAFE
Run `evaluate.sh MainUnsafe`. This solution is just to push the limits for fun. It is also fully compatible and works in Graalvm Native.
For native run `evaluate.sh MainUnsafe --native`
