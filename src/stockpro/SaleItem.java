package stockpro;

/** A record (one line of a sale). The compact constructor checks the quantity. */
public record SaleItem(int productId, int quantity) {
    public SaleItem {
        if (quantity <= 0) throw new InvalidProductException("Sale quantity must be positive");
    }
}
