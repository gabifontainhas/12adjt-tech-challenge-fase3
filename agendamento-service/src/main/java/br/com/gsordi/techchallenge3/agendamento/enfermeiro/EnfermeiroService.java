package br.com.gsordi.techchallenge3.agendamento.enfermeiro;

import br.com.gsordi.techchallenge3.agendamento.infra.exception.RegistroJaExisteException;
import br.com.gsordi.techchallenge3.agendamento.usuario.Perfil;
import br.com.gsordi.techchallenge3.agendamento.usuario.UsuarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
public class EnfermeiroService {
    private final EnfermeiroRepository enfermeiroRepository;
    private final UsuarioService usuarioService;

    public EnfermeiroService(EnfermeiroRepository enfermeiroRepository, UsuarioService usuarioService) {
        this.enfermeiroRepository = enfermeiroRepository;
        this.usuarioService = usuarioService;
    }

    public Enfermeiro criarEnfermeiro(Enfermeiro enfermeiro, String email, String senha) {
        if (enfermeiroRepository.existsByCoren(enfermeiro.getCoren())) {
            throw new RegistroJaExisteException("Coren já cadastrado na base");
        }
        var usuario = usuarioService.criarUsuario(email, senha, Perfil.ENFERMEIRO);
        enfermeiro.associarUsuario(usuario);

        return enfermeiroRepository.save(enfermeiro);
    }

    public List<Enfermeiro> listarTodos() {
        return enfermeiroRepository.findAll();
    }
}
