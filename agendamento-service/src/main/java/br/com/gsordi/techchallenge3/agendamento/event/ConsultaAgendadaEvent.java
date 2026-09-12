package br.com.gsordi.techchallenge3.agendamento.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConsultaAgendadaEvent(
        UUID idConsulta,
        UUID idMedico,
        UUID idPaciente,
        LocalDateTime dataConsulta
) {}