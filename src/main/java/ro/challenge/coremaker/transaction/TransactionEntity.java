package ro.challenge.coremaker.transaction;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.ToString;
import ro.challenge.coremaker.entry.EntryEntity;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "transaction")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "entries")
public class TransactionEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "id", length = 36, updatable = false)
    private String id;

    @Column(name = "description", nullable = false, length = 255, updatable = false)
    private String description;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reverses_id", unique = true, updatable = false)
    private TransactionEntity reverses;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 255, updatable = false)
    private String idempotencyKey;

    @Column(name = "recorded_at", nullable = false, updatable = false)
    private Instant recordedAt;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.PERSIST)
    private List<EntryEntity> entries;

    @Builder
    private TransactionEntity(String id,
                              String description,
                              TransactionEntity reverses,
                              String idempotencyKey,
                              Instant recordedAt,
                              @Singular List<EntryEntity> entries) {
        this.id = Objects.requireNonNull(id);
        this.description = Objects.requireNonNull(description);
        this.reverses = reverses;
        this.idempotencyKey = Objects.requireNonNull(idempotencyKey);
        this.recordedAt = Objects.requireNonNull(recordedAt);
        if (entries.size() < 2) {
            throw new IllegalArgumentException("A transaction needs at least two entries");
        }
        this.entries = entries;
        this.entries.forEach(entry -> entry.attachTo(this));
    }
}
