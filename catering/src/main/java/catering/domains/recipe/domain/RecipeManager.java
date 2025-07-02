package catering.domains.recipe.domain;

import catering.domains.recipe.infrastructure.RecipeDAO;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@NoArgsConstructor
public class RecipeManager {

    public ArrayList<Recipe> getRecipeBook() {
        return RecipeDAO.loadAllRecipes();
    }
}
