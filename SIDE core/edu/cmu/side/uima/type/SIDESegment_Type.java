
/* First created by JCasGen Wed Feb 04 17:28:06 EST 2009 */
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
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Tue Jun 30 17:09:05 EDT 2009
 * @generated */
public class SIDESegment_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (SIDESegment_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = SIDESegment_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new SIDESegment(addr, SIDESegment_Type.this);
  			   SIDESegment_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new SIDESegment(addr, SIDESegment_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = SIDESegment.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.cmu.side.uima.type.SIDESegment");
 
  /** @generated */
  final Feature casFeat_subtypeName;
  /** @generated */
  final int     casFeatCode_subtypeName;
  /** @generated */ 
  public String getSubtypeName(int addr) {
        if (featOkTst && casFeat_subtypeName == null)
      jcas.throwFeatMissing("subtypeName", "edu.cmu.side.uima.type.SIDESegment");
    return ll_cas.ll_getStringValue(addr, casFeatCode_subtypeName);
  }
  /** @generated */    
  public void setSubtypeName(int addr, String v) {
        if (featOkTst && casFeat_subtypeName == null)
      jcas.throwFeatMissing("subtypeName", "edu.cmu.side.uima.type.SIDESegment");
    ll_cas.ll_setStringValue(addr, casFeatCode_subtypeName, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public SIDESegment_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_subtypeName = jcas.getRequiredFeatureDE(casType, "subtypeName", "uima.cas.String", featOkTst);
    casFeatCode_subtypeName  = (null == casFeat_subtypeName) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_subtypeName).getCode();

  }
}



    