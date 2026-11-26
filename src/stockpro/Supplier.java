package stockpro;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * My custom immutable type (a normal class, not a record).
 * What makes it immutable: final class, private final fields, no setters, and
 * defensive copies of the mutable List on the way in and on the way out.
 * A "change" gives back a brand new object (see withEmail).
 */
public final class Supplier {
    private final String name;
    private final String email;
    private final List<String> deliveryDays;

    public Supplier(String name, String email, List<String> deliveryDays) {
        if (name == null || name.isBlank()) throw new InvalidProductException("Supplier name required");
        this.name = name.trim();
        this.email = email;
        this.deliveryDays = new ArrayList<>(deliveryDays);          // copy on the way in
    }

    public String getName() { return name; }
    public String getEmail() { return email; }

    public List<String> getDeliveryDays() {
        return Collections.unmodifiableList(new ArrayList<>(deliveryDays)); // copy on the way out
    }

    public Supplier withEmail(String newEmail) {
        return new Supplier(name, newEmail, deliveryDays);
    }

    @Override
    public String toString() {
        return name + " <" + email + "> delivers " + deliveryDays;
    }
}
