/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.linkedspending.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.queryparser.classic.QueryParser;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class QueryIndexWithLucene {

    protected Analyzer analyzer;
    protected Directory directory;
    Text2Keywords keywordExtractor = new Text2Keywords();

    public QueryIndexWithLucene(String path) {        
        System.out.println("Loading index...");
        HashMap<String, Analyzer> analyzerMap = new HashMap<>();
        analyzerMap.put("label", new EnglishAnalyzer(CharArraySet.EMPTY_SET));
        analyzerMap.put("uri", new WhitespaceAnalyzer());
        analyzerMap.put("type", new WhitespaceAnalyzer());
        analyzer = new PerFieldAnalyzerWrapper(new WhitespaceAnalyzer(), analyzerMap);
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        directory = new RAMDirectory();
        FSDirectory tempDirectory;
        System.out.println("Loading index from disk...");
        try {
            tempDirectory = FSDirectory.open(Paths.get(path));
            for (String file : tempDirectory.listAll()) {
                directory.copyFrom(tempDirectory, file, file, IOContext.DEFAULT);

            }
        } catch (IOException ex) {
            Logger.getLogger(QueryIndexWithLucene.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ArrayList<WeightedDataSet> queryDataset(String query) throws Exception {
        BooleanQuery globalQuery = new BooleanQuery();
        BooleanQuery typeQuery = new BooleanQuery();
        typeQuery.add(new TermQuery(new Term("type", "dataset")), BooleanClause.Occur.MUST);
        globalQuery.add(typeQuery, BooleanClause.Occur.MUST);
        BooleanQuery searchQuery = new BooleanQuery();
        for (String s : keywordExtractor.normalizeWords(query)) {
            searchQuery.add(new TermQuery(new Term("label", QueryParser.escape(s))), BooleanClause.Occur.SHOULD);
        }
        globalQuery.add(searchQuery, BooleanClause.Occur.MUST);
        QueryParser parser = new QueryParser("", analyzer);
        ArrayList<WeightedDataSet> res=new ArrayList<>();
        try (IndexReader reader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            String queryString = globalQuery.toString(); //I need this because the parser works differently of different search features - look at its definition
            ScoreDoc[] hits = searcher.search(parser.parse(queryString), 50).scoreDocs;            
            for (ScoreDoc r : hits) {
                Document doc = searcher.doc(r.doc);
                res.add(new WeightedDataSet(doc.getField("uri").stringValue(), r.score));
            }
            return res;
        } catch (Exception ex) {
            Logger.getLogger(QueryIndexWithLucene.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }

    public static void main(String[] args) throws Exception {
        QueryIndexWithLucene index = new QueryIndexWithLucene("/home/massimo/Downloads/benchmarkdatasets/lucene");
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
            System.out.println("Schema: " + index.queryDataset(q.toLowerCase()));
        }

    }
}
