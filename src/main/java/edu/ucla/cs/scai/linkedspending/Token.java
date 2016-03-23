/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.linkedspending;

import edu.stanford.nlp.ling.CoreLabel;
import edu.ucla.cs.scai.linkedspending.template.KbTag;
import edu.ucla.cs.scai.linkedspending.template.KbTagsProvider;
import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class Token {

    public static final String SUM = "S", AVERAGE = "A", MAX = "M", MIN = "N", NONE = "O";
    CoreLabel token;
    ArrayList<KbTag> kbTags;
    String aggregateFunction;

    public Token(CoreLabel coreLabel, KbTagsProvider kbTagger) {
        this.token = coreLabel;
        kbTags = kbTagger.getTagsByPosition(coreLabel.beginPosition(), coreLabel.endPosition());
        if (kbTags.isEmpty()) { //try the aggregate funcions
            if (token.lemma().equals("sum") || token.lemma().equals("total")) {
                aggregateFunction = SUM;
            } else if (token.lemma().equals("average") || token.lemma().equals("avg")) {
                aggregateFunction = AVERAGE;
            } else if (token.lemma().equals("max") || token.lemma().equals("maximum") || token.lemma().equals("highest") || token.lemma().equals("largest")) {
                aggregateFunction = MAX;
            } else if (token.lemma().equals("min") || token.lemma().equals("minimum") || token.lemma().equals("lowest") || token.lemma().equals("smallest")) {
                aggregateFunction = MIN;
            } else {
                aggregateFunction = NONE;
            }
        } else {
            aggregateFunction = NONE;
        }
    }

    public String aggregateFunction() {
        return aggregateFunction;
    }

    public String tag() {
        return token.tag();
    }

    public String lemma() {
        return token.lemma();
    }

    public String ner() {
        return token.ner();
    }

    public int beginPosition() {
        return token.beginPosition();
    }

    public int endPosition() {
        return token.endPosition();
    }

    public boolean hasKbTag(HashSet<String> kbTagTypes) {
        for (String t : kbTagTypes) {
            if (t.equals(KbTag.NONE)) {
                if (kbTags.isEmpty()) {
                    return true;
                }
            } else {
                for (KbTag t2 : kbTags) {
                    if (t2.getType().equals(t)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public KbTag getKbTag(String kbTagType) {
        for (KbTag tag : kbTags) {
            if (tag.getType().equals(kbTagType)) {
                return tag;
            }
        }
        return null;
    }

}
