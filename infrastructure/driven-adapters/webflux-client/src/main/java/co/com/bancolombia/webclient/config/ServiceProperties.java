package co.com.bancolombia.webclient.config;

public record ServiceProperties(
    String baseUrl,
    Integer connectTimeout,
    Integer responseTimeout
) {}
