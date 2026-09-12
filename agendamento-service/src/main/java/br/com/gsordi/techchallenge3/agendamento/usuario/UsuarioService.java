package br.com.gsordi.techchallenge3.agendamento.usuario;

import br.com.gsordi.techchallenge3.agendamento.infra.exception.RegistroJaExisteException;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Transactional
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario criarUsuario(String email, String senha, Perfil perfil) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new RegistroJaExisteException("E-mail já está em uso");
        }

        var senhaCriptografada = passwordEncoder.encode(senha);

        var usuario = new Usuario(email, senhaCriptografada, perfil);
        return usuarioRepository.save(usuario);
    }
}
