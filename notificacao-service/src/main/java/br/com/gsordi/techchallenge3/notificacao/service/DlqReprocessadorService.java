package br.com.gsordi.techchallenge3.notificacao.service;

import br.com.gsordi.techchallenge3.notificacao.infra.rabbitmq.RabbitMQConfig;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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
            String routingKey = identificarRoutingKey(message);
            rabbitTemplate.send(RabbitMQConfig.EXCHANGE_NAME, routingKey, message);
            totalReprocessadas++;
        }

        return totalReprocessadas;
    }

    @SuppressWarnings("unchecked")
    private String identificarRoutingKey(Message message) {
        List<Map<String, ?>> xDeath = (List<Map<String, ?>>) message.getMessageProperties().getHeaders().get("x-death");
        if (xDeath != null && !xDeath.isEmpty()) {
            List<String> routingKeys = (List<String>) xDeath.getFirst().get("routing-keys");
            if (routingKeys != null && !routingKeys.isEmpty()) {
                return routingKeys.getFirst();
            }
        }

        String typeId = (String) message.getMessageProperties().getHeaders().get("__TypeId__");
        if (typeId != null && typeId.contains("ConsultaAlterada")) {
            return RabbitMQConfig.ROUTING_KEY_ALTERADA;
        }

        return RabbitMQConfig.ROUTING_KEY_AGENDADA;
    }
}

