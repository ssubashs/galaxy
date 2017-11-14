package com.fun.ms.account.service;

import com.atlassian.fugue.Option;
import com.fun.ms.account.Account;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.fun.ms.account.service.AccountService.Data.ACCOUNT_BY_ID;

public class AccountService {

    public static Option<Account> getAccount(int id){
      return  Option.option(ACCOUNT_BY_ID.get(id));
    }

    public static List<Account> getAccountOfType(String id){
        return  ACCOUNT_BY_ID.values().stream()
                .filter(account -> account.getTypeId().equalsIgnoreCase(id))
                .collect(Collectors.toList());
    }

    public static class Data{
        public static Map<Integer,Account> ACCOUNT_BY_ID = new ImmutableMap.Builder()
                .put(1,Account.builder().id(1).owningCompany("Amazon").typeId("8").status("Active").build())
                .put(2,Account.builder().id(2).owningCompany("IBM").typeId("4").status("Active").build())
                .put(3,Account.builder().id(3).owningCompany("Geico").typeId("1").status("Active").build())
                .put(4,Account.builder().id(4).owningCompany("Honda").typeId("2").status("Active").build())
                .put(5,Account.builder().id(5).owningCompany("Anthem").typeId("3").status("Active").build())
                .build();

    }
}
