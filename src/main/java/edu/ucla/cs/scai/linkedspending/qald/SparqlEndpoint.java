/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.linkedspending.qald;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class SparqlEndpoint {

    static final HashMap<String, String> prefixes=new HashMap<>();
    static {
        prefixes.put("xsd:", "http://www.w3.org/2001/XMLSchema#");
        prefixes.put("rdfs:", "http://www.w3.org/2000/01/rdf-schema#");
        prefixes.put("foaf:", "http://xmlns.com/foaf/0.1/");
        prefixes.put("rdf:", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        prefixes.put("qb:", "http://purl.org/linked-data/cube#");
    }
    
    String endPointURI;

    public SparqlEndpoint(String endPointURI) {
        this.endPointURI = endPointURI;

    }

    public JsonAnswer[] executeQuery(JsonQuery jsonQuery) throws Exception {
        return executeQuery(jsonQuery.sparql);
    }
    
    public JsonAnswer[] executeQuery(String sparqlQuery) throws Exception {
        StringBuilder sb=new StringBuilder();
        for (Map.Entry<String, String> e:prefixes.entrySet()) {
            sb.append("PREFIX ").append(e.getKey()).append("\t<").append(e.getValue()).append(">\n");
        }
        sb.append(sparqlQuery);
        System.out.println(sb.toString());
        Query query = QueryFactory.create(sb.toString().replaceAll("\\s+", " "));
        QueryExecution qexec = QueryExecutionFactory.sparqlService(endPointURI, query);
        ResultSet rs = qexec.execSelect();
        ArrayList<HashMap<String, JsonValue>> bindings = new ArrayList<>();
        ArrayList<JsonAnswer> res = new ArrayList<>();
        for (; rs.hasNext();) {
            QuerySolution qs = rs.next();
            HashMap<String, JsonValue> binding = new HashMap<>();
            for (Iterator<String> it = qs.varNames(); it.hasNext();) {
                String varName = it.next();
                RDFNode node = qs.get(varName);
                binding.put(varName, new JsonValue(node));
            }
            bindings.add(binding);
        }
        JsonAnswer jAnswer = new JsonAnswer(new JsonResults(bindings));
        return new JsonAnswer[]{jAnswer};
    }
    
}
