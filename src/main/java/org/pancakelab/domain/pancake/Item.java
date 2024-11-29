package org.pancakelab.domain.pancake;

import java.util.List;
import org.pancakelab.domain.shared.Ingredient;

/**
 * @author Shantanu Singh.
 */
public interface Item {
    List<Ingredient> getIngredients();

    String getDescription();
}
