package br.com.gsordi.techchallenge3.notificacao.consumer;

import br.com.gsordi.techchallenge3.notificacao.event.ConsultaAgendadaEvent;
import br.com.gsordi.techchallenge3.notificacao.event.ConsultaAlteradaEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ConsultaConsumer {

    @RabbitListener(queues = "consultas.notificacoes.queue")
    public void consumirConsultaAgendada(ConsultaAgendadaEvent evento) {
        System.out.println("==================================================");
        System.out.println(" [EVENTO RECEBIDO] Nova Consulta Agendada!");
        System.out.println(" -> ID da Consulta: " + evento.idConsulta());
        System.out.println(" -> Médico ID: " + evento.idMedico());
        System.out.println(" -> Paciente ID: " + evento.idPaciente());
        System.out.println(" -> Data: " + evento.dataConsulta());
        System.out.println(" [AÇÃO] E-mail de confirmação enviado para os envolvidos.");
        System.out.println("==================================================");
    }

    @RabbitListener(queues = "consultas.notificacoes.queue")
    public void consumirConsultaAlterada(ConsultaAlteradaEvent evento) {
        System.out.println("==================================================");
        System.out.println(" [EVENTO RECEBIDO] Consulta Alterada/Remarcada!");
        System.out.println(" -> ID da Consulta: " + evento.idConsulta());
        System.out.println(" -> Médico ID: " + evento.idMedico());
        System.out.println(" -> Nova Data: " + evento.novaData());
        System.out.println(" [AÇÃO] E-mail de remarcação enviado.");
        System.out.println("==================================================");
    }
}