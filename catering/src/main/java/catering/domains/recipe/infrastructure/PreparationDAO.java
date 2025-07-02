package catering.domains.recipe.infrastructure;

import catering.domains.recipe.domain.Preparation;
import catering.domains.recipe.domain.Recipe;
import catering.persistence.PersistenceManager;

import java.sql.SQLException;
import java.util.*;

/**
 * DAO class for accessing Preparation data from the database.
 */
public class PreparationDAO {

    public static ArrayList<Preparation> loadAllPreparations() {
        ArrayList<Preparation> preparations = new ArrayList<>();

        String query = "SELECT * FROM Preparations";
        PersistenceManager.executeQuery(query, rs -> {
            Preparation prep = new Preparation(rs.getString("name"));
            prep.setId(rs.getInt("id"));
            try {
                prep.setDescription(rs.getString("description"));
            } catch (SQLException e) {
                prep.setDescription("");
            }
            preparations.add(prep);
        });

        preparations.sort(Comparator.comparing(Preparation::getName));
        return preparations;
    }

    public static Preparation loadPreparationById(int id) {
        Preparation[] holder = new Preparation[1];

        String query = "SELECT * FROM Preparations WHERE id = ?";
        PersistenceManager.executeQuery(query, rs -> {
            Preparation prep = new Preparation();
            prep.setName(rs.getString("name"));
            prep.setId(id);
            try {
                prep.setDescription(rs.getString("description"));
            } catch (SQLException e) {
                prep.setDescription("");
            }
            holder[0] = prep;
        }, id);

        return holder[0];
    }

    public static boolean savePreparation(Preparation prep) {
        if (prep.getId() != 0)
            return false;

        String query = "INSERT INTO Preparations (name, description) VALUES (?, ?)";
        PersistenceManager.executeUpdate(query, prep.getName(), prep.getDescription());
        prep.setId(PersistenceManager.getLastId());

        return true;
    }

    public static boolean updatePreparation(Preparation prep) {
        if (prep.getId() == 0)
            return false;

        String query = "UPDATE Preparations SET name = ?, description = ? WHERE id = ?";
        int rows = PersistenceManager.executeUpdate(query, prep.getName(), prep.getDescription(), prep.getId());

        return rows > 0;
    }

    public static List<Recipe> getRecipesUsingPreparation(Preparation prep) {
        List<Recipe> recipes = new ArrayList<>();

        if (prep.getId() == 0)
            return recipes;

        String query = "SELECT recipe_id FROM RecipePreparations WHERE preparation_id = ?";
        PersistenceManager.executeQuery(query, rs -> {
            int recipeId = rs.getInt("recipe_id");
            Recipe recipe = RecipeDAO.loadRecipeById(recipeId);
            if (recipe != null)
                recipes.add(recipe);
        }, prep.getId());

        return recipes;
    }
}
