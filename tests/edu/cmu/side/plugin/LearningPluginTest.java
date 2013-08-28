package edu.cmu.side.plugin;

import org.junit.*;

/* Things to test:
 * training models
 * 	with no features
 *  with boolean features
 *  with numeric features
 *  numeric class labels
 *  nominal class labels
 *  
 *  with all validation schemes
 *  	cross validation by annotation
 *  	cross validation by annotation - no annotation present
 *  	cross validation by file - multiple files
 *  	cross validation by file - one file
 *  	cross validation randomly
 *  	supplied test set - matches original data format
 *  	supplied test set - incompatible columns
 *  with each learning plugin? or subclasses of this test case, per plugin?
 *  
 * evaluating models - might be a separate test case, per eval plugin
 *  
 * predicting on unlabeled data from saved model - PredictorTest
 * */

public class LearningPluginTest
{

	@BeforeClass
	public static void setUp() throws Exception
	{
		
	}

	@Test
	public void testTrain()
	{
		//TODO: choo choo
	}

}
