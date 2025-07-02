package catering.domains.recipe.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Recipe represents a complete dish that can be prepared and served.
 */
@Data
@NoArgsConstructor
public class Recipe implements KitchenProcess {

    private int id;
    private String name;
    private String description;
    private ArrayList<Preparation> preparations = new ArrayList<>();

    public Recipe(String name) {
        this.id = 0;
        this.name = name;
        this.description = "";
    }

    @Override
    public boolean isRecipe() {
        return true;
    }

    public void addPreparation(Preparation preparation) {
        if (!preparations.contains(preparation)) {
            preparations.add(preparation);
        }
    }

    public boolean removePreparation(Preparation preparation) {
        return preparations.remove(preparation);
    }

    public ArrayList<Preparation> getPreparations() {
        return new ArrayList<>(preparations);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Recipe))
            return false;

        Recipe other = (Recipe) obj;

        if (this.id > 0 && other.id > 0)
            return this.id == other.id;

        boolean nameMatch = Objects.equals(this.name, other.name);
        boolean descMatch = Objects.equals(this.description, other.description);

        if (!nameMatch || !descMatch)
            return false;

        if (this.preparations.size() != other.preparations.size())
            return false;

        return this.preparations.containsAll(other.preparations);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = (id > 0) ? prime * id : prime * Objects.hash(name, description);
        for (Preparation prep : preparations) {
            result += (prep.getId() > 0) ? prime * prep.getId() :
                (prep.getName() != null ? prime * prep.getName().hashCode() : 0);
        }
        return result;
    }
}
