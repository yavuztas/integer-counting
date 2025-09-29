#!/bin/bash

JAVA_VERSION=25
JAVA_VERSION_ID="25-graal"
INPUT_ARGS="./data/1M_random_numbers.txt"

# Handle positional arguments
shift $((OPTIND - 1))
param1=$1
param2=$2
param3=$3

if [ "$param1" = "" ]; then
  echo "Usage: $0 <classname>"
  exit 1
fi

echo "using $JAVA_VERSION_ID"

# set java version
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk use java $JAVA_VERSION_ID

# java compile
"$HOME"/.sdkman/candidates/java/$JAVA_VERSION_ID/bin/javac --release "$JAVA_VERSION" --enable-preview -d ./bin ./src/"$param1".java

if [ "$param2" == "--native" ] && [ "$param3" != "skip" ]; then
    NATIVE_IMAGE_OPTS="--initialize-at-build-time=$param1 -O3 -march=native --gc=epsilon -R:MaxHeapSize=192m -H:-GenLoopSafepoints" # --gc=epsilon -R:MaxHeapSize=64m -H:-GenLoopSafepoints --enable-preview
    native-image $NATIVE_IMAGE_OPTS -cp ./bin "$param1"
fi

TIMEOUT="gtimeout -v 30" # in seconds, from `brew install coreutils`
HYPERFINE_OPTS="--warmup 10 --runs 10 --output ./$param1.out" # --show-output

if [ "$param2" == "--native" ]; then
    imageName=$(echo "$param1" | tr '[:upper:]' '[:lower:]')
    echo "Picking up native image './$imageName'" 1>&2
    hyperfine $HYPERFINE_OPTS "$TIMEOUT ./$imageName $INPUT_ARGS"
else
    JAVA_OPTS="" # -Xmx192m -XX:MaxGCPauseMillis=1 -XX:-AlwaysPreTouch -XX:+UseSerialGC -XX:+TieredCompilation --enable-preview
    echo "Choosing to run the app in JVM mode" 1>&2
    hyperfine $HYPERFINE_OPTS "$TIMEOUT sh -c '$HOME/.sdkman/candidates/java/$JAVA_VERSION_ID/bin/java $JAVA_OPTS -classpath ./bin $param1 $INPUT_ARGS'"
fi
