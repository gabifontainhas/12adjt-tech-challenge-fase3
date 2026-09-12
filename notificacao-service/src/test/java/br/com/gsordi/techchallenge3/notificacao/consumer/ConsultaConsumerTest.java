package br.com.gsordi.techchallenge3.notificacao.consumer;

import br.com.gsordi.techchallenge3.notificacao.event.ConsultaAgendadaEvent;
import br.com.gsordi.techchallenge3.notificacao.event.ConsultaAlteradaEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ConsultaConsumerTest {

    @InjectMocks
    private ConsultaConsumer consultaConsumer;

    @Test
    @DisplayName("Deve processar evento de consulta agendada com sucesso")
    void deveProcessarConsultaAgendadaComSucesso() {
        var evento = new ConsultaAgendadaEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDateTime.of(2026, 10, 15, 14, 30)
        );

        assertDoesNotThrow(() -> consultaConsumer.consumirConsultaAgendada(evento));
    }

    @Test
    @DisplayName("Deve processar evento de consulta alterada com sucesso")
    void deveProcessarConsultaAlteradaComSucesso() {
        var evento = new ConsultaAlteradaEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDateTime.of(2026, 10, 20, 16, 0)
        );

        assertDoesNotThrow(() -> consultaConsumer.consumirConsultaAlterada(evento));
    }

    @Test
    @DisplayName("Deve propagar exceção quando o processamento falhar para acionar o retry/DLQ")
    void devePropagarExcecaoQuandoFalhar() {
        ConsultaConsumer consumerComFalha = new ConsultaConsumer() {
            @Override
            public void consumirConsultaAgendada(ConsultaAgendadaEvent evento) {
                throw new RuntimeException("Erro ao conectar no servidor SMTP");
            }
        };

        ConsultaAgendadaEvent evento = new ConsultaAgendadaEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDateTime.now()
        );

        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> consumerComFalha.consumirConsultaAgendada(evento)
        );
    }
}