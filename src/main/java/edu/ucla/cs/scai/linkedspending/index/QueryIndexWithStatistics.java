/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.linkedspending.index;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class QueryIndexWithStatistics {

    HashMap<String, Double> documentLength;
    HashMap<String, HashMap<String, Double>> tfidt;
    HashMap<String, Double> idf = new HashMap<>();
    Text2Keywords keywordExtractor=new Text2Keywords();
    
    public QueryIndexWithStatistics(String fileName) throws Exception {
        System.out.println("Loading statistics...");
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            documentLength = (HashMap<String, Double>) ois.readObject();
            tfidt = (HashMap<String, HashMap<String, Double>>) ois.readObject();
            idf = (HashMap<String, Double>) ois.readObject();
        }        
    }

    public ArrayList<WeightedDataSet> queryDataset(String query) throws Exception {        
        HashMap<String, Integer> wordCount = new HashMap<>();
        int max = 1;
        for (String w : keywordExtractor.normalizeWords(query)) {
            Integer c = wordCount.get(w);
            if (c == null) {
                wordCount.put(w, 1);
            } else {
                wordCount.put(w, c + 1);
                max = Math.max(max, c + 1);
            }
        }
        HashMap<String, Double> queryTfidt = new HashMap<>();
        double queryLength = 0;
        for (String w : wordCount.keySet()) {
            Double val = idf.get(w);
            if (val != null) {
                queryLength += val * val;
                queryTfidt.put(w, (wordCount.get(w) / max) * val);
            }
        }
        queryLength = Math.sqrt(queryLength);
        ArrayList<WeightedDataSet> res = new ArrayList<>();
        for (String dataset : tfidt.keySet()) {
            double docLength = documentLength.get(dataset);
            HashMap<String, Double> row = tfidt.get(dataset);
            double product = 0;
            for (String w : wordCount.keySet()) {
                Double val = row.get(w);
                if (val != null) {
                    product += val * queryTfidt.get(w);
                }
            }
            double sim = product / (queryLength * docLength);
            if (sim > 0) {
                res.add(new WeightedDataSet(dataset, sim));
            }
        }
        Collections.sort(res);
        return res;
    }

    public static void main(String[] args) throws Exception {
        QueryIndexWithStatistics index=new QueryIndexWithStatistics("/home/massimo/Downloads/benchmarkdatasets/statistics.data");
        try (BufferedReader in = new BufferedReader(new FileReader("/home/massimo/Downloads/benchmarkdatasets/questions.txt"))) {
            String l;
            int[] count=new int[52];
            while ((l = in.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(l, "\t");
                String d = st.nextToken();
                String q = st.nextToken();
                ArrayList<WeightedDataSet> res = index.queryDataset(q.toLowerCase());
                int i = 1;
                for (WeightedDataSet w : res) {
                    if (w.dataset.endsWith(d)) {
                        break;
                    }
                    i++;
                }
                if (i > res.size()) {
                    count[51]++;
                    System.out.println("Not found");
                } else {
                    count[i]++;
                    System.out.println(i+" "+q+" "+res.get(i-1).weight+" vs "+res.get(0).weight);
                }
            }
            for (int i=1; i<count.length; i++) {
                if (count[i]>0) {
                    System.out.println(count[i]+" dataset in "+i+"a posizione");
                }
            }
        }

        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            String q = in.nextLine();
            ArrayList<WeightedDataSet> res =index.queryDataset(q.toLowerCase());
            for (WeightedDataSet w : res) {
                System.out.println(w.dataset + ": " + w.weight);
            }

        }

    }
}
