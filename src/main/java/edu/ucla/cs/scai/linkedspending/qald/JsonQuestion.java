/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.linkedspending.qald;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class JsonQuestion {

    String id;
    JsonUtterance[] question;
    JsonQuery query;
    JsonAnswer[] answers;

    public JsonQuestion(String id, JsonUtterance[] question, JsonQuery query, SparqlEndpoint endPoint) throws Exception {
        this.id = id;
        this.question = question;
        this.query = query;
        answers=endPoint.executeQuery(query);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public JsonUtterance[] getQuestion() {
        return question;
    }

    public void setQuestion(JsonUtterance[] question) {
        this.question = question;
    }

    public JsonQuery getQuery() {
        return query;
    }

    public void setQuery(JsonQuery query) {
        this.query = query;
    }

}
