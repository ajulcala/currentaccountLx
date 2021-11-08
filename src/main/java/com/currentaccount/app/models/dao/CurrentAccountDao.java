package com.currentaccount.app.models.dao;

import com.currentaccount.app.models.documents.CurrentAccount;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CurrentAccountDao extends ReactiveMongoRepository<CurrentAccount, String> {
    public Flux<CurrentAccount> findByCustomerId(String id);
    public Flux<CurrentAccount> findByCustomerDocumentNumber(String number);
    Mono<CurrentAccount> findByCardNumber(String number);
}
