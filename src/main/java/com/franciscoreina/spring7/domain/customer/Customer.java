package com.franciscoreina.spring7.domain.customer;

import com.franciscoreina.spring7.domain.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Locale;

import static com.franciscoreina.spring7.domain.base.DomainAssert.notBlank;

@NoArgsConstructor(access = AccessLevel.PROTECTED) // For Hibernate
@AllArgsConstructor(access = AccessLevel.PRIVATE) // For Factory
@Getter
@Setter(AccessLevel.NONE) // Defensive programming
@Entity
@Table(name = "customers")
public class Customer extends BaseEntity {

    // Business Attributes

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String name;

    @NotBlank
    @Email(message = "Must be a well-formed email address")
    @Size(max = 120)
    @Column(nullable = false, length = 120, unique = true)
    private String email;

    // Factory Method

    public static Customer createCustomer(String name, String email) {
        var normalizedName = normalizeName(name);
        var normalizedEmail = normalizeEmail(email);

        return new Customer(normalizedName, normalizedEmail);
    }

    // Business Methods (Rich Model)

    public void renameTo(String newName) {
        this.name = normalizeName(newName);
    }

    public void changeEmailTo(String newEmail) {
        this.email = normalizeEmail(newEmail);
    }

    // Utilities

    private static String normalizeName(String name) {
        notBlank(name, "Name is required");
        return name.trim();
    }

    private static String normalizeEmail(String email) {
        notBlank(email, "Email is required");
        var normalized = email.trim().toLowerCase(Locale.ROOT);

        if (!normalized.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Must be a valid email address");
        }

        return normalized;
    }

    // Equals / HashCode

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer that)) return false;
        return super.getId() != null && super.getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
