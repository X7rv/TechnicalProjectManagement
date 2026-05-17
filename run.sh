#!/bin/bash
cd "$(dirname "$0")"
rm -rf out && mkdir -p out
javac -encoding UTF-8 -d out -sourcepath src src/Main.java
java -cp out Main
