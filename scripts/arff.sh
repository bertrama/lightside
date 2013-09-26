#!/bin/bash

MAXHEAP="8g"
OS_ARGS=""
    
MAIN_CLASS="edu.cmu.side.recipe.ArffChef"

CLASSPATH="bin:lib/jfreechart-1.0.11.jar:lib/lingpipe-2.3.0.jar:lib/riverlayout.jar:lib/stanford-postagger-3.2.0.jar:lib/stanford-parser.jar:lib/trove.jar:lib/quiet-weka.jar:lib/XMLBoss.jar:lib/xmlparserv2.jar:lib/yeritools.jar:lib/java-cup.jar:lib/JFlex.jar:lib/junit.jar:lib/packageManager.jar:lib/quiet-weka.jar:lib/commons-math3-3.1.jar:lib/simple-5.0.4.jar:lib/jide-oss-3.4.9.jar:wekafiles/packages/chiSquaredAttributeEval/chiSquaredAttributeEval.jar:wekafiles/packages/bayesianLogisticRegression/bayesianLogisticRegression.jar:wekafiles/packages/LibLINEAR/lib/liblinear-1.8.jar:wekafiles/packages/LibLINEAR/LibLINEAR.jar:wekafiles/packages/LibSVM/lib/libsvm.jar:wekafiles/packages/LibSVM/LibSVM.jar"
    
java $OS_ARGS -Xmx$MAXHEAP -classpath $CLASSPATH $MAIN_CLASS $@

