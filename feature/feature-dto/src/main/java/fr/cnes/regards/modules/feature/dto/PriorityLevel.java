/**
 *
 */
package fr.cnes.regards.modules.feature.dto;

/**
 * @author kevin
 *
 */
public enum PriorityLevel {

    HIGH(0),
    AVERAGE(1),
    LOW(2);

    private int priority;

    private PriorityLevel(int priority) {
        this.priority = priority;
    }

    public int getPriorityLevel() {
        return priority;
    }
}
