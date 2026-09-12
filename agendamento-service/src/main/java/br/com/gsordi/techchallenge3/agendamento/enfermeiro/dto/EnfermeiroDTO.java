package br.com.gsordi.techchallenge3.agendamento.enfermeiro.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.br.CPF;

import java.util.UUID;

public class EnfermeiroDTO {
    public record PostRequest(
            @NotBlank
            String nome,

            @NotBlank
            String coren,

            @NotBlank
            @Email
            String email,

            @NotBlank
            String senha
    ) {
    }

    public record Response(
            UUID id,
            String nome,

            String coren,

            String email
    ) {
    }
}
