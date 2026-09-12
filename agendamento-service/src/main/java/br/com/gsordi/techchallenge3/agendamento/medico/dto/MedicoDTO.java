package br.com.gsordi.techchallenge3.agendamento.medico.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public class MedicoDTO {
    public record PostRequest(

            @NotBlank
            String nome,

            @NotBlank
            String crm,

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

            String crm,

            String email
    ) {
    }
}
