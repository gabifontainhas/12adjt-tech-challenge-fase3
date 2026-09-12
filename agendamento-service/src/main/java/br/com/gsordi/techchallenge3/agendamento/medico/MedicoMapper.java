package br.com.gsordi.techchallenge3.agendamento.medico;

import br.com.gsordi.techchallenge3.agendamento.medico.dto.MedicoDTO;
import org.springframework.stereotype.Component;

@Component
public class MedicoMapper {
    public static Medico toEntity(MedicoDTO.PostRequest dto) {
        return new Medico(dto.nome(), dto.crm());
    }

    public static MedicoDTO.Response toResponse(Medico medico) {
        return new MedicoDTO.Response(
                medico.getId(),
                medico.getNome(),
                medico.getCrm(),
                medico.getUsuario().getEmail()
        );
    }
}

