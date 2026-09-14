package br.com.gsordi.techchallenge3.agendamento.paciente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.br.CPF;

import java.util.UUID;

public final class PacienteDTO {
    public record PostRequest(

            @NotBlank(message = "O nome é obrigatório")
            String nome,

            @NotBlank(message = "O cpf é obrigatório")
            @CPF(message = "CPF inválido")
            String cpf,

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

            String cpf,

            String email
    ) {

    }
}
