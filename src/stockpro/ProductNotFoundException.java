package stockpro;

/** Another checked exception. */
public class ProductNotFoundException extends Exception {
    private static final long serialVersionUID = 1L;
    public ProductNotFoundException(int id) {
        super("No product found with id " + id);
    }
}
