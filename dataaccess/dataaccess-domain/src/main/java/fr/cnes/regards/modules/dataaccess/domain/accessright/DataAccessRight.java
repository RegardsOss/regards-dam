/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.domain.accessright;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Entity
// TODO: validation, id of check data access should be present if data access level is custom
public class DataAccessRight {

    @Id
    @SequenceGenerator(name = "DataAccessRightSequence", initialValue = 1, sequenceName = "SEQ_DATA_ACCESS_RIGHT")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DataAccessRightSequence")
    private Long id;

    @Enumerated
    private DataAccessLevel dataAccessLevel;
    // TODO: add CheckDataAccess plugin reference

}
