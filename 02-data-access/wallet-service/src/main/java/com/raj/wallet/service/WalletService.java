package com.raj.wallet.service;

import com.raj.wallet.dto.request.CreateWalletRequest;
import com.raj.wallet.dto.response.WalletResponse;
import com.raj.wallet.entity.WalletEntity;
import com.raj.wallet.exception.WalletNotFoundException;
import com.raj.wallet.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    //logger added, why final, as we only need one instance per class
    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);


    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    // create Wallet
    public WalletResponse createWallet(CreateWalletRequest request) {

        WalletEntity entity = mapToEntity(request);

        WalletEntity saved = walletRepository.save(entity);

        return mapToResponse(saved);

    }

//    find/get wallet
    public WalletResponse getWallet(Long id) {

        WalletEntity entity = walletRepository.findById(id).orElseThrow(() ->
                                        new WalletNotFoundException("Wallet not found"));

        return mapToResponse(entity);

    }

//    Find all wallet
    public List<WalletResponse> getAllWallets() {
        return walletRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

//    Update Wallet
    public WalletResponse updateWallet(Long id, CreateWalletRequest request) {
        WalletEntity entity = walletRepository.findById(id).orElseThrow(() -> new WalletNotFoundException("Wallet not found"));

        entity.setOwner(request.getOwner());
        entity.setBalance(request.getBalance());

        WalletEntity updated = walletRepository.save(entity);
        return mapToResponse(updated);
    }

//    delete wallet
    public void deleteWallet(Long id) {
        WalletEntity entity = walletRepository.findById(id)
                        .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));

        walletRepository.delete(entity);

    }

    //find by pageable
    public Page<WalletResponse> getWallets(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return walletRepository.findAll(pageable).map(this::mapToResponse);
    }


    private WalletEntity mapToEntity(CreateWalletRequest request) {

        WalletEntity entity = new WalletEntity();
        entity.setOwner(request.getOwner());
        entity.setBalance(request.getBalance());

        return entity;
    }

    private WalletResponse mapToResponse(WalletEntity entity) {

        return new WalletResponse(entity.getId(), entity.getOwner(), entity.getBalance());

    }

}