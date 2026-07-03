package com.raj.wallet;

import com.raj.wallet.dto.response.WalletResponse;
import com.raj.wallet.exception.WalletNotFoundException;
import com.raj.wallet.service.WalletService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WalletServiceTest {

    private final WalletService walletService = new WalletService();

    @Test
    void shouldReturnWallet() {

        WalletResponse response = walletService.getWallet(1L);

        assertEquals(1L, response.getId());
        assertEquals("Raj", response.getOwner());

    }

    @Test
    void shouldThrowWalletNotFoundException() {

        assertThrows(WalletNotFoundException.class, () -> walletService.getWallet(-1L));

    }

}

