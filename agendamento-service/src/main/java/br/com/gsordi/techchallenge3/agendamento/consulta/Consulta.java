package br.com.gsordi.techchallenge3.agendamento.consulta;

import br.com.gsordi.techchallenge3.agendamento.medico.Medico;
import br.com.gsordi.techchallenge3.agendamento.paciente.Paciente;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "consultas")
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id")
    private Medico medico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @Column(name = "data_consulta", nullable = false)
    private LocalDateTime data;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusConsulta status;

    @Column(name = "motivo_cancelamento")
    private String motivoCancelamento;

    public Consulta(Medico medico, Paciente paciente, LocalDateTime data) {
        this.medico = medico;
        this.paciente = paciente;
        this.data = data;
        this.status = StatusConsulta.AGENDADA; // Toda nova consulta nasce como AGENDADA
    }

    public void cancelar(String motivo) {
        this.status = StatusConsulta.CANCELADA;
        this.motivoCancelamento = motivo;
    }

    public void alterarData(LocalDateTime data) {
        this.data = data;
    }
}
