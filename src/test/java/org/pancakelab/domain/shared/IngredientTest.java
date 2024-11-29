package org.pancakelab.domain.shared;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * @author Shantanu Singh.
 */
class IngredientTest {

    @Test
    public void GivenValidIngredientName_WhenCreatingIngredient_ThenSuccess_Test() {
        Ingredient ingredient = new Ingredient(IngredientName.DARK_CHOCOLATE);
        assertEquals(IngredientName.DARK_CHOCOLATE, ingredient.name());
        assertEquals("dark chocolate", ingredient.getDisplayName());
    }

    @Test
    public void GivenTwoIngredientsWithSameName_WhenComparing_ThenTheyAreEqual_Test() {
        Ingredient ingredient1 = new Ingredient(IngredientName.DARK_CHOCOLATE);
        Ingredient ingredient2 = new Ingredient(IngredientName.DARK_CHOCOLATE);
        assertEquals(ingredient1, ingredient2);
        assertEquals(ingredient1.hashCode(), ingredient2.hashCode());
    }

    @Test
    public void GivenNullIngredientName_WhenCreatingIngredient_ThenThrowException_Test() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> new Ingredient(null));

        assertEquals("Ingredient name cannot be null.", exception.getMessage());
    }
}