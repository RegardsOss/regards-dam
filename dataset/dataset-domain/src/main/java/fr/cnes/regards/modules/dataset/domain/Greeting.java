/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataset.domain;

/**
 *
 * TODO Description
 *
 * @author TODO
 *
 */
public class Greeting {

    private final String content_;

    public Greeting(String pName) {
        this.content_ = "Hello " + pName;
    }

    public String getContent() {
        return content_;
    }
}