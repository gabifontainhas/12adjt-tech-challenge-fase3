package br.com.gsordi.techchallenge3.agendamento.enfermeiro;

import br.com.gsordi.techchallenge3.agendamento.enfermeiro.dto.EnfermeiroDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/enfermeiros")
public class EnfermeiroController {
    private final EnfermeiroService enfermeiroService;

    public EnfermeiroController(EnfermeiroService enfermeiroService) {
        this.enfermeiroService = enfermeiroService;
    }

    @PostMapping
    public ResponseEntity<EnfermeiroDTO.Response> criarEnfermeiro(@RequestBody @Valid EnfermeiroDTO.PostRequest dto) {
        var enfermeiro = EnfermeiroMapper.toEntity(dto);
        var response = enfermeiroService.criarEnfermeiro(enfermeiro, dto.email(), dto.senha());
        return ResponseEntity.status(HttpStatus.CREATED).body(EnfermeiroMapper.toResponse(response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO')")
    public ResponseEntity<List<EnfermeiroDTO.Response>> listarTodos() {
        var response = enfermeiroService.listarTodos().stream()
                .map(EnfermeiroMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }
}