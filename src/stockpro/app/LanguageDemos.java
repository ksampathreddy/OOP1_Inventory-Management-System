package stockpro.app;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import stockpro.Category;
import stockpro.GeneralProduct;
import stockpro.Inventory;
import stockpro.Product;
import stockpro.Supplier;

/** Small demos for the ideas that are easier to see running than to read about. */
public final class LanguageDemos {
    private LanguageDemos() { }

    // ---------------- call-by-value ----------------
    private static void changePrimitive(int x) {
        x = 999;                                    // only changes this method's copy
    }

    private static void changeReference(Product p) {
        p = new GeneralProduct("Other", Category.HOUSEHOLD, 1.0);   // only changes the copy of the reference
    }

    private static void changeContents(StringBuilder sb) {
        sb.append(" (edited)");                     // follows the reference to the SAME object
    }

    public static void callByValue(Product product) {
        System.out.println("Java is always pass-by-value. For objects, the value that gets copied is the reference.");

        int qty = product.getQuantity();
        changePrimitive(qty);
        System.out.println("1) changePrimitive(qty): qty is still " + qty);

        Product before = product;
        changeReference(product);
        System.out.println("2) changeReference(product): still the same object? " + (before == product)
                + " (" + product.getName() + ")");

        StringBuilder label = new StringBuilder("Shelf label");
        changeContents(label);
        System.out.println("3) changeContents(label): the object itself changed -> \"" + label + "\"");
    }

    // ---------------- defensive copying ----------------
    public static void defensiveCopy(Inventory inv) {
        int before = inv.getProducts().size();
        List<Product> copy = inv.getProducts();
        copy.clear();
        System.out.println("1) Cleared the list that getProducts() gave me. Inventory still has "
                + inv.getProducts().size() + " products (was " + before + ").");

        Product[] low = inv.lowStockReport();
        int length = low.length;
        if (length > 0) low[0] = null;
        System.out.println("2) Set low[0] = null. A fresh report still has " + inv.lowStockReport().length
                + " entries (was " + length + ") and none are null: " + (inv.lowStockReport()[0] != null));

        Supplier s = new Supplier("Test Supplier", "test@example.ie", new ArrayList<>(List.of("Mon")));
        try {
            s.getDeliveryDays().add("Tue");
        } catch (UnsupportedOperationException e) {
            System.out.println("3) Supplier is immutable: its delivery days cannot be changed ("
                    + e.getClass().getSimpleName() + ").");
        }
        System.out.println("4) Product has no public setters, so code outside the package cannot change a product.");
    }

    // ---------------- lambdas, final / effectively final, method references ----------------
    public static void lambdas(Inventory inv) {
        var threshold = 10;     // var infers int. Never reassigned, so it is effectively final.
        Predicate<Product> fewLeft = p -> p.getQuantity() < threshold;
        // threshold = 20;      <- if I un-comment this line, the lambda above no longer compiles
        System.out.println("Fewer than " + threshold + " in stock: " + inv.find(fewLeft));

        final var minLength = 12;   // explicitly final: same result, just written out
        Predicate<Product> longName = p -> p.getName().length() > minLength;
        System.out.println("Name longer than " + minLength + " characters: " + inv.find(longName));

        System.out.println("Method references on the first three products:");
        inv.getProducts().stream()
                .limit(3)
                .map(Product::getName)          // instance method of any Product
                .map(String::toUpperCase)       // instance method of any String
                .forEach(System.out::println);  // method of one specific object (System.out)
    }
}
