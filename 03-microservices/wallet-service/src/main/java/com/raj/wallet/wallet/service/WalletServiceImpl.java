package com.raj.wallet.wallet.service;

import com.raj.wallet.wallet.dto.request.CreateWalletRequest;
import com.raj.wallet.wallet.dto.request.DepositRequest;
import com.raj.wallet.wallet.dto.request.WithdrawRequest;
import com.raj.wallet.wallet.dto.response.WalletResponse;
import com.raj.wallet.wallet.entity.Wallet;
import com.raj.wallet.wallet.enums.WalletStatus;
import com.raj.wallet.wallet.exception.InsufficientBalanceException;
import com.raj.wallet.wallet.exception.WalletFrozenException;
import com.raj.wallet.wallet.exception.WalletNotFoundException;
import com.raj.wallet.wallet.mapper.WalletMapper;
import com.raj.wallet.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletMapper walletMapper;

    @Override
    @Transactional
    public WalletResponse createWallet(CreateWalletRequest request) {

        log.info("Creating wallet for user {}", request.userId());

        Wallet wallet = new Wallet();
        wallet.setUserId(request.userId());
        wallet.setCurrency(request.currency());

        Wallet saved = walletRepository.save(wallet);

        log.info("Wallet {} created successfully", saved.getWalletId());

        return walletMapper.toResponse(saved);
    }

    @Override
    public WalletResponse getWallet(String walletId) {

        Wallet wallet = findWallet(walletId);

        return walletMapper.toResponse(wallet);
    }

    @Override
    @Transactional
    public WalletResponse deposit(String walletId,
                                  DepositRequest request) {

        Wallet wallet = findWallet(walletId);

        validateActive(wallet);

        wallet.setBalance(
                wallet.getBalance().add(request.amount())
        );

        log.info("Deposited {} into wallet {}",
                request.amount(),
                walletId);

        return walletMapper.toResponse(wallet);
    }

    @Override
    @Transactional
    public WalletResponse withdraw(String walletId,
                                   WithdrawRequest request) {

        Wallet wallet = findWallet(walletId);

        validateActive(wallet);

        validateBalance(wallet, request.amount());

        wallet.setBalance(
                wallet.getBalance().subtract(request.amount())
        );

        log.info("Withdraw {} from wallet {}",
                request.amount(),
                walletId);

        return walletMapper.toResponse(wallet);
    }

    @Override
    @Transactional
    public void freezeWallet(String walletId) {

        Wallet wallet = findWallet(walletId);

        wallet.setStatus(WalletStatus.FROZEN);

    }

    @Override
    @Transactional
    public void unfreezeWallet(String walletId) {

        Wallet wallet = findWallet(walletId);

        wallet.setStatus(WalletStatus.ACTIVE);

    }

    private Wallet findWallet(String walletId) {

        return walletRepository.findByWalletId(UUID.fromString(walletId))
                .orElseThrow(() -> new WalletNotFoundException(walletId));
    }

    private void validateActive(Wallet wallet) {

        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new WalletFrozenException();
        }

    }

    private void validateBalance(Wallet wallet,
                                 BigDecimal amount) {

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }

    }

}