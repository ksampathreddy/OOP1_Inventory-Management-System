package stockpro.tests;

import java.time.LocalDate;

import stockpro.Category;
import stockpro.GeneralProduct;
import stockpro.InsufficientStockException;
import stockpro.Inventory;
import stockpro.InvalidProductException;
import stockpro.PerishableProduct;
import stockpro.Product;
import stockpro.ProductExpiredException;
import stockpro.SaleItem;

/**
 * Small plain-Java checks (no JUnit needed) for the awkward cases:
 * bad prices, overflow, empty sales, merged duplicate lines, expired stock.
 * Prints how many passed. Exits with code 1 if anything failed.
 */
public class EdgeCaseChecks {
    private static int passed = 0;
    private static int failed = 0;

    private interface Action { void run() throws Exception; }

    private static void expectError(String label, Class<? extends Exception> type, Action action) {
        try {
            action.run();
            fail(label + " (no exception thrown)");
        } catch (Exception e) {
            if (type.isInstance(e)) passed++; else fail(label + " (got " + e + ")");
        }
    }

    private static void check(String label, boolean condition) {
        if (condition) passed++; else fail(label);
    }

    private static void fail(String message) {
        failed++;
        System.out.println("FAIL: " + message);
    }

    public static void main(String[] args) throws Exception {
        Inventory inv = new Inventory();
        Product pen = new GeneralProduct("Pen", Category.STATIONERY, 1.0, 5, 2);
        inv.addProduct(pen);
        int id = pen.getId();

        // validation
        expectError("NaN price", InvalidProductException.class, () -> new GeneralProduct("x", Category.FOOD, Double.NaN));
        expectError("infinite price", InvalidProductException.class, () -> new GeneralProduct("x", Category.FOOD, Double.POSITIVE_INFINITY));
        expectError("negative price", InvalidProductException.class, () -> new GeneralProduct("x", Category.FOOD, -1));
        expectError("blank name", InvalidProductException.class, () -> new GeneralProduct("   ", Category.FOOD, 1));
        expectError("NaN price change", InvalidProductException.class, () -> inv.changePrice(id, Double.NaN));
        expectError("null expiry date", InvalidProductException.class, () -> new PerishableProduct("Milk", 1, 1, null));
        expectError("zero sale quantity", InvalidProductException.class, () -> new SaleItem(id, 0));

        // restocking
        expectError("cases x units overflow", InvalidProductException.class, () -> inv.restock(id, 100_000, 100_000));
        expectError("quantity overflow", InvalidProductException.class, () -> inv.restock(id, Integer.MAX_VALUE));
        expectError("zero cases", InvalidProductException.class, () -> inv.restock(id, 0, 5));

        // selling
        expectError("empty sale", InvalidProductException.class, () -> inv.sell("Bob"));
        expectError("blank customer", InvalidProductException.class, () -> inv.sell(" ", new SaleItem(id, 1)));
        expectError("duplicate lines overflow int", InvalidProductException.class,
                () -> inv.sell("Bob", new SaleItem(id, Integer.MAX_VALUE), new SaleItem(id, 1)));
        check("overflowing sale changed nothing", pen.getQuantity() == 5);
        expectError("duplicate lines add up past the stock", InsufficientStockException.class,
                () -> inv.sell("Bob", new SaleItem(id, 3), new SaleItem(id, 3)));
        check("failed sale changed nothing", pen.getQuantity() == 5);

        Product yoghurt = new PerishableProduct("Yoghurt", 2.0, 10, LocalDate.now().minusDays(1));
        inv.addProduct(yoghurt);
        expectError("expired food cannot be sold", ProductExpiredException.class,
                () -> inv.sell("Bob", new SaleItem(yoghurt.getId(), 1)));
        check("expired food is worth 0", yoghurt.stockValue() == 0.0);

        var sale = inv.sell("Ann", new SaleItem(id, 2));
        check("normal sale takes stock off", pen.getQuantity() == 3);
        check("normal sale total", sale.total() == 2.0);

        System.out.println("Passed: " + passed + "   Failed: " + failed);
        if (failed > 0) System.exit(1);
    }
}
