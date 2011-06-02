
/* First created by JCasGen Wed Jan 21 17:22:15 EST 2009 */
package edu.cmu.side.uima.type;

import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;

/** 
 * Updated by JCasGen Tue Jun 30 17:09:05 EDT 2009
 * @generated */
public class SIDEAnnotation_Type extends SIDESegment_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (SIDEAnnotation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = SIDEAnnotation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new SIDEAnnotation(addr, SIDEAnnotation_Type.this);
  			   SIDEAnnotation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new SIDEAnnotation(addr, SIDEAnnotation_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = SIDEAnnotation.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.cmu.side.uima.type.SIDEAnnotation");
 
  /** @generated */
  final Feature casFeat_sourceSegmentation;
  /** @generated */
  final int     casFeatCode_sourceSegmentation;
  /** @generated */ 
  public int getSourceSegmentation(int addr) {
        if (featOkTst && casFeat_sourceSegmentation == null)
      jcas.throwFeatMissing("sourceSegmentation", "edu.cmu.side.uima.type.SIDEAnnotation");
    return ll_cas.ll_getRefValue(addr, casFeatCode_sourceSegmentation);
  }
  /** @generated */    
  public void setSourceSegmentation(int addr, int v) {
        if (featOkTst && casFeat_sourceSegmentation == null)
      jcas.throwFeatMissing("sourceSegmentation", "edu.cmu.side.uima.type.SIDEAnnotation");
    ll_cas.ll_setRefValue(addr, casFeatCode_sourceSegmentation, v);}
    
  
 
  /** @generated */
  final Feature casFeat_labelString;
  /** @generated */
  final int     casFeatCode_labelString;
  /** @generated */ 
  public String getLabelString(int addr) {
        if (featOkTst && casFeat_labelString == null)
      jcas.throwFeatMissing("labelString", "edu.cmu.side.uima.type.SIDEAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_labelString);
  }
  /** @generated */    
  public void setLabelString(int addr, String v) {
        if (featOkTst && casFeat_labelString == null)
      jcas.throwFeatMissing("labelString", "edu.cmu.side.uima.type.SIDEAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_labelString, v);}
    
  
 
  /** @generated */
  final Feature casFeat_setting;
  /** @generated */
  final int     casFeatCode_setting;
  /** @generated */ 
  public int getSetting(int addr) {
        if (featOkTst && casFeat_setting == null)
      jcas.throwFeatMissing("setting", "edu.cmu.side.uima.type.SIDEAnnotation");
    return ll_cas.ll_getRefValue(addr, casFeatCode_setting);
  }
  /** @generated */    
  public void setSetting(int addr, int v) {
        if (featOkTst && casFeat_setting == null)
      jcas.throwFeatMissing("setting", "edu.cmu.side.uima.type.SIDEAnnotation");
    ll_cas.ll_setRefValue(addr, casFeatCode_setting, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public SIDEAnnotation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_sourceSegmentation = jcas.getRequiredFeatureDE(casType, "sourceSegmentation", "edu.cmu.side.uima.type.SIDESegment", featOkTst);
    casFeatCode_sourceSegmentation  = (null == casFeat_sourceSegmentation) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_sourceSegmentation).getCode();

 
    casFeat_labelString = jcas.getRequiredFeatureDE(casType, "labelString", "uima.cas.String", featOkTst);
    casFeatCode_labelString  = (null == casFeat_labelString) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_labelString).getCode();

 
    casFeat_setting = jcas.getRequiredFeatureDE(casType, "setting", "edu.cmu.side.uima.type.SIDEAnnotationSetting", featOkTst);
    casFeatCode_setting  = (null == casFeat_setting) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_setting).getCode();

  }
}



    