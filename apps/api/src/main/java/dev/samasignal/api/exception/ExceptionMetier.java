package dev.samasignal.api.exception;

import org.springframework.http.HttpStatus;

public class ExceptionMetier extends RuntimeException {
    private final HttpStatus statut;
    private final String code;

    public ExceptionMetier(HttpStatus statut, String code, String message) {
        super(message);
        this.statut = statut;
        this.code = code;
    }

    public HttpStatus getStatut() {
        return statut;
    }

    public String getCode() {
        return code;
    }
}
