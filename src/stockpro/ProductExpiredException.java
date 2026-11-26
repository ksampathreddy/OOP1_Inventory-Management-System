package stockpro;

import java.time.LocalDate;

/** Checked exception: thrown when someone tries to sell perishable stock that is past its date. */
public class ProductExpiredException extends Exception {
    private static final long serialVersionUID = 1L;

    public ProductExpiredException(String productName, LocalDate expiry) {
        super("'" + productName + "' expired on " + expiry + " and cannot be sold");
    }
}
