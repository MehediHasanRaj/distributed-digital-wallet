package com.raj.wallet;

import com.raj.wallet.wallet.dto.request.WithdrawRequest;
import com.raj.wallet.wallet.entity.Wallet;
import com.raj.wallet.wallet.enums.WalletStatus;
import com.raj.wallet.wallet.exception.InsufficientBalanceException;
import com.raj.wallet.wallet.mapper.WalletMapper;
import com.raj.wallet.wallet.repository.WalletRepository;
import com.raj.wallet.wallet.service.WalletServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {
//
//    @Mock
//    private WalletRepository walletRepository;
//
//    @Mock
//    private WalletMapper walletMapper;
//
//    @InjectMocks
//    private WalletServiceImpl walletService;
//
//    @Test
//    void shouldThrowExceptionWhenBalanceIsInsufficient() {
//
//        Wallet wallet = new Wallet();
//        wallet.setBalance(new BigDecimal("100"));
//        wallet.setStatus(WalletStatus.ACTIVE);
//
//        when(walletRepository.findByWalletId(any()))
//                .thenReturn(Optional.of(wallet));
//
//        WithdrawRequest request =
//                new WithdrawRequest(new BigDecimal("200"));
//
//        assertThrows(
//                InsufficientBalanceException.class,
//                () -> walletService.withdraw(
//                        UUID.randomUUID().toString(),
//                        request
//                )
//        );

//    }

}