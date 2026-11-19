package com.dgsw.javasuhangteam4.dto.request;

import com.dgsw.javasuhangteam4.enitity.PayPeriod;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class SubCreateRequest {

    private String subName;

    private int price;

    private PayPeriod payPeriod;

    private LocalDate createdAt;

}
