package stockpro;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * All the shop logic lives here. The methods that change a product are package-private
 * in Product, so code outside this package (like the menu) has to go through
 * Inventory's restock, sell and changePrice methods to change anything.
 */
public class Inventory {
    private final List<Product> products = new ArrayList<>();
    private final List<Transaction> history = new ArrayList<>();

    // ---------- adding and viewing ----------
    public void addProduct(Product p) {
        if (p == null) throw new InvalidProductException("Product cannot be null");
        products.add(p);
    }

    /**
     * Returns a sorted COPY of the list (defensive copy), so callers cannot add to or
     * clear our real list. It is a shallow copy: the Product objects inside are the
     * same ones, but they have no public setters, so callers can only read them.
     */
    public List<Product> getProducts() {
        List<Product> copy = new ArrayList<>(products);
        Collections.sort(copy);
        return copy;
    }

    public List<Transaction> getHistory() {
        return List.copyOf(history);
    }

    public Product findById(int id) throws ProductNotFoundException {
        for (Product p : products) {
            if (p.getId() == id) return p;
        }
        throw new ProductNotFoundException(id);
    }

    // ---------- searching (find is overloaded three ways) ----------
    public List<Product> find(String text) {
        String needle = text.trim().toLowerCase();
        return find(p -> p.getName().toLowerCase().contains(needle));
    }

    public List<Product> find(Category category) {
        return find(p -> p.getCategory() == category);
    }

    public List<Product> find(Predicate<Product> filter) {
        List<Product> result = new ArrayList<>();
        for (Product p : products) {
            if (filter.test(p)) result.add(p);
        }
        return result;
    }

    /**
     * minPrice is never reassigned in this method, so it is "effectively final" and
     * the lambda is allowed to use it. If I added minPrice = 0; anywhere below, the
     * lambda would stop compiling, because a lambda keeps its own COPY of the value
     * and Java makes sure that copy can never go out of date.
     */
    public List<Product> findPricedAtLeast(double minPrice) {
        return find(p -> p.sellingPrice() >= minPrice);
    }

    // ---------- changing things ----------
    public void assignSupplier(int productId, Supplier supplier) throws ProductNotFoundException {
        findById(productId).setSupplier(supplier);
    }

    public void restock(int productId, int amount) throws ProductNotFoundException {
        findById(productId).addStock(amount);
        history.add(new Transaction.Restock(LocalDateTime.now(), productId, amount));
    }

    /** Overload: stock that arrives in cases. */
    public void restock(int productId, int cases, int unitsPerCase) throws ProductNotFoundException {
        Product p = findById(productId);
        int before = p.getQuantity();
        p.addStock(cases, unitsPerCase);
        history.add(new Transaction.Restock(LocalDateTime.now(), productId, p.getQuantity() - before));
    }

    public void changePrice(int productId, double newPrice) throws ProductNotFoundException {
        Product p = findById(productId);
        double old = p.getUnitPrice();
        p.setUnitPrice(newPrice);
        history.add(new Transaction.PriceChange(LocalDateTime.now(), productId, old, newPrice));
    }

    // ---------- selling (varargs) ----------
    /**
     * A sale is all-or-nothing: I check every line first and only then take stock off.
     * If the same product appears twice, the quantities are added together first.
     */
    public Transaction.Sale sell(String customer, SaleItem... items)
            throws ProductNotFoundException, InsufficientStockException, ProductExpiredException {
        if (customer == null || customer.isBlank()) {
            throw new InvalidProductException("Customer name is required");
        }
        if (items == null || items.length == 0) {
            throw new InvalidProductException("A sale needs at least one item");
        }

        Map<Integer, Integer> wanted = new LinkedHashMap<>();
        try {
            for (SaleItem item : items) {
                // Math::addExact is a static method reference like Integer::sum was,
                // but it throws instead of silently wrapping around on overflow
                wanted.merge(item.productId(), item.quantity(), Math::addExact);
            }
        } catch (ArithmeticException e) {
            throw new InvalidProductException("The quantity for one product is too large");
        }

        LocalDate today = LocalDate.now();
        for (var entry : wanted.entrySet()) {                                  // var = LVTI
            Product p = findById(entry.getKey());
            if (p instanceof PerishableProduct pp && pp.isExpired(today)) {    // pattern matching
                throw new ProductExpiredException(p.getName(), pp.getExpiryDate());
            }
            if (p.getQuantity() < entry.getValue()) {
                throw new InsufficientStockException(p.getName(), p.getQuantity(), entry.getValue());
            }
        }

        double total = 0;
        for (var entry : wanted.entrySet()) {
            Product p = findById(entry.getKey());
            total += p.sellingPrice() * entry.getValue();
            p.removeStock(entry.getValue());
        }
        var sale = new Transaction.Sale(LocalDateTime.now(), customer.trim(), Arrays.asList(items), total);
        history.add(sale);
        return sale;
    }

    // ---------- reports ----------
    /** Returns an array (a fresh one each time, so changing it does not affect the inventory). */
    public Product[] lowStockReport() {
        List<Product> low = find(Product::isLowStock);      // method reference used as a Predicate
        return low.toArray(new Product[0]);
    }

    /** Slot i holds the number of products in the category with ordinal i. */
    public int[] countByCategory() {
        int[] counts = new int[Category.values().length];
        for (Product p : products) {
            counts[p.getCategory().ordinal()]++;
        }
        return counts;
    }

    /** Perishables that expire within the next "days" days. Already-expired ones are included. */
    public List<PerishableProduct> expiringWithin(int days) {
        LocalDate today = LocalDate.now();
        List<PerishableProduct> result = new ArrayList<>();
        for (Product p : products) {
            if (p instanceof PerishableProduct pp && pp.daysUntilExpiry(today) <= days) {
                result.add(pp);
            }
        }
        result.sort(Comparator.comparing(PerishableProduct::getExpiryDate));
        return result;
    }

    public double totalValue() {
        double sum = 0;
        for (Product p : products) sum += p.stockValue();
        return sum;
    }

    public String valuationReport() {
        StringBuilder sb = new StringBuilder();
        sb.append(Describable.divider()).append('\n');
        sb.append("STOCK VALUATION (expired food counts as 0)\n");
        sb.append(Describable.divider()).append('\n');
        for (Product p : getProducts()) {
            sb.append(String.format("%-22s %4d x EUR %7.2f = EUR %9.2f%n",
                    p.getName(), p.getQuantity(), p.sellingPrice(), p.stockValue()));
        }
        sb.append(Describable.divider()).append('\n');
        sb.append(String.format("TOTAL: EUR %.2f", totalValue()));
        return sb.toString();
    }

    // ---------- turning transactions and products into text (switch + pattern matching) ----------
    public static String describeTransaction(Transaction t) {
        return switch (t) {
            case Transaction.Sale(var when, var customer, var items, var total) ->
                    String.format("SALE     to %s: %d line(s), EUR %.2f", customer, items.size(), total);
            case Transaction.Restock r when r.quantity() >= 100 ->       // "when" is a guard
                    "RESTOCK  (bulk) product #" + r.productId() + " +" + r.quantity();
            case Transaction.Restock r ->
                    "RESTOCK  product #" + r.productId() + " +" + r.quantity();
            case Transaction.PriceChange pc ->
                    String.format("PRICE    product #%d: EUR %.2f -> EUR %.2f",
                            pc.productId(), pc.oldPrice(), pc.newPrice());
        };   // no default branch needed because Transaction is sealed
    }

    public static String productNote(Product p) {
        return switch (p) {
            case PerishableProduct pp -> "Use by " + pp.getExpiryDate();
            case ElectronicProduct ep -> ep.getWarrantyMonths() + " month warranty";
            case GeneralProduct gp    -> "Standard item";
        };
    }
}
