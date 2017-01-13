/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.domain.accessright;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.framework.jpa.IIdentifiable;
import fr.cnes.regards.modules.dataaccess.domain.accessright.validation.SubsettedAccessRight;
import fr.cnes.regards.modules.entities.domain.DataSet;

/**
 * Access right of either a group or a user
 * @author Sylvain Vissiere-Guerinet
 */
@Entity(name = "t_access_right")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "kind")
@SubsettedAccessRight
public abstract class AbstractAccessRight implements IIdentifiable<Long> {

    @Id
    @SequenceGenerator(name = "AccessRightSequence", initialValue = 1, sequenceName = "seq_access_right")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AccessRightSequence")
    private Long id;

    @Embedded
    @NotNull
    protected QualityFilter qualityFilter;

    @Column(length = 30, name = "access_level")
    @Enumerated(EnumType.STRING)
    @NotNull
    protected AccessLevel accessLevel;

    @Embedded
    protected DataAccessRight dataAccessRight;

    /**
     * allow to define a subset of the dataSet, if it is empty then all the dataSet is concerned
     */
    // TODO : commenté pour l'instant, à préciser
    /*     @ManyToMany
    @NotNull
    protected Set<AttributeModel> subsettingCriteria;*/

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    private DataSet dataSet;

    public AbstractAccessRight(QualityFilter pQualityFilter, AccessLevel pAccessLevel, DataSet pDataset) {
        super();
        qualityFilter = pQualityFilter;
        accessLevel = pAccessLevel;
        dataSet = pDataset;
        // subsettingCriteria = new HashSet<>();
    }

    /*    public Set<AttributeModel> getSubsettingCriteria() {
        return subsettingCriteria;
    }

    public void setSubsettingCriteria(Set<AttributeModel> pSubsettingCriteria) {
        subsettingCriteria = pSubsettingCriteria;
    }*/

    public DataSet getDataSet() {
        return dataSet;
    }

    public void setDataSet(DataSet pDataSet) {
        dataSet = pDataSet;
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
