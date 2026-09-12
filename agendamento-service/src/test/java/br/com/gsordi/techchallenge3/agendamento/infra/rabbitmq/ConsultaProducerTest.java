package br.com.gsordi.techchallenge3.agendamento.infra.rabbitmq;

import br.com.gsordi.techchallenge3.agendamento.consulta.Consulta;
import br.com.gsordi.techchallenge3.agendamento.event.ConsultaAgendadaEvent;
import br.com.gsordi.techchallenge3.agendamento.event.ConsultaAlteradaEvent;
import br.com.gsordi.techchallenge3.agendamento.medico.Medico;
import br.com.gsordi.techchallenge3.agendamento.paciente.Paciente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultaProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ConsultaProducer consultaProducer;

    @Captor
    private ArgumentCaptor<ConsultaAgendadaEvent> agendadaCaptor;

    @Captor
    private ArgumentCaptor<ConsultaAlteradaEvent> alteradaCaptor;

    @Test
    @DisplayName("Deve extrair os dados da Consulta e publicar ConsultaAgendadaEvent corretamente")
    void deveDispararConsultaAgendada() {
        var idConsulta = UUID.randomUUID();
        var idMedico = UUID.randomUUID();
        var idPaciente = UUID.randomUUID();
        var data = LocalDateTime.of(2026, 10, 15, 14, 30);

        var consulta = mock(Consulta.class);
        var medico = mock(Medico.class);
        var paciente = mock(Paciente.class);

        when(consulta.getId()).thenReturn(idConsulta);
        when(consulta.getMedico()).thenReturn(medico);
        when(medico.getId()).thenReturn(idMedico);
        when(consulta.getPaciente()).thenReturn(paciente);
        when(paciente.getId()).thenReturn(idPaciente);
        when(consulta.getData()).thenReturn(data);

        consultaProducer.dispararConsultaAgendada(consulta);

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq("consultas.exchange"),
                eq("consulta.agendada"),
                agendadaCaptor.capture()
        );

        var evento = agendadaCaptor.getValue();
        assertNotNull(evento);
        assertEquals(idConsulta, evento.idConsulta());
        assertEquals(idMedico, evento.idMedico());
        assertEquals(idPaciente, evento.idPaciente());
        assertEquals(data, evento.dataConsulta());
    }

    @Test
    @DisplayName("Deve extrair os dados da Consulta e publicar ConsultaAlteradaEvent corretamente")
    void deveDispararConsultaAlterada() {
        var idConsulta = UUID.randomUUID();
        var idMedico = UUID.randomUUID();
        var novaData = LocalDateTime.of(2026, 10, 20, 9, 0);

        var consulta = mock(Consulta.class);
        var medico = mock(Medico.class);

        when(consulta.getId()).thenReturn(idConsulta);
        when(consulta.getMedico()).thenReturn(medico);
        when(medico.getId()).thenReturn(idMedico);
        when(consulta.getData()).thenReturn(novaData);

        consultaProducer.dispararConsultaAlterada(consulta);

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq("consultas.exchange"),
                eq("consulta.alterada"),
                alteradaCaptor.capture()
        );

        var evento = alteradaCaptor.getValue();
        assertNotNull(evento);
        assertEquals(idConsulta, evento.idConsulta());
        assertEquals(idMedico, evento.idMedico());
        assertEquals(novaData, evento.novaData());
    }

}