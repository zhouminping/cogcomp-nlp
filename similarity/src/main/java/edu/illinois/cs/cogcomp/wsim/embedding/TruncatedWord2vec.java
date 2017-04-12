package edu.illinois.cs.cogcomp.wsim.embedding;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.sim.PhraseSim;

public class TruncatedWord2vec {
	
	private HTreeMap<String, double[]> vectors;
	private Logger logger = LoggerFactory.getLogger( PhraseSim.class );
    private double unknownValue;
    private int dimension;
    private int bit;
	
    private void loadTruncated( String vectorFilename, int dimension, int bit ) throws FileNotFoundException {
        
        DB db = DBMaker.memoryDB().make();
        vectors = db
                .hashMap("some_other_map", Serializer.STRING, Serializer.DOUBLE_ARRAY)
                .create();
        this.bit=bit;
        this.dimension = dimension;

        int count =0;
        try {
            for (String line : LineIO.readFromClasspath(vectorFilename)) {
                line = line.trim();
                if(line.length() > 0) {
                    String[] arr = line.split("\\s");
                    String word = arr[0];
                    double[] vec = new double[dimension];
                    for(int i=1; i < arr.length; i++) {
                        vec[i-1] = Double.parseDouble(arr[i])/bit;
                    }
                    vectors.put(word, vec);
                    count++;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw e;
        }
        logger.info( "loaded " + count + " vectors with " + dimension + " dimensions." );
    }


    /**
     * get the paragram score for a pair of words.
     * @param w1
     * @param w2
     * @return
     */
    public double simScore(String w1, String w2) {
        String first = StringUtils.lowerCase( w1 );
        String second = StringUtils.lowerCase( w2 );
        double[] d1 = getVector(first);
        double[] d2 = getVector(second);

        if ( null == d1 || null == d2 )
            return unknownValue;

        return cosine(d1,d2);
    }


    public static double[] add(ArrayList<double[]> lis) {
        double[] sum = new double[lis.get(0).length];
        for(int i=0; i < lis.size(); i++) {
            sum = add(sum,lis.get(i));
        }
        return sum;
    }



    public double[] getVector(String s) {
        s = s.toLowerCase();

        if(vectors.containsKey(s)) {
            //System.out.println(s);
            //System.out.println(vectors.get(s));
            return vectors.get(s);
        }

        //System.out.println(vectors.get(unknown)+" "+s);
//        return vectors.get(unknown);
        return null;
    }

    public static double cosine(double[] vec1, double[] vec2) {
        double cosine = 0;
        double t1 = 0;
        double t2 = 0;

        for(int i=0; i < vec1.length; i++) {
            cosine += vec1[i]*vec2[i];
            t1 += vec1[i]*vec1[i];
            t2 += vec2[i]*vec2[i];
        }

        return cosine / (Math.sqrt(t1)*Math.sqrt(t2));
    }
    
    public static double[] add(double[] vec1, double[] vec2) {
        double[] sum = new double[vec1.length ];

        for(int i=0; i < vec1.length; i++) {
            sum[i] = vec1[i] + vec2[i];
        }

        return sum;
    }
}
