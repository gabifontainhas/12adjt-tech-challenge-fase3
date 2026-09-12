package br.com.gsordi.techchallenge3.agendamento.enfermeiro;

import br.com.gsordi.techchallenge3.agendamento.enfermeiro.dto.EnfermeiroDTO;
import br.com.gsordi.techchallenge3.agendamento.infra.security.SecurityFilter;
import br.com.gsordi.techchallenge3.agendamento.usuario.Usuario;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EnfermeiroController.class)
@EnableMethodSecurity
class EnfermeiroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private EnfermeiroService enfermeiroService;

    @MockitoBean
    private SecurityFilter securityFilter;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            var request = invocation.getArgument(0, ServletRequest.class);
            var response = invocation.getArgument(1, ServletResponse.class);
            var chain = invocation.getArgument(2, FilterChain.class);
            chain.doFilter(request, response);
            return null;
        }).when(securityFilter).doFilter(any(), any(), any());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve cadastrar enfermeiro com sucesso e retornar status 201 Created")
    void deveCadastrarEnfermeiroComSucesso() throws Exception {
        var idEnfermeiro = UUID.randomUUID();
        var coren = "COREN-SP-123456";
        var nome = "Ana Souza";
        var email = "ana.enfermeira@hospital.com";
        var senha = "senhaSegura123";

        var postRequest = new EnfermeiroDTO.PostRequest(nome, coren, email, senha);

        var usuarioMock = mock(Usuario.class);
        when(usuarioMock.getEmail()).thenReturn(email);

        var enfermeiroMock = mock(Enfermeiro.class);
        when(enfermeiroMock.getId()).thenReturn(idEnfermeiro);
        when(enfermeiroMock.getNome()).thenReturn(nome);
        when(enfermeiroMock.getCoren()).thenReturn(coren);
        when(enfermeiroMock.getUsuario()).thenReturn(usuarioMock);

        when(enfermeiroService.criarEnfermeiro(any(Enfermeiro.class), eq(email), eq(senha)))
                .thenReturn(enfermeiroMock);

        mockMvc.perform(post("/enfermeiros")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(idEnfermeiro.toString()))
                .andExpect(jsonPath("$.nome").value(nome))
                .andExpect(jsonPath("$.coren").value(coren))
                .andExpect(jsonPath("$.email").value(email));

        verify(enfermeiroService, times(1)).criarEnfermeiro(any(Enfermeiro.class), eq(email), eq(senha));
    }

    @Test
    @WithMockUser(roles = "ENFERMEIRO")
    @DisplayName("Deve listar enfermeiros com status 200 quando autenticado com perfil autorizado")
    void deveListarEnfermeirosComSucesso() throws Exception {
        var idEnfermeiro = UUID.randomUUID();
        var coren = "COREN-SP-123456";
        var nome = "Ana Souza";
        var email = "ana.enfermeira@hospital.com";

        var usuarioMock = mock(Usuario.class);
        when(usuarioMock.getEmail()).thenReturn(email);

        var enfermeiroMock = mock(Enfermeiro.class);
        when(enfermeiroMock.getId()).thenReturn(idEnfermeiro);
        when(enfermeiroMock.getNome()).thenReturn(nome);
        when(enfermeiroMock.getCoren()).thenReturn(coren);
        when(enfermeiroMock.getUsuario()).thenReturn(usuarioMock);

        when(enfermeiroService.listarTodos()).thenReturn(List.of(enfermeiroMock));

        mockMvc.perform(get("/enfermeiros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(idEnfermeiro.toString()))
                .andExpect(jsonPath("$[0].nome").value(nome))
                .andExpect(jsonPath("$[0].coren").value(coren))
                .andExpect(jsonPath("$[0].email").value(email));

        verify(enfermeiroService, times(1)).listarTodos();
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    @DisplayName("Deve retornar status 403 Forbidden ao listar enfermeiros com perfil PACIENTE")
    void deveNegarListagemParaPerfilPaciente() throws Exception {
        mockMvc.perform(get("/enfermeiros"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(enfermeiroService);
    }
}