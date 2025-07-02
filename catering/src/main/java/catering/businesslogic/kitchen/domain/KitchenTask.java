package catering.businesslogic.kitchen.domain;

import catering.businesslogic.recipe.KitchenProcess;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a kitchen task derived from a kitchen process (recipe or preparation)
 */
@Data
@NoArgsConstructor
public class KitchenTask {
    private int id;
    private String description;
    private KitchenProcess kitchenProcess;
    private int quantity;
    private int portions;
    private boolean ready;
    private boolean type; // true = Recipe, false = Preparation

    public KitchenTask(KitchenProcess process, String description) {
        this.id = 0;
        this.kitchenProcess = process;
        this.description = description;
        this.type = process.isRecipe();
        this.ready = false;
        this.quantity = 0;
        this.portions = 0;
    }

    public void setReady() {
        this.ready = true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(isReady() ? "[✓] " : "[ ] ")
            .append(getDescription());

        if (quantity > 0 || portions > 0) {
            sb.append(" (");
            if (quantity > 0) sb.append("Qty: ").append(quantity);
            if (quantity > 0 && portions > 0) sb.append(", ");
            if (portions > 0) sb.append("Portions: ").append(portions);
            sb.append(")");
        }

        return sb.toString();
    }
}
