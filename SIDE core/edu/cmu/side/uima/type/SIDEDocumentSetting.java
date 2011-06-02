

/* First created by JCasGen Tue Feb 24 11:13:46 EST 2009 */
package edu.cmu.side.uima.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.DocumentAnnotation;


/** 
 * Updated by JCasGen Tue Jun 30 17:09:05 EDT 2009
 * XML source: C:/yeri/projects/summarization/SIDE project/program/SIDE/descriptors/SIDETypeSystem.xml
 * @generated */
public class SIDEDocumentSetting extends DocumentAnnotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(SIDEDocumentSetting.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected SIDEDocumentSetting() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public SIDEDocumentSetting(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public SIDEDocumentSetting(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public SIDEDocumentSetting(JCas jcas, int begin, int end) {
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
  //* Feature: sourceURI

  /** getter for sourceURI - gets 
   * @generated */
  public String getSourceURI() {
    if (SIDEDocumentSetting_Type.featOkTst && ((SIDEDocumentSetting_Type)jcasType).casFeat_sourceURI == null)
      jcasType.jcas.throwFeatMissing("sourceURI", "edu.cmu.side.uima.type.SIDEDocumentSetting");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SIDEDocumentSetting_Type)jcasType).casFeatCode_sourceURI);}
    
  /** setter for sourceURI - sets  
   * @generated */
  public void setSourceURI(String v) {
    if (SIDEDocumentSetting_Type.featOkTst && ((SIDEDocumentSetting_Type)jcasType).casFeat_sourceURI == null)
      jcasType.jcas.throwFeatMissing("sourceURI", "edu.cmu.side.uima.type.SIDEDocumentSetting");
    jcasType.ll_cas.ll_setStringValue(addr, ((SIDEDocumentSetting_Type)jcasType).casFeatCode_sourceURI, v);}    
  }

    