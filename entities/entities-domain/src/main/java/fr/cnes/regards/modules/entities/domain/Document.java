/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import java.util.List;

import fr.cnes.regards.modules.models.domain.Model;

/**
 *
 * TODO: to be implemented, just created for the handling of links between entities
 * 
 * @author Sylvain Vissiere-Guerinet
 *
 */
public class Document extends DataEntity {

    /**
     * @param pId
     * @param pSidId
     * @param pModel
     * @param pFiles
     */
    public Document(Long pId, String pSidId, Model pModel, List<Data> pFiles) {
        super(pId, pSidId, pModel, pFiles);
    }

}
