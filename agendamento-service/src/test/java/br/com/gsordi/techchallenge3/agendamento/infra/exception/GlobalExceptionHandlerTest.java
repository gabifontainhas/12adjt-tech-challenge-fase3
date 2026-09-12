package br.com.gsordi.techchallenge3.agendamento.infra.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Deve retornar status 400 Bad Request com lista de campos mapeados ao capturar MethodArgumentNotValidException")
    void deveTratarErroValidacao() {
        var ex = mock(MethodArgumentNotValidException.class);
        var erro = new FieldError("pacienteDTO", "cpf", "CPF inválido");
        when(ex.getFieldErrors()).thenReturn(List.of(erro));

        var response = handler.handleTratarErrosDeValidacao(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());

        var erroRetornado = response.getBody().get(0);
        assertEquals("cpf", erroRetornado.campo());
        assertEquals("CPF inválido", erroRetornado.mensagem());
    }

    @Test
    @DisplayName("Deve retornar ProblemDetail com status 422 Unprocessable Content para RegistroJaExisteException")
    void deveTratarRegistroJaExisteException() {
        var mensagem = "Médico já cadastrado com este CRM";
        var ex = new RegistroJaExisteException(mensagem);

        var problemDetail = handler.handle(ex);

        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT.value(), problemDetail.getStatus());
        assertEquals(mensagem, problemDetail.getDetail());
    }

    @Test
    @DisplayName("Deve retornar ProblemDetail com status 404 Not Found para RegistroNaoEncontradoException")
    void deveTratarRegistroNaoEncontradoException() {
        var mensagem = "Paciente não encontrado";
        var ex = new RegistroNaoEncontradoException(mensagem);

        var problemDetail = handler.handle(ex);

        assertEquals(HttpStatus.NOT_FOUND.value(), problemDetail.getStatus());
        assertEquals(mensagem, problemDetail.getDetail());
    }

    @Test
    @DisplayName("Deve retornar ProblemDetail com status 422 Unprocessable Content para CriteriosNaoAtendidosParaConsultaException")
    void deveTratarCriteriosNaoAtendidosException() {
        var mensagem = "Horário de consulta fora do expediente clínico";
        var ex = new CriteriosNaoAtendidosParaConsultaException(mensagem);

        var problemDetail = handler.handle(ex);

        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT.value(), problemDetail.getStatus());
        assertEquals(mensagem, problemDetail.getDetail());
    }

    @Test
    @DisplayName("Deve retornar ProblemDetail com status 403 Forbidden para AccessDeniedException")
    void deveTratarAccessDeniedException() {
        var mensagem = "Acesso negado para este recurso";
        var ex = new AccessDeniedException(mensagem);

        var problemDetail = handler.handle(ex);

        assertEquals(HttpStatus.FORBIDDEN.value(), problemDetail.getStatus());
        assertEquals(mensagem, problemDetail.getDetail());
    }

    @Test
    @DisplayName("Deve retornar ProblemDetail com status 401 Unauthorized para TokenInvalidoException")
    void deveTratarTokenInvalidoException() {
        var mensagem = "Token JWT inválido ou expirado";
        var ex = new TokenInvalidoException(mensagem);

        var problemDetail = handler.handle(ex);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), problemDetail.getStatus());
        assertEquals(mensagem, problemDetail.getDetail());
    }
}