/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.linkedspending.index;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class Text2Keywords {

    StanfordCoreNLP pipeline;

    public Text2Keywords() {
        Properties props;
        props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        pipeline = new StanfordCoreNLP(props);
    }

    private LinkedList<String> lemmatize(String documentText) {
        LinkedList<String> lemmas = new LinkedList<>();

        // create an empty Annotation just with the given text
        Annotation document = new Annotation(documentText);

        // run all Annotators on this text
        pipeline.annotate(document);

        // Iterate over all of the sentences found
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            // Iterate over all tokens in a sentence
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // Retrieve and add the lemma for each word into the list of lemmas
                lemmas.add(token.get(CoreAnnotations.LemmaAnnotation.class));
            }
        }

        return lemmas;
    }

    public LinkedList<String> normalizeWords(String keywords) {
        LinkedList<String> res = new LinkedList<>();
        keywords = Utils.normalizeWords(keywords);
        LinkedList<String> lemmas = lemmatize(keywords);
        for (String w : lemmas) {
            w = Utils.normalizeAndFilterStopWords(w);
            if (w.length() == 0) {
                continue;
            }
            if (w.contains(" ")) {
                for (String w2 : w.split(" ")) {
                    res.addAll(normalizeWords(w2));
                }
            } else {
                res.add(w);
            }
        }
        return res;
    }

}
