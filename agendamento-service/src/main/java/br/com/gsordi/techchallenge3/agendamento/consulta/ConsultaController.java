package br.com.gsordi.techchallenge3.agendamento.consulta;

import br.com.gsordi.techchallenge3.agendamento.consulta.dto.ConsultaDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/consultas")
public class ConsultaController {

    private final ConsultaService consultaService;
    private final ConsultaMapper consultaMapper;

    public ConsultaController(ConsultaService consultaService, ConsultaMapper consultaMapper) {
        this.consultaService = consultaService;
        this.consultaMapper = consultaMapper;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO')")
    public ResponseEntity<ConsultaDTO.Response> agendar(@RequestBody @Valid ConsultaDTO.PostRequest request) {

        var consulta = consultaService.agendar(
                request.idMedico(),
                request.idPaciente(),
                request.data()
        );

        return ResponseEntity.ok(consultaMapper.toResponse(consulta));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MEDICO')")
    public ResponseEntity<ConsultaDTO.Response> editar(
            @PathVariable UUID id,
            @RequestBody @Valid ConsultaDTO.PutRequest request) {
        var consultaAtualizada = consultaService.editar(id, request.novaData());

        return ResponseEntity.ok(consultaMapper.toResponse(consultaAtualizada));
    }
}
