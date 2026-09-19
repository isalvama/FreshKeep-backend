package com.isalvama.fresh_keep.modules.product.domain.model.value_object;

import com.isalvama.fresh_keep.modules.product.domain.exception.InvalidCurrencyException;

import java.util.Arrays;
import java.util.List;

public enum Currency {
    USD("United States Dollar", "$"),
    EUR("Euro", "€"),
    GBP("British Pound", "£"),
    JPY("Japanese Yen", "¥"),
    CHF("Swiss Franc", "CHf"),
    CAD("Canadian Dollar", "C$"),
    AUD("Australian Dollar", "A$"),
    NZD("New Zealand Dollar", "NZ$"),

    SEK("Swedish Krona", "kr"),
    NOK("Norwegian Krone", "kr"),
    DKK("Danish Krone", "kr"),
    PLN("Polish Zloty", "zł"),
    CZK("Czech Koruna", "Kč"),
    HUF("Hungarian Forint", "Ft"),
    RON("Romanian Leu", "lei"),
    BGN("Bulgarian Lev", "лв"),

    MXN("Mexican Peso", "$"),
    BRL("Brazilian Real", "R$"),
    ARS("Argentine Peso", "$"),
    CLP("Chilean Peso", "$"),
    COP("Colombian Peso", "$"),
    PEN("Peruvian Sol", "S/"),
    UYU("Uruguayan Peso", "$U"),

    CNY("Chinese Yuan", "¥"),
    HKD("Hong Kong Dollar", "HK$"),
    SGD("Singapore Dollar", "S$"),
    INR("Indian Rupee", "₹"),
    KRW("South Korean Won", "₩"),
    THB("Thai Baht", "฿"),
    IDR("Indonesian Rupiah", "Rp"),
    MYR("Malaysian Ringgit", "RM"),
    PHP("Philippine Peso", "₱"),
    VND("Vietnamese Dong", "₫"),

    AED("UAE Dirham", "د.إ"),
    SAR("Saudi Riyal", "ر.س"),
    ILS("Israeli New Shekel", "₪"),
    TRY("Turkish Lira", "₺"),
    ZAR("South African Rand", "R"),
    EGP("Egyptian Pound", "E£"),
    NGN("Nigerian Naira", "₦"),

    UAH("Ukrainian Hryvnia", "₴"),
    RUB("Russian Ruble", "₽");

    private final String displayName;
    private final String symbol;

    Currency(String displayName, String symbol) {
        this.displayName = displayName;
        this.symbol = symbol;
    }

    public String getDisplayName() { return displayName; }
    public String getSymbol() { return symbol; }

    public static Currency getValueOf(String value){
        try {
            return Currency.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidCurrencyException("Value " + value + " does not match any currency constant name.");
        }
    }

    public static List<String> listValuesNames(){
        return Arrays.stream(values()).map(Enum::name).toList();
    }
}
