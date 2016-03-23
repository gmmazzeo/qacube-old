/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.linkedspending.template;

import edu.ucla.cs.scai.linkedspending.AnnotatedText;
import edu.ucla.cs.scai.linkedspending.Triple;
import edu.ucla.cs.scai.linkedspending.model.rdfcube.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class KbTagsProvider {

    HashMap<AnnotatedText, ArrayList<KbTag>> tagging = new HashMap<>();
    DataSet dataset;

    public KbTagsProvider(HashMap<AnnotatedText, ArrayList<Triple>> inputTagging, DataSet dataset) {
        //compute KbTags
        for (Map.Entry<AnnotatedText, ArrayList<Triple>> e : inputTagging.entrySet()) {
            ArrayList<KbTag> kbTags = new ArrayList<>();
            String k = e.getKey().getText().toLowerCase().trim();
            for (Triple t : e.getValue()) {
                Attribute ap = dataset.getAttribute(t.predicate);
                if (ap != null) { //triple of type <observation attribute literal>: <http://linkedspending.aksw.org/instance/observation-town_of_cary_expenditures-a99179b2b547204b13509b402bc49889e55c354c>	<http://linkedspending.aksw.org/ontology/refYear>	"2011"^^<http://www.w3.org/2001/XMLSchema#gYear>
                    kbTags.add(new KbTag(t.value, ap));
                    continue;
                }
                Dimension ds = dataset.getDimension(t.subject);
                if (ds != null) { //triple of type <dimension property value>
                    if (t.predicate.equals("http://purl.org/dc/terms/identifier") || t.predicate.equals("http://www.w3.org/2000/01/rdf-schema#label") || t.predicate.equals("http://www.w3.org/2000/01/rdf-schema#comment")) { //<http://linkedspending.aksw.org/ontology/town_of_cary_expenditures-Class>	<http://purl.org/dc/terms/identifier>	"Class"
                        kbTags.add(new KbTag(ds));
                        continue;
                    }
                }
                Attribute as = dataset.getAttribute(t.subject);
                if (as != null) { //triple of type <dimension property value>
                    if (t.predicate.equals("http://purl.org/dc/terms/identifier") || t.predicate.equals("http://www.w3.org/2000/01/rdf-schema#label") || t.predicate.equals("http://www.w3.org/2000/01/rdf-schema#comment")) { //<http://linkedspending.aksw.org/ontology/town_of_cary_expenditures-Class>	<http://purl.org/dc/terms/identifier>	"Class"
                        kbTags.add(new KbTag(as));
                        continue;
                    }
                }
                Measure ms = dataset.getMeasure(t.subject);
                if (ms != null) { //triple of type <measure property value>
                    if (t.predicate.equals("http://purl.org/dc/terms/identifier") || t.predicate.equals("http://www.w3.org/2000/01/rdf-schema#label") || t.predicate.equals("http://www.w3.org/2000/01/rdf-schema#comment")) { //<http://linkedspending.aksw.org/ontology/town_of_cary_expenditures-Class>	<http://purl.org/dc/terms/identifier>	"Class"
                        kbTags.add(new KbTag(ms));
                        continue;
                    }
                }
                Entity es = dataset.getEntity(t.subject);
                if (es != null) { //triple of type <entity attribute literal>: <https://openspending.org/town_of_cary_expenditures/Class/6>	<http://www.w3.org/2000/01/rdf-schema#label>	"Public Works and Utilities"
                    if (t.predicate.equals("http://www.w3.org/2000/01/rdf-schema#label") || t.predicate.equals("http://www.w3.org/2000/01/rdf-schema#comment")) { //triple of type <entity rdfs:label "label">
                        HashSet<Dimension> dims = dataset.getDimensionsHavingValue(es);
                        if (dims.isEmpty()) {
                            kbTags.add(new KbTag(es));
                        } else {
                            if (dims.size() > 1) {
                                System.out.println("Entity " + t.subject + " can be value of multiple dimensions");
                            } else {
                                kbTags.add(new KbTag(es, dims.iterator().next()));
                            }
                        }
                        continue;
                    }
                }
                if (t.subject.equals(dataset.getUri())) { //triple of type <entity attribute literal>: <http://linkedspending.aksw.org/instance/town_of_cary_revenues>	<http://purl.org/dc/terms/identifier>	"town_of_cary_revenues"
                    if (t.predicate.equals("http://purl.org/dc/terms/identifier") || t.predicate.equals("http://www.w3.org/2000/01/rdf-schema#label") || t.predicate.equals("http://www.w3.org/2000/01/rdf-schema#comment")) {
                        kbTags.add(new KbTag(dataset));
                        continue;
                    }
                }
                System.out.println("Unrecognized triple: " + t.toString());
                ap = dataset.getAttribute(t.predicate);
                if (ap != null) { //triple of type <observation attribute literal>: <http://linkedspending.aksw.org/instance/observation-town_of_cary_expenditures-a99179b2b547204b13509b402bc49889e55c354c>	<http://linkedspending.aksw.org/ontology/refYear>	"2011"^^<http://www.w3.org/2001/XMLSchema#gYear>
                    kbTags.add(new KbTag(t.value, ap));
                    continue;
                }
                ds = dataset.getDimension(t.subject);
                if (ds != null) { //triple of type <dimension property value>
                    if (t.predicate.equals("http://purl.org/dc/terms/identifier") || t.predicate.equals("http://www.w3.org/2000/01/rdf-schema#label") || t.predicate.equals("http://www.w3.org/2000/01/rdf-schema#comment")) { //<http://linkedspending.aksw.org/ontology/town_of_cary_expenditures-Class>	<http://purl.org/dc/terms/identifier>	"Class"
                        kbTags.add(new KbTag(ds));
                        continue;
                    }
                }
                as = dataset.getAttribute(t.subject);
                if (as != null) { //triple of type <dimension property value>
                    if (t.predicate.equals("http://purl.org/dc/terms/identifier") || t.predicate.equals("http://www.w3.org/2000/01/rdf-schema#label") || t.predicate.equals("http://www.w3.org/2000/01/rdf-schema#comment")) { //<http://linkedspending.aksw.org/ontology/town_of_cary_expenditures-Class>	<http://purl.org/dc/terms/identifier>	"Class"
                        kbTags.add(new KbTag(as));
                        continue;
                    }
                }
                ms = dataset.getMeasure(t.subject);
                if (ms != null) { //triple of type <measure property value>
                    if (t.predicate.equals("http://purl.org/dc/terms/identifier") || t.predicate.equals("http://www.w3.org/2000/01/rdf-schema#label") || t.predicate.equals("http://www.w3.org/2000/01/rdf-schema#comment")) { //<http://linkedspending.aksw.org/ontology/town_of_cary_expenditures-Class>	<http://purl.org/dc/terms/identifier>	"Class"
                        kbTags.add(new KbTag(ms));
                        continue;
                    }
                }
                es = dataset.getEntity(t.subject);
                if (es != null) { //triple of type <entity attribute literal>: <https://openspending.org/town_of_cary_expenditures/Class/6>	<http://www.w3.org/2000/01/rdf-schema#label>	"Public Works and Utilities"
                    if (t.predicate.equals("http://purl.org/dc/terms/identifier") || t.predicate.equals("http://www.w3.org/2000/01/rdf-schema#label") || t.predicate.equals("http://www.w3.org/2000/01/rdf-schema#comment")) { //triple of type <entity rdfs:label "label">
                        HashSet<Dimension> dims = dataset.getDimensionsHavingValue(es);
                        if (dims.isEmpty()) {
                            kbTags.add(new KbTag(es));
                        } else {
                            if (dims.size() > 1) {
                                System.out.println("Entity " + t.subject + " can be value of multiple dimensions");
                            } else {
                                kbTags.add(new KbTag(es, dims.iterator().next()));
                            }
                        }
                        continue;
                    }
                }
                if (t.subject.equals(dataset.getUri())) { //triple of type <entity attribute literal>: <http://linkedspending.aksw.org/instance/town_of_cary_revenues>	<http://purl.org/dc/terms/identifier>	"town_of_cary_revenues"
                    if (t.predicate.equals("http://purl.org/dc/terms/identifier") || t.predicate.equals("http://www.w3.org/2000/01/rdf-schema#label") || t.predicate.equals("http://www.w3.org/2000/01/rdf-schema#comment")) {
                        kbTags.add(new KbTag(dataset));
                        continue;
                    }
                }
            }
            tagging.put(e.getKey(), kbTags);
        }
    }

    public ArrayList<KbTag> getTagsByPosition(int begin, int end) {
        ArrayList<KbTag> res = new ArrayList<>();
        for (Map.Entry<AnnotatedText, ArrayList<KbTag>> e : tagging.entrySet()) {
            AnnotatedText at = e.getKey();
            if (at.getBegin() >= begin && at.getBegin() <= end || begin >= at.getBegin() && begin <= at.getEnd()) {
                res.addAll(e.getValue());
            }
        }
        return res;
    }

    public boolean hasAnnotation(int begin, int end, String annotationType) {
        for (Map.Entry<AnnotatedText, ArrayList<KbTag>> e : tagging.entrySet()) {
            AnnotatedText at = e.getKey();
            if (at.getBegin() >= begin && at.getBegin() <= end || begin >= at.getBegin() && begin <= at.getEnd()) {
                for (KbTag tag : e.getValue()) {
                    if (tag.getType().equals(annotationType)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
