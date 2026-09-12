package br.com.gsordi.techchallenge3.agendamento.medico;

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
class MedicoServiceTest {

    @Mock
    private MedicoRepository medicoRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private MedicoService medicoService;

    @Test
    @DisplayName("Deve cadastrar médico com sucesso criando usuário associado e salvando na base")
    void deveCriarMedicoComSucesso() {
        var crm = "123456/SP";
        var email = "medico@clinica.com";
        var senha = "senhaSegura123";

        var medico = mock(Medico.class);
        var usuario = mock(Usuario.class);
        var medicoSalvo = mock(Medico.class);

        doCallRealMethod().when(medico).associarUsuario(any(Usuario.class));

        when(medico.getCrm()).thenReturn(crm);
        when(medicoRepository.existsByCrm(crm)).thenReturn(false);
        when(usuarioService.criarUsuario(email, senha, Perfil.MEDICO)).thenReturn(usuario);
        when(medicoRepository.save(medico)).thenReturn(medicoSalvo);

        var resultado = medicoService.criarMedico(medico, email, senha);

        assertNotNull(resultado);
        assertEquals(medicoSalvo, resultado);
        verify(medicoRepository, times(1)).existsByCrm(crm);
        verify(usuarioService, times(1)).criarUsuario(email, senha, Perfil.MEDICO);
        verify(medico, times(1)).associarUsuario(usuario);
        verify(medicoRepository, times(1)).save(medico);
    }

    @Test
    @DisplayName("Deve lançar RegistroJaExisteException se o CRM já estiver cadastrado")
    void deveLancarExcecaoQuandoCrmJaExistir() {
        var crm = "123456/SP";
        var email = "medico@clinica.com";
        var senha = "senhaSegura123";

        var medico = mock(Medico.class);
        when(medico.getCrm()).thenReturn(crm);
        when(medicoRepository.existsByCrm(crm)).thenReturn(true);

        var ex = assertThrows(
                RegistroJaExisteException.class,
                () -> medicoService.criarMedico(medico, email, senha)
        );

        assertEquals("Crm já cadastrado na base", ex.getMessage());
        verify(medicoRepository, times(1)).existsByCrm(crm);
        verifyNoInteractions(usuarioService);
        verify(medico, never()).associarUsuario(any());
        verify(medicoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar todos os médicos cadastrados")
    void deveListarTodosOsMedicos() {
        var listaMock = List.of(mock(Medico.class), mock(Medico.class));
        when(medicoRepository.findAll()).thenReturn(listaMock);

        var resultado = medicoService.listarTodos();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(medicoRepository, times(1)).findAll();
    }

}