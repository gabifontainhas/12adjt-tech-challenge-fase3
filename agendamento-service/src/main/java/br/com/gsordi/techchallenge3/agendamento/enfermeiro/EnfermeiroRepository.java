package br.com.gsordi.techchallenge3.agendamento.enfermeiro;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EnfermeiroRepository extends JpaRepository<Enfermeiro, UUID> {

    Optional<Enfermeiro> findByCoren(String coren);

    boolean existsByCoren(String coren);

    Optional<Enfermeiro> findByUsuarioId(UUID id);
}
