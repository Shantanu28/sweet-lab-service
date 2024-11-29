package org.pancakelab.domain.pancake;

import java.util.LinkedHashSet;
import java.util.Set;
import org.pancakelab.domain.shared.Ingredient;

/**
 * @author Shantanu Singh
 */
public class PancakeBuilder {
    private final Set<Ingredient> ingredients = new LinkedHashSet<>();

    public PancakeBuilder addIngredient(final Ingredient ingredient) {
        if (ingredient == null) {
            throw new IllegalArgumentException("Ingredient cannot be null.");
        } else if (!ingredients.add(ingredient)) {
            throw new IllegalArgumentException("Duplicate ingredient: " + ingredient.getDisplayName());
        }
        return this;
    }

    public Item build() {
        if (ingredients.isEmpty()) {
            throw new IllegalStateException("Ingredients cannot be empty.");
        }

        return new Pancake(ingredients
            .stream()
            .toList());
    }
}
