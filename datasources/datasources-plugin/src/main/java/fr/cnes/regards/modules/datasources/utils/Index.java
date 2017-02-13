/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.utils;

/**
 * This class describes an index of a SQL database.
 *
 * @author Christophe Mertz
 * 
 */
public final class Index {

    /**
     * The index name
     */
    private String name;

    /**
     * The column name
     */
    private String columm;

    /**
     * Flag for primary key column. Only one primary key is allowed for a table.<br>
     * Multicolumn primary key is not supported at the moment.
     */
    private Boolean isUnique = Boolean.FALSE;

    private String ascOrDesc = "A";

    /**
     *
     */
    public Index() {
        super();
    }

    public Index(String pName, String pColumm, Boolean pIsUnique, String pAscOrDesc) {
        super();
        this.name = pName;
        this.columm = pColumm;
        this.isUnique = pIsUnique;
        this.ascOrDesc = pAscOrDesc;
    }

    public String getName() {
        return name;
    }

    public void setName(String pName) {
        this.name = pName;
    }

    public String getColumm() {
        return columm;
    }

    public void setColumm(String pColumm) {
        this.columm = pColumm;
    }

    public Boolean getIsUnique() {
        return isUnique;
    }

    public void setIsUnique(Boolean pIsUnique) {
        this.isUnique = pIsUnique;
    }

    public String getAscOrDesc() {
        return ascOrDesc;
    }

    public void setAscOrDesc(String pAscOrDesc) {
        this.ascOrDesc = pAscOrDesc;
    }

    @Override
    public String toString() {
        return "[" + name + " : " + columm + (isUnique ? " : is unique" : ":") + ascOrDesc + "]";
    }

}
