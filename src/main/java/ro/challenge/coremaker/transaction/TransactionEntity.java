package ro.challenge.coremaker.transaction;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
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
@EntityListeners(AuditingEntityListener.class)
public class TransactionEntity {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.UUID)
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
    @CreationTimestamp
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
        this.description = Objects.requireNonNull(description);
        this.reverses = reverses;
        this.idempotencyKey = Objects.requireNonNull(idempotencyKey);
        if (entries.size() < 2) {
            throw new IllegalArgumentException("A transaction needs at least two entries");
        }
        this.entries = entries;
        this.entries.forEach(entry -> entry.attachTo(this));
    }
}
