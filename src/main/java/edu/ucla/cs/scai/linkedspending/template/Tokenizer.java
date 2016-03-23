/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.linkedspending.template;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.ucla.cs.scai.linkedspending.Token;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class Tokenizer {

    //StanfordCoreNLP pipelineTree;
    StanfordCoreNLP pipelineTokens;

    public Tokenizer() {
        Properties propsTokens = new Properties();
        propsTokens.put("annotators", "tokenize, ssplit, pos, lemma, ner, regexner, parse, dcoref");
        pipelineTokens = new StanfordCoreNLP(propsTokens);
        //Properties propsTree = new Properties();
        //propsTree.put("annotators", "tokenize, ssplit, parse");
        //pipelineTree = new StanfordCoreNLP(propsTree);        
    }
    
    public ArrayList<Token> tokenize(String text, KbTagsProvider kbTagger) {
        Annotation qaTokens = new Annotation(text);
        pipelineTokens.annotate(qaTokens);
        List<CoreMap> qssTokens = qaTokens.get(CoreAnnotations.SentencesAnnotation.class);
        CoreMap sentenceTokens = qssTokens.get(0);
        ArrayList<CoreLabel> tokens = (ArrayList<CoreLabel>) sentenceTokens.get(CoreAnnotations.TokensAnnotation.class);
        ArrayList<Token> res=new ArrayList<>();
        for (CoreLabel t:tokens) {
            res.add(new Token(t, kbTagger));
        }
        return res;
    }
    

}
