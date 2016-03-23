/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.linkedspending.model.sparqlquery;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class Constraint {

    String subject, attribute, value;
    String invertedAttribute; //used when the parameter attribute of the constructor contains bothregular and inverted attributes

    public Constraint(String subject, String attribute, String value) {

        String[] atts = attribute.split("\\|");
        StringBuilder tempA = new StringBuilder();
        StringBuilder tempIA = new StringBuilder();
        for (int i = 0; i < atts.length; i++) {
            if (atts[i].endsWith("Inv")) {
                if (tempIA.length() > 0) {
                    tempIA.append("|");
                }
                tempIA.append(atts[i].substring(0, atts[i].length() - 3));
            } else {
                if (tempA.length() > 0) {
                    tempA.append("|");                    
                }
                tempA.append(atts[i]);
            }
        }

        if (tempA.length() > 0) {
            this.subject = subject;
            this.value = value;
            this.attribute = tempA.toString();
            if (tempIA.length() > 0) {
                invertedAttribute = tempIA.toString();
            }
        } else {
            this.subject = value;
            this.value = subject;
            this.attribute = tempIA.toString();
        }
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getInvertedAttribute() {
        return invertedAttribute;
    }

    public void setInvertedAttribute(String invertedAttribute) {
        this.invertedAttribute = invertedAttribute;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        fillStringBuilder(sb);
        return sb.toString();
    }

    public void fillStringBuilder(StringBuilder sb) {
        if (subject.startsWith("http:") || subject.startsWith("https:")) {
            sb.append("<").append(subject).append(">");
        } else {
            sb.append("?").append(subject);
        }
        sb.append(" <").append(attribute).append("> ");
        if (value.startsWith("http:") || value.startsWith("https:")) {
            sb.append("<").append(value).append(">");
        } else {
            sb.append("?").append(value);
        }
    }
}
