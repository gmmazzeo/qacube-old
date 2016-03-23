/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.linkedspending;

import edu.ucla.cs.scai.linkedspending.model.rdfcube.Attribute;
import edu.ucla.cs.scai.linkedspending.model.rdfcube.DataSet;
import edu.ucla.cs.scai.linkedspending.model.rdfcube.Dimension;
import edu.ucla.cs.scai.linkedspending.model.rdfcube.Measure;
import edu.ucla.cs.scai.linkedspending.template.AnnotatedTokens;
import edu.ucla.cs.scai.linkedspending.template.FlatPattern;
import edu.ucla.cs.scai.linkedspending.template.KbTag;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class Translator {

    FlatPattern pattern;
    HashMap<AnnotatedText, ArrayList<Triple>> kbAnnotations;
    AnnotatedTokens annotatedTokens;
    Pattern prn = Pattern.compile("(<(.+)##(.+)>)");

    public Translator(FlatPattern pattern, AnnotatedTokens annotatedTokens, HashMap<AnnotatedText, ArrayList<Triple>> kbAnnotations) {
        this.pattern = pattern;
        this.annotatedTokens = annotatedTokens;
        this.kbAnnotations = kbAnnotations;
    }

    public String toSparql(DataSet dataset, HashMap<String, HashMap<FlatPattern, AnnotatedTokens>> features) throws Exception {
        //find the measure        
        String res = pattern.getTemplate();
        if (res.contains("<measure>")) {
            if (dataset.getMeasures().size() == 1) {
                res = res.replace("<measure>", "<" + dataset.getMeasures().iterator().next().getUri() + ">");
            } else {
                HashMap<String, Integer> measureCount = new HashMap<>();
                for (Measure m : dataset.getMeasures()) {
                    measureCount.put(m.getUri(), 0);
                }
                for (ArrayList<Triple> l : kbAnnotations.values()) {
                    for (Triple t : l) {
                        if (measureCount.containsKey(t.subject)) {
                            measureCount.put(t.subject, measureCount.get(t.subject) + 1);
                        }
                    }
                }
                String measureMax = "";
                int max = 0;
                for (Map.Entry<String, Integer> e : measureCount.entrySet()) {
                    if (e.getValue() > max) {
                        max = e.getValue();
                        measureMax = e.getKey();
                    }
                }
                for (AnnotatedText ann : new HashSet<>(kbAnnotations.keySet())) {
                    for (Iterator<Triple> it = kbAnnotations.get(ann).iterator(); it.hasNext();) {
                        Triple t = it.next();
                        if (t.subject.equals(measureMax)) {
                            it.remove();
                        }
                    }
                    if (kbAnnotations.get(ann).isEmpty()) {
                        kbAnnotations.remove(ann);
                    }
                }
                if (measureMax.length() > 0) {
                    res = res.replace("<measure>", "<" + measureMax + ">");
                } else { //annotations did not help to disambiguate
                    //use the default measure
                    if (dataset.getDefaultMeasure() != null) {
                        res = res.replace("<measure>", "<" + dataset.getDefaultMeasure().getUri() + ">");
                    } else {
                        System.out.println("Dataset con più misure e nessuna di default (amout)");
                    }
                }

            }
        }
        res = res.replace("<dataset>", "<" + dataset.getUri() + ">");

        //handle the template nlAnnotations
        Matcher m = prn.matcher(res);
        while (m.find()) {
            String whole = m.group(1);
            String type = m.group(2);
            String id = m.group(3);
            ArrayList<Token> tokens = annotatedTokens.getAnnotations().get(id);
            if (type.equals("measure")) {
                Measure measure = null;
                if (tokens != null && !tokens.isEmpty()) {
                    for (Token t : tokens) {
                        KbTag tag = t.getKbTag(KbTag.MEASURE);
                        if (tag != null) {
                            measure = dataset.getMeasure(tag.getUri());
                        }
                        if (measure != null) {
                            break;
                        }
                        tag = t.getKbTag(KbTag.DATASET);
                        if (tag != null) {
                            measure = dataset.getDefaultMeasure();
                        }
                        if (measure != null) {
                            break;
                        }
                    }
                } else {
                    measure = dataset.getDefaultMeasure();
                }
                if (measure != null) {
                    res = res.replace(whole, "<" + measure.getUri() + ">");
                } else {
                    throw new Exception("Could not find the measure for replacing " + whole);
                }
            } else if (type.equals("property")) {
                Dimension dimension = null;
                Attribute attribute = null;
                if (tokens != null) {
                    for (Token t : tokens) {
                        KbTag tag = t.getKbTag(KbTag.ATTRIBUTE);
                        if (tag != null) {
                            attribute = dataset.getAttribute(tag.getUri());
                        }
                        if (attribute != null) {
                            break;
                        }
                        tag = t.getKbTag(KbTag.DIMENSION);
                        if (tag != null) {
                            dimension = dataset.getDimension(tag.getUri());
                        }
                        if (dimension != null) {
                            break;
                        }
                    }
                }
                if (dimension != null) {
                    res = res.replace(whole, "<" + dimension.getUri() + ">");
                } else if (attribute != null) {
                    res = res.replace(whole, "<" + attribute.getUri() + ">");
                } else {
                    throw new Exception("Could not find the attribute/dimension for replacing " + whole);
                }
            } else if (type.equals("aggregationFunction")) {
                String aggregationFuncion = null;
                if (tokens != null) {
                    for (Token t : tokens) {
                        String tag = t.aggregateFunction();
                        if (tag != null && !tag.equals(Token.NONE)) {
                            if (tag.equals(Token.AVERAGE)) {
                                aggregationFuncion = "avg";
                            } else if (tag.equals(Token.MAX)) {
                                aggregationFuncion = "max";
                            } else if (tag.equals(Token.MIN)) {
                                aggregationFuncion = "min";
                            } else if (tag.equals(Token.SUM)) {
                                aggregationFuncion = "sum";
                            }
                        }
                    }
                }
                if (aggregationFuncion != null) {
                    res = res.replace(whole, aggregationFuncion);
                } else {
                    throw new Exception("Could not find the aggregation function for replacing " + whole);
                }
            }
        }

        //now create the contraints not related to nl annotations
        int var = 0;
        StringBuilder sb = new StringBuilder();
        HashSet<String> usedAttributes = new HashSet<>();
        HashSet<String> usedDimensions = new HashSet<>();
        for (Map.Entry<AnnotatedText, ArrayList<Triple>> e : kbAnnotations.entrySet()) {
            ArrayList<Triple> l = e.getValue();
            Triple t = l.get(0); //this could be improved considering all the triples in l
            if (sb.length() > 0) {
                sb.append("\n");
            }
            if (dataset.getEntity(t.subject) != null) { //the subject of the triple is an entity
                //connect the observation to the entity through its dimension
                HashSet<Dimension> dimensions = dataset.getDimensionsHavingValue(dataset.getEntity(t.subject));
                if (dimensions.size() > 1) {
                    System.out.println("Cannot handle this triple (so far)");
                    System.out.println(t);
                }
                if (!dimensions.isEmpty()) {
                    Dimension d = dimensions.iterator().next();
                    if (sb.length() > 0) {
                        sb.append("\n");
                    }
                    sb.append("?obs <").append(d.getUri()).append("> <").append(t.subject).append("> .");
                    usedDimensions.add(d.getUri());
                } else { //could not find the dimension
                    System.out.println("There should be a mistake: the entity " + t.subject + " could not be linked to the observations through any dimension or attribute");
                    var++;
                    sb.append("?obs ?var").append(var).append(" <").append(t.subject).append("> .");
                }
            } else if (dataset.getAttribute(t.subject) != null) {
                //this triple should define an attribute
                System.out.println("The triple " + t + " should define/label an attribute and it is not currently used");
            } else if (dataset.getDimension(t.subject) != null) {
                System.out.println("The triple " + t + " should define/label a dimension and it is not currently used");
            } else if (dataset.getAttribute(t.predicate) != null) { //the property of the triple is an attribute
                //the triple of the annotation should be <observation_id, attribute, value>
                sb.append("?obs <").append(t.predicate).append("> ").append(t.value).append(" .\n");
                /*var++;
                 sb.append("?obs <").append(t.predicate).append("> ?var").append(var).append(" .\n");
                 sb.append("FILTER (REGEX(str(?var").append(var).append("), '").append(e.getKey().text).append("', 'i')) .");
                 */
                usedAttributes.add(t.predicate);
            } else if (dataset.getUri().equals(t.subject)) {
                //this triple should define the dataset
                System.out.println("The triple " + t + " should define/label the dataset and it is not used - dataset is specifically defined");
            } else if (dataset.getMeasure(t.subject) != null) {
                //this triple should define the dataset
                System.out.println("The triple " + t + " should define/label a measure and it is not used - The measure was already determined by its uniqueness or by other triples");
            } else {
                System.out.println("Cannot handle this triple (so far)");
                System.out.println(t);
            }
        }
        res = res.replace("<constraints>", sb.toString());

        res = res.replace("<groupby>", "");
        res = res.replace("<groupbyvar>", "");

        //look for top-k queries
        String template = "";
        HashMap<FlatPattern, AnnotatedTokens> obsm = features.get(FlatPattern.ORDER_BY_SUM_MEASURE);
        if (obsm != null && !obsm.isEmpty()) {
            if (obsm.size() > 1) {
                throw new Exception("Multiple patterns found for " + FlatPattern.ORDER_BY_SUM_MEASURE);
            }
            FlatPattern pattern = obsm.keySet().iterator().next();
            System.out.println("Found pattern " + pattern.getName() + " of type " + pattern.getType());
            template = pattern.getTemplate();
            m = prn.matcher(template);
            if (m.find()) {
                String whole = m.group(1);
                String type = m.group(2);
                String id = m.group(3);
                if (!type.equals("limit")) {
                    throw new Exception("Unexpected template: " + template);
                }
                ArrayList<Token> tokens = obsm.values().iterator().next().getAnnotations().get(id);
                int val = 1;
                if (tokens != null && !tokens.isEmpty()) {
                    for (Token t : tokens) {
                        try {
                            val *= Integer.parseInt(t.lemma()); //is a number
                        } catch (Exception e) { //is a word
                            switch (t.lemma()) {
                                case "hundred":
                                    val *= 100;
                                    break;
                                case "thousand":
                                    val *= 1000;
                                    break;
                                default:
                                    throw new Exception("Unrecognized number: " + t.lemma());
                            }
                        }
                    }
                }
                template = template.replace(whole, "" + val);

            } else { //no annotation found
                throw new Exception("Unexpected template: " + template);
            }
            if (m.find()) { //more than one annotation
                throw new Exception("Unexpected template: " + template);
            }
        }
        res = res.replace("<orderbysummeasure>", template);

        res = res.replaceAll("\n+", "\n");

        return res;
    }

    public static void main(String[] args) {
        Pattern prn = Pattern.compile("<#(\\d+)>");
        Matcher m = prn.matcher("annotation <#1> and <#2> ok!");
        while (m.find()) {
            System.out.println(m.group(1));
        }
    }
}
