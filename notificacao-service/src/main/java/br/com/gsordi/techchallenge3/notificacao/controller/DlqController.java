package br.com.gsordi.techchallenge3.notificacao.controller;

import br.com.gsordi.techchallenge3.notificacao.service.DlqReprocessadorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/dlq")
public class DlqController {

    private final DlqReprocessadorService dlqReprocessadorService;

    public DlqController(DlqReprocessadorService dlqReprocessadorService) {
        this.dlqReprocessadorService = dlqReprocessadorService;
    }

    @PostMapping("/reprocessar")
    public ResponseEntity<Map<String, Object>> reprocessar() {
        int total = dlqReprocessadorService.reprocessarMensagens();

        return ResponseEntity.ok(Map.of(
                "mensagem", "Processamento da DLQ concluído",
                "mensagensReprocessadas", total
        ));
    }
}