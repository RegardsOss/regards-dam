/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.validator;

import javax.validation.constraints.NotNull;

import fr.cnes.regards.modules.entities.domain.AbstractEntity;

/**
 * @author Marc Sordi
 *
 */
public class Doc extends AbstractEntity {

    public Doc() {
        super(null, "Doc");
    }

    @NotNull
    private String docName;

    public String getDocName() {
        return docName;
    }

    public void setDocName(String pDocName) {
        docName = pDocName;
    }
}
