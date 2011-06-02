
/* First created by JCasGen Mon Feb 23 17:49:10 EST 2009 */
package edu.cmu.side.uima.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.DocumentAnnotation_Type;

/** 
 * Updated by JCasGen Tue Jun 30 17:09:05 EDT 2009
 * @generated */
public class SIDEAnnotationSetting_Type extends DocumentAnnotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (SIDEAnnotationSetting_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = SIDEAnnotationSetting_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new SIDEAnnotationSetting(addr, SIDEAnnotationSetting_Type.this);
  			   SIDEAnnotationSetting_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new SIDEAnnotationSetting(addr, SIDEAnnotationSetting_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = SIDEAnnotationSetting.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.cmu.side.uima.type.SIDEAnnotationSetting");
 
  /** @generated */
  final Feature casFeat_labelColorMapString;
  /** @generated */
  final int     casFeatCode_labelColorMapString;
  /** @generated */ 
  public String getLabelColorMapString(int addr) {
        if (featOkTst && casFeat_labelColorMapString == null)
      jcas.throwFeatMissing("labelColorMapString", "edu.cmu.side.uima.type.SIDEAnnotationSetting");
    return ll_cas.ll_getStringValue(addr, casFeatCode_labelColorMapString);
  }
  /** @generated */    
  public void setLabelColorMapString(int addr, String v) {
        if (featOkTst && casFeat_labelColorMapString == null)
      jcas.throwFeatMissing("labelColorMapString", "edu.cmu.side.uima.type.SIDEAnnotationSetting");
    ll_cas.ll_setStringValue(addr, casFeatCode_labelColorMapString, v);}
    
  
 
  /** @generated */
  final Feature casFeat_datatypeString;
  /** @generated */
  final int     casFeatCode_datatypeString;
  /** @generated */ 
  public String getDatatypeString(int addr) {
        if (featOkTst && casFeat_datatypeString == null)
      jcas.throwFeatMissing("datatypeString", "edu.cmu.side.uima.type.SIDEAnnotationSetting");
    return ll_cas.ll_getStringValue(addr, casFeatCode_datatypeString);
  }
  /** @generated */    
  public void setDatatypeString(int addr, String v) {
        if (featOkTst && casFeat_datatypeString == null)
      jcas.throwFeatMissing("datatypeString", "edu.cmu.side.uima.type.SIDEAnnotationSetting");
    ll_cas.ll_setStringValue(addr, casFeatCode_datatypeString, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public SIDEAnnotationSetting_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_labelColorMapString = jcas.getRequiredFeatureDE(casType, "labelColorMapString", "uima.cas.String", featOkTst);
    casFeatCode_labelColorMapString  = (null == casFeat_labelColorMapString) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_labelColorMapString).getCode();

 
    casFeat_datatypeString = jcas.getRequiredFeatureDE(casType, "datatypeString", "uima.cas.String", featOkTst);
    casFeatCode_datatypeString  = (null == casFeat_datatypeString) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_datatypeString).getCode();

  }
}



    