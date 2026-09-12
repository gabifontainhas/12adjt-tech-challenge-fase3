package br.com.gsordi.techchallenge3.agendamento.paciente;

import br.com.gsordi.techchallenge3.agendamento.medico.dto.MedicoDTO;
import br.com.gsordi.techchallenge3.agendamento.paciente.dto.PacienteDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @PostMapping
    public ResponseEntity<PacienteDTO.Response> criarPaciente(@RequestBody @Valid PacienteDTO.PostRequest dto) {
        var paciente = PacienteMapper.toEntity(dto);
        var response = pacienteService.criarPaciente(paciente, dto.email(), dto.senha());
        return ResponseEntity.status(HttpStatus.CREATED).body(PacienteMapper.toResponse(response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO')")
    public ResponseEntity<List<PacienteDTO.Response>> listarTodos() {
        var response = pacienteService.listarTodos().stream()
                .map(PacienteMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }
}
