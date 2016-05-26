/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.linkedspending.qald;

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class JsonAnswer {

    JsonResults results;

    @SerializedName("boolean")
    Boolean booleanResult;

    public JsonAnswer(JsonResults results) {
        this.results = results;
    }

    public JsonAnswer(boolean booelanResult) {
        this.booleanResult = booelanResult;
    }

    public JsonResults getResults() {
        return results;
    }

    public void setResults(JsonResults results) {
        this.results = results;
    }

    public Boolean getBooleanResult() {
        return booleanResult;
    }

    public void setBooleanResult(Boolean booleanResult) {
        this.booleanResult = booleanResult;
    }

}
