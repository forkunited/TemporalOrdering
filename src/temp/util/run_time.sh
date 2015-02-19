#!/bin/bash

INCLUDES="-IAD3"
LIBS="-L/usr/local/lib/ -LAD3/ad3/"


LFLAGS="${LIBS} -lad3"

javac -sourcepath src -d bin src/AD3/*.java
javah -classpath bin/  -d src AD3.TemporalDecoder
JNI_PATH="/usr/lib/jvm/java-8-oracle/include/"
g++ -fPIC -shared -I./lib/SVDLIBC/ -I${JNI_PATH} -I${JNI_PATH}/linux src/time_relation.cpp ${INCLUDES} ${LFLAGS} -O2 -o bin/libad3temporal.so
java -classpath /home/lingpenk/eclipse_workspace/AD3/bin -Djava.library.path=/home/lingpenk/eclipse_workspace/AD3/bin AD3.TemporalDecoder 
