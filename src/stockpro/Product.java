package stockpro;

/**
 * Base class for everything the shop sells. It is sealed, so only the three
 * subclasses named in "permits" can extend it.
 *
 * Encapsulation: every field is private. The methods that CHANGE a product
 * (stock, price, name, supplier) are package-private. That means any class in the
 * stockpro package can call them, but code in other packages, such as the menu
 * in stockpro.app, cannot. Inventory is the class that offers the controlled
 * operations (restock, sell, changePrice) that use them and record the history.
 */
public abstract sealed class Product implements Describable, Comparable<Product>
        permits GeneralProduct, PerishableProduct, ElectronicProduct {

    private static int nextId = 1000;   // shared by all products, gives each one a new id

    private final int id;
    private String name;
    private final Category category;
    private double unitPrice;
    private int quantity;
    private int reorderLevel;
    private Supplier supplier;          // Supplier is immutable so it is safe to hold on to

    /** Full constructor. */
    protected Product(String name, Category category, double unitPrice, int quantity, int reorderLevel) {
        this.id = nextId++;
        this.category = category;
        this.name = checkName(name);
        this.unitPrice = checkPrice(unitPrice);
        if (quantity < 0 || reorderLevel < 0) {
            throw new InvalidProductException("Quantity and reorder level cannot be negative");
        }
        this.quantity = quantity;
        this.reorderLevel = reorderLevel;
    }

    /**
     * this(...) calls ANOTHER constructor in the same class (constructor chaining).
     * Here: no starting stock, and the category's default reorder level.
     */
    protected Product(String name, Category category, double unitPrice) {
        this(name, category, unitPrice, 0, category.getDefaultReorderLevel());
    }

    // ---------- validation helpers ----------
    private static String checkName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidProductException("Product name cannot be blank");
        }
        return name.trim();
    }

    private static double checkPrice(double price) {
        // NaN < 0 is false, so a "price < 0" check alone would let NaN through
        if (!Double.isFinite(price) || price < 0) {
            throw new InvalidProductException("Price must be a normal number, zero or more");
        }
        return price;
    }

    // ---------- getters (public: reading is fine) ----------
    public int getId() { return id; }
    public String getName() { return name; }
    public Category getCategory() { return category; }
    public double getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
    public int getReorderLevel() { return reorderLevel; }
    public Supplier getSupplier() { return supplier; }

    // ---------- mutators (package-private: not reachable from other packages) ----------
    void setName(String name) {
        this.name = checkName(name);    // this.name is the field, name is the parameter
    }

    void setUnitPrice(double unitPrice) {
        this.unitPrice = checkPrice(unitPrice);
    }

    void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    // Overloaded: same name, different parameters
    void addStock(int amount) {
        if (amount <= 0) throw new InvalidProductException("Amount must be positive");
        try {
            quantity = Math.addExact(quantity, amount);
        } catch (ArithmeticException e) {
            throw new InvalidProductException("That much stock does not fit in the quantity field");
        }
    }

    void addStock(int cases, int unitsPerCase) {
        if (cases <= 0 || unitsPerCase <= 0) {
            throw new InvalidProductException("Cases and units per case must both be positive");
        }
        try {
            addStock(Math.multiplyExact(cases, unitsPerCase));
        } catch (ArithmeticException e) {
            throw new InvalidProductException("Cases x units per case is too large");
        }
    }

    void removeStock(int amount) throws InsufficientStockException {
        if (amount <= 0) throw new InvalidProductException("Amount must be positive");
        if (amount > quantity) throw new InsufficientStockException(name, quantity, amount);
        quantity -= amount;
    }

    // ---------- behaviour ----------
    public boolean isLowStock() { return quantity <= reorderLevel; }

    /** Each subclass works out its own selling price (this is the polymorphic part). */
    public abstract double sellingPrice();

    public double stockValue() { return sellingPrice() * quantity; }

    @Override
    public String summary() {
        return String.format("#%d %-20s %-13s qty=%-4d price=EUR %.2f",
                id, name, category, quantity, sellingPrice());
    }

    @Override
    public int compareTo(Product other) {
        return name.compareToIgnoreCase(other.name);
    }

    @Override
    public String toString() {
        return name + " (#" + id + ")";
    }
}
