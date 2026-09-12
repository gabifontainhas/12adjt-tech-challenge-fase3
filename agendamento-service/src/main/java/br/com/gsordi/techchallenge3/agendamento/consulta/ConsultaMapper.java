package br.com.gsordi.techchallenge3.agendamento.consulta;

import br.com.gsordi.techchallenge3.agendamento.consulta.dto.ConsultaDTO;
import org.springframework.stereotype.Component;

@Component
public class ConsultaMapper {
    public ConsultaDTO.Response toResponse(Consulta consulta) {
        return new ConsultaDTO.Response(
                consulta.getId(),
                consulta.getMedico().getId(),
                consulta.getPaciente().getId(),
                consulta.getData(),
                consulta.getStatus()
        );
    }
}
