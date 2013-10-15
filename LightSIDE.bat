@echo off
(echo Launching LightSide! Console output and errors are being saved to lightside_log.) >CON

set memory=1G

set classpath=bin;lib/*;lib/xstream/*

set wekafiles=wekafiles/packages/chiSquaredAttributeEval/chiSquaredAttributeEval.jar;wekafiles/packages/bayesianLogisticRegression/bayesianLogisticRegression.jar;wekafiles/packages/LibLINEAR/lib/liblinear-1.8.jar;wekafiles/packages/LibLINEAR/LibLINEAR.jar;wekafiles/packages/LibSVM/lib/libsvm.jar;wekafiles/packages/LibSVM/LibSVM.jar

set splash=toolkits/icons/logo.png

set mainclass=edu.cmu.side.Workbench

( echo %date% %time%
  java.exe -Xmx"%memory%" -splash:"%splash%" -classpath "%classpath%";"%wekafiles%" "%mainclass%"
) >>lightside_log.log 2>&1