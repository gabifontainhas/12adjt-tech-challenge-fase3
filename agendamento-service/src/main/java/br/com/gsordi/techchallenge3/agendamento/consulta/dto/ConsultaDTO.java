package br.com.gsordi.techchallenge3.agendamento.consulta.dto;

import br.com.gsordi.techchallenge3.agendamento.consulta.Consulta;
import br.com.gsordi.techchallenge3.agendamento.consulta.StatusConsulta;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public class ConsultaDTO {

    public record PostRequest(
            @NotNull
            UUID idMedico,

            @NotNull
            UUID idPaciente,

            @NotNull
            @Future
            LocalDateTime data
    ) {
    }

    public record Response(
            UUID id,
            UUID idMedico,
            UUID idPaciente,
            LocalDateTime data,
            StatusConsulta status
    ) {
        public Response(Consulta consulta) {
            this(
                    consulta.getId(),
                    consulta.getMedico().getId(),
                    consulta.getPaciente().getId(),
                    consulta.getData(),
                    consulta.getStatus()
            );
        }
    }
    public record PutRequest(
            @NotNull
            @Future
            LocalDateTime novaData
    ) {}
}