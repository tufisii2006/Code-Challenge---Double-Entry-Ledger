package ro.challenge.coremaker.transaction;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ro.challenge.coremaker.api.TransactionsApi;
import ro.challenge.coremaker.dto.CreateTransactionRequest;
import ro.challenge.coremaker.dto.Transaction;

import java.util.UUID;

@RestController
public class TransactionController implements TransactionsApi {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public ResponseEntity<Transaction> createTransaction(String idempotencyKey, CreateTransactionRequest createTransactionRequest) {
        return ResponseEntity.ok(transactionService.createTransaction(idempotencyKey, createTransactionRequest));
    }

    @Override
    public ResponseEntity<Transaction> getTransaction(UUID transactionId) {
        return null;
    }

    @Override
    public ResponseEntity<Transaction> reverseTransaction(UUID transactionId, String idempotencyKey) {
        return ResponseEntity.ok(transactionService.reverseTransaction(transactionId.toString(), idempotencyKey));
    }
}
