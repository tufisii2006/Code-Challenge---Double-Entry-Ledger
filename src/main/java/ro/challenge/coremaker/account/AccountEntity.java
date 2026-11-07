package ro.challenge.coremaker.account;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Builder
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@Table(name = "account")
@EntityListeners(AuditingEntityListener.class)
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency; // ISO 4217 code, FK to currency.code

    // Minor units (cents, yen). Null means no limit, used by the settlement accounts.
    @Column(name = "overdraft_limit")
    private Long overdraftLimit;

    // Minor units. Credit increases it, debit decreases it.
    @Column(name = "balance", nullable = false)
    private long balance;

    // Optimistic locking, a second line of defense next to row locks.
    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    protected AccountEntity() {
        // Required by JPA
    }

    public void credit(long amount) {
        balance += amount;
    }

    public void debit(long amount) {
        balance -= amount;
    }

    // The balance may go down to -overdraftLimit, never further.
    public boolean allowsBalance(long newBalance) {
        return overdraftLimit == null || newBalance >= -overdraftLimit;
    }

}
