package br.com.gsordi.techchallenge3.agendamento.enfermeiro;

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
class EnfermeiroServiceTest {

    @Mock
    private EnfermeiroRepository enfermeiroRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private EnfermeiroService enfermeiroService;

    @Test
    @DisplayName("Deve cadastrar enfermeiro com sucesso, associar usuário com perfil ENFERMEIRO e persistir")
    void deveCriarEnfermeiroComSucesso() {
        var coren = "COREN-SP-123456";
        var email = "enfermeiro@hospital.com";
        var senha = "senhaSegura123";

        var enfermeiro = mock(Enfermeiro.class);
        var usuario = mock(Usuario.class);
        var enfermeiroSalvo = mock(Enfermeiro.class);

        doCallRealMethod().when(enfermeiro).associarUsuario(any(Usuario.class));

        when(enfermeiro.getCoren()).thenReturn(coren);
        when(enfermeiroRepository.existsByCoren(coren)).thenReturn(false);
        when(usuarioService.criarUsuario(email, senha, Perfil.ENFERMEIRO)).thenReturn(usuario);
        when(enfermeiroRepository.save(enfermeiro)).thenReturn(enfermeiroSalvo);

        var resultado = enfermeiroService.criarEnfermeiro(enfermeiro, email, senha);

        assertNotNull(resultado);
        assertEquals(enfermeiroSalvo, resultado);
        verify(enfermeiroRepository, times(1)).existsByCoren(coren);
        verify(usuarioService, times(1)).criarUsuario(email, senha, Perfil.ENFERMEIRO);
        verify(enfermeiro, times(1)).associarUsuario(usuario);
        verify(enfermeiroRepository, times(1)).save(enfermeiro);
    }
    @Test
    @DisplayName("Deve lançar RegistroJaExisteException se o COREN já estiver cadastrado")
    void deveLancarExcecaoQuandoCorenJaExistir() {
        var coren = "COREN-SP-123456";
        var email = "enfermeiro@hospital.com";
        var senha = "senhaSegura123";

        var enfermeiro = mock(Enfermeiro.class);
        when(enfermeiro.getCoren()).thenReturn(coren);
        when(enfermeiroRepository.existsByCoren(coren)).thenReturn(true);

        var ex = assertThrows(
                RegistroJaExisteException.class,
                () -> enfermeiroService.criarEnfermeiro(enfermeiro, email, senha)
        );

        assertEquals("Coren já cadastrado na base", ex.getMessage());
        verify(enfermeiroRepository, times(1)).existsByCoren(coren);
        verifyNoInteractions(usuarioService);
        verify(enfermeiro, never()).associarUsuario(any());
        verify(enfermeiroRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar todos os enfermeiros cadastrados")
    void deveListarTodosOsEnfermeiros() {
        var listaMock = List.of(mock(Enfermeiro.class), mock(Enfermeiro.class));
        when(enfermeiroRepository.findAll()).thenReturn(listaMock);

        var resultado = enfermeiroService.listarTodos();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(enfermeiroRepository, times(1)).findAll();
    }


}