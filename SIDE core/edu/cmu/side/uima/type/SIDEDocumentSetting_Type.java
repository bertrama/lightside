
/* First created by JCasGen Tue Feb 24 11:13:46 EST 2009 */
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
public class SIDEDocumentSetting_Type extends DocumentAnnotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (SIDEDocumentSetting_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = SIDEDocumentSetting_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new SIDEDocumentSetting(addr, SIDEDocumentSetting_Type.this);
  			   SIDEDocumentSetting_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new SIDEDocumentSetting(addr, SIDEDocumentSetting_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = SIDEDocumentSetting.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.cmu.side.uima.type.SIDEDocumentSetting");
 
  /** @generated */
  final Feature casFeat_sourceURI;
  /** @generated */
  final int     casFeatCode_sourceURI;
  /** @generated */ 
  public String getSourceURI(int addr) {
        if (featOkTst && casFeat_sourceURI == null)
      jcas.throwFeatMissing("sourceURI", "edu.cmu.side.uima.type.SIDEDocumentSetting");
    return ll_cas.ll_getStringValue(addr, casFeatCode_sourceURI);
  }
  /** @generated */    
  public void setSourceURI(int addr, String v) {
        if (featOkTst && casFeat_sourceURI == null)
      jcas.throwFeatMissing("sourceURI", "edu.cmu.side.uima.type.SIDEDocumentSetting");
    ll_cas.ll_setStringValue(addr, casFeatCode_sourceURI, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public SIDEDocumentSetting_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_sourceURI = jcas.getRequiredFeatureDE(casType, "sourceURI", "uima.cas.String", featOkTst);
    casFeatCode_sourceURI  = (null == casFeat_sourceURI) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_sourceURI).getCode();

  }
}



    