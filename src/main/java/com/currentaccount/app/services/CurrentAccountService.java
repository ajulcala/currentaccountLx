package com.currentaccount.app.services;

import com.currentaccount.app.models.documents.CurrentAccount;
import com.currentaccount.app.models.dto.CreditCard;
import com.currentaccount.app.models.dto.Customer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CurrentAccountService {
    Mono<CurrentAccount> create(CurrentAccount t);
    Flux<CurrentAccount> findAll();
    Mono<CurrentAccount> findById(String id);
    Mono<CurrentAccount> update(CurrentAccount t);
    Mono<Boolean> delete(String t);
    Mono<Long> countCustomerAccountBank(String id);
    Flux<CurrentAccount>customerIdAccount (String id);
    Mono<Long> countCustomerAccountBankDocumentNumber(String number);
    Mono<Customer> findCustomerByDocumentNumber(String number);
    Mono<Customer> findCustomerById(String id);
    Flux<CreditCard> findCreditCardByCustomerId(String id);
    Mono<CurrentAccount> findByCardNumber(String number);
}
