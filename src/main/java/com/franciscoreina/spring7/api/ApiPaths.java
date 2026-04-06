package com.franciscoreina.spring7.api;

public final class ApiPaths {

    private ApiPaths() {
    }

    public static final String API_V1 = "/api/v1";

    // CATEGORIES
    public static final String CATEGORIES = API_V1 + "/categories";

    // CUSTOMERS
    public static final String CUSTOMERS = API_V1 + "/customers";
    public static final String CUSTOMER_ID = "/{customerId}";

    // MILKS
    public static final String MILKS = API_V1 + "/milks";
    public static final String MILK_ID = "/{milkId}";

    // MILK ORDERS
    public static final String MILK_ORDERS = API_V1 + "/milk-orders";
    public static final String MILK_ORDER_ID = "/{milkOrderId}";

    // ORDER LINES
    public static final String ORDER_LINES = "/lines";
    public static final String ORDER_LINE_ID = "/{orderLineId}";
}
