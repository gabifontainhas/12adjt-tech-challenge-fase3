package br.com.gsordi.techchallenge3.agendamento.infra.exception;

public class CriteriosNaoAtendidosParaConsultaException extends RuntimeException {
    public CriteriosNaoAtendidosParaConsultaException(String message) {
        super(message);
    }
}
