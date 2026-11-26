package stockpro;

/** Unchecked exception (extends RuntimeException): used for bad arguments. */
public class InvalidProductException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public InvalidProductException(String message) {
        super(message);
    }
}
