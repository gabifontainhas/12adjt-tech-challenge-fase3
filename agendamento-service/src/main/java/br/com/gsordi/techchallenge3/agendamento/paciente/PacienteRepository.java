package br.com.gsordi.techchallenge3.agendamento.paciente;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PacienteRepository extends JpaRepository<Paciente, UUID> {

    Optional<Paciente> findByCpf(String cpf);
    boolean existsByCpf(String cpf);
    Optional<Paciente> findByUsuarioId(UUID usuarioId);
}
