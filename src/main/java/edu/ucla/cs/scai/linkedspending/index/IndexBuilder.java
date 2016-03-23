/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.linkedspending.index;

import edu.ucla.cs.scai.linkedspending.model.rdfcube.Attribute;
import edu.ucla.cs.scai.linkedspending.model.rdfcube.DataSet;
import edu.ucla.cs.scai.linkedspending.model.rdfcube.Dimension;
import edu.ucla.cs.scai.linkedspending.model.rdfcube.Entity;
import edu.ucla.cs.scai.linkedspending.model.rdfcube.Measure;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class IndexBuilder {

    String directory;
    HashMap<String, DataSet> datasets = new HashMap<>();
    HashMap<String, DataSet> dataSetFromFile = new HashMap<>();
    HashMap<String, String> fileFromDataSet = new HashMap<>();
    HashMap<String, Entity> entities = new HashMap<>();
    Text2Keywords keywordsExtractor = new Text2Keywords();

    public IndexBuilder(String directory) {
        this.directory = directory;
    }

    public void loadDataSets() throws Exception {
        File dir = new File(directory);
        if (!dir.exists()) {
            System.out.println("Directory " + directory + " does not exist");
            System.exit(0);
        }

        File[] files = dir.listFiles();
        String regex = "(\\s|\\t)*<([^<>]*)>(\\s|\\t)*<([^<>]*)>(\\s|\\t)*(<|\")(.*)(>|\")";
        Pattern p = Pattern.compile(regex);
        //find the datasets defined by the files
        System.out.println("Loading datasets");
        for (File f : files) {
            if (!f.getName().endsWith(".nt")) {
                System.out.println("Invalid file found: " + f.getName());
                continue;
            } else {
                System.out.println("Processing " + f.getName());
            }
            try (BufferedReader in = new BufferedReader(new FileReader(f))) {
                String l;
                while ((l = in.readLine()) != null) {
                    Matcher m = p.matcher(l);
                    if (m.find()) {
                        String s = m.group(2);
                        String a = m.group(4);
                        String v = m.group(7);
                        if (a.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
                            if (v.equals("http://purl.org/linked-data/cube#DataSet")) {
                                DataSet ds = new DataSet(s);
                                datasets.put(s, ds);
                                if (dataSetFromFile.containsKey(f.getName())) {
                                    System.out.println(f.getName() + " has more than one dataset");
                                    System.exit(0);
                                }
                                dataSetFromFile.put(f.getName(), ds);
                                fileFromDataSet.put(ds.getUri(), f.getName());
                            }
                        }
                    }
                }
            }
        }
        for (File f : files) {
            DataSet dataset = dataSetFromFile.get(f.getName());
            if (!f.getName().endsWith(".nt")) {
                System.out.println("Invalid file found: " + f.getName());
                continue;
            } else {
                System.out.println("Processing " + f.getName());
            }
            //In the first scan find the schema of the observations: dimensions, measures and attributes
            boolean refDate = false;
            boolean refYear = false;
            try (BufferedReader in = new BufferedReader(new FileReader(f))) {
                String l;
                while ((l = in.readLine()) != null) {
                    Matcher m = p.matcher(l);
                    if (m.find()) {
                        String s = m.group(2);
                        String a = m.group(4);
                        String v = m.group(7);
                        if (a.equals("http://www.w3.org/2000/01/rdf-schema#label")) {
                            if (s.equals(dataset.getUri())) {
                                if (!dataset.setLabel(v)) {
                                    System.out.println("Multiple dataset label for " + dataset.getUri());
                                }
                                System.out.println("Label: " + v);
                            }
                        } else if (a.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
                            if (v.equals("http://purl.org/linked-data/cube#DimensionProperty")) {
                                System.out.println("Dimension: " + s);
                                dataset.addDimension(new Dimension(s));
                            } else if (v.equals("http://purl.org/linked-data/cube#MeasureProperty")) {
                                System.out.println("Measure: " + s);
                                dataset.addMeasure(new Measure(s));
                            } else if (v.equals("http://purl.org/linked-data/cube#AttributeProperty")) {
                                System.out.println("Attribute: " + s);
                                dataset.addAttribute(new Attribute(s));
                            }
                        } else if (a.equals("http://linkedspending.aksw.org/ontology/refDate") && !refDate) {
                            System.out.println("Attribute: " + a);
                            Attribute att = new Attribute(a);
                            att.setLabel("date");
                            dataset.addAttribute(att);
                            refDate = true;
                        } else if (a.equals("http://linkedspending.aksw.org/ontology/refYear") && !refYear) {
                            System.out.println("Attribute: " + a);
                            Attribute att = new Attribute(a);
                            att.setLabel("year");
                            dataset.addAttribute(att);
                            refYear = true;
                        }
                    }
                }
            }
            //In the second scan find the labels of dimensions and measures and the values of dimensions
            try (BufferedReader in = new BufferedReader(new FileReader(f))) {
                String l;
                while ((l = in.readLine()) != null) {
                    Matcher m = p.matcher(l);
                    if (m.find()) {
                        String s = m.group(2);
                        String a = m.group(4);
                        String v = m.group(7);
                        if (a.equals("http://www.w3.org/2000/01/rdf-schema#label")) {
                            Dimension dim = dataset.getDimension(s);
                            if (dim != null) {
                                if (!dim.setLabel(v)) {
                                    System.out.println("Multiple dimension label for " + dim.getUri());
                                }
                                System.out.println("Dimension Label: " + v);
                                continue;
                            }
                            Measure mea = dataset.getMeasure(s);
                            if (mea != null) {
                                if (!mea.setLabel(v)) {
                                    System.out.println("Multiple measure label for " + dataset.getUri());
                                }
                                System.out.println("Measure Label: " + v);
                                continue;
                            }
                            Attribute att = dataset.getAttribute(s);
                            if (att != null) {
                                if (!att.setLabel(v)) {
                                    System.out.println("Multiple attribute label for " + dataset.getUri());
                                }
                                System.out.println("Attribute Label: " + v);
                                continue;
                            }
                        } else {
                            Dimension dim = dataset.getDimension(a);
                            if (dim != null) {
                                if (v.startsWith("http")) { //it is an entity
                                    Entity e = entities.get(v);
                                    if (e == null) {
                                        e = new Entity(v);
                                        entities.put(v, e);
                                    }
                                    dataset.addEntity(e);
                                    dim.getEntityValues().add(e);
                                } else { //it is a literal
                                    System.out.println("Unexpected value " + v + " for " + a);
                                }
                                continue;
                            }
                            Attribute att = dataset.getAttribute(a);
                            if (att != null) {
                                if (v.startsWith("http")) { //it is an entity
                                    System.out.println("Unexpected value " + v + " for " + a);
                                } else { //it is a literal
                                    if (v.contains("^^")) {
                                        v = v.split("\\^\\^")[0];
                                        v = v.substring(0, v.length() - 1); //remove "
                                    }
                                    att.getLiteralValues().add(v);
                                }
                                continue;
                            }
                        }
                    }
                }
            }
            //In the third scan find the labels of entities
            try (BufferedReader in = new BufferedReader(new FileReader(f))) {
                String l;
                while ((l = in.readLine()) != null) {
                    Matcher m = p.matcher(l);
                    if (m.find()) {
                        String s = m.group(2);
                        String a = m.group(4);
                        String v = m.group(7);
                        if (a.equals("http://www.w3.org/2000/01/rdf-schema#label")) {
                            Entity ent = entities.get(s);
                            if (ent != null) {
                                ent.setLabel(v);
                                //System.out.println("Entity Label: " + v);
                                continue;
                            }
                        }
                    }
                }
            }
        }
        System.out.println("Datasets found: " + datasets.size());

        for (DataSet ds : datasets.values()) {
            ds.setDefaultMeasure();
            System.out.println("Dataset: <" + ds.getUri() + "> " + ds.getLabel());
            for (Measure m : ds.getMeasures()) {
                System.out.println("Measure: <" + m.getUri() + "> " + m.getLabel());
            }
            for (Dimension d : ds.getDimensions()) {
                System.out.println("Dimension: <" + d.getUri() + "> " + d.getLabel());
            }
            for (Attribute a : ds.getAttributes()) {
                System.out.println("Attribute: <" + a.getUri() + "> " + a.getLabel());
            }
        }
    }

    public void createLuceneIndex(String path) throws IOException {
        //now create the Lucene index    
        //datasets, measures, dimensions, attributes, and entities are indexed        

        HashMap<String, Analyzer> analyzerMap = new HashMap<>();
        analyzerMap.put("label", new EnglishAnalyzer(CharArraySet.EMPTY_SET));
        analyzerMap.put("id", new WhitespaceAnalyzer());
        analyzerMap.put("type", new WhitespaceAnalyzer());
        Analyzer analyzer = new PerFieldAnalyzerWrapper(new WhitespaceAnalyzer(), analyzerMap);
        try (FSDirectory directory = FSDirectory.open(Paths.get(path))) {
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            try (IndexWriter writer = new IndexWriter(directory, iwc)) {
                System.out.println("Creating the Lucene index...");
                long t = System.currentTimeMillis();
                //entities are indexed using their label
                for (Entity entity : entities.values()) {
                    String entityKeywords = entity.getLabel();
                    indexElement(writer, entity.getUri(), entityKeywords, "entity");
                }
                System.out.println(System.currentTimeMillis() - t);
                t = System.currentTimeMillis();
                for (DataSet dataset : dataSetFromFile.values()) {
                    String datasetKeywords = dataset.getLabel();
                    for (Attribute attribute : dataset.getAttributes()) {
                        String attributeKeywords = attribute.getLabel();
                        for (Entity e : attribute.getEntityValues()) {
                            attributeKeywords += "|" + e.getLabel();
                        }
                        for (String l : attribute.getLiteralValues()) {
                            attributeKeywords += "|" + l;
                        }
                        indexElement(writer, attribute.getUri(), attributeKeywords, "attribute");
                        datasetKeywords += "|" + attributeKeywords;
                    }
                    for (Dimension dimension : dataset.getDimensions()) {
                        String dimensionKeywords = dimension.getLabel();
                        for (Entity e : dimension.getEntityValues()) {
                            dimensionKeywords += "|" + e.getLabel();
                        }
                        //for (String l : dimension.getLiteralValues()) {
                        //  dimensionKeywords += "|" + l;
                        //}
                        indexElement(writer, dimension.getUri(), dimensionKeywords, "dimension");
                        datasetKeywords += "|" + dimensionKeywords;
                    }
                    for (Measure measure : dataset.getMeasures()) {
                        String measureKeywords = measure.getLabel();
                        indexElement(writer, measure.getUri(), measureKeywords, "measure");
                        datasetKeywords += "|" + measureKeywords;
                    }
                    indexElement(writer, dataset.getUri(), datasetKeywords, "dataset");
                    try (PrintWriter out = new PrintWriter(new FileOutputStream(fileFromDataSet.get(dataset.getUri()) + ".index", false), true)) {
                        out.println(datasetKeywords);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void indexElement(IndexWriter writer, String uri, String keyWords, String type) throws IOException {
        Document doc = new Document();
        for (String k : keywordsExtractor.normalizeWords(keyWords)) {
            doc.add(new Field("label", k, StringField.TYPE_NOT_STORED));
        }
        doc.add(new Field("uri", uri, StringField.TYPE_STORED));
        doc.add(new Field("type", type, StringField.TYPE_NOT_STORED));
        if (type.equals("dataset")) {
            System.out.println("Indexed dataset " + uri);
        }
        writer.addDocument(doc);
    }

    public void buildTfItfMatrix(String path) throws IOException {

        HashMap<String, Integer> nOfDocsWithWord = new HashMap<>();
        HashMap<String, HashMap<String, Integer>> fOfWordsInDocuments = new HashMap<>();
        for (String dataset : datasets.keySet()) {
            String keywords = datasets.get(dataset).getKeyWords();
            HashMap<String, Integer> wordCount = new HashMap<>();
            fOfWordsInDocuments.put(dataset, wordCount);
            for (String w : keywordsExtractor.normalizeWords(keywords)) {
                Integer c = wordCount.get(w);
                if (c == null) {
                    wordCount.put(w, 1);
                } else {
                    wordCount.put(w, c + 1);
                }
            }

            for (String w : wordCount.keySet()) {
                Integer c = nOfDocsWithWord.get(w);
                if (c == null) {
                    nOfDocsWithWord.put(w, 1);
                } else {
                    nOfDocsWithWord.put(w, c + 1);
                }
            }
        }
        HashMap<String, Double> documentLength = new HashMap<>();
        HashMap<String, HashMap<String, Double>> tfidt = new HashMap<>();
        HashMap<String, Double> idf = new HashMap<>();
        double N = fOfWordsInDocuments.keySet().size();
        for (String w
                : nOfDocsWithWord.keySet()) {
            double val = Math.log(N / nOfDocsWithWord.get(w)) / Math.log(2);
            idf.put(w, val);
        }
        for (String dataset
                : fOfWordsInDocuments.keySet()) {
            HashMap<String, Double> row = new HashMap<>();
            HashMap<String, Integer> wordCount = fOfWordsInDocuments.get(dataset);
            tfidt.put(dataset, row);
            double length = 0;
            for (String w : wordCount.keySet()) {
                double val = wordCount.get(w) * idf.get(w);
                row.put(w, val);
                length += val * val;
            }
            documentLength.put(dataset, Math.sqrt(length));
        }
        System.out.println("Writing matrix to file");
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path))) {
            out.writeObject(documentLength);
            out.writeObject(tfidt);
            out.writeObject(idf);
        }
    }

    public HashMap<String, DataSet> getDatasets() {
        return datasets;
    }

    public static void main(String[] args) throws Exception {
        args = new String[1];
        args[0] = "/home/massimo/Downloads/benchmarkdatasets";
        IndexBuilder ib = new IndexBuilder(args[0]);
        ib.loadDataSets();
        //ib.createLuceneIndex("/home/massimo/Downloads/benchmarkdatasets/lucene");
        ib.buildTfItfMatrix("/home/massimo/Downloads/benchmarkdatasets/statistics.data");
    }
}
