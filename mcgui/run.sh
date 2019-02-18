#!/bin/bash

# Compile ExampleCaster and ExampleMessage
javac ExampleCaster.java ExampleMessage.java
echo "Successfully compiled"

# Start number of clients
for i in 0 1 2
do
   java mcgui.Main ExampleCaster $i setupfile &
done
