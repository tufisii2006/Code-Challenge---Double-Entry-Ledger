package ro.challenge.coremaker.entry;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import ro.challenge.coremaker.account.AccountEntity;
import ro.challenge.coremaker.transaction.TransactionEntity;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "entry")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "transaction")
@EntityListeners(AuditingEntityListener.class)
public class EntryEntity {

    public enum Direction {
        DEBIT, CREDIT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false, updatable = false)
    private TransactionEntity transaction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, updatable = false)
    private AccountEntity account;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, updatable = false)
    private Direction direction;

    // Minor units (cents, yen). Always positive; the sign comes from the direction.
    @Column(name = "amount", nullable = false, updatable = false)
    private long amount;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Builder
    private EntryEntity(AccountEntity account, Direction direction, long amount) {
        this.account = Objects.requireNonNull(account);
        this.direction = Objects.requireNonNull(direction);
        if (amount <= 0) {
            throw new IllegalArgumentException("Entry amount must be positive");
        }
        this.amount = amount;
    }

    public void attachTo(TransactionEntity transaction) {
        this.transaction = transaction;
    }
}