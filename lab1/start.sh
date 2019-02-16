#!/bin/bash


javac ExampleCaster.java
javac ExampleMessage.java

for i in 0 1 2
do
    java mcgui.Main ExampleCaster $i localhostsetup &
done
