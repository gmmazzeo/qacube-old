/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.linkedspending;

import edu.ucla.cs.scai.linkedspending.index.IndexBuilder;
import edu.ucla.cs.scai.linkedspending.model.rdfcube.DataSet;
import edu.ucla.cs.scai.linkedspending.template.AnnotatedTokens;
import edu.ucla.cs.scai.linkedspending.template.FlatPattern;
import edu.ucla.cs.scai.linkedspending.template.KbTag;
import edu.ucla.cs.scai.linkedspending.template.KbTagsProvider;
import edu.ucla.cs.scai.linkedspending.template.Question2Pattern;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class QA {

    HashMap<String, DataSet> datasets;
    //QueryIndexWithStatistics index;
    Question2Pattern q2p = new Question2Pattern();
    Pattern prn = Pattern.compile("(<(.+)>)");

    public QA(String pathDatasets, String pathIndex) throws Exception {
        /*
        IndexBuilder indexBuilder = new IndexBuilder(pathDatasets);
        indexBuilder.loadDataSets();
        datasets = indexBuilder.getDatasets();
        ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream(pathDatasets+"/datasets.data"));
        oos.writeObject(datasets);        
        index = new QueryIndexWithStatistics(pathIndex);
        */
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(pathDatasets + "/datasets.data"));
        datasets = (HashMap<String, DataSet>) ois.readObject();
       
        for (DataSet dataset : datasets.values()) {
            dataset.setDefaultMeasure();
        }
    }

    public String translateToSparql(String question, String datasetName, HashMap<AnnotatedText, ArrayList<Triple>> tagging) throws Exception {
        DataSet dataset = datasets.get("http://linkedspending.aksw.org/instance/" + datasetName);
        if (dataset == null) {
            throw new Exception("Unrecognized dataset: " + datasetName);
        }
        KbTagsProvider kbTagger = new KbTagsProvider(tagging, dataset);
        HashMap<FlatPattern, AnnotatedTokens> patterns = q2p.getPatterns(question, kbTagger, dataset);
        if (patterns.isEmpty()) {
            throw new Exception("No pattern recognized");
        }
        if (patterns.size() > 1) { //this can be improved            
            for (FlatPattern fp : patterns.keySet()) {
                System.out.println(fp.getName());
            }
            throw new Exception("More than one pattern recognized");
        }
        FlatPattern pattern = patterns.keySet().iterator().next();
        System.out.println(pattern.getName());
        AnnotatedTokens annotations = patterns.values().iterator().next();

        Translator translator = new Translator(pattern, annotations, tagging);
        
        //now, extract the features
        HashMap<String, HashMap<FlatPattern, AnnotatedTokens>> features=new HashMap<>();
        Matcher m = prn.matcher(pattern.getTemplate());
        while (m.find()) {
            String featureType = m.group(1);
            featureType=featureType.substring(1, featureType.length()-1);
            HashMap<FlatPattern, AnnotatedTokens> typeFeatures=q2p.getFeatures(question, kbTagger, dataset, featureType);
            if (typeFeatures!=null) {
                features.put(featureType, typeFeatures);
            }
        }
        return translator.toSparql(dataset, features);
    }

    public void printTags(String question, String dataset, HashMap<AnnotatedText, ArrayList<Triple>> tagging) {
        KbTagsProvider kbTagger = new KbTagsProvider(tagging, datasets.get("http://linkedspending.aksw.org/instance/" + dataset));
        for (AnnotatedText at : tagging.keySet()) {
            System.out.println(at);
            for (KbTag t : kbTagger.getTagsByPosition(at.begin, at.end)) {
                System.out.println("\t" + t);
            }
        }

    }

    public static void main(String[] args) throws Exception {
        QA qa = new QA("/home/massimo/Downloads/benchmarkdatasets", "/home/massimo/Downloads/benchmarkdatasets/statistics.data");
        BufferedReader in = new BufferedReader(new FileReader("/home/massimo/Downloads/benchmarkdatasets/tagging.txt"));
        String l;

        while ((l = in.readLine()) != null) {
            String question = l;
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
            if (question.equals("In which year did the City of Oakland have the highest total expenditure budget?")) {
                System.out.print("");
            }
            qa.printTags(question, dataset, tagging);
            try {
                System.out.println(qa.translateToSparql(question, dataset, tagging));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
