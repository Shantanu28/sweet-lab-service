package org.pancakelab.domain.shared;

/**
 * @author Shantanu Singh.
 */
public enum IngredientName {
    DARK_CHOCOLATE("dark chocolate"),
    MILK_CHOCOLATE("milk chocolate"),
    WHIPPED_CREAM("whipped cream"),
    HAZELNUTS("hazelnuts");

    private final String displayName;

    IngredientName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
