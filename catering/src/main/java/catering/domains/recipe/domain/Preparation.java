package catering.domains.recipe.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Preparation represents an intermediate food preparation step.
 */
@Data
@NoArgsConstructor
public class Preparation implements KitchenProcess {

    private int id;
    private String name;
    private String description;

    public Preparation(String name) {
        this.id = 0;
        this.name = name;
        this.description = "";
    }

    @Override
    public boolean isRecipe() {
        return false;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        Preparation other = (Preparation) obj;

        if (this.id > 0 && other.id > 0)
            return this.id == other.id;

        boolean nameMatch = (this.name == null && other.name == null) ||
            (this.name != null && this.name.equals(other.name));
        boolean descMatch = (this.description == null && other.description == null) ||
            (this.description != null && this.description.equals(other.description));

        return nameMatch && descMatch;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        if (id > 0)
            result = prime * result + id;
        else {
            result = prime * result + (name != null ? name.hashCode() : 0);
            result = prime * result + (description != null ? description.hashCode() : 0);
        }

        return result;
    }
}
