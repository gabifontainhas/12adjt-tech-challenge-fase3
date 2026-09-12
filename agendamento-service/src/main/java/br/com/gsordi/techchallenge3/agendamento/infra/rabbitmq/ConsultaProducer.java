package br.com.gsordi.techchallenge3.agendamento.infra.rabbitmq;

import br.com.gsordi.techchallenge3.agendamento.consulta.Consulta;
import br.com.gsordi.techchallenge3.agendamento.event.ConsultaAgendadaEvent;
import br.com.gsordi.techchallenge3.agendamento.event.ConsultaAlteradaEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class ConsultaProducer {

    private final RabbitTemplate rabbitTemplate;

    public ConsultaProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void dispararConsultaAgendada(Consulta consulta) {
        var evento = new ConsultaAgendadaEvent(
                consulta.getId(),
                consulta.getMedico().getId(),
                consulta.getPaciente().getId(),
                consulta.getData()
        );

        rabbitTemplate.convertAndSend(
                "consultas.exchange",
                "consulta.agendada",
                evento
        );
    }

    public void dispararConsultaAlterada(Consulta consulta) {
        var evento = new ConsultaAlteradaEvent(
                consulta.getId(),
                consulta.getMedico().getId(),
                consulta.getData()
        );

        rabbitTemplate.convertAndSend(
                "consultas.exchange",
                "consulta.alterada",
                evento
        );
    }
}