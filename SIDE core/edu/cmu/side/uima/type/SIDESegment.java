

/* First created by JCasGen Wed Feb 04 17:28:06 EST 2009 */
package edu.cmu.side.uima.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Tue Jun 30 17:09:05 EDT 2009
 * XML source: C:/yeri/projects/summarization/SIDE project/program/SIDE/descriptors/SIDETypeSystem.xml
 * @generated */
public class SIDESegment extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(SIDESegment.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected SIDESegment() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public SIDESegment(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public SIDESegment(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public SIDESegment(JCas jcas, int begin, int end) {
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
  //* Feature: subtypeName

  /** getter for subtypeName - gets 
   * @generated */
  public String getSubtypeName() {
    if (SIDESegment_Type.featOkTst && ((SIDESegment_Type)jcasType).casFeat_subtypeName == null)
      jcasType.jcas.throwFeatMissing("subtypeName", "edu.cmu.side.uima.type.SIDESegment");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SIDESegment_Type)jcasType).casFeatCode_subtypeName);}
    
  /** setter for subtypeName - sets  
   * @generated */
  public void setSubtypeName(String v) {
    if (SIDESegment_Type.featOkTst && ((SIDESegment_Type)jcasType).casFeat_subtypeName == null)
      jcasType.jcas.throwFeatMissing("subtypeName", "edu.cmu.side.uima.type.SIDESegment");
    jcasType.ll_cas.ll_setStringValue(addr, ((SIDESegment_Type)jcasType).casFeatCode_subtypeName, v);}    
  }

    