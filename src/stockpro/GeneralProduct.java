package stockpro;

/** A normal product with no special rules: it sells at its unit price. */
public final class GeneralProduct extends Product {

    public GeneralProduct(String name, Category category, double price, int qty, int reorder) {
        super(name, category, price, qty, reorder);
    }

    /** Short version, starts with 0 stock (uses the parent's 3-argument constructor). */
    public GeneralProduct(String name, Category category, double price) {
        super(name, category, price);
    }

    @Override
    public double sellingPrice() { return getUnitPrice(); }
}
