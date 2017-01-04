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

    @SuppressWarnings("unused")
    private AbstractIndexable() {
    }

    public AbstractIndexable(String pType) {
        this.type = pType;
    }

    public AbstractIndexable(String pDocId, String pType) {
        this(pType);
        this.docId = pDocId;
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
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (pObj == null) {
            return false;
        }
        if (getClass() != pObj.getClass()) {
            return false;
        }
        AbstractIndexable other = (AbstractIndexable) pObj;
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
