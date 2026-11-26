package stockpro;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Food with a use-by date.
 * Rules I picked: within 3 days of expiry the price drops 30%.
 * Once it is expired it cannot be sold (Inventory checks this) and it counts as 0 in the stock value.
 */
public final class PerishableProduct extends Product {
    public static final int MARKDOWN_DAYS = 3;

    private final LocalDate expiryDate;     // LocalDate is immutable, so no copy needed

    public PerishableProduct(String name, double price, int qty, LocalDate expiryDate) {
        super(name, Category.FOOD, price, qty, Category.FOOD.getDefaultReorderLevel());
        if (expiryDate == null) throw new InvalidProductException("Expiry date is required");
        this.expiryDate = expiryDate;
    }

    public LocalDate getExpiryDate() { return expiryDate; }

    public long daysUntilExpiry(LocalDate today) {
        return ChronoUnit.DAYS.between(today, expiryDate);
    }

    public boolean isExpired(LocalDate today) {
        return daysUntilExpiry(today) < 0;
    }

    @Override
    public double sellingPrice() {
        long days = daysUntilExpiry(LocalDate.now());
        return (days >= 0 && days <= MARKDOWN_DAYS) ? getUnitPrice() * 0.70 : getUnitPrice();
    }

    @Override
    public double stockValue() {
        return isExpired(LocalDate.now()) ? 0.0 : super.stockValue();   // super. = the parent's version
    }

    @Override
    public String summary() {
        return super.summary() + " expires=" + expiryDate;
    }
}
