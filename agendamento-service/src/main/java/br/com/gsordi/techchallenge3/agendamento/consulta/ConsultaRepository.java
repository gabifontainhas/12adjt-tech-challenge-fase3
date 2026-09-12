package br.com.gsordi.techchallenge3.agendamento.consulta;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ConsultaRepository extends JpaRepository<Consulta, UUID> {

    boolean existsByMedicoIdAndDataAndStatus(UUID medicoId, LocalDateTime data, StatusConsulta status);

    boolean existsByPacienteIdAndDataAndStatus(UUID pacienteId, LocalDateTime data, StatusConsulta status);

    List<Consulta> findByPacienteUsuarioIdOrderByDataDesc(UUID usuarioId);
    List<Consulta> findByPacienteUsuarioIdAndDataAfterOrderByDataAsc(UUID usuarioId, LocalDateTime data);

    List<Consulta> findByMedicoUsuarioIdOrderByDataDesc(UUID usuarioId);
    List<Consulta> findByMedicoUsuarioIdAndDataAfterOrderByDataAsc(UUID usuarioId, LocalDateTime data);

    List<Consulta> findAllByOrderByDataDesc();
    List<Consulta> findByDataAfterOrderByDataAsc(LocalDateTime data);
}
