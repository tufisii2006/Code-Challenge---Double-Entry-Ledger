package ro.challenge.coremaker.entry;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ro.challenge.coremaker.account.AccountEntity;
import ro.challenge.coremaker.transaction.TransactionEntity;

import java.util.Objects;

@Entity
@Table(name = "entry")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "transaction")
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