package dripnote.bean.enums;

public enum RoastingType {
    LIGHT("Light"),
    MEDIUM("Medium"),
    DARK("Dark"),
    MEDIUMLIGHT("MediumLight"),
    MEDIUMDARK("MediumDark");

    private final String description;
    RoastingType(String description) { this.description = description; }
}
