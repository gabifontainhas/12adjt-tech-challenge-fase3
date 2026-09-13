package br.com.gsordi.techchallenge3.notificacao;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
        "spring.rabbitmq.listener.simple.auto-startup=false"
})
class NotificacaoServiceApplicationTests {

    @MockitoBean
    private RabbitAdmin rabbitAdmin;

    @MockitoBean
    private ConnectionFactory connectionFactory;

    @Test
    void contextLoads() {
    }
}