package br.com.gsordi.techchallenge3.agendamento.consulta;

import br.com.gsordi.techchallenge3.agendamento.usuario.Usuario;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class ConsultaGraphQLController {

    private final ConsultaService consultaService;

    public ConsultaGraphQLController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    @QueryMapping(name = "historicoConsultas")
    public List<Consulta> listarHistorico(
            @Argument Boolean apenasFuturas,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        boolean buscarApenasFuturas = apenasFuturas != null && apenasFuturas;

        return consultaService.listarHistorico(usuarioLogado, buscarApenasFuturas);
    }
}

