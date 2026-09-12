package br.com.gsordi.techchallenge3.agendamento.consulta;

import br.com.gsordi.techchallenge3.agendamento.infra.exception.CriteriosNaoAtendidosParaConsultaException;
import br.com.gsordi.techchallenge3.agendamento.infra.exception.RegistroNaoEncontradoException;
import br.com.gsordi.techchallenge3.agendamento.infra.rabbitmq.ConsultaProducer;
import br.com.gsordi.techchallenge3.agendamento.medico.Medico;
import br.com.gsordi.techchallenge3.agendamento.medico.MedicoRepository;
import br.com.gsordi.techchallenge3.agendamento.paciente.Paciente;
import br.com.gsordi.techchallenge3.agendamento.paciente.PacienteRepository;
import br.com.gsordi.techchallenge3.agendamento.usuario.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultaServiceTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private MedicoRepository medicoRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private ConsultaProducer consultaProducer;

    @InjectMocks
    private ConsultaService consultaService;


    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(consultaService, "horaAbertura", 7);
        ReflectionTestUtils.setField(consultaService, "horaFechamento", 19);
        ReflectionTestUtils.setField(consultaService, "antecedenciaMinima", 30);
        ReflectionTestUtils.setField(consultaService, "diasFechados", List.of(DayOfWeek.SUNDAY));
    }

    @Test
    @DisplayName("Deve agendar consulta válida, persistir e disparar evento no RabbitMQ")
    void deveAgendarConsultaEDispararEvento() {
        var idMedico = UUID.randomUUID();
        var idPaciente = UUID.randomUUID();
        var dataConsulta = LocalDate.now()
                .with(TemporalAdjusters.next(DayOfWeek.TUESDAY))
                .atTime(14, 0);

        var medico = mock(Medico.class);
        var paciente = mock(Paciente.class);
        var consultaSalva = mock(Consulta.class);

        when(consultaRepository.existsByMedicoIdAndDataAndStatus(idMedico, dataConsulta, StatusConsulta.AGENDADA))
                .thenReturn(false);
        when(consultaRepository.existsByPacienteIdAndDataAndStatus(idPaciente, dataConsulta, StatusConsulta.AGENDADA))
                .thenReturn(false);

        when(medicoRepository.findById(idMedico)).thenReturn(Optional.of(medico));
        when(pacienteRepository.findById(idPaciente)).thenReturn(Optional.of(paciente));
        when(consultaRepository.save(any(Consulta.class))).thenReturn(consultaSalva);

        var resultado = consultaService.agendar(idMedico, idPaciente, dataConsulta);

        assertNotNull(resultado);
        verify(consultaRepository, times(1)).save(any(Consulta.class));
        verify(consultaProducer, times(1)).dispararConsultaAgendada(consultaSalva);
    }

    @Test
    @DisplayName("Deve impedir agendamento e não publicar evento se médico já estiver ocupado")
    void deveLancarExcecaoQuandoMedicoEstiverOcupado() {
        var idMedico = UUID.randomUUID();
        var idPaciente = UUID.randomUUID();
        var dataConsulta = LocalDate.now()
                .with(TemporalAdjusters.next(DayOfWeek.WEDNESDAY))
                .atTime(10, 0);

        when(consultaRepository.existsByMedicoIdAndDataAndStatus(idMedico, dataConsulta, StatusConsulta.AGENDADA))
                .thenReturn(true);

        assertThrows(
                CriteriosNaoAtendidosParaConsultaException.class,
                () -> consultaService.agendar(idMedico, idPaciente, dataConsulta)
        );

        verify(consultaRepository, never()).save(any());
        verify(consultaProducer, never()).dispararConsultaAgendada(any());
    }

    @Test
    @DisplayName("Deve editar data da consulta, salvar e disparar evento no RabbitMQ")
    void deveEditarConsultaEDispararEvento() {
        var idConsulta = UUID.randomUUID();
        var novaData = LocalDate.now()
                .with(TemporalAdjusters.next(DayOfWeek.THURSDAY))
                .atTime(11, 0);

        var consultaExistente = mock(Consulta.class);
        var consultaAlterada = mock(Consulta.class);

        when(consultaRepository.findById(idConsulta)).thenReturn(Optional.of(consultaExistente));
        when(consultaRepository.save(consultaExistente)).thenReturn(consultaAlterada);

        var resultado = consultaService.editar(idConsulta, novaData);

        assertNotNull(resultado);
        verify(consultaExistente, times(1)).alterarData(novaData);
        verify(consultaRepository, times(1)).save(consultaExistente);
        verify(consultaProducer, times(1)).dispararConsultaAlterada(consultaAlterada);
    }

    @Test
    @DisplayName("Não deve disparar evento ao tentar editar consulta que não existe")
    void naoDeveDispararEventoQuandoConsultaNaoExistirParaEdicao() {
        var idInexistente = UUID.randomUUID();
        var novaData = LocalDate.now()
                .with(TemporalAdjusters.next(DayOfWeek.FRIDAY))
                .atTime(15, 0);

        when(consultaRepository.findById(idInexistente)).thenReturn(Optional.empty());

        assertThrows(
                RegistroNaoEncontradoException.class,
                () -> consultaService.editar(idInexistente, novaData)
        );

        verify(consultaRepository, never()).save(any());
        verify(consultaProducer, never()).dispararConsultaAlterada(any());
    }

    @Test
    @DisplayName("Deve impedir agendamento em dias fechados (Domingo)")
    void deveLancarExcecaoQuandoAgendarNoDomingo() {
        var idMedico = UUID.randomUUID();
        var idPaciente = UUID.randomUUID();
        var domingo = LocalDate.now()
                .with(TemporalAdjusters.next(DayOfWeek.SUNDAY))
                .atTime(10, 0);

        var ex = assertThrows(
                CriteriosNaoAtendidosParaConsultaException.class,
                () -> consultaService.agendar(idMedico, idPaciente, domingo)
        );

        assertEquals("Consulta fora do horário de funcionamento.", ex.getMessage());
        verify(consultaRepository, never()).save(any());
        verifyNoInteractions(consultaProducer);
    }

    @Test
    @DisplayName("Deve impedir agendamento antes do horário de abertura (antes das 07:00)")
    void deveLancarExcecaoQuandoAgendarAntesDaAbertura() {
        var idMedico = UUID.randomUUID();
        var idPaciente = UUID.randomUUID();
        var antesDasSete = LocalDate.now()
                .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .atTime(6, 45);

        var ex = assertThrows(
                CriteriosNaoAtendidosParaConsultaException.class,
                () -> consultaService.agendar(idMedico, idPaciente, antesDasSete)
        );

        assertEquals("Consulta fora do horário de funcionamento.", ex.getMessage());
        verify(consultaRepository, never()).save(any());
        verifyNoInteractions(consultaProducer);
    }

    @Test
    @DisplayName("Deve impedir agendamento após o horário de encerramento (19:00 ou posterior)")
    void deveLancarExcecaoQuandoAgendarAposEncerramento() {
        var idMedico = UUID.randomUUID();
        var idPaciente = UUID.randomUUID();
        var aposFechamento = LocalDate.now()
                .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .atTime(19, 0);

        var ex = assertThrows(
                CriteriosNaoAtendidosParaConsultaException.class,
                () -> consultaService.agendar(idMedico, idPaciente, aposFechamento)
        );

        assertEquals("Consulta fora do horário de funcionamento.", ex.getMessage());
        verify(consultaRepository, never()).save(any());
        verifyNoInteractions(consultaProducer);
    }

    @Test
    @DisplayName("Deve impedir agendamento sem a antecedência mínima de 30 minutos")
    void deveLancarExcecaoQuandoNaoCumprirAntecedenciaMinima() {
        var idMedico = UUID.randomUUID();
        var idPaciente = UUID.randomUUID();

        var dataNoPassado = LocalDate.now()
                .with(TemporalAdjusters.previous(DayOfWeek.MONDAY))
                .atTime(10, 0);

        var ex = assertThrows(
                CriteriosNaoAtendidosParaConsultaException.class,
                () -> consultaService.agendar(idMedico, idPaciente, dataNoPassado)
        );

        assertEquals(
                "A consulta deve ser agendada ou remarcada com no mínimo 30 minutos de antecedência.",
                ex.getMessage()
        );
        verify(consultaRepository, never()).save(any());
        verifyNoInteractions(consultaProducer);
    }

    @Test
    @DisplayName("Deve validar horário e antecedência no método editar consulta")
    void deveLancarExcecaoAoEditarComHorarioInvalido() {
        var idConsulta = UUID.randomUUID();
        var domingo = LocalDate.now()
                .with(TemporalAdjusters.next(DayOfWeek.SUNDAY))
                .atTime(14, 0);

        var ex = assertThrows(
                CriteriosNaoAtendidosParaConsultaException.class,
                () -> consultaService.editar(idConsulta, domingo)
        );

        assertEquals("Consulta fora do horário de funcionamento.", ex.getMessage());
        verify(consultaRepository, never()).findById(any());
        verify(consultaRepository, never()).save(any());
        verifyNoInteractions(consultaProducer);
    }

    @Test
    @DisplayName("Deve listar todas as consultas do paciente quando buscarApenasFuturas for falso")
    void deveListarHistoricoCompletoDoPaciente() {
        var usuario = mock(Usuario.class);
        var usuarioId = UUID.randomUUID();
        var autoridade = new SimpleGrantedAuthority("ROLE_PACIENTE");

        when(usuario.getId()).thenReturn(usuarioId);
        doReturn(List.of(autoridade)).when(usuario).getAuthorities();
        when(consultaRepository.findByPacienteUsuarioIdOrderByDataDesc(usuarioId))
                .thenReturn(List.of(mock(Consulta.class)));

        var resultado = consultaService.listarHistorico(usuario, false);

        assertFalse(resultado.isEmpty());
        verify(consultaRepository, times(1)).findByPacienteUsuarioIdOrderByDataDesc(usuarioId);
        verify(consultaRepository, never()).findByPacienteUsuarioIdAndDataAfterOrderByDataAsc(any(), any());
    }

    @Test
    @DisplayName("Deve listar apenas consultas futuras do paciente quando buscarApenasFuturas for verdadeiro")
    void deveListarApenasConsultasFuturasDoPaciente() {
        var usuario = mock(Usuario.class);
        var usuarioId = UUID.randomUUID();
        var autoridade = new SimpleGrantedAuthority("ROLE_PACIENTE");

        when(usuario.getId()).thenReturn(usuarioId);
        doReturn(List.of(autoridade)).when(usuario).getAuthorities();
        when(consultaRepository.findByPacienteUsuarioIdAndDataAfterOrderByDataAsc(eq(usuarioId), any(LocalDateTime.class)))
                .thenReturn(List.of(mock(Consulta.class)));

        var resultado = consultaService.listarHistorico(usuario, true);

        assertFalse(resultado.isEmpty());
        verify(consultaRepository, times(1))
                .findByPacienteUsuarioIdAndDataAfterOrderByDataAsc(eq(usuarioId), any(LocalDateTime.class));
        verify(consultaRepository, never()).findByPacienteUsuarioIdOrderByDataDesc(any());
    }

    @Test
    @DisplayName("Deve listar todas as consultas do médico quando buscarApenasFuturas for falso")
    void deveListarHistoricoCompletoDoMedico() {
        var usuario = mock(Usuario.class);
        var usuarioId = UUID.randomUUID();
        var autoridade = new SimpleGrantedAuthority("ROLE_MEDICO");

        when(usuario.getId()).thenReturn(usuarioId);
        doReturn(List.of(autoridade)).when(usuario).getAuthorities();
        when(consultaRepository.findByMedicoUsuarioIdOrderByDataDesc(usuarioId))
                .thenReturn(List.of(mock(Consulta.class)));

        var resultado = consultaService.listarHistorico(usuario, false);

        assertFalse(resultado.isEmpty());
        verify(consultaRepository, times(1)).findByMedicoUsuarioIdOrderByDataDesc(usuarioId);
        verify(consultaRepository, never()).findByMedicoUsuarioIdAndDataAfterOrderByDataAsc(any(), any());
    }

    @Test
    @DisplayName("Deve listar apenas consultas futuras do médico quando buscarApenasFuturas for verdadeiro")
    void deveListarApenasConsultasFuturasDoMedico() {
        var usuario = mock(Usuario.class);
        var usuarioId = UUID.randomUUID();
        var autoridade = new SimpleGrantedAuthority("ROLE_MEDICO");

        when(usuario.getId()).thenReturn(usuarioId);
        doReturn(List.of(autoridade)).when(usuario).getAuthorities();
        when(consultaRepository.findByMedicoUsuarioIdAndDataAfterOrderByDataAsc(eq(usuarioId), any(LocalDateTime.class)))
                .thenReturn(List.of(mock(Consulta.class)));

        var resultado = consultaService.listarHistorico(usuario, true);

        assertFalse(resultado.isEmpty());
        verify(consultaRepository, times(1))
                .findByMedicoUsuarioIdAndDataAfterOrderByDataAsc(eq(usuarioId), any(LocalDateTime.class));
        verify(consultaRepository, never()).findByMedicoUsuarioIdOrderByDataDesc(any());
    }

    @Test
    @DisplayName("Deve listar todas as consultas gerais quando o perfil não for paciente nem médico (ex: Admin)")
    void deveListarHistoricoGeralParaAdmin() {
        var usuario = mock(Usuario.class);
        var autoridade = new SimpleGrantedAuthority("ROLE_ADMIN");

        when(usuario.getId()).thenReturn(UUID.randomUUID());
        doReturn(List.of(autoridade)).when(usuario).getAuthorities();
        when(consultaRepository.findAllByOrderByDataDesc())
                .thenReturn(List.of(mock(Consulta.class)));

        var resultado = consultaService.listarHistorico(usuario, false);

        assertFalse(resultado.isEmpty());
        verify(consultaRepository, times(1)).findAllByOrderByDataDesc();
        verify(consultaRepository, never()).findByDataAfterOrderByDataAsc(any());
    }

    @Test
    @DisplayName("Deve listar todas as consultas futuras gerais quando perfil for Admin e buscarApenasFuturas for verdadeiro")
    void deveListarConsultasFuturasGeraisParaAdmin() {
        var usuario = mock(Usuario.class);
        var autoridade = new SimpleGrantedAuthority("ROLE_ADMIN");

        when(usuario.getId()).thenReturn(UUID.randomUUID());
        doReturn(List.of(autoridade)).when(usuario).getAuthorities();
        when(consultaRepository.findByDataAfterOrderByDataAsc(any(LocalDateTime.class)))
                .thenReturn(List.of(mock(Consulta.class)));

        var resultado = consultaService.listarHistorico(usuario, true);

        assertFalse(resultado.isEmpty());
        verify(consultaRepository, times(1)).findByDataAfterOrderByDataAsc(any(LocalDateTime.class));
        verify(consultaRepository, never()).findAllByOrderByDataDesc();
    }

    @Test
    @DisplayName("Deve impedir agendamento e não publicar evento se o paciente já estiver ocupado")
    void deveLancarExcecaoQuandoPacienteEstiverOcupado() {
        var idMedico = UUID.randomUUID();
        var idPaciente = UUID.randomUUID();
        var dataConsulta = LocalDate.now()
                .with(TemporalAdjusters.next(DayOfWeek.TUESDAY))
                .atTime(14, 0);

        // Médico livre, mas paciente já tem consulta agendada no mesmo horário
        when(consultaRepository.existsByMedicoIdAndDataAndStatus(idMedico, dataConsulta, StatusConsulta.AGENDADA))
                .thenReturn(false);
        when(consultaRepository.existsByPacienteIdAndDataAndStatus(idPaciente, dataConsulta, StatusConsulta.AGENDADA))
                .thenReturn(true);

        var ex = assertThrows(
                CriteriosNaoAtendidosParaConsultaException.class,
                () -> consultaService.agendar(idMedico, idPaciente, dataConsulta)
        );

        assertEquals("O paciente já possui uma consulta agendada nesse mesmo horário.", ex.getMessage());
        verify(medicoRepository, never()).findById(any());
        verify(pacienteRepository, never()).findById(any());
        verify(consultaRepository, never()).save(any());
        verifyNoInteractions(consultaProducer);
    }
}