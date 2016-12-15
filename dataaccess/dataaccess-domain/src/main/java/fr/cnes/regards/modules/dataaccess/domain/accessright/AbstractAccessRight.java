/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.domain.accessright;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.framework.jpa.IIdentifiable;
import fr.cnes.regards.modules.dataset.domain.DataSet;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "KIND")
// TODO: subsetting
public abstract class AbstractAccessRight implements IIdentifiable<Long> {

    @Id
    @SequenceGenerator(name = "AccessRightSequence", initialValue = 1, sequenceName = "SEQ_ACCESS_RIGHT")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AccessRightSequence")
    private Long id;

    @OneToOne(orphanRemoval = true, cascade = { CascadeType.ALL })
    @NotNull
    protected QualityFilter qualityFilter;

    @Enumerated
    @NotNull
    protected AccessLevel accessLevel;

    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL)
    protected DataAccessRight dataAccessRight;

    @ManyToOne
    @NotNull
    private DataSet dataSet;

    public AbstractAccessRight(QualityFilter pQualityFilter, AccessLevel pAccessLevel, DataSet pDataset) {
        super();
        qualityFilter = pQualityFilter;
        accessLevel = pAccessLevel;
        dataSet = pDataset;
    }

    public QualityFilter getQualityFilter() {
        return qualityFilter;
    }

    public void setQualityFilter(QualityFilter pQualityFilter) {
        qualityFilter = pQualityFilter;
    }

    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(AccessLevel pAccessLevel) {
        accessLevel = pAccessLevel;
    }

    public DataAccessRight getDataAccessRight() {
        return dataAccessRight;
    }

    public void setDataAccessRight(DataAccessRight pDataAccessRight) {
        dataAccessRight = pDataAccessRight;
    }

    public DataSet getConstrained() {
        return dataSet;
    }

    public void setConstrained(DataSet pConstrained) {
        dataSet = pConstrained;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long pId) {
        id = pId;
    }

    public DataSet getDataset() {
        return dataSet;
    }

    public void setDataset(DataSet pDataset) {
        dataSet = pDataset;
    }

}
