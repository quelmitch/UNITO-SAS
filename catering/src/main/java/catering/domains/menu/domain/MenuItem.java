package catering.domains.menu.domain;

import catering.domains.recipe.domain.Recipe;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MenuItem {

    private int id;
    private String description;
    private Recipe recipe;

    public MenuItem(Recipe recipe, String description) {
        this.id = 0;
        this.recipe = recipe;
        this.description = description;
    }

    public MenuItem(MenuItem menuToCopy) {
        this(menuToCopy.recipe, menuToCopy.description);
    }


    public MenuItem deepCopy() {
        MenuItem copy = new MenuItem(this.recipe, this.description);
        copy.id = this.id;
        return copy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(description);

        if (recipe != null) {
            sb.append(" (Recipe: ").append(recipe.getName()).append(")");
        } else {
            sb.append(" (No recipe assigned)");
        }

        if (id > 0) {
            sb.append(" [ID: ").append(id).append("]");
        }

        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MenuItem)) return false;
        MenuItem other = (MenuItem) obj;

        if (this.id > 0 && other.id > 0) return this.id == other.id;

        boolean descriptionMatch = (this.description == null && other.description == null) ||
            (this.description != null && this.description.equals(other.description));
        boolean recipeMatch = (this.recipe == null && other.recipe == null) ||
            (this.recipe != null && this.recipe.equals(other.recipe));
        return descriptionMatch && recipeMatch;
    }

    @Override
    public int hashCode() {
        if (id > 0) return Integer.hashCode(id);
        int result = 31;
        result += description != null ? description.hashCode() : 0;
        result += recipe != null ? recipe.hashCode() : 0;
        return result;
    }
}
