/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.linkedspending.model.sparqlquery;

import edu.ucla.cs.scai.linkedspending.Triple;
import java.util.ArrayList;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public abstract class Query {
    
    ArrayList<Constraint> constraints=new ArrayList<>();
    ArrayList<Filter> filters=new ArrayList<>();
    
    public abstract String toSparql();
    
}
