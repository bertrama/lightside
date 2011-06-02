

/* First created by JCasGen Mon Feb 23 17:49:10 EST 2009 */
package edu.cmu.side.uima.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.DocumentAnnotation;


/** 
 * Updated by JCasGen Tue Jun 30 17:09:05 EDT 2009
 * XML source: C:/yeri/projects/summarization/SIDE project/program/SIDE/descriptors/SIDETypeSystem.xml
 * @generated */
public class SIDEAnnotationSetting extends DocumentAnnotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(SIDEAnnotationSetting.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected SIDEAnnotationSetting() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public SIDEAnnotationSetting(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public SIDEAnnotationSetting(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public SIDEAnnotationSetting(JCas jcas, int begin, int end) {
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
  //* Feature: labelColorMapString

  /** getter for labelColorMapString - gets 
   * @generated */
  public String getLabelColorMapString() {
    if (SIDEAnnotationSetting_Type.featOkTst && ((SIDEAnnotationSetting_Type)jcasType).casFeat_labelColorMapString == null)
      jcasType.jcas.throwFeatMissing("labelColorMapString", "edu.cmu.side.uima.type.SIDEAnnotationSetting");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SIDEAnnotationSetting_Type)jcasType).casFeatCode_labelColorMapString);}
    
  /** setter for labelColorMapString - sets  
   * @generated */
  public void setLabelColorMapString(String v) {
    if (SIDEAnnotationSetting_Type.featOkTst && ((SIDEAnnotationSetting_Type)jcasType).casFeat_labelColorMapString == null)
      jcasType.jcas.throwFeatMissing("labelColorMapString", "edu.cmu.side.uima.type.SIDEAnnotationSetting");
    jcasType.ll_cas.ll_setStringValue(addr, ((SIDEAnnotationSetting_Type)jcasType).casFeatCode_labelColorMapString, v);}    
   
    
  //*--------------*
  //* Feature: datatypeString

  /** getter for datatypeString - gets 
   * @generated */
  public String getDatatypeString() {
    if (SIDEAnnotationSetting_Type.featOkTst && ((SIDEAnnotationSetting_Type)jcasType).casFeat_datatypeString == null)
      jcasType.jcas.throwFeatMissing("datatypeString", "edu.cmu.side.uima.type.SIDEAnnotationSetting");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SIDEAnnotationSetting_Type)jcasType).casFeatCode_datatypeString);}
    
  /** setter for datatypeString - sets  
   * @generated */
  public void setDatatypeString(String v) {
    if (SIDEAnnotationSetting_Type.featOkTst && ((SIDEAnnotationSetting_Type)jcasType).casFeat_datatypeString == null)
      jcasType.jcas.throwFeatMissing("datatypeString", "edu.cmu.side.uima.type.SIDEAnnotationSetting");
    jcasType.ll_cas.ll_setStringValue(addr, ((SIDEAnnotationSetting_Type)jcasType).casFeatCode_datatypeString, v);}    
  }

    