package com.raj.wallet.wallet.mapper;

import com.raj.wallet.wallet.dto.response.WalletResponse;
import com.raj.wallet.wallet.entity.Wallet;
import org.springframework.stereotype.Component;

@Component
public class WalletMapper {

    public WalletResponse toResponse(Wallet wallet){

        return new WalletResponse(
                wallet.getWalletId(),
                wallet.getUserId(),
                wallet.getBalance(),
                wallet.getCurrency(),
                wallet.getStatus());

    }

}