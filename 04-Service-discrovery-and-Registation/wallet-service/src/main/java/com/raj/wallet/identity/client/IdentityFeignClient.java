package com.raj.wallet.identity.client;

import com.raj.wallet.identity.dto.UserSummaryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name="identity-service", url="${identity-service.base-url}")
public interface IdentityFeignClient {

    @GetMapping("/api/v1/users/{userId}")
    UserSummaryResponse getUser(@PathVariable UUID userId);

}