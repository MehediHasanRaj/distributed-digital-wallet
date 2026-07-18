package com.raj.wallet.wallet.controller;

import com.raj.wallet.wallet.dto.request.CreateWalletRequest;
import com.raj.wallet.wallet.dto.request.DepositRequest;
import com.raj.wallet.wallet.dto.request.WithdrawRequest;
import com.raj.wallet.wallet.dto.response.WalletResponse;
import com.raj.wallet.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<WalletResponse> create(@Valid @RequestBody CreateWalletRequest request){

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(walletService.createWallet(request));

    }

    @PostMapping("/{walletId}/deposit")
    public WalletResponse deposit(@PathVariable String walletId,
                                  @Valid @RequestBody DepositRequest request
    ){

        return walletService.deposit(walletId, request);
    }

    @PostMapping("/{walletId}/withdraw")
    public WalletResponse withdraw(@PathVariable String walletId,
                                   @Valid @RequestBody WithdrawRequest request){

        return walletService.withdraw(walletId, request);

    }

}