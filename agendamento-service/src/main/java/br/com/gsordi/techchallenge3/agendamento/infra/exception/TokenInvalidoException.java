package br.com.gsordi.techchallenge3.agendamento.infra.exception;

public class TokenInvalidoException extends RuntimeException {
    public TokenInvalidoException(String message) {
        super(message);
    }
}
