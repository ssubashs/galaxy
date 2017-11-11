package com.fun.ms.account;

import lombok.Value;
import lombok.experimental.Builder;

@Value
@Builder
public class Account {
    private final Integer id;
    private final String typeId;
    private final String owningCompany;
    private final String status;
    private final String typeDescription;
}
