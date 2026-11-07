package ro.challenge.coremaker.account;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.InvalidRequestException;
import org.springframework.stereotype.Service;
import ro.challenge.coremaker.dto.Account;
import ro.challenge.coremaker.dto.CreateAccountRequest;

import java.util.UUID;

@Service
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account createAccount(CreateAccountRequest createAccountRequest) {
        log.info("Creating account: {}", createAccountRequest);
        CurrencyValidator.validate(createAccountRequest.getCurrency());
        var accountEntity = AccountEntity.builder()
            .currency(createAccountRequest.getCurrency())
            .balance(0)
            .overdraftLimit(createAccountRequest.getOverdraftLimit())
            .build();
        accountRepository.save(accountEntity);
        return new Account().balance(accountEntity.getBalance())
            .currency(accountEntity.getCurrency())
            .version(accountEntity.getVersion())
            .overdraftLimit(accountEntity.getOverdraftLimit())
            .createdAt(accountEntity.getCreatedAt().toString())
            .id(UUID.fromString(accountEntity.getId()));
    }

    public Account retrieveAccount(UUID accountId) {
        log.info("Retrieving account: {}", accountId);
        var accountEntity = accountRepository.findById(String.valueOf(accountId))
            .orElseThrow(() -> new InvalidRequestException("Cannot find account with id: " + accountId));
        return new Account().balance(accountEntity.getBalance())
            .currency(accountEntity.getCurrency())
            .version(accountEntity.getVersion())
            .overdraftLimit(accountEntity.getOverdraftLimit())
            .createdAt(accountEntity.getCreatedAt().toString())
            .id(UUID.fromString(accountEntity.getId()));
    }

}
