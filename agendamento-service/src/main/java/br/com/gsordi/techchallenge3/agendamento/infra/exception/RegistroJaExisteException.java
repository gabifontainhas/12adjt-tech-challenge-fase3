package br.com.gsordi.techchallenge3.agendamento.infra.exception;

public class RegistroJaExisteException extends RuntimeException {
    public RegistroJaExisteException(String message) {
        super(message);
    }
}
