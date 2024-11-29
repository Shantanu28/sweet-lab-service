package org.pancakelab.domain.pancake;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.pancakelab.domain.shared.Ingredient;
import org.pancakelab.domain.shared.IngredientName;

/**
 * @author Shantanu Singh.
 */
class PancakeBuilderTest {

    @Test
    public void GivenIngredients_WhenCreatingCustomPancake_ThenPancakeCreatedCorrectly_Test() {

        PancakeBuilder pancakeBuilder = new PancakeBuilder();
        Item pancake = pancakeBuilder.addIngredient(new Ingredient(IngredientName.DARK_CHOCOLATE))
            .build();

        assertEquals("Delicious pancake with dark chocolate!", pancake.getDescription());
    }

    @Test
    public void GivenIngredients_WhenCreatingCustomPancakeWithTwoIngredient_ThenPancakeCreatedCorrectly_Test() {
        PancakeBuilder pancakeBuilder = new PancakeBuilder();
        Item pancake = pancakeBuilder.addIngredient(new Ingredient(IngredientName.DARK_CHOCOLATE))
            .addIngredient(new Ingredient(IngredientName.WHIPPED_CREAM))
            .build();

        assertEquals(2, pancake.getIngredients().size());
    }

    @Test
    public void GivenNoIngredients_WhenBuildingPancake_ThenThrowException() {
        Exception exception = assertThrows(IllegalStateException.class, () -> new PancakeBuilder().build());

        assertEquals("Ingredients cannot be empty.", exception.getMessage());
    }

    @Test
    public void GivenNullIngredient_WhenAddingIngredient_ThenThrowException() {
        PancakeBuilder builder = new PancakeBuilder();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> builder.addIngredient(null));

        assertEquals("Ingredient cannot be null.", exception.getMessage());
    }

    @Test
    void givenDuplicateIngredient_whenAddingToPancake_thenThrowsException() {
        PancakeBuilder builder = new PancakeBuilder();

        builder.addIngredient(new Ingredient(IngredientName.DARK_CHOCOLATE));
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            builder.addIngredient(new Ingredient(IngredientName.DARK_CHOCOLATE))
        );
        assertEquals("Duplicate ingredient: dark chocolate", exception.getMessage());
    }

}