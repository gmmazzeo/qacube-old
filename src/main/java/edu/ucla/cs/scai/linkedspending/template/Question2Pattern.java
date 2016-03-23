/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.linkedspending.template;

import edu.ucla.cs.scai.linkedspending.AnnotatedText;
import edu.ucla.cs.scai.linkedspending.Triple;
import edu.ucla.cs.scai.linkedspending.model.rdfcube.DataSet;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class Question2Pattern {

    FlatPatternMatcher matcher = new FlatPatternMatcher();
    private final Tokenizer parser = new Tokenizer();

    public FlatPattern getPatternByName(String name, String type) {
        return matcher.getPatternByName(name, type);
    }
    
    public HashMap<FlatPattern, AnnotatedTokens> getPatterns(String qt, KbTagsProvider kbTagger, DataSet dataset) throws Exception {
        return matcher.match(parser.tokenize(qt, kbTagger), FlatPattern.TEMPLATE);
    }    
    
    public HashMap<FlatPattern, AnnotatedTokens> getFeatures(String qt, KbTagsProvider kbTagger, DataSet dataset, String featureType) throws Exception {
        return matcher.match(parser.tokenize(qt, kbTagger), featureType);
    }    

    public void reloadPatterns() {
        matcher = new FlatPatternMatcher();
    }
}
