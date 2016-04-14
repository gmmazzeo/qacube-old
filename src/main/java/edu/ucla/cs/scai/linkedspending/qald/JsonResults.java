/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.linkedspending.qald;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class JsonResults {

    HashMap<String, JsonValue>[] bindings;

    public JsonResults(HashMap<String, JsonValue>[] bindings) {
        this.bindings = bindings;
    }

    public JsonResults(ArrayList<HashMap<String, JsonValue>> bindings) {
        this.bindings = new HashMap[bindings.size()];
        int i=0;
        for (HashMap<String, JsonValue> b:bindings) {
            this.bindings[i]=b;
            i++;
        }
    }

    public HashMap<String, JsonValue>[] getBindings() {
        return bindings;
    }

    public void setBinding(HashMap<String, JsonValue>[] bindings) {
        this.bindings = bindings;
    }

}
