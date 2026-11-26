package stockpro;

/** Electronics come with a warranty, and I add a 5% warranty levy to the selling price. */
public final class ElectronicProduct extends Product {
    private final int warrantyMonths;

    public ElectronicProduct(String name, double price, int qty, int warrantyMonths) {
        // super(...) calls the PARENT constructor and must be the first line
        super(name, Category.ELECTRONICS, price, qty, Category.ELECTRONICS.getDefaultReorderLevel());
        if (warrantyMonths < 0) throw new InvalidProductException("Warranty cannot be negative");
        this.warrantyMonths = warrantyMonths;
    }

    public int getWarrantyMonths() { return warrantyMonths; }

    @Override
    public double sellingPrice() { return getUnitPrice() * 1.05; }

    @Override
    public String summary() {
        // super.summary() runs the parent's version, then we add our own bit
        return super.summary() + " warranty=" + warrantyMonths + "m";
    }
}
