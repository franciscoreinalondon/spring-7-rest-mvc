package com.franciscoreina.spring7.domain.customer;

import com.franciscoreina.spring7.domain.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import static com.franciscoreina.spring7.domain.base.DomainAssert.notBlank;

@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For Hibernate
@AllArgsConstructor(access = AccessLevel.PRIVATE) // For Builder
@Getter
@Setter(AccessLevel.NONE) // Defensive programming
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "customer")
public class Customer extends BaseEntity {

    // Business Attributes

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String name;

    @NotBlank
    @Email
    @Size(max = 120)
    @Column(nullable = false, length = 120, unique = true)
    private String email;

    // Factory Method

    public static Customer createCustomer(String name, String email) {
        notBlank(name, "Name is required");
        notBlank(email, "Email is required");

        return Customer.builder()
                .name(name.trim())
                .email(normalizeEmail(email))
                .build();
    }

    // Business Methods (Rich Model)

    public void renameTo(String newName) {
        notBlank(newName, "Name cannot be empty");
        this.name = newName.trim();
    }

    public void changeEmailTo(String newEmail) {
        notBlank(newEmail, "Email cannot be empty");
        this.email = normalizeEmail(newEmail);
    }

    // Utilities

    private static String normalizeEmail(String email) {
        return email.toLowerCase().trim();
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
