/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.linkedspending.model.sparqlquery;

import java.util.ArrayList;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class AggregationQuery extends Query {

    public static final int SUM = 1, COUNT = 2, AVERAGE = 3, MIN = 4, MAX = 5;

    int variableCounter = 0;

    String aggregatedVariable;
    ArrayList<String> groupBy = new ArrayList<>();
    public int aggregationFuncion;

    @Override
    public String toSparql() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ").append(" ?").append(aggregatedVariable);
        for (String g : groupBy) {
            sb.append(" ?").append(g);
        }
        sb.append("\nWHERE {");
        for (Constraint c:constraints) {
            sb.append("\n").append(c.toString());
        }
        for (Filter f:filters) {
            sb.append("\n").append(f.toString());
        }        
        sb.append("\n}");
        return sb.toString();
    }

}
