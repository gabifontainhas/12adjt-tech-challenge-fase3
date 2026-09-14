package br.com.gsordi.techchallenge3.agendamento.medico.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public class MedicoDTO {
    public record PostRequest(
            @NotBlank(message = "O nome é obrigatório")
            String nome,

            @NotBlank(message = "O crm é obrigatório")
            String crm,

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

            String crm,

            String email
    ) {
    }
}
