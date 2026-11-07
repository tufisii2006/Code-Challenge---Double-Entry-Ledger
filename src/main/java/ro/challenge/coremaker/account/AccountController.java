package ro.challenge.coremaker.account;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ro.challenge.coremaker.api.AccountsApi;
import ro.challenge.coremaker.dto.Account;
import ro.challenge.coremaker.dto.CreateAccountRequest;

import java.util.UUID;

@RestController
public class AccountController implements AccountsApi {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public ResponseEntity<Account> createAccount(CreateAccountRequest createAccountRequest) {
        return ResponseEntity.ok(accountService.createAccount(createAccountRequest));
    }

    @Override
    public ResponseEntity<Account> getAccount(UUID accountId) {
        return  ResponseEntity.ok(accountService.retrieveAccount(accountId));
    }

}
