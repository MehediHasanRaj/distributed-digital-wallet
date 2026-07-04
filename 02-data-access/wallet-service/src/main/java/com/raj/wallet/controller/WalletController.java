package com.raj.wallet.controller;

import com.raj.wallet.dto.request.CreateWalletRequest;
import com.raj.wallet.dto.response.WalletResponse;
import com.raj.wallet.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    //Create Wallet Controller
    @PostMapping
    public ResponseEntity<WalletResponse>  create(@Valid @RequestBody CreateWalletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(walletService.createWallet(request));

    }

    //find wallet
    @GetMapping("/{id}")
    public ResponseEntity<WalletResponse> find(@PathVariable Long id) {
        System.out.println("Getting wallet with id " + id);

        return ResponseEntity.ok(walletService.getWallet(id));

    }

    //find all wallet
    @GetMapping
    public ResponseEntity<List<WalletResponse>> findAll() {
        return  ResponseEntity.ok(walletService.getAllWallets());
    }

//    Update Wallet
    public ResponseEntity<WalletResponse> update(@PathVariable Long id,@Valid @RequestBody CreateWalletRequest request) {
        return ResponseEntity.ok(walletService.updateWallet(id,request));
    }

//    Delete Wallet
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        walletService.deleteWallet(id);
        return ResponseEntity.noContent().build();

    }


}