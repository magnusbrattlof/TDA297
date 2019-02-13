#!/bin/bash

caster=ExampleCaster.java
message=ExampleMessage.java
file=setupfile

# Compile ExampleCaster and ExampleMessage
javac $caster $message
echo "Successfully compiled" $caster "and" $message

# Start number of clients
for i in 0 1 2
do
   java mcgui.Main $caster $i $file &
done
