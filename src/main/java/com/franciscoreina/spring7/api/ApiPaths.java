package com.franciscoreina.spring7.api;

public class ApiPaths {

    public static final String API_V1 = "/api/v1";

    // CUSTOMERS
    public static final String CUSTOMERS = API_V1 + "/customers";
    public static final String CUSTOMER_ID = "/{customerId}";

    // MILKS
    public static final String MILKS = API_V1 + "/milks";
    public static final String MILK_ID = "/{milkId}";

}
