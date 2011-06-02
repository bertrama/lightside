/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE xmiFile
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this xmiFile
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this xmiFile except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package edu.cmu.side.uima;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;

import edu.cmu.side.uima.type.SIDESegment;

public class SIDEAnnotationWriter extends JCasAnnotator_ImplBase {

  /**
   * @see JCasAnnotator_ImplBase#process(JCas)
   */
  public void process(JCas aJCas) {
    // get document text
    String docText = aJCas.getDocumentText();
    int docTextLength = docText.length(); 
    
    int length = 30;
    for(int begin=0; begin<docTextLength; begin+=length){
    	SIDESegment annotation = new SIDESegment(aJCas);
        annotation.setBegin(begin);
        int end = Math.min(begin+length, docTextLength);
        annotation.setEnd(end);
//        annotation.setSegmentationName(null);
        annotation.addToIndexes();
    }
  }
}
