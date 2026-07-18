package com.raj.wallet.wallet.service;

import com.raj.wallet.wallet.dto.request.CreateWalletRequest;
import com.raj.wallet.wallet.dto.request.DepositRequest;
import com.raj.wallet.wallet.dto.request.WithdrawRequest;
import com.raj.wallet.wallet.dto.response.WalletResponse;

public interface WalletService {

    WalletResponse createWallet(CreateWalletRequest request);

    WalletResponse getWallet(String walletId);

    WalletResponse deposit(String walletId, DepositRequest request);

    WalletResponse withdraw(String walletId, WithdrawRequest request);

    void freezeWallet(String walletId);

    void unfreezeWallet(String walletId);

}