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
public class JsonAnswer {

    JsonResults results;

    public JsonAnswer(JsonResults results) {
        this.results = results;
    }

    public JsonResults getResults() {
        return results;
    }

    public void setResults(JsonResults results) {
        this.results = results;
    }

}
