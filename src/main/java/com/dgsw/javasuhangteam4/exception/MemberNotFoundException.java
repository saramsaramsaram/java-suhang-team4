package com.dgsw.javasuhangteam4.exception;

public class MemberNotFoundException extends RuntimeException {

    public MemberNotFoundException() {
        super("회원 없음");
    }
}
