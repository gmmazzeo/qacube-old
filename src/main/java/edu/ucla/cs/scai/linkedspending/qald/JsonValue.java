/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucla.cs.scai.linkedspending.qald;

import org.apache.jena.rdf.model.RDFNode;

/**
 *
 * @author Giuseppe M. Mazzeo <mazzeo@cs.ucla.edu>
 */
public class JsonValue {

    String type;
    String datatype;
    String value;
    public final static String URI = "uri", LITERAL = "literal", TYPED_LITERAL = "typed-literal";

    public JsonValue(RDFNode node) {
        if (node.isURIResource()) {
            type = "uri";
            value = node.asResource().getURI();
        } else if (node.isLiteral()) {
            if (node.asLiteral().getDatatype() == null) {
                type = "literal";
                value = node.asLiteral().toString();
            } else {
                type = "typed-literal";
                datatype = node.asLiteral().getDatatypeURI();
                value = node.asLiteral().getLexicalForm();
            }

        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
