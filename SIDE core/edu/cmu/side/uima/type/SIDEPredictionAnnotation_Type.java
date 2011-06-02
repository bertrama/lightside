
/* First created by JCasGen Sun Mar 15 13:08:41 EDT 2009 */
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

/** 
 * Updated by JCasGen Tue Jun 30 17:09:05 EDT 2009
 * @generated */
public class SIDEPredictionAnnotation_Type extends SIDEAnnotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (SIDEPredictionAnnotation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = SIDEPredictionAnnotation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new SIDEPredictionAnnotation(addr, SIDEPredictionAnnotation_Type.this);
  			   SIDEPredictionAnnotation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new SIDEPredictionAnnotation(addr, SIDEPredictionAnnotation_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = SIDEPredictionAnnotation.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("edu.cmu.side.uima.type.SIDEPredictionAnnotation");
 
  /** @generated */
  final Feature casFeat_predictionArray;
  /** @generated */
  final int     casFeatCode_predictionArray;
  /** @generated */ 
  public int getPredictionArray(int addr) {
        if (featOkTst && casFeat_predictionArray == null)
      jcas.throwFeatMissing("predictionArray", "edu.cmu.side.uima.type.SIDEPredictionAnnotation");
    return ll_cas.ll_getRefValue(addr, casFeatCode_predictionArray);
  }
  /** @generated */    
  public void setPredictionArray(int addr, int v) {
        if (featOkTst && casFeat_predictionArray == null)
      jcas.throwFeatMissing("predictionArray", "edu.cmu.side.uima.type.SIDEPredictionAnnotation");
    ll_cas.ll_setRefValue(addr, casFeatCode_predictionArray, v);}
    
   /** @generated */
  public double getPredictionArray(int addr, int i) {
        if (featOkTst && casFeat_predictionArray == null)
      jcas.throwFeatMissing("predictionArray", "edu.cmu.side.uima.type.SIDEPredictionAnnotation");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getDoubleArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_predictionArray), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_predictionArray), i);
  return ll_cas.ll_getDoubleArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_predictionArray), i);
  }
   
  /** @generated */ 
  public void setPredictionArray(int addr, int i, double v) {
        if (featOkTst && casFeat_predictionArray == null)
      jcas.throwFeatMissing("predictionArray", "edu.cmu.side.uima.type.SIDEPredictionAnnotation");
    if (lowLevelTypeChecks)
      ll_cas.ll_setDoubleArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_predictionArray), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_predictionArray), i);
    ll_cas.ll_setDoubleArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_predictionArray), i, v);
  }
 



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public SIDEPredictionAnnotation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_predictionArray = jcas.getRequiredFeatureDE(casType, "predictionArray", "uima.cas.DoubleArray", featOkTst);
    casFeatCode_predictionArray  = (null == casFeat_predictionArray) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_predictionArray).getCode();

  }
}



    