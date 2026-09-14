package br.com.gsordi.techchallenge3.agendamento.enfermeiro.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public class EnfermeiroDTO {
    public record PostRequest(
            @NotBlank(message = "O nome é obrigatório")
            String nome,

            @NotBlank(message = "O coren é obrigatório")
            String coren,

            @NotBlank(message = "O email é obrigatório")
            @Email(message = "Formato de e-mail inválido")
            String email,

            @NotBlank(message = "A senha é obrigatória")
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
