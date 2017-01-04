package fr.cnes.regards.modules.crawler.domain;

/**
 * Minimal abstraction of IIndexable
 */
public abstract class AbstractIndexable implements IIndexable {

    /**
     * Document id
     */
    private String docId;

    /**
     * Document type
     */
    private String type;

    public AbstractIndexable() {
    }

    public AbstractIndexable(String pDocId, String pType) {
        this.docId = pDocId;
        this.type = pType;
    }

    @Override
    public String getDocId() {
        return docId;
    }

    @Override
    public String getType() {
        return type;
    }

    public void setDocId(String pDocId) {
        docId = pDocId;
    }

    public void setType(String pType) {
        type = pType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((docId == null) ? 0 : docId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AbstractIndexable other = (AbstractIndexable) obj;
        if (docId == null) {
            if (other.docId != null) {
                return false;
            }
        } else
            if (!docId.equals(other.docId)) {
                return false;
            }
        return true;
    }

}
