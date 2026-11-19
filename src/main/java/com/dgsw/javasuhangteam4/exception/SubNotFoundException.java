package com.dgsw.javasuhangteam4.exception;

public class SubNotFoundException extends RuntimeException {

    public SubNotFoundException() {
        super("구독 없음");
    }
}
