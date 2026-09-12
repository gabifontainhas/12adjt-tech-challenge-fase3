package br.com.gsordi.techchallenge3.agendamento.paciente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.br.CPF;

import java.util.UUID;

public final class PacienteDTO {
    public record PostRequest(

            @NotBlank
            String nome,

            @NotBlank
            @CPF
            String cpf,

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

            String cpf,

            String email
    ) {

    }
}
