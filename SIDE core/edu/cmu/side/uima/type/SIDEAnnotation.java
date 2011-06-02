

/* First created by JCasGen Wed Jan 21 17:22:15 EST 2009 */
package edu.cmu.side.uima.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;


/** 
 * Updated by JCasGen Tue Jun 30 17:09:05 EDT 2009
 * XML source: C:/yeri/projects/summarization/SIDE project/program/SIDE/descriptors/SIDETypeSystem.xml
 * @generated */
public class SIDEAnnotation extends SIDESegment {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(SIDEAnnotation.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected SIDEAnnotation() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public SIDEAnnotation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public SIDEAnnotation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public SIDEAnnotation(JCas jcas, int begin, int end) {
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
  //* Feature: sourceSegmentation

  /** getter for sourceSegmentation - gets 
   * @generated */
  public SIDESegment getSourceSegmentation() {
    if (SIDEAnnotation_Type.featOkTst && ((SIDEAnnotation_Type)jcasType).casFeat_sourceSegmentation == null)
      jcasType.jcas.throwFeatMissing("sourceSegmentation", "edu.cmu.side.uima.type.SIDEAnnotation");
    return (SIDESegment)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((SIDEAnnotation_Type)jcasType).casFeatCode_sourceSegmentation)));}
    
  /** setter for sourceSegmentation - sets  
   * @generated */
  public void setSourceSegmentation(SIDESegment v) {
    if (SIDEAnnotation_Type.featOkTst && ((SIDEAnnotation_Type)jcasType).casFeat_sourceSegmentation == null)
      jcasType.jcas.throwFeatMissing("sourceSegmentation", "edu.cmu.side.uima.type.SIDEAnnotation");
    jcasType.ll_cas.ll_setRefValue(addr, ((SIDEAnnotation_Type)jcasType).casFeatCode_sourceSegmentation, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: labelString

  /** getter for labelString - gets 
   * @generated */
  public String getLabelString() {
    if (SIDEAnnotation_Type.featOkTst && ((SIDEAnnotation_Type)jcasType).casFeat_labelString == null)
      jcasType.jcas.throwFeatMissing("labelString", "edu.cmu.side.uima.type.SIDEAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SIDEAnnotation_Type)jcasType).casFeatCode_labelString);}
    
  /** setter for labelString - sets  
   * @generated */
  public void setLabelString(String v) {
    if (SIDEAnnotation_Type.featOkTst && ((SIDEAnnotation_Type)jcasType).casFeat_labelString == null)
      jcasType.jcas.throwFeatMissing("labelString", "edu.cmu.side.uima.type.SIDEAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((SIDEAnnotation_Type)jcasType).casFeatCode_labelString, v);}    
   
    
  //*--------------*
  //* Feature: setting

  /** getter for setting - gets 
   * @generated */
  public SIDEAnnotationSetting getSetting() {
    if (SIDEAnnotation_Type.featOkTst && ((SIDEAnnotation_Type)jcasType).casFeat_setting == null)
      jcasType.jcas.throwFeatMissing("setting", "edu.cmu.side.uima.type.SIDEAnnotation");
    return (SIDEAnnotationSetting)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((SIDEAnnotation_Type)jcasType).casFeatCode_setting)));}
    
  /** setter for setting - sets  
   * @generated */
  public void setSetting(SIDEAnnotationSetting v) {
    if (SIDEAnnotation_Type.featOkTst && ((SIDEAnnotation_Type)jcasType).casFeat_setting == null)
      jcasType.jcas.throwFeatMissing("setting", "edu.cmu.side.uima.type.SIDEAnnotation");
    jcasType.ll_cas.ll_setRefValue(addr, ((SIDEAnnotation_Type)jcasType).casFeatCode_setting, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    