package fr.cnes.regards.modules.entities.domain;

import javax.persistence.*;

/**
 * Document locally stored on rs-dam
 * @author Léo Mieulet
 */
@Entity
@Table(name = "t_document_file_locally_stored",
        uniqueConstraints = @UniqueConstraint(columnNames = { "document_id", "file_checksum" }, name = "uk_t_document_file_locally_stored_document_file_checksum"))
@SequenceGenerator(name = "documentLSSequence", initialValue = 1, sequenceName = "documentLS_Sequence")
public class DocumentLS {


    /**
     * Internal identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "documentLSSequence")
    private Long id;


    /**
     * Related document
     */
    @ManyToOne
    @JoinColumn(name = "document_id", foreignKey = @ForeignKey(name = "fk_documentLS_doc_id"), nullable = false, updatable = false)
    private Document document;

    /**
     * File checksum
     */
    @Column(name = "file_checksum", nullable = false, updatable = false)
    private String fileChecksum;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public String getFileChecksum() {
        return fileChecksum;
    }

    public void setFileChecksum(String fileChecksum) {
        this.fileChecksum = fileChecksum;
    }

}
