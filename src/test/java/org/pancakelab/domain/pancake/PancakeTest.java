package org.pancakelab.domain.pancake;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.pancakelab.domain.shared.Ingredient;
import org.pancakelab.domain.shared.IngredientName;

/**
 * @author Shantanu Singh.
 */
public class PancakeTest {

    @Test
    public void GivenValidIngredients_WhenCreatingPancake_ThenSuccess_Test() {
        List<Ingredient> ingredients = List.of(
            new Ingredient(IngredientName.DARK_CHOCOLATE),
            new Ingredient(IngredientName.WHIPPED_CREAM)
        );
        Item pancake = new Pancake(ingredients);

        assertEquals(ingredients, pancake.getIngredients());
        assertEquals("Delicious pancake with dark chocolate, whipped cream!", pancake.getDescription());
    }

    @Test
    public void GivenNoIngredients_WhenCreatingPancake_ThenThrowException_Test() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new Pancake(List.of()));

        assertEquals("Pancake must have at least one ingredient.", exception.getMessage());
    }

    @Test
    public void GivenNullIngredients_WhenCreatingPancake_ThenThrowException_Test() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new Pancake(null));

        assertEquals("Pancake must have at least one ingredient.", exception.getMessage());
    }
}
