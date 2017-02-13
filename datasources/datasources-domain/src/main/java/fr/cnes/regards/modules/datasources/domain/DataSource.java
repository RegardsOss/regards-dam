/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.domain;

import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.modules.models.domain.Model;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 *
 *         FIXME: to be completed
 */
@Entity
@Table(name = "T_DATA_SOURCE")
@SequenceGenerator(name = "dataSourceSequence", initialValue = 1, sequenceName = "seq_data_source")
public class DataSource {

    @Id

    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dataSourceSequence")
    protected Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "data_model_id", foreignKey = @ForeignKey(name = "fk_dataset_datasource_id"))
    private Model modelOfData;

    public DataSource(Model pModelOfData) {
        modelOfData = pModelOfData;
    }

    public Model getModelOfData() {
        return modelOfData;
    }

    public void setModelOfData(Model pModelOfObjects) {
        modelOfData = pModelOfObjects;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long pId) {
        id = pId;
    }

}
