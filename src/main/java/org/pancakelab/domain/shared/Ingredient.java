package org.pancakelab.domain.shared;

/**
 * @author Shantanu Singh.
 */
public record Ingredient(IngredientName name) {
    public Ingredient {
        if (name == null) {
            throw new IllegalArgumentException("Ingredient name cannot be null.");
        }
    }

    public String getDisplayName() {
        return name.getDisplayName();
    }
}
