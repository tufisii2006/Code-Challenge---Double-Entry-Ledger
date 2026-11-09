package ro.challenge.coremaker.events.rabbitmq;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ro.challenge.coremaker.dto.CreateTransactionRequest;
import ro.challenge.coremaker.dto.EntryRequest;
import ro.challenge.coremaker.exceptions.LedgerRuleViolation;
import ro.challenge.coremaker.transaction.TransactionService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private static final Map<String, String> SETTLEMENT_ACCOUNTS = Map.of(
        "EUR", "00000000-0000-0000-0000-000000000001",
        "JPY", "00000000-0000-0000-0000-000000000002");

    private final TransactionService transactionService;

    public void settle(PaymentSettledMessage message) {
        String settlementAccountId = SETTLEMENT_ACCOUNTS.get(message.currency());
        if (settlementAccountId == null) {
            throw new LedgerRuleViolation("No settlement account for currency " + message.currency());
        }

        CreateTransactionRequest request = new CreateTransactionRequest()
            .description("Payment " + message.paymentId())
            .entries(List.of(
                new EntryRequest()
                    .accountId(UUID.fromString(settlementAccountId))
                    .direction(EntryRequest.DirectionEnum.DEBIT)
                    .amount(message.amount()),
                new EntryRequest()
                    .accountId(UUID.fromString(message.merchantAccountId()))
                    .direction(EntryRequest.DirectionEnum.CREDIT)
                    .amount(message.amount())));

        // Same path as the REST API. A repeated paymentId returns the existing transaction.
        transactionService.createTransaction("payment-settled:" + message.paymentId(), request);
    }
}