package ro.challenge.coremaker.account;

import org.springframework.stereotype.Service;
import ro.challenge.coremaker.dto.Account;
import ro.challenge.coremaker.dto.CreateAccountRequest;

import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account createAccount(CreateAccountRequest createAccountRequest) {
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
        //Should add validations later
        var accountEntity = accountRepository.findById(String.valueOf(accountId)).get();
        return new Account().balance(accountEntity.getBalance())
            .currency(accountEntity.getCurrency())
            .version(accountEntity.getVersion())
            .overdraftLimit(accountEntity.getOverdraftLimit())
            .createdAt(accountEntity.getCreatedAt().toString())
            .id(UUID.fromString(accountEntity.getId()));
    }
}
