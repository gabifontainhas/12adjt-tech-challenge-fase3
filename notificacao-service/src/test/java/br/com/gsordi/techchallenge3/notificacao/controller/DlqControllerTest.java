package br.com.gsordi.techchallenge3.notificacao.controller;

import br.com.gsordi.techchallenge3.notificacao.service.DlqReprocessadorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DlqController.class)
class DlqControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DlqReprocessadorService dlqReprocessadorService;

    @Test
    @DisplayName("Deve retornar status 200 e a contagem correta quando houver mensagens reprocessadas")
    void deveReprocessarMensagensComSucesso() throws Exception {

        when(dlqReprocessadorService.reprocessarMensagens()).thenReturn(5);

        mockMvc.perform(post("/admin/dlq/reprocessar")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Processamento da DLQ concluído"))
                .andExpect(jsonPath("$.mensagensReprocessadas").value(5));

        verify(dlqReprocessadorService, times(1)).reprocessarMensagens();
    }

    @Test
    @DisplayName("Deve retornar status 200 com contador zerado quando a DLQ estiver vazia")
    void deveRetornarSucessoQuandoNaoHouverMensagens() throws Exception {
        // Cenário: fila vazia
        when(dlqReprocessadorService.reprocessarMensagens()).thenReturn(0);

        mockMvc.perform(post("/admin/dlq/reprocessar")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Processamento da DLQ concluído"))
                .andExpect(jsonPath("$.mensagensReprocessadas").value(0));

        verify(dlqReprocessadorService, times(1)).reprocessarMensagens();
    }
}