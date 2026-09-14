package br.com.gsordi.techchallenge3.agendamento.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleTratarErrosDeValidacao(MethodArgumentNotValidException ex) {
        var problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Um ou mais campos contêm valores inválidos"
        );
        problemDetail.setTitle("Erro de validação");

        List<DadosErroValidacao> erros = ex.getFieldErrors().stream()
                .map(erro -> new DadosErroValidacao(erro.getField(), erro.getDefaultMessage()))
                .toList();

        problemDetail.setProperty("erros", erros);

        return problemDetail;
    }

    @ExceptionHandler(RegistroJaExisteException.class)
    public ProblemDetail handle(RegistroJaExisteException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_CONTENT,
                ex.getMessage());
    }

    @ExceptionHandler(RegistroNaoEncontradoException.class)
    public ProblemDetail handle(RegistroNaoEncontradoException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage());
    }

    @ExceptionHandler(CriteriosNaoAtendidosParaConsultaException.class)
    public ProblemDetail handle(CriteriosNaoAtendidosParaConsultaException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_CONTENT,
                ex.getMessage());
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ProblemDetail handle(org.springframework.security.access.AccessDeniedException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                ex.getMessage());
    }

    @ExceptionHandler(TokenInvalidoException.class)
    public ProblemDetail handle(TokenInvalidoException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage());
    }

    public record DadosErroValidacao(String campo, String mensagem) {
    }
}

