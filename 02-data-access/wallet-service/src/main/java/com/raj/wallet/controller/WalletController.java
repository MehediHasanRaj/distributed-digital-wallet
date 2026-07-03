package com.raj.wallet.controller;

import com.raj.wallet.dto.request.CreateWalletRequest;
import com.raj.wallet.dto.response.WalletResponse;
import com.raj.wallet.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable Long id) {
        System.out.println("Getting wallet with id " + id);

        return ResponseEntity.ok(walletService.getWallet(id));

    }

    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(@Valid @RequestBody CreateWalletRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(walletService.createWallet(request));

    }

}