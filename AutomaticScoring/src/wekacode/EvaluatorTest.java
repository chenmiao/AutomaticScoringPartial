package wekacode;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.converters.ConverterUtils.DataSink;
import weka.core.Utils;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AddClassification;

import java.util.Random;

public class EvaluatorTest {
	
	  /**
	   * Calculates the weighted (by class size) F-Measure.
	   *
	   * @return the weighted F-Measure.
	   */
	  public static double weightedFMeasure(int numClasses, double[][] confusionMatrix) {
		  
	    double[] classCounts = new double[numClasses];
	    double classCountSum = 0;
	    
	    for (int i = 0; i < numClasses; i++) {
	      for (int j = 0; j < numClasses; j++) {
	        classCounts[i] += confusionMatrix[i][j];
	      }
	      classCountSum += classCounts[i];
	    }

	    double fMeasureTotal = 0;
	    for(int i = 0; i < numClasses; i++) {
	    	//compute f-measure of class i, for preparing computing the final f-measure (macro-averaged)
	      double temp = fMeasure(i, numClasses, confusionMatrix);
	      fMeasureTotal += (temp * classCounts[i]);
	    }

	    return fMeasureTotal / classCountSum;
	  }
	  
	  /**
	   * Calculate the F-Measure with respect to a particular class. 
	   * This is defined as<p/>
	   * <pre>
	   * 2 * recall * precision
	   * ----------------------
	   *   recall + precision
	   * </pre>
	   *
	   * @param classIndex the index of the class to consider as "positive"
	   * @return the F-Measure
	   */
	  public static double fMeasure(int classIndex, int numClasses, double[][] confusionMatrix) {

	    double precision = precision(classIndex, numClasses, confusionMatrix);
	    double recall = recall(classIndex, numClasses, confusionMatrix);
	    if ((precision + recall) == 0) {
	      return 0;
	    }
	    return 2 * precision * recall / (precision + recall);
	  }
	  
	  /**
	   * Calculate the precision with respect to a particular class. 
	   * This is defined as<p/>
	   * <pre>
	   * correctly classified positives
	   * ------------------------------
	   *  total predicted as positive
	   * </pre>
	   *
	   * @param classIndex the index of the class to consider as "positive"
	   * @return the precision
	   */
	  public static double precision(int classIndex, int numClasses, double[][] confusionMatrix) {

	    double correct = 0, total = 0;
	    for (int i = 0; i < numClasses; i++) {
	      if (i == classIndex) {
		correct += confusionMatrix[i][classIndex];
	      }
	      total += confusionMatrix[i][classIndex];
	    }
	    if (total == 0) {
	      return 0;
	    }
	    return correct / total;
	  }
	  
	  /**
	   * Calculate the true positive rate with respect to a particular class. 
	   * This is defined as<p/>
	   * <pre>
	   * correctly classified positives
	   * ------------------------------
	   *       total positives
	   * </pre>
	   *
	   * @param classIndex the index of the class to consider as "positive"
	   * @return the true positive rate
	   */
	  public static double recall(int classIndex, int numClasses, double[][] confusionMatrix) {

		    double correct = 0, total = 0;
		    for (int j = 0; j < numClasses; j++) {
		      if (j == classIndex) {
			correct += confusionMatrix[classIndex][j];
		      }
		      total += confusionMatrix[classIndex][j];
		    }
		    if (total == 0) {
		      return 0;
		    }
		    return correct / total;
		  }
	
	public static void main(String[] args) throws Exception {
	    // loads data and set class index
	    Instances data = DataSource.read(Utils.getOption("t", args));
	    String clsIndex = Utils.getOption("c", args);
	    if (clsIndex.length() == 0)
	      clsIndex = "last";
	    if (clsIndex.equals("first"))
	      data.setClassIndex(0);
	    else if (clsIndex.equals("last"))
	      data.setClassIndex(data.numAttributes() - 1);
	    else
	      data.setClassIndex(Integer.parseInt(clsIndex) - 1);

	    // classifier
	    String[] tmpOptions;
	    String classname;
	    tmpOptions     = Utils.splitOptions(Utils.getOption("W", args));
	    classname      = tmpOptions[0];
	    tmpOptions[0]  = "";
	    Classifier cls = (Classifier) Utils.forName(Classifier.class, classname, tmpOptions);

	    // other options
	    int seed  = Integer.parseInt(Utils.getOption("s", args));
	    int folds = Integer.parseInt(Utils.getOption("x", args));

	    // randomize data
	    Random rand = new Random(seed);
	    Instances randData = new Instances(data);
	    randData.randomize(rand);
	    if (randData.classAttribute().isNominal())
	      randData.stratify(folds);
//
//	    // perform cross-validation and add predictions
	    Instances predictedData = null;
	    Evaluation eval = new Evaluation(randData);
			    
	    //added code
	    int numClasses=3;
	    double[][] confusionMatrix=new double[numClasses][numClasses];
	    
	    for (int n = 0; n < folds; n++) {
	      Instances train = randData.trainCV(folds, n);
	      Instances test = randData.testCV(folds, n);
	      // the above code is used by the StratifiedRemoveFolds filter, the
	      // code below by the Explorer/Experimenter:
	      // Instances train = randData.trainCV(folds, n, rand);

	      // build and evaluate classifier
	      Classifier clsCopy = Classifier.makeCopy(cls);
	      clsCopy.buildClassifier(train);
	      eval.evaluateModel(clsCopy, test);
	      
	      //add up the numbers in subConfusionMatrix to the final confusionMatrix
	      double[][] subConfusionMatrix=eval.confusionMatrix();
	      for (int i=0; i<numClasses; i++){
	    	  for (int j=0; j<numClasses; j++){
	    		  confusionMatrix[i][j]=+subConfusionMatrix[i][j];	    		  
	    	  }
	      }

	      // add predictions
	      AddClassification filter = new AddClassification();
	      filter.setClassifier(cls);
	      filter.setOutputClassification(true);
	      filter.setOutputDistribution(true);
	      filter.setOutputErrorFlag(true);
	      filter.setInputFormat(train);
	      Filter.useFilter(train, filter);  // trains the classifier
	      Instances pred = Filter.useFilter(test, filter);  // perform predictions on test set
	      if (predictedData == null)
	        predictedData = new Instances(pred, 0);
	      for (int j = 0; j < pred.numInstances(); j++)
	        predictedData.add(pred.instance(j));
	    }

	    // output evaluation
//	    System.out.println();
//	    System.out.println("=== Setup ===");
//	    System.out.println("Classifier: " + cls.getClass().getName() + " " + Utils.joinOptions(cls.getOptions()));
//	    System.out.println("Dataset: " + data.relationName());
//	    System.out.println("Folds: " + folds);
//	    System.out.println("Seed: " + seed);
//	    System.out.println();
	    System.out.println(eval.toSummaryString("=== " + folds + "-fold Cross-validation ===", false));
	    System.out.println(eval.toMatrixString());
	    System.out.println(eval.toClassDetailsString());

	    // output "enriched" dataset
	    DataSink.write(Utils.getOption("o", args), predictedData);
	    
	    //print out the final confusionMatrix
	    System.out.println("######Printing out the final confusion matrix:");
	    for (int i=0; i<numClasses; i++){
	    	for (int j=0; j<numClasses; j++){
	    		  System.out.println(confusionMatrix[i][j]);	    		  
	    	  }	    	
	    }
	    System.out.println(weightedFMeasure(numClasses, confusionMatrix));
	    
	  }
	

}
