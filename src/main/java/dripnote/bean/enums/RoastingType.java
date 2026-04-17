package dripnote.bean.enums;

/**
 * TODO 확정나지 않음
 */

public enum RoastingType {
    LIGHT("Light"),
    MEDIUM("Medium"),
    DARK("Dark");

    private final String description;
    RoastingType(String description) { this.description = description; }
}
