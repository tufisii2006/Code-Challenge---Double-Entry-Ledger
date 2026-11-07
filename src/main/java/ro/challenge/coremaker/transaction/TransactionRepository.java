package ro.challenge.coremaker.transaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<TransactionEntity, String> {

    // Idempotency: the same request key returns the transaction that was recorded first
    Optional<TransactionEntity> findByIdempotencyKey(String idempotencyKey);

    // A transaction can be reversed only once
    Optional<TransactionEntity> findByReversesId(String transactionId);
}
