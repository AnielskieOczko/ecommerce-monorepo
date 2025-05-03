package com.rj.payment_service.type;

import lombok.Getter;

/**
 * Enum representing supported currencies.
 * Each currency has a unique currency code.
 */
@Getter
public enum Currency {
    PLN("PLN"),
    USD("USD"),
    EUR("EUR");

    private final String currencyCode;

    Currency(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    /**
     * Converts a currency code string to the corresponding Currency enum.
     * @param currencyCode the currency code to convert (case insensitive)
     * @return the matching Currency enum value
     * @throws IllegalArgumentException if the currency code is not supported
     */
    public static Currency fromCode(String currencyCode) {
        if (currencyCode == null || currencyCode.isEmpty()) {
            throw new IllegalArgumentException("Currency code cannot be null or empty");
        }

        for (Currency currency: values()) {
            if(currency.getCurrencyCode().equals(currencyCode)) {
                return currency;
            }
        }
        throw new IllegalArgumentException("Unsupported currency code: " + currencyCode);
    }

}
