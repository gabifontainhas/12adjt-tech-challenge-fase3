package br.com.gsordi.techchallenge3.notificacao;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class NotificacaoServiceApplicationTests {

    @MockitoBean
    private RabbitAdmin rabbitAdmin;

    @Test
    void contextLoads() {
    }

}
