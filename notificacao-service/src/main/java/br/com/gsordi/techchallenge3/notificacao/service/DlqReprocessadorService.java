package br.com.gsordi.techchallenge3.notificacao.service;

import br.com.gsordi.techchallenge3.notificacao.infra.rabbitmq.RabbitMQConfig;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class DlqReprocessadorService {

    private final RabbitTemplate rabbitTemplate;

    public DlqReprocessadorService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public int reprocessarMensagens() {
        int totalReprocessadas = 0;

        Message message;
        while ((message = rabbitTemplate.receive(RabbitMQConfig.DLQ_NAME)) != null) {
            rabbitTemplate.send("", RabbitMQConfig.QUEUE_NAME, message);
            totalReprocessadas++;
        }

        return totalReprocessadas;
    }
}

