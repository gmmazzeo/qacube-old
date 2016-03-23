/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.linkedspending.index;

import java.util.Arrays;
import java.util.HashSet;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class Utils {

    private final static HashSet<String> stopwords = new HashSet<>();

    static {
        String s = "a\n"
                + "about\n"
                + "above\n"
                + "after\n"
                + "again\n"
                + "against\n"
                + "all\n"
                + "am\n"
                + "an\n"
                + "and\n"
                + "any\n"
                + "are\n"
                + "aren't\n"
                + "as\n"
                + "at\n"
                + "be\n"
                + "because\n"
                + "been\n"
                + "before\n"
                + "being\n"
                + "below\n"
                + "between\n"
                + "both\n"
                + "but\n"
                + "by\n"
                + "can't\n"
                + "cannot\n"
                + "could\n"
                + "couldn't\n"
                + "did\n"
                + "didn't\n"
                + "do\n"
                + "does\n"
                + "doesn't\n"
                + "doing\n"
                + "don't\n"
                + "down\n"
                + "during\n"
                + "each\n"
                + "few\n"
                + "for\n"
                + "from\n"
                + "further\n"
                + "had\n"
                + "hadn't\n"
                + "has\n"
                + "hasn't\n"
                + "have\n"
                + "haven't\n"
                + "having\n"
                + "he\n"
                + "he'd\n"
                + "he'll\n"
                + "he's\n"
                + "her\n"
                + "here\n"
                + "here's\n"
                + "hers\n"
                + "herself\n"
                + "him\n"
                + "himself\n"
                + "his\n"
                + "how\n"
                + "how's\n"
                + "i\n"
                + "i'd\n"
                + "i'll\n"
                + "i'm\n"
                + "i've\n"
                + "if\n"
                + "in\n"
                + "into\n"
                + "is\n"
                + "isn't\n"
                + "it\n"
                + "it's\n"
                + "its\n"
                + "itself\n"
                + "let's\n"
                + "me\n"
                + "more\n"
                + "most\n"
                + "mustn't\n"
                + "my\n"
                + "myself\n"
                + "no\n"
                + "nor\n"
                + "not\n"
                + "of\n"
                + "off\n"
                + "on\n"
                + "once\n"
                + "only\n"
                + "or\n"
                + "other\n"
                + "ought\n"
                + "our\n"
                + "ours	ourselves\n"
                + "out\n"
                + "over\n"
                + "own\n"
                + "same\n"
                + "shan't\n"
                + "she\n"
                + "she'd\n"
                + "she'll\n"
                + "she's\n"
                + "should\n"
                + "shouldn't\n"
                + "so\n"
                + "some\n"
                + "such\n"
                + "than\n"
                + "that\n"
                + "that's\n"
                + "the\n"
                + "their\n"
                + "theirs\n"
                + "them\n"
                + "themselves\n"
                + "then\n"
                + "there\n"
                + "there's\n"
                + "these\n"
                + "they\n"
                + "they'd\n"
                + "they'll\n"
                + "they're\n"
                + "they've\n"
                + "this\n"
                + "those\n"
                + "through\n"
                + "to\n"
                + "too\n"
                + "under\n"
                + "until\n"
                + "up\n"
                + "very\n"
                + "was\n"
                + "wasn't\n"
                + "we\n"
                + "we'd\n"
                + "we'll\n"
                + "we're\n"
                + "we've\n"
                + "were\n"
                + "weren't\n"
                + "what\n"
                + "what's\n"
                + "when\n"
                + "when's\n"
                + "where\n"
                + "where's\n"
                + "which\n"
                + "while\n"
                + "who\n"
                + "who's\n"
                + "whom\n"
                + "why\n"
                + "why's\n"
                + "with\n"
                + "won't\n"
                + "would\n"
                + "wouldn't\n"
                + "you\n"
                + "you'd\n"
                + "you'll\n"
                + "you're\n"
                + "you've\n"
                + "your\n"
                + "yours\n"
                + "yourself\n"
                + "yourselves\n'";
        stopwords.addAll(Arrays.asList(s.split("\\\n")));
    }

    public static String normalizeWords(String w) {
        w = w.replaceAll("[^A-Za-z0-9']", " ");
        //separate camel case words
        if (!w.contains(" ")) {
            w = StringUtils.join(
                    StringUtils.splitByCharacterTypeCamelCase(w),
                    ' '
            );
        }
        //collapse multiple spaces
        w = w.replaceAll("\\s+", " ");
        //change to lower case
        w = w.toLowerCase();
        return w;
    }

    public static String normalizeAndFilterStopWords(String s) {
        StringBuilder sb = new StringBuilder();
        for (String w : normalizeWords(s).split(" ")) {
            if (!stopwords.contains(w)) {
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(w);
            }
        }
        return sb.toString();
    }
}
