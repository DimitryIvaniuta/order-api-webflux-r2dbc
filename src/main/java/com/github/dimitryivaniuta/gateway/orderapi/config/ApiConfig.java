package com.github.dimitryivaniuta.gateway.orderapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@Configuration
@EnableR2dbcRepositories(basePackages = "com.github.dimitryivaniuta.gateway.orderapi.domain")
public class ApiConfig {
}
