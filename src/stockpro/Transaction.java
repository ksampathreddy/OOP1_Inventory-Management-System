package stockpro;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Sealed interface: only the three records below can implement it, so a
 * switch over a Transaction needs no default branch.
 */
public sealed interface Transaction permits Transaction.Sale, Transaction.Restock, Transaction.PriceChange {

    LocalDateTime timestamp();

    record Sale(LocalDateTime timestamp, String customer, List<SaleItem> items, double total)
            implements Transaction {
        public Sale {
            items = List.copyOf(items); // copy the list so the record cannot be changed from outside
        }
    }

    record Restock(LocalDateTime timestamp, int productId, int quantity) implements Transaction { }

    record PriceChange(LocalDateTime timestamp, int productId, double oldPrice, double newPrice)
            implements Transaction { }
}
