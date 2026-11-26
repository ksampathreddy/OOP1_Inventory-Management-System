/**
 * EXTRA MARKS: Java 27, JEP 532 "Primitive Types in Patterns, instanceof, and switch" (fifth preview).
 *
 * This file is separate from the main app on purpose. The main app builds on plain Java 21+,
 * but this needs JDK 27 with preview features turned on:
 *
 *     javac --release 27 --enable-preview PrimitivePatternsDemo.java
 *     java  --enable-preview PrimitivePatternsDemo
 *
 * It stands on its own, so it does not need the stockpro classes.
 */
public class PrimitivePatternsDemo {

    // A reading from a stock-take scanner. units is an int, but small counts are the norm.
    record StockReading(String sku, int units) { }

    // 1) instanceof with a primitive type pattern.
    //    "units instanceof byte b" is true only when the int fits in a byte WITHOUT losing anything.
    //    Before this feature I would have written: if (units >= -128 && units <= 127) { byte b = (byte) units; ... }
    static String shelfLabel(int units) {
        if (units instanceof byte small) {
            return "small shelf label, byte value " + small;
        }
        return "pallet label, value " + units + " does not fit in a byte";
    }

    // 2) switch on a boolean (not allowed before).
    static String reorderMessage(boolean lowStock) {
        return switch (lowStock) {
            case true  -> "Reorder now";
            case false -> "Stock is fine";
        };
    }

    // 3) switch on a long, with constant labels.
    static String batchName(long units) {
        return switch (units) {
            case 0L   -> "empty";
            case 1L   -> "single item";
            case 100L -> "standard case of 100";
            default   -> "custom batch of " + units;
        };
    }

    // 4) primitive patterns with guards, on an Object selector (the Integer is matched as an int).
    static String describeCount(Object count) {
        return switch (count) {
            case int i when i < 0  -> "negative count, something is wrong";
            case int i when i == 0 -> "none left";
            case int i             -> i + " in stock";
            case double d          -> "weighed stock: " + d + " kg";
            default                -> "unknown type " + count.getClass().getSimpleName();
        };
    }

    // 5) primitive pattern INSIDE a record pattern: matches only if units fits in a byte.
    static String readingSize(StockReading r) {
        return switch (r) {
            case StockReading(String sku, byte small) -> sku + ": small count (" + small + ")";
            case StockReading(String sku, int big)    -> sku + ": large count (" + big + ")";
        };
    }

    public static void main(String[] args) {
        System.out.println(shelfLabel(42));
        System.out.println(shelfLabel(5000));
        System.out.println(reorderMessage(true));
        System.out.println(batchName(100L));
        System.out.println(batchName(37L));
        System.out.println(describeCount(0));
        System.out.println(describeCount(250));
        System.out.println(describeCount(-3));
        System.out.println(describeCount(12.5));
        System.out.println(readingSize(new StockReading("MILK-2L", 30)));
        System.out.println(readingSize(new StockReading("PENS", 1200)));
    }
}
