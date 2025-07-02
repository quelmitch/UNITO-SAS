package catering.businesslogic.recipe;

import catering.persistence.PersistenceManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * DAO class for managing persistence of Recipe objects.
 */
public class RecipeDAO {

    public static ArrayList<Recipe> loadAllRecipes() {
        ArrayList<Recipe> recipes = new ArrayList<>();

        String query = "SELECT * FROM Recipes";
        PersistenceManager.executeQuery(query, rs -> {
            Recipe rec = createRecipeFromResultSet(rs);
            recipes.add(rec);
        });

        for (Recipe recipe : recipes) {
            loadPreparationsForRecipe(recipe);
        }

        recipes.sort(Comparator.comparing(Recipe::getName));
        return recipes;
    }

    public static Recipe loadRecipeById(int id) {
        Recipe[] holder = new Recipe[1];
        String query = "SELECT * FROM Recipes WHERE id = ?";

        PersistenceManager.executeQuery(query, rs -> {
            holder[0] = createRecipeFromResultSet(rs);
        }, id);

        Recipe recipe = holder[0];
        if (recipe != null) {
            loadPreparationsForRecipe(recipe);
        }
        return recipe;
    }

    public static Recipe loadRecipeByName(String name) {
        Recipe[] holder = new Recipe[1];
        String query = "SELECT * FROM Recipes WHERE name = ?";

        PersistenceManager.executeQuery(query, rs -> {
            holder[0] = createRecipeFromResultSet(rs);
        }, name);

        Recipe recipe = holder[0];
        if (recipe != null) {
            loadPreparationsForRecipe(recipe);
        }
        return recipe;
    }

    public static boolean saveRecipe(Recipe recipe) {
        if (recipe.getId() != 0)
            return false;

        String query = "INSERT INTO Recipes (name, description) VALUES (?, ?)";
        PersistenceManager.executeUpdate(query, recipe.getName(), recipe.getDescription());
        recipe.setId(PersistenceManager.getLastId());

        savePreparationRelationships(recipe);
        return true;
    }

    public static boolean updateRecipe(Recipe recipe) {
        if (recipe.getId() == 0)
            return false;

        String query = "UPDATE Recipes SET name = ?, description = ? WHERE id = ?";
        int rows = PersistenceManager.executeUpdate(query, recipe.getName(), recipe.getDescription(), recipe.getId());

        savePreparationRelationships(recipe);
        return rows > 0;
    }


    // HELPERS
    private static Recipe createRecipeFromResultSet(ResultSet rs) throws SQLException {
        Recipe recipe = new Recipe(rs.getString("name"));
        recipe.setId(rs.getInt("id"));
        try {
            recipe.setDescription(rs.getString("description"));
        } catch (SQLException e) {
            recipe.setDescription("");
        }
        return recipe;
    }

    private static void loadPreparationsForRecipe(Recipe recipe) {
        String query = "SELECT preparation_id FROM RecipePreparations WHERE recipe_id = ?";
        PersistenceManager.executeQuery(query, rs -> {
            int prepId = rs.getInt("preparation_id");
            Preparation prep = PreparationDAO.loadPreparationById(prepId);
            if (prep != null) {
                recipe.addPreparation(prep);
            }
        }, recipe.getId());
    }

    private static void savePreparationRelationships(Recipe recipe) {
        if (recipe.getId() == 0)
            return;

        String deleteQuery = "DELETE FROM RecipePreparations WHERE recipe_id = ?";
        PersistenceManager.executeUpdate(deleteQuery, recipe.getId());

        for (Preparation prep : recipe.getPreparations()) {
            if (prep.getId() != 0) {
                String insertQuery = "INSERT INTO RecipePreparations (recipe_id, preparation_id) VALUES (?, ?)";
                PersistenceManager.executeUpdate(insertQuery, recipe.getId(), prep.getId());
            }
        }
    }
}
