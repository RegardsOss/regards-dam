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
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.framework.gson.annotation.Gsonable;
import fr.cnes.regards.framework.jpa.IIdentifiable;
import fr.cnes.regards.modules.entities.domain.Dataset;

/**
 * Access right of either a group or a user
 *
 * @author Sylvain Vissiere-Guerinet
 */
@Entity
@Table(name = "t_access_right")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "kind")
@Gsonable
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
     * It is mandatory to have no cascade at all on Dataset (a Dataset CRUD must be done through DatasetService)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    private Dataset dataset;

    protected AbstractAccessRight() {
    }

    public AbstractAccessRight(QualityFilter pQualityFilter, AccessLevel pAccessLevel, Dataset pDataset) {
        super();
        qualityFilter = pQualityFilter;
        accessLevel = pAccessLevel;
        dataset = pDataset;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset pDataset) {
        dataset = pDataset;
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

    public Dataset getConstrained() {
        return dataset;
    }

    public void setConstrained(Dataset pConstrained) {
        dataset = pConstrained;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long pId) {
        id = pId;
    }

}
