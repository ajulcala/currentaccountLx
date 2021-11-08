package com.currentaccount.app.services.impl;

import com.currentaccount.app.models.dao.CurrentAccountDao;
import com.currentaccount.app.models.documents.CurrentAccount;
import com.currentaccount.app.models.dto.CreditCard;
import com.currentaccount.app.models.dto.Customer;
import com.currentaccount.app.services.CurrentAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
@Slf4j
@Service
public class CurrentAccountServiceImpl implements CurrentAccountService {
    private final WebClient webClient;
    private final ReactiveCircuitBreaker reactiveCircuitBreaker;

    @Value("${config.base.apigatewey}")
    private String url;

    public CurrentAccountServiceImpl(ReactiveResilience4JCircuitBreakerFactory circuitBreakerFactory) {
        this.webClient = WebClient.builder().baseUrl(this.url).build();
        this.reactiveCircuitBreaker = circuitBreakerFactory.create("customerCredit");
    }

    @Autowired
    CurrentAccountDao dao;

    @Override
    public Mono<Customer> findCustomerByDocumentNumber(String number) {
       log.info("buscando cliente");
        return reactiveCircuitBreaker.run(webClient.get().uri(this.url + "/customer/customer/documentNumber/{number}",number).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(Customer.class),
                throwable -> { return this.getDefaultCustomer();});
    }

    @Override
    public Mono<Customer> findCustomerById(String id) {
        return reactiveCircuitBreaker.run(webClient.get().uri(this.url + "/customer/customer/find/{id}",id).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(Customer.class),
                throwable -> { return this.getDefaultCustomer();});
    }

    public Mono<Customer> getDefaultCustomer() {
        Mono<Customer> customer = Mono.just(new Customer());
        return customer;
    }

    @Override
    public Flux<CreditCard> findCreditCardByCustomerId(String id) {
        return reactiveCircuitBreaker.run(webClient.get().uri(this.url + "/creditcard/creditcard/find/{id}",id).accept(MediaType.APPLICATION_JSON).retrieve().bodyToFlux(CreditCard.class),
                throwable -> {
                    return this.getDefaultCreditCard();
                });
    }

    public Flux<CreditCard> getDefaultCreditCard() {
        Flux<CreditCard> creditCard = Flux.just(new CreditCard("0", null, null,null,null,null));
        return creditCard;
    }

    @Override
    public Mono<CurrentAccount> create(CurrentAccount t) {
        return dao.save(t);
    }

    @Override
    public Flux<CurrentAccount> findAll() {
        return dao.findAll();
    }

    @Override
    public Mono<CurrentAccount> findById(String id) {
        return dao.findById(id);
    }

    @Override
    public Mono<CurrentAccount> update(CurrentAccount t) {
        return dao.save(t);
    }

    @Override
    public Mono<Boolean> delete(String t) {
        return dao.findById(t)
                .flatMap(ca -> dao.delete(ca).then(Mono.just(Boolean.TRUE)))
                .defaultIfEmpty(Boolean.FALSE);
    }

    @Override
    public Mono<Long> countCustomerAccountBank(String id) {
        return dao.findByCustomerId(id).count();
    }

    @Override
    public Flux<CurrentAccount> customerIdAccount(String id) {
        return dao.findByCustomerId(id);
    }

    @Override
    public Mono<Long> countCustomerAccountBankDocumentNumber(String number) {
        return dao.findByCustomerDocumentNumber(number).count();
    }

    @Override
    public Mono<CurrentAccount> findByCardNumber(String number) {
        return dao.findByCardNumber(number);
    }
}
