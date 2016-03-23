package edu.ucla.cs.scai.linkedspending.index;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class WeightedDataSet implements Comparable<WeightedDataSet> {

    public String dataset;
    public double weight;

    public WeightedDataSet(String dataset, double weight) {
        this.dataset = dataset;
        this.weight = weight;
    }

    @Override
    public int compareTo(WeightedDataSet o) {
        return Double.compare(o.weight, weight);
    }

}
