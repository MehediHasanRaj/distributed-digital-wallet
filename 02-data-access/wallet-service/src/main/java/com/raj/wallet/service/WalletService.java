package com.raj.wallet.service;

import com.raj.wallet.dto.request.CreateWalletRequest;
import com.raj.wallet.dto.response.WalletResponse;
import com.raj.wallet.exception.WalletNotFoundException;
import com.raj.wallet.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    //logger added, why final, as we only need one instance per class
    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);


    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    
    public WalletResponse getWallet(Long id) {
        if (id <= 0) {
            logger.warn("Invalid wallet id received: {}", id);
            throw new WalletNotFoundException(
                    "Wallet with id " + id + " not found"
            );
        }
        logger.info("Wallet {} fetched successfully", id);
        return new WalletResponse(id, "Raj", 5000.00);

    }

    public WalletResponse createWallet(CreateWalletRequest request) {

        return new WalletResponse(1L, request.getOwner(), request.getBalance());

    }

}