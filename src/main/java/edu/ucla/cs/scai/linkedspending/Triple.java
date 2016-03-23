/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.linkedspending;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class Triple {

    public String subject, predicate, value;

    public Triple(String subject, String predicate, String value) {
        if (subject.startsWith("<")) {
            subject = subject.substring(1, subject.length() - 1).trim();
        }
        if (predicate.startsWith("<")) {
            predicate = predicate.substring(1, predicate.length() - 1).trim();
        }
        if (value.startsWith("<")) {
            value = value.substring(1, value.length() - 1).trim();
        }
        this.subject = subject;
        this.predicate = predicate;
        this.value = value;
    }

    @Override
    public String toString() {
        return "Triple{" + "subject=" + subject + ", predicate=" + predicate + ", value=" + value + '}';
    }

}
