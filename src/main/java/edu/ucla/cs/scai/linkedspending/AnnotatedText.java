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
public class AnnotatedText {

    String text;
    int begin, end;

    public AnnotatedText(String text, int begin, int end) {
        this.text = text;
        this.begin = begin;
        this.end = end;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getBegin() {
        return begin;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.begin;
        hash = 29 * hash + this.end;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AnnotatedText other = (AnnotatedText) obj;
        if (this.begin != other.begin) {
            return false;
        }
        if (this.end != other.end) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "AnnotatedText{" + "text=" + text + ", begin=" + begin + ", end=" + end + '}';
    }

}
