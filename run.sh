#!/bin/bash

MAXHEAP="8G"
OS_ARGS=""
#OTHER_ARGS=""
OTHER_ARGS="-XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode"

if [ `uname` == "Darwin" ]; then
    OS_ARGS="-Xdock:icon=toolkits/icons/bulbs/bulb_128.png -Xdock:name=LightSide"
fi
    
MAIN_CLASS="edu.cmu.side.Workbench"

CLASSPATH="bin:lib/*:lib/xstream/*:wekafiles/packages/chiSquaredAttributeEval/chiSquaredAttributeEval.jar:wekafiles/packages/bayesianLogisticRegression/bayesianLogisticRegression.jar:wekafiles/packages/LibLINEAR/lib/liblinear-1.8.jar:wekafiles/packages/LibLINEAR/LibLINEAR.jar:wekafiles/packages/LibSVM/lib/libsvm.jar:wekafiles/packages/LibSVM/LibSVM.jar"
    
java $OS_ARGS -Xmx$MAXHEAP $OTHER_ARGS -splash:toolkits/icons/logo.png -classpath $CLASSPATH $MAIN_CLASS $@

