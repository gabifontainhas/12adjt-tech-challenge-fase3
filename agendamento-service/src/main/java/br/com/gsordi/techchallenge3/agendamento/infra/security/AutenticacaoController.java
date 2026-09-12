package br.com.gsordi.techchallenge3.agendamento.infra.security;

import br.com.gsordi.techchallenge3.agendamento.infra.security.dto.AutenticacaoDTO;
import br.com.gsordi.techchallenge3.agendamento.usuario.Usuario;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
public class AutenticacaoController {
    private final AuthenticationManager manager;
    private final TokenService tokenService;

    public AutenticacaoController(AuthenticationManager manager, TokenService tokenService) {
        this.manager = manager;
        this.tokenService = tokenService;
    }

    @PostMapping
    public ResponseEntity<AutenticacaoDTO.Response> efetuarLogin(@RequestBody @Valid AutenticacaoDTO.Request dto) {

        var authenticationToken = new UsernamePasswordAuthenticationToken(dto.email(), dto.senha());
        var authentication = manager.authenticate(authenticationToken);
        var tokenJWT = tokenService.gerarToken((Usuario) authentication.getPrincipal());

        return ResponseEntity.ok(new AutenticacaoDTO.Response(tokenJWT));
    }

}
