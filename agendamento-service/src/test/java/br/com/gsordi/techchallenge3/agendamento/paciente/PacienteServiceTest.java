package br.com.gsordi.techchallenge3.agendamento.paciente;

import br.com.gsordi.techchallenge3.agendamento.infra.exception.RegistroJaExisteException;
import br.com.gsordi.techchallenge3.agendamento.usuario.Perfil;
import br.com.gsordi.techchallenge3.agendamento.usuario.Usuario;
import br.com.gsordi.techchallenge3.agendamento.usuario.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private PacienteService pacienteService;

    @Test
    @DisplayName("Deve cadastrar paciente com sucesso, associar usuário com perfil PACIENTE e persistir")
    void deveCriarPacienteComSucesso() {
        var cpf = "12345678900";
        var email = "paciente@email.com";
        var senha = "senhaSegura123";

        var paciente = mock(Paciente.class);
        var usuario = mock(Usuario.class);
        var pacienteSalvo = mock(Paciente.class);

        doCallRealMethod().when(paciente).associarUsuario(any(Usuario.class));

        when(paciente.getCpf()).thenReturn(cpf);
        when(pacienteRepository.existsByCpf(cpf)).thenReturn(false);
        when(usuarioService.criarUsuario(email, senha, Perfil.PACIENTE)).thenReturn(usuario);
        when(pacienteRepository.save(paciente)).thenReturn(pacienteSalvo);

        var resultado = pacienteService.criarPaciente(paciente, email, senha);

        assertNotNull(resultado);
        assertEquals(pacienteSalvo, resultado);
        verify(pacienteRepository, times(1)).existsByCpf(cpf);
        verify(usuarioService, times(1)).criarUsuario(email, senha, Perfil.PACIENTE);
        verify(paciente, times(1)).associarUsuario(usuario);
        verify(pacienteRepository, times(1)).save(paciente);
    }

    @Test
    @DisplayName("Deve lançar RegistroJaExisteException se o CPF já estiver cadastrado")
    void deveLancarExcecaoQuandoCpfJaExistir() {
        var cpf = "12345678900";
        var email = "paciente@email.com";
        var senha = "senhaSegura123";

        var paciente = mock(Paciente.class);
        when(paciente.getCpf()).thenReturn(cpf);
        when(pacienteRepository.existsByCpf(cpf)).thenReturn(true);

        var ex = assertThrows(
                RegistroJaExisteException.class,
                () -> pacienteService.criarPaciente(paciente, email, senha)
        );

        assertEquals("CPF já cadastrado na base", ex.getMessage());
        verify(pacienteRepository, times(1)).existsByCpf(cpf);
        verifyNoInteractions(usuarioService);
        verify(paciente, never()).associarUsuario(any());
        verify(pacienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar todos os pacientes cadastrados")
    void deveListarTodosOsPacientes() {
        var listaMock = List.of(mock(Paciente.class), mock(Paciente.class));
        when(pacienteRepository.findAll()).thenReturn(listaMock);

        var resultado = pacienteService.listarTodos();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(pacienteRepository, times(1)).findAll();
    }

}