package stockpro;

/** Enum with fields, a constructor and a method. */
public enum Category {
    FOOD("Food & Drink", 20),
    ELECTRONICS("Electronics", 5),
    CLOTHING("Clothing", 10),
    HOUSEHOLD("Household", 8),
    STATIONERY("Stationery", 15);

    private final String displayName;
    private final int defaultReorderLevel;

    Category(String displayName, int defaultReorderLevel) {
        this.displayName = displayName;
        this.defaultReorderLevel = defaultReorderLevel;
    }

    public String getDisplayName() { return displayName; }
    public int getDefaultReorderLevel() { return defaultReorderLevel; }

    @Override
    public String toString() { return displayName; }
}
