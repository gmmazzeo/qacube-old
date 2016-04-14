/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.linkedspending.qald;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class JsonSubmission {

    JsonDataset dataset;
    ArrayList<JsonQuestion> questions = new ArrayList<>();

    public JsonSubmission(JsonDataset dataset) {
        this.dataset = dataset;
    }

    public void add(JsonQuestion q) {
        questions.add(q);
    }
    
    public String toJson() {
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        return gson.toJson(this);
    }
}
