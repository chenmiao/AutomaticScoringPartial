package common;

public class VS_model {
	public double VS_score (double [] vec1, double [] vec2) {         //get vector cosine similarity 
    	double result = 0;
    	double similarityscore = 0,SQ1 = 0, SQ2 = 0;
    	for (int k = 0; k < vec1.length; k ++) {
    		vec1[k] = vec1[k]*100; vec2[k] = vec2[k]*100;
    		similarityscore = similarityscore + vec1[k] * vec2[k];
    		SQ1 = SQ1 + vec1[k]*vec1[k];
    		SQ2 = SQ2 + vec2[k]*vec2[k];
    	}
    	result = similarityscore/(Math.sqrt(SQ1)*Math.sqrt(SQ1));
    	return result;
    }
}
