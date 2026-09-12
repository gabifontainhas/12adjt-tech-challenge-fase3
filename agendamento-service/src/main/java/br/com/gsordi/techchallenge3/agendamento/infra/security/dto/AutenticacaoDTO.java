package br.com.gsordi.techchallenge3.agendamento.infra.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AutenticacaoDTO {

    public record Request(
            @NotBlank
            @Email
            String email,

            @NotBlank
            String senha
    ) {
    }

    public record Response(
            String token
    ) {
    }
}
