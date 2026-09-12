package br.com.gsordi.techchallenge3.agendamento.medico;

import br.com.gsordi.techchallenge3.agendamento.infra.exception.RegistroJaExisteException;
import br.com.gsordi.techchallenge3.agendamento.medico.dto.MedicoDTO;
import br.com.gsordi.techchallenge3.agendamento.usuario.Perfil;
import br.com.gsordi.techchallenge3.agendamento.usuario.UsuarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
public class MedicoService {
    private final MedicoRepository medicoRepository;
    private final UsuarioService usuarioService;

    public MedicoService(MedicoRepository medicoRepository, UsuarioService usuarioService) {
        this.medicoRepository = medicoRepository;
        this.usuarioService = usuarioService;
    }

    public Medico criarMedico(Medico medico, String email, String senha) {
        if (medicoRepository.existsByCrm(medico.getCrm())) {
            throw new RegistroJaExisteException("Crm já cadastrado na base");
        }
        var usuario = usuarioService.criarUsuario(email, senha, Perfil.MEDICO);
        medico.associarUsuario(usuario);

        return medicoRepository.save(medico);
    }

    public List<Medico> listarTodos() {
        return medicoRepository.findAll();
    }
}
