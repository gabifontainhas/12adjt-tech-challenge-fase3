package br.com.gsordi.techchallenge3.agendamento.enfermeiro;

import br.com.gsordi.techchallenge3.agendamento.enfermeiro.dto.EnfermeiroDTO;
import org.springframework.stereotype.Component;

@Component
public class EnfermeiroMapper {
    public static Enfermeiro toEntity(EnfermeiroDTO.PostRequest dto) {
        return new Enfermeiro(dto.nome(), dto.coren());
    }

    public static EnfermeiroDTO.Response toResponse(Enfermeiro enfermeiro) {
        return new EnfermeiroDTO.Response(
                enfermeiro.getId(),
                enfermeiro.getNome(),
                enfermeiro.getCoren(),
                enfermeiro.getUsuario().getEmail()
        );
    }
}
