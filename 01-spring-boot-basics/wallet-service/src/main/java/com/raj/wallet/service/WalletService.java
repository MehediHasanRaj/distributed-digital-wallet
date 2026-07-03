package com.raj.wallet.service;

import com.raj.wallet.dto.request.CreateWalletRequest;
import com.raj.wallet.dto.response.WalletResponse;
import com.raj.wallet.exception.WalletNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    public WalletResponse getWallet(Long id) {
        if (id <= 0) {

            throw new WalletNotFoundException(
                    "Wallet with id " + id + " not found"
            );
        }
        return new WalletResponse(id, "Raj", 5000.00);

    }

    public WalletResponse createWallet(CreateWalletRequest request) {

        return new WalletResponse(1L, request.getOwner(), request.getBalance());

    }

}