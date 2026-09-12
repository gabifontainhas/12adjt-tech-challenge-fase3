package br.com.gsordi.techchallenge3.agendamento.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConsultaAlteradaEvent(
        UUID idConsulta,
        UUID idMedico,
        LocalDateTime novaData
) {}