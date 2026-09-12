package br.com.gsordi.techchallenge3.agendamento.paciente;

import br.com.gsordi.techchallenge3.agendamento.infra.exception.RegistroJaExisteException;
import br.com.gsordi.techchallenge3.agendamento.medico.Medico;
import br.com.gsordi.techchallenge3.agendamento.usuario.Perfil;
import br.com.gsordi.techchallenge3.agendamento.usuario.UsuarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
public class PacienteService {
    private final PacienteRepository pacienteRepository;
    private final UsuarioService usuarioService;

    public PacienteService(PacienteRepository pacienteRepository, UsuarioService usuarioService) {
        this.pacienteRepository = pacienteRepository;
        this.usuarioService = usuarioService;
    }

    public Paciente criarPaciente(Paciente paciente, String email, String senha) {
        if(pacienteRepository.existsByCpf(paciente.getCpf())) {
            throw new RegistroJaExisteException("CPF já cadastrado na base");
        }
        var usuario = usuarioService.criarUsuario(email, senha, Perfil.PACIENTE);
        paciente.associarUsuario(usuario);

        return pacienteRepository.save(paciente);
    }

    public List<Paciente> listarTodos() {
        return pacienteRepository.findAll();
    }
}
