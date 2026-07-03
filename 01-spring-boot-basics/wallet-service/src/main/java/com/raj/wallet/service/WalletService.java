package com.raj.wallet.service;

import com.raj.wallet.dto.request.CreateWalletRequest;
import com.raj.wallet.dto.response.WalletResponse;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    public WalletResponse getWallet(Long id) {

        return new WalletResponse(id, "Raj", 2500.0);

    }

    public WalletResponse createWallet(CreateWalletRequest request) {

        return new WalletResponse(1L, request.getOwner(), request.getBalance());

    }

}