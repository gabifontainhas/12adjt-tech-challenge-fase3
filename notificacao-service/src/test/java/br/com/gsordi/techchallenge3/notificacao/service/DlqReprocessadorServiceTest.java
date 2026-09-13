package br.com.gsordi.techchallenge3.notificacao.service;


import br.com.gsordi.techchallenge3.notificacao.infra.rabbitmq.RabbitMQConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DlqReprocessadorServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private DlqReprocessadorService dlqReprocessadorService;

    @Test
    @DisplayName("Deve transferir mensagens da DLQ para a exchange e retornar a contagem correta")
    void deveReprocessarMensagensDaDlq() {
        var props = new MessageProperties();
        var msg1 = mock(Message.class);
        var msg2 = mock(Message.class);

        when(msg1.getMessageProperties()).thenReturn(props);
        when(msg2.getMessageProperties()).thenReturn(props);

        when(rabbitTemplate.receive(RabbitMQConfig.DLQ_NAME))
                .thenReturn(msg1, msg2, null);

        int total = dlqReprocessadorService.reprocessarMensagens();

        assertEquals(2, total);
        verify(rabbitTemplate, times(3)).receive(RabbitMQConfig.DLQ_NAME);
        verify(rabbitTemplate, times(1)).send(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_AGENDADA, msg1);
        verify(rabbitTemplate, times(1)).send(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY_AGENDADA, msg2);
    }

    @Test
    @DisplayName("Deve retornar zero quando a DLQ estiver vazia")
    void deveRetornarZeroQuandoDlqEstiverVazia() {
        when(rabbitTemplate.receive(RabbitMQConfig.DLQ_NAME)).thenReturn(null);

        int total = dlqReprocessadorService.reprocessarMensagens();

        assertEquals(0, total);
        verify(rabbitTemplate, never()).send(anyString(), anyString(), any(Message.class));
    }
}