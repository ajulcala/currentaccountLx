package com.currentaccount.app.controllers;

import com.currentaccount.app.models.documents.CurrentAccount;
import com.currentaccount.app.services.CurrentAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Date;
@Slf4j
@RefreshScope
@RestController
@RequestMapping("/currentAccount")
public class CurrentAccountController {
    @Autowired
    CurrentAccountService service;

    @GetMapping("list")
    public Flux<CurrentAccount> findAll(){
        return service.findAll();
    }

    @GetMapping("/find/{id}")
    public Mono<CurrentAccount> findById(@PathVariable String id){
        return service.findById(id);
    }

    @GetMapping("/findAccountByCustomerId/{id}")
    public Flux<CurrentAccount> findCustomerAccountId(@PathVariable String id){
        return service.customerIdAccount(id);
    }

    @PostMapping("/create")
    public Mono<ResponseEntity<CurrentAccount>> create(@Valid @RequestBody CurrentAccount currentAccount){
        // VERIFICAMOS SI EXISTE EL CLIENTE
        log.info("entro en el metodo");
        return service.findCustomerByDocumentNumber(currentAccount.getCustomer().getDocumentNumber())
                .flatMap(cst -> {
                    return service.countCustomerAccountBankDocumentNumber(currentAccount.getCustomer().getDocumentNumber()) // Mono<Long> # Cuentas bancarias del Cliente
                            .filter(count -> {
                                log.info("comprobando cliente");
                                switch (cst.getTypeCustomer().getValue()){
                                    case PERSONAL:
                                        log.info("paso uno");
                                        return count < 1; // max 1 Cuenta por Cliente PERSONAL

                                    case EMPRESARIAL:
                                        log.info("paso dos");
                                        return currentAccount.getOwners() != null & currentAccount.getOwners().size() > 0; // Cliente EMPRESARIAL debe tener 1 o mas titulares

                                    default: return false;
                                }
                            })
                            .flatMap(c -> {
                                switch (cst.getTypeCustomer().getValue()){
                                    case EMPRESARIAL:
                                        switch (cst.getTypeCustomer().getSubType().getValue()){
                                            case PYME: return service.findCreditCardByCustomerId(cst.getId())
                                                    .count()
                                                    .filter(cntCCard -> cntCCard > 0)
                                                    .flatMap(cntCCard -> {
                                                        currentAccount.setCustomer(cst);
                                                        currentAccount.setCreateAt(LocalDateTime.now());
                                                        currentAccount.setCommissionMaintenance(0.0);
                                                        return service.create(currentAccount);
                                                    });
                                        }
                                    default:
                                        log.info("agregando cuenta");
                                        currentAccount.setCustomer(cst);
                                        currentAccount.setCreateAt(LocalDateTime.now());
                                        return service.create(currentAccount); // Mono<CurrentAccount>
                                }
                            });
                })
                .map(ca -> new ResponseEntity<>(ca, HttpStatus.CREATED))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @PutMapping("/update")
    public Mono<ResponseEntity<CurrentAccount>> update(@RequestBody CurrentAccount currentAccount) {
        return service.findCustomerById(currentAccount.getCustomer().getId())
                .filter(customer -> currentAccount.getBalance() >= 0)
                .flatMap(customer -> {
                    switch (customer.getTypeCustomer().getValue()){
                        case EMPRESARIAL:
                            switch (customer.getTypeCustomer().getSubType().getValue()){
                                case PYME: return service.findCreditCardByCustomerId(customer.getId())
                                        .count()
                                        .filter(cntCCard -> cntCCard > 0)
                                        .flatMap(cntCCard -> {
                                            currentAccount.setCustomer(customer);
                                            currentAccount.setCreateAt(LocalDateTime.now());
                                            currentAccount.setCommissionMaintenance(0.0);
                                            return service.create(currentAccount);
                                        });
                            }
                        default: currentAccount.setCustomer(customer);
                            currentAccount.setCreateAt(LocalDateTime.now());
                            return service.create(currentAccount); // Mono<CurrentAccount>
                    }
                })
                .map(ca -> new ResponseEntity<>(ca, HttpStatus.CREATED))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @DeleteMapping("/delete/{id}")
    public Mono<ResponseEntity<String>> delete(@PathVariable String id) {
        return service.delete(id)
                .filter(deleteCustomer -> deleteCustomer)
                .map(deleteCustomer -> new ResponseEntity<>("Account Deleted", HttpStatus.ACCEPTED))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/findByAccountNumber/{number}")
    public Mono<CurrentAccount> findByAccountNumber(@PathVariable String number){
        return service.findByCardNumber(number);
    }

    @PutMapping("/updateTransference")
    public Mono<ResponseEntity<CurrentAccount>> updateForTransference(@Valid @RequestBody CurrentAccount currentAccount) {
        return service.create(currentAccount)
                .filter(customer -> currentAccount.getBalance() >= 0)
                .map(ft -> new ResponseEntity<>(ft, HttpStatus.CREATED));
    }
}
