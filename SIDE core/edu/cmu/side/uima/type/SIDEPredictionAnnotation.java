

/* First created by JCasGen Sun Mar 15 13:08:41 EDT 2009 */
package edu.cmu.side.uima.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.DoubleArray;


/** 
 * Updated by JCasGen Tue Jun 30 17:09:05 EDT 2009
 * XML source: C:/yeri/projects/summarization/SIDE project/program/SIDE/descriptors/SIDETypeSystem.xml
 * @generated */
public class SIDEPredictionAnnotation extends SIDEAnnotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(SIDEPredictionAnnotation.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected SIDEPredictionAnnotation() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public SIDEPredictionAnnotation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public SIDEPredictionAnnotation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public SIDEPredictionAnnotation(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
 
    
  //*--------------*
  //* Feature: predictionArray

  /** getter for predictionArray - gets 
   * @generated */
  public DoubleArray getPredictionArray() {
    if (SIDEPredictionAnnotation_Type.featOkTst && ((SIDEPredictionAnnotation_Type)jcasType).casFeat_predictionArray == null)
      jcasType.jcas.throwFeatMissing("predictionArray", "edu.cmu.side.uima.type.SIDEPredictionAnnotation");
    return (DoubleArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((SIDEPredictionAnnotation_Type)jcasType).casFeatCode_predictionArray)));}
    
  /** setter for predictionArray - sets  
   * @generated */
  public void setPredictionArray(DoubleArray v) {
    if (SIDEPredictionAnnotation_Type.featOkTst && ((SIDEPredictionAnnotation_Type)jcasType).casFeat_predictionArray == null)
      jcasType.jcas.throwFeatMissing("predictionArray", "edu.cmu.side.uima.type.SIDEPredictionAnnotation");
    jcasType.ll_cas.ll_setRefValue(addr, ((SIDEPredictionAnnotation_Type)jcasType).casFeatCode_predictionArray, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for predictionArray - gets an indexed value - 
   * @generated */
  public double getPredictionArray(int i) {
    if (SIDEPredictionAnnotation_Type.featOkTst && ((SIDEPredictionAnnotation_Type)jcasType).casFeat_predictionArray == null)
      jcasType.jcas.throwFeatMissing("predictionArray", "edu.cmu.side.uima.type.SIDEPredictionAnnotation");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((SIDEPredictionAnnotation_Type)jcasType).casFeatCode_predictionArray), i);
    return jcasType.ll_cas.ll_getDoubleArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((SIDEPredictionAnnotation_Type)jcasType).casFeatCode_predictionArray), i);}

  /** indexed setter for predictionArray - sets an indexed value - 
   * @generated */
  public void setPredictionArray(int i, double v) { 
    if (SIDEPredictionAnnotation_Type.featOkTst && ((SIDEPredictionAnnotation_Type)jcasType).casFeat_predictionArray == null)
      jcasType.jcas.throwFeatMissing("predictionArray", "edu.cmu.side.uima.type.SIDEPredictionAnnotation");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((SIDEPredictionAnnotation_Type)jcasType).casFeatCode_predictionArray), i);
    jcasType.ll_cas.ll_setDoubleArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((SIDEPredictionAnnotation_Type)jcasType).casFeatCode_predictionArray), i, v);}
  }

    