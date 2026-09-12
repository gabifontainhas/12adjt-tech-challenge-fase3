package br.com.gsordi.techchallenge3.agendamento.medico;

import br.com.gsordi.techchallenge3.agendamento.medico.dto.MedicoDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/medicos")
public class MedicoController {
    private final MedicoService medicoService;

    public MedicoController(MedicoService medicoService) {
        this.medicoService = medicoService;
    }

    @PostMapping
    public ResponseEntity<MedicoDTO.Response> criarMedico(@RequestBody @Valid MedicoDTO.PostRequest dto) {
        var medico = MedicoMapper.toEntity(dto);
        var response = medicoService.criarMedico(medico, dto.email(), dto.senha());
        return ResponseEntity.status(HttpStatus.CREATED).body(MedicoMapper.toResponse(response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO')")
    public ResponseEntity<List<MedicoDTO.Response>> listarTodos() {
        var response = medicoService.listarTodos().stream()
                .map(MedicoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }
}