package stockpro;

/** Second enum, used with a switch expression. */
public enum StockStatus {
    OUT_OF_STOCK, LOW, OK;

    public static StockStatus of(Product p) {
        if (p.getQuantity() == 0) return OUT_OF_STOCK;
        return p.isLowStock() ? LOW : OK;
    }

    /** Switch expression over an enum: no default needed because every constant is covered. */
    public String label() {
        return switch (this) {
            case OUT_OF_STOCK -> "** OUT **";
            case LOW          -> "* LOW *";
            case OK           -> "OK";
        };
    }
}
