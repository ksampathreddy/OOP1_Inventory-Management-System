package stockpro.app;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import stockpro.Category;
import stockpro.Describable;
import stockpro.ElectronicProduct;
import stockpro.GeneralProduct;
import stockpro.InsufficientStockException;
import stockpro.Inventory;
import stockpro.InvalidProductException;
import stockpro.PerishableProduct;
import stockpro.Product;
import stockpro.ProductExpiredException;
import stockpro.ProductNotFoundException;
import stockpro.SaleItem;
import stockpro.StockStatus;
import stockpro.Supplier;
import stockpro.Transaction;

/**
 * Console menu for StockPro. Every user story and every concept demo is one menu option.
 */
public class Main {

    private static final Inventory inventory = new Inventory();
    private static final Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            seedData();
        } catch (ProductNotFoundException e) {
            throw new IllegalStateException(e);     // cannot happen: ids come straight from the products
        }

        boolean running = true;
        while (running) {
            printMenu();
            try {
                String choice = in.nextLine().trim();
                switch (choice) {
                    case "1" -> addProductUi();
                    case "2" -> listProducts(inventory.getProducts());
                    case "3" -> searchUi();
                    case "4" -> restockUi();
                    case "5" -> sellUi();
                    case "6" -> lowStockUi();
                    case "7" -> expiringUi();
                    case "8" -> historyUi();
                    case "9" -> System.out.println(inventory.valuationReport());
                    case "10" -> changePriceUi();
                    case "11" -> detailsUi();
                    case "12" -> conceptsUi();
                    case "0" -> running = false;
                    default -> System.out.println("Unknown option, try again.");
                }
            } catch (ProductNotFoundException | InsufficientStockException | ProductExpiredException e) {
                System.out.println("Could not do that: " + e.getMessage());               // checked exceptions
            } catch (InvalidProductException | IllegalArgumentException | java.time.format.DateTimeParseException e) {
                System.out.println("Invalid input: " + e.getMessage());                    // unchecked exceptions
            } catch (NoSuchElementException e) {
                running = false;                                                           // input stream closed
            }
        }
        System.out.println("Goodbye!");
    }

    private static void printMenu() {
        System.out.println("\n" + Describable.divider());
        System.out.println(" STOCKPRO - Inventory and Stock Management System for Retailers");
        System.out.println(Describable.divider());
        System.out.println(" 1  Add product         7  Expiring items");
        System.out.println(" 2  View all products   8  Transaction history");
        System.out.println(" 3  Search              9  Stock valuation");
        System.out.println(" 4  Restock             10 Change a price");
        System.out.println(" 5  Sell                11 Product details");
        System.out.println(" 6  Low-stock report    12 Java concepts");
        System.out.println(" 0  Exit");
        System.out.print("Choose: ");
    }

    // ---------- small helpers ----------
    private static String ask(String prompt) {
        System.out.print(prompt + ": ");
        return in.nextLine().trim();
    }

    private static void listProducts(List<Product> list) {
        if (list.isEmpty()) {
            System.out.println("(nothing found)");
            return;
        }
        for (Product p : list) {
            System.out.printf("%s  [%s]%n", p.describe(), StockStatus.of(p).label());
        }
    }

    // ---------- the user stories ----------
    // US1: add a product
    private static void addProductUi() {
        System.out.println("Type: 1 = General, 2 = Perishable, 3 = Electronic");
        String type = ask("Type");
        String name = ask("Name");
        double price = Double.parseDouble(ask("Unit price"));
        int qty = Integer.parseInt(ask("Starting quantity"));

        Product p = switch (type) {                                     // switch expression
            case "2" -> new PerishableProduct(name, price, qty, LocalDate.parse(ask("Expiry date (yyyy-mm-dd)")));
            case "3" -> new ElectronicProduct(name, price, qty, Integer.parseInt(ask("Warranty in months")));
            default -> {
                System.out.println("Categories: " + Arrays.toString(Category.values()));
                Category c = Category.valueOf(ask("Category (enum name, e.g. CLOTHING)").toUpperCase());
                yield new GeneralProduct(name, c, price, qty, c.getDefaultReorderLevel());
            }
        };
        inventory.addProduct(p);
        System.out.println("Added: " + p.describe());
    }

    // US3: search (three ways, all going through the overloaded find methods)
    private static void searchUi() {
        System.out.println("Search by: 1 = name, 2 = category, 3 = minimum price");
        switch (ask("Choice")) {
            case "1" -> listProducts(inventory.find(ask("Part of the name")));
            case "2" -> {
                System.out.println("Categories: " + Arrays.toString(Category.values()));
                listProducts(inventory.find(Category.valueOf(ask("Category (enum name)").toUpperCase())));
            }
            case "3" -> listProducts(inventory.findPricedAtLeast(Double.parseDouble(ask("Minimum selling price"))));
            default -> System.out.println("Unknown option.");
        }
    }

    // US4: restock, in units or in cases (overloaded restock methods)
    private static void restockUi() throws ProductNotFoundException {
        int id = Integer.parseInt(ask("Product id"));
        if (ask("1 = units, 2 = cases").equals("2")) {
            inventory.restock(id, Integer.parseInt(ask("Number of cases")), Integer.parseInt(ask("Units per case")));
        } else {
            inventory.restock(id, Integer.parseInt(ask("Units received")));
        }
        System.out.println("New quantity: " + inventory.findById(id).getQuantity());
    }

    // US5: sell
    private static void sellUi() throws ProductNotFoundException, InsufficientStockException, ProductExpiredException {
        String customer = ask("Customer name");
        List<SaleItem> items = new ArrayList<>();
        while (true) {
            String idText = ask("Product id (leave blank to finish)");
            if (idText.isEmpty()) break;
            items.add(new SaleItem(Integer.parseInt(idText), Integer.parseInt(ask("Quantity"))));
        }
        var sale = inventory.sell(customer, items.toArray(new SaleItem[0]));    // array goes into the varargs
        System.out.printf("Sale done. Total: EUR %.2f%n", sale.total());
    }

    // US6: low stock
    private static void lowStockUi() {
        Product[] low = inventory.lowStockReport();
        System.out.println(low.length + " product(s) at or below their reorder level:");
        for (Product p : low) {
            System.out.printf("  %-22s qty=%-3d reorder at %-3d %s%n",
                    p.getName(), p.getQuantity(), p.getReorderLevel(), StockStatus.of(p).label());
        }
        int[] counts = inventory.countByCategory();
        StringBuilder sb = new StringBuilder("Products per category: ");
        for (Category c : Category.values()) {
            sb.append(c).append(" = ").append(counts[c.ordinal()]).append("   ");
        }
        System.out.println(sb.toString().trim());
    }

    // US7: expiring items
    private static void expiringUi() {
        int days = Integer.parseInt(ask("Show items expiring within how many days"));
        listExpiring(days);
    }

    private static void listExpiring(int days) {
        LocalDate today = LocalDate.now();
        List<PerishableProduct> list = inventory.expiringWithin(days);
        if (list.isEmpty()) System.out.println("(nothing expiring)");
        for (PerishableProduct p : list) {
            long d = p.daysUntilExpiry(today);
            String when = d < 0 ? "EXPIRED " + (-d) + " day(s) ago" : "in " + d + " day(s)";
            System.out.printf("  %-22s %s  (%s)  price now EUR %.2f%n",
                    p.getName(), p.getExpiryDate(), when, p.sellingPrice());
        }
    }

    // US8: history
    private static void historyUi() {
        List<Transaction> history = inventory.getHistory();
        if (history.isEmpty()) System.out.println("(no transactions yet)");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM HH:mm:ss");
        for (Transaction t : history) {
            System.out.println("  " + t.timestamp().format(fmt) + "  " + Inventory.describeTransaction(t));
        }
    }

    private static void changePriceUi() throws ProductNotFoundException {
        int id = Integer.parseInt(ask("Product id"));
        inventory.changePrice(id, Double.parseDouble(ask("New unit price")));
        System.out.println(inventory.findById(id).describe());
    }

    // Product details: switch pattern matching over the sealed Product type, and the immutable Supplier
    private static void detailsUi() throws ProductNotFoundException {
        Product p = inventory.findById(Integer.parseInt(ask("Product id")));
        System.out.println(p.describe());
        System.out.println("Status:   " + StockStatus.of(p).label());
        System.out.println("Note:     " + Inventory.productNote(p));
        System.out.println("Supplier: " + (p.getSupplier() == null ? "none" : p.getSupplier()));
    }

    private static void conceptsUi() {
        System.out.println("1 = call-by-value, 2 = defensive copying, 3 = lambdas and method references");
        switch (ask("Choice")) {
            case "1" -> LanguageDemos.callByValue(inventory.find(p -> p.getQuantity() > 0).get(0));
            case "2" -> LanguageDemos.defensiveCopy(inventory);
            case "3" -> LanguageDemos.lambdas(inventory);
            default -> System.out.println("Unknown option.");
        }
    }

    // ---------- sample data ----------
    private static void seedData() throws ProductNotFoundException {
        LocalDate today = LocalDate.now();
        Supplier fresh = new Supplier("Fresh Farms Ltd", "orders@freshfarms.ie", List.of("Mon", "Thu"));
        Supplier tech = new Supplier("TechWholesale", "sales@techwholesale.ie", List.of("Wed"));

        Product[] start = {
            new PerishableProduct("Milk 2L", 1.89, 30, today.plusDays(2)),
            new PerishableProduct("Brown Bread", 2.40, 12, today.plusDays(1)),
            new PerishableProduct("Cheddar Block", 3.99, 25, today.plusDays(20)),
            new PerishableProduct("Yoghurt 4-pack", 2.20, 10, today.minusDays(1)),     // already expired
            new ElectronicProduct("USB-C Charger", 14.99, 4, 24),
            new ElectronicProduct("Bluetooth Headset", 39.00, 15, 12),
            new GeneralProduct("Cotton T-Shirt", Category.CLOTHING, 9.99, 40, 10),
            new GeneralProduct("Bin Bags (20)", Category.HOUSEHOLD, 3.50, 6, 8),
            new GeneralProduct("Ballpoint Pens", Category.STATIONERY, 0.99)            // 3-arg constructor: starts at 0
        };
        for (Product p : start) {
            inventory.addProduct(p);
        }
        for (Product p : inventory.find(Category.FOOD)) inventory.assignSupplier(p.getId(), fresh);
        for (Product p : inventory.find(Category.ELECTRONICS)) inventory.assignSupplier(p.getId(), tech);
    }
}
