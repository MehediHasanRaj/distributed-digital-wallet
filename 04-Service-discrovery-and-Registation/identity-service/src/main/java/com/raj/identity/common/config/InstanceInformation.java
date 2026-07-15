package com.raj.identity.common.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class InstanceInformation {

    @Value("${server.port}")
    private String port;
}