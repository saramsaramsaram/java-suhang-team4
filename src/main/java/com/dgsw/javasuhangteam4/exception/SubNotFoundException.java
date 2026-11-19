package com.dgsw.javasuhangteam4.exception;

public class SubNotFoundException extends RuntimeException {

    public SubNotFoundException(Long id) {
        super("구독 정보를 찾을 수 없습니다. id=" + id);
    }
}
