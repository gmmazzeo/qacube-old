/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.linkedspending.qald;

import edu.ucla.cs.scai.linkedspending.AnnotatedText;
import edu.ucla.cs.scai.linkedspending.QA;
import edu.ucla.cs.scai.linkedspending.Triple;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class TestSetQuestions {
    public final static String statisticsPath="/home/massimo/Downloads/benchmarkdatasets/statistics.data";
    public final static String datasetsPath="/home/massimo/Downloads/benchmarkdatasets";
    public final static String taggingsPath="/home/massimo/Dropbox/qa3/testset/exp1_tagging_max.txt";
    public final static String outputResultsPath="/home/massimo/Dropbox/qa3/testset/test.json";
    //public final static String taggingsPath="/home/massimo/Downloads/benchmarkdatasets/tagging.txt";
    //public final static String outputResultsPath="/home/massimo/Dropbox/qa3/training_results.json";
     public static void main(String[] args) throws Exception {
        QA qa = new QA(datasetsPath, statisticsPath);
        BufferedReader in = new BufferedReader(new FileReader(taggingsPath));
        String l;
        JsonSubmission submission=new JsonSubmission(new JsonDataset("qald-6-test-datacube"));
        //JsonSubmission submission=new JsonSubmission(new JsonDataset("qald-6-test-datacube"));
        int id=1;
        while ((l = in.readLine()) != null) {
            String question = l;
            JsonUtterance utterance=new JsonUtterance("en", question);
            String dataset = in.readLine().split("\t")[0];
            HashMap<AnnotatedText, ArrayList<Triple>> tagging = new HashMap<>();
            l = in.readLine();
            while (l != null && l.length() > 0) {
                l = l.trim();
                StringTokenizer st = new StringTokenizer(l, "\t");
                if (!st.hasMoreTokens()) {
                    l = in.readLine();
                    continue;
                }
                String key = st.nextToken();
                int begin = question.indexOf(key);
                int end = begin + key.length() - 1;
                ArrayList<Triple> triples = new ArrayList<>();
                String k = key.toLowerCase().trim();
                if (k.equals("years") || k.equals("year")) {
                    triples.add(new Triple("http://linkedspending.aksw.org/ontology/refYear", "http://www.w3.org/2000/01/rdf-schema#label", "year"));
                } else if (k.equals("date") || k.equals("dates") || k.equals("day") || k.equals("days")) {
                    triples.add(new Triple("http://linkedspending.aksw.org/ontology/refDate", "http://www.w3.org/2000/01/rdf-schema#label", "date"));
                } else if (!st.hasMoreTokens()) {
                    //try to find a missing year/date annotation - very rough implementaion
                    if (k.contains("years")) {                        
                        begin=key.indexOf("years");
                        end=begin+4;
                        key="years";
                        triples.add(new Triple("http://linkedspending.aksw.org/ontology/refYear", "http://www.w3.org/2000/01/rdf-schema#label", "year"));
                    } else if (k.contains("year")) {
                        begin=key.indexOf("year");
                        end=begin+3;
                        key="year";
                        triples.add(new Triple("http://linkedspending.aksw.org/ontology/refYear", "http://www.w3.org/2000/01/rdf-schema#label", "year"));
                    } else if (k.contains("dates")) {
                        begin=key.indexOf("dates");
                        end=begin+4;
                        key="dates";
                        triples.add(new Triple("http://linkedspending.aksw.org/ontology/refDate", "http://www.w3.org/2000/01/rdf-schema#label", "date"));
                    } else if (k.contains("date")) {
                        begin=key.indexOf("date");
                        end=begin+3;
                        key="date";
                        triples.add(new Triple("http://linkedspending.aksw.org/ontology/refDate", "http://www.w3.org/2000/01/rdf-schema#label", "date"));
                    } else if (k.contains("days")) {
                        begin=key.indexOf("days");
                        end=begin+3;
                        key="days";
                        triples.add(new Triple("http://linkedspending.aksw.org/ontology/refDate", "http://www.w3.org/2000/01/rdf-schema#label", "date"));
                    } else if (k.contains("day")) {
                        begin=key.indexOf("day");
                        end=begin+2;
                        key="day";
                        triples.add(new Triple("http://linkedspending.aksw.org/ontology/refDate", "http://www.w3.org/2000/01/rdf-schema#label", "date"));
                    }
                } else {
                    while (st.hasMoreTokens()) {
                        triples.add(new Triple(st.nextToken(), st.nextToken(), st.nextToken()));
                    }
                }
                if (!triples.isEmpty()) {
                    tagging.put(new AnnotatedText(key, begin, end), triples);
                }
                l = in.readLine();
            }
            System.out.println("> " + question);
            qa.printTags(question, dataset, tagging);
            try {
                String sparql=qa.translateToSparql(question, dataset, tagging);
                System.out.println(sparql);
                JsonQuery query=new JsonQuery(sparql.replaceAll("\n", " "));
                JsonQuestion q=new JsonQuestion(id+"", new JsonUtterance[]{utterance}, query, new SparqlEndpoint("http://cubeqa.aksw.org/sparql"));
                if (q.answers!=null && q.answers.length>0 && q.answers[0].results!=null && q.answers[0].results.bindings.length>0) {
                    submission.add(q);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            id++;
        }
        PrintWriter sout=new PrintWriter(new FileOutputStream(outputResultsPath, false), true);
        sout.println(submission.toJson());
        /*
         in = new BufferedReader(new InputStreamReader(System.in));
         String q;
         HashMap<String, ArrayList<Triple>> annotations=loadAnnotationsFromFile("/home/massimo/Downloads/benchmarkdatasets/matching.txt");
         while ((q=in.readLine())!=null) {
         System.out.println(qa.translateToSparql(q, annotations));
         }
         */
    }
   
}
