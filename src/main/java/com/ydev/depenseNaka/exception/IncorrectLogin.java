package com.ydev.depenseNaka.exception;

public class IncorrectLogin extends RuntimeException {
    public IncorrectLogin() {
        super("Le pseudononime ou mot de passe incorrecte");
    }
}
