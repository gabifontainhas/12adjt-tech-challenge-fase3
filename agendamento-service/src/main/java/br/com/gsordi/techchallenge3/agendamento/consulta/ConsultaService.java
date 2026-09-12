package br.com.gsordi.techchallenge3.agendamento.consulta;

import br.com.gsordi.techchallenge3.agendamento.infra.exception.CriteriosNaoAtendidosParaConsultaException;
import br.com.gsordi.techchallenge3.agendamento.infra.exception.RegistroNaoEncontradoException;
import br.com.gsordi.techchallenge3.agendamento.infra.rabbitmq.ConsultaProducer;
import br.com.gsordi.techchallenge3.agendamento.medico.MedicoRepository;
import br.com.gsordi.techchallenge3.agendamento.paciente.PacienteRepository;
import br.com.gsordi.techchallenge3.agendamento.usuario.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final MedicoRepository medicoRepository;
    private final PacienteRepository pacienteRepository;
    private final ConsultaProducer consultaProducer;

    @Value("${api.clinica.horario.abertura:7}")
    private int horaAbertura;

    @Value("${api.clinica.horario.fechamento:19}")
    private int horaFechamento;

    @Value("${api.clinica.antecedencia.minutos:30}")
    private int antecedenciaMinima;

    @Value("${api.clinica.dias.fechados:SUNDAY}")
    private List<DayOfWeek> diasFechados;

    public ConsultaService(ConsultaRepository consultaRepository, MedicoRepository medicoRepository, PacienteRepository pacienteRepository, ConsultaProducer consultaProducer) {
        this.consultaRepository = consultaRepository;
        this.medicoRepository = medicoRepository;
        this.pacienteRepository = pacienteRepository;
        this.consultaProducer = consultaProducer;
    }

    @Transactional
    public Consulta agendar(UUID idMedico, UUID idPaciente, LocalDateTime dataConsulta) {

        validarHorarioFuncionamento(dataConsulta);
        validarAntecedenciaMinima(dataConsulta);

        var medicoOcupado = consultaRepository.existsByMedicoIdAndDataAndStatus(idMedico, dataConsulta, StatusConsulta.AGENDADA);
        if (medicoOcupado) {
            throw new CriteriosNaoAtendidosParaConsultaException("O médico já possui outra consulta agendada nesse mesmo horário.");
        }

        var pacienteOcupado = consultaRepository.existsByPacienteIdAndDataAndStatus(idPaciente, dataConsulta, StatusConsulta.AGENDADA);
        if (pacienteOcupado) {
            throw new CriteriosNaoAtendidosParaConsultaException("O paciente já possui uma consulta agendada nesse mesmo horário.");
        }

        var medico = medicoRepository.findById(idMedico)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Médico não encontrado."));

        var paciente = pacienteRepository.findById(idPaciente)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Paciente não encontrado."));

        var consulta = new Consulta(medico, paciente, dataConsulta);
        var consultaSalva = consultaRepository.save(consulta);
        consultaProducer.dispararConsultaAgendada(consultaSalva);

        return consultaSalva;
    }

    public List<Consulta> listarHistorico(Usuario usuarioLogado, boolean buscarApenasFuturas) {
        LocalDateTime agora = LocalDateTime.now();
        var usuarioId = usuarioLogado.getId();

        if (isPaciente(usuarioLogado)) {
            if (buscarApenasFuturas) {
                return consultaRepository.findByPacienteUsuarioIdAndDataAfterOrderByDataAsc(usuarioId, agora);
            }
            return consultaRepository.findByPacienteUsuarioIdOrderByDataDesc(usuarioId);

        } else if (isMedico(usuarioLogado)) {
            if (buscarApenasFuturas) {
                return consultaRepository.findByMedicoUsuarioIdAndDataAfterOrderByDataAsc(usuarioId, agora);
            }
            return consultaRepository.findByMedicoUsuarioIdOrderByDataDesc(usuarioId);

        } else {
            if (buscarApenasFuturas) {
                return consultaRepository.findByDataAfterOrderByDataAsc(agora);
            }
            return consultaRepository.findAllByOrderByDataDesc();
        }
    }

    @Transactional
    public Consulta editar(UUID idConsulta, LocalDateTime novaData) {
        validarHorarioFuncionamento(novaData);
        validarAntecedenciaMinima(novaData);

        var consulta = consultaRepository.findById(idConsulta)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Consulta não encontrada."));

        consulta.alterarData(novaData);

        var consultaAlterada = consultaRepository.save(consulta);
        consultaProducer.dispararConsultaAlterada(consultaAlterada);

        return consultaAlterada;
    }

    private void validarHorarioFuncionamento(LocalDateTime data) {
        var diaFechado = diasFechados.contains(data.getDayOfWeek());
        var antesDaAbertura = data.getHour() < horaAbertura;
        var depoisDoEncerramento = data.getHour() >= horaFechamento;

        if (diaFechado || antesDaAbertura || depoisDoEncerramento) {
            throw new CriteriosNaoAtendidosParaConsultaException(
                    "Consulta fora do horário de funcionamento."
            );
        }
    }

    private void validarAntecedenciaMinima(LocalDateTime data) {
        var agora = LocalDateTime.now();
        var tempoDeAntecedencia = java.time.Duration.between(agora, data);

        if (tempoDeAntecedencia.toMinutes() < 30) {
            throw new CriteriosNaoAtendidosParaConsultaException(
                    "A consulta deve ser agendada ou remarcada com no mínimo 30 minutos de antecedência."
            );
        }
    }

    private boolean isPaciente(Usuario usuario) {
        return usuario.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().contains("PACIENTE"));
    }

    private boolean isMedico(Usuario usuario) {
        return usuario.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().contains("MEDICO"));
    }
}
