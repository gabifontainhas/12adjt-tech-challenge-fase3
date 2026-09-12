package br.com.gsordi.techchallenge3.agendamento.medico;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MedicoRepository extends JpaRepository<Medico, UUID> {

    Optional<Medico> findByCrm(String crm);
    boolean existsByCrm(String crm);
    Optional<Medico> findByUsuarioId(UUID id);
}
