package br.com.gsordi.techchallenge3.agendamento.paciente;

import br.com.gsordi.techchallenge3.agendamento.paciente.dto.PacienteDTO;
import org.springframework.stereotype.Component;

@Component
public class PacienteMapper {
    public static Paciente toEntity(PacienteDTO.PostRequest dto) {
        return new Paciente(dto.nome(), dto.cpf());
    }

    public static PacienteDTO.Response toResponse(Paciente paciente) {
        return new PacienteDTO.Response(
                paciente.getId(),
                paciente.getNome(),
                paciente.getCpf(),
                paciente.getUsuario().getEmail()
        );
    }
}
