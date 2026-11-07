package ro.challenge.coremaker.transaction;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ro.challenge.coremaker.api.TransactionsApi;
import ro.challenge.coremaker.dto.CreateTransactionRequest;
import ro.challenge.coremaker.dto.ReverseTransactionRequest;
import ro.challenge.coremaker.dto.Transaction;

import java.util.UUID;

@RestController
public class TransactionController implements TransactionsApi {

    @Override
    public ResponseEntity<Transaction> createTransaction(String idempotencyKey, CreateTransactionRequest createTransactionRequest) {
        return null;
    }

    @Override
    public ResponseEntity<Transaction> getTransaction(UUID transactionId) {
        return null;
    }

    @Override
    public ResponseEntity<Transaction> reverseTransaction(UUID transactionId, String idempotencyKey,
                                                          ReverseTransactionRequest reverseTransactionRequest) {
        return null;
    }
}
