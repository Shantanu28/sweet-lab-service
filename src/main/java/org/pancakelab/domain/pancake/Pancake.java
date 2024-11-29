package org.pancakelab.domain.pancake;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.pancakelab.domain.shared.Ingredient;

/**
 * @author Shantanu Singh.
 */
public record Pancake(List<Ingredient> ingredients) implements Item {
    public Pancake {
        if (ingredients == null || ingredients.isEmpty()) {
            throw new IllegalArgumentException("Pancake must have at least one ingredient.");
        }
        ingredients = List.copyOf(ingredients);
    }

    @Override
    public List<Ingredient> ingredients() {
        return Collections.unmodifiableList(ingredients);
    }

    @Override public List<Ingredient> getIngredients() {
        return ingredients();
    }

    public String getDescription() {
        return "Delicious pancake with " +
            ingredients
                .stream()
                .map(Ingredient::getDisplayName)
                .collect(Collectors.joining(", ")) + "!";
    }
}
