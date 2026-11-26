package stockpro;

/**
 * Interface with all three kinds of "extra" methods:
 *  - default: implementers get it for free (and can override it)
 *  - private: a helper that only this interface can use
 *  - static:  belongs to the interface, called as Describable.divider()
 */
public interface Describable {

    /** The one abstract method every implementer has to write. */
    String summary();

    /** Default method, built on top of the abstract one. */
    default String describe() {
        return header() + summary();
    }

    /** Private interface method (Java 9+), hidden from implementing classes. */
    private String header() {
        return "[" + getClass().getSimpleName() + "] ";
    }

    /** Static interface method, no object needed. */
    static String divider() {
        return "-".repeat(70);
    }
}
