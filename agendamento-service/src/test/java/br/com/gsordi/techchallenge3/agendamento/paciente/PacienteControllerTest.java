package br.com.gsordi.techchallenge3.agendamento.paciente;

import br.com.gsordi.techchallenge3.agendamento.infra.security.SecurityFilter;
import br.com.gsordi.techchallenge3.agendamento.paciente.dto.PacienteDTO;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PacienteController.class)
@EnableMethodSecurity
class PacienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private PacienteService pacienteService;

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
    @DisplayName("Deve cadastrar paciente com sucesso e retornar status 201 Created")
    void deveCadastrarPacienteComSucesso() throws Exception {
        var idPaciente = UUID.randomUUID();
        var cpfValido = "12345678909"; // Dígitos verificadores válidos para o validador @CPF
        var nome = "Maria Oliveira";
        var email = "maria.paciente@email.com";
        var senha = "senhaSegura123";

        var postRequest = new PacienteDTO.PostRequest(nome, cpfValido, email, senha);

        var usuarioMock = mock(Usuario.class);
        when(usuarioMock.getEmail()).thenReturn(email);

        var pacienteMock = mock(Paciente.class);
        when(pacienteMock.getId()).thenReturn(idPaciente);
        when(pacienteMock.getNome()).thenReturn(nome);
        when(pacienteMock.getCpf()).thenReturn(cpfValido);
        when(pacienteMock.getUsuario()).thenReturn(usuarioMock);

        when(pacienteService.criarPaciente(any(Paciente.class), eq(email), eq(senha)))
                .thenReturn(pacienteMock);

        mockMvc.perform(post("/pacientes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(idPaciente.toString()))
                .andExpect(jsonPath("$.cpf").value(cpfValido));

        verify(pacienteService, times(1)).criarPaciente(any(Paciente.class), eq(email), eq(senha));
    }

    @Test
    @WithMockUser(roles = "ENFERMEIRO")
    @DisplayName("Deve listar pacientes com status 200 quando autenticado com perfil autorizado")
    void deveListarPacientesComSucesso() throws Exception {
        var idPaciente = UUID.randomUUID();
        var cpf = "12345678909";
        var email = "maria.paciente@email.com";

        var usuarioMock = mock(Usuario.class);
        when(usuarioMock.getEmail()).thenReturn(email);

        var pacienteMock = mock(Paciente.class);
        when(pacienteMock.getId()).thenReturn(idPaciente);
        when(pacienteMock.getNome()).thenReturn("Maria Oliveira");
        when(pacienteMock.getCpf()).thenReturn(cpf);
        when(pacienteMock.getUsuario()).thenReturn(usuarioMock);

        when(pacienteService.listarTodos()).thenReturn(List.of(pacienteMock));

        mockMvc.perform(get("/pacientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(idPaciente.toString()))
                .andExpect(jsonPath("$[0].cpf").value(cpf));

        verify(pacienteService, times(1)).listarTodos();
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    @DisplayName("Deve retornar status 403 Forbidden ao listar pacientes com perfil PACIENTE")
    void deveNegarListagemParaPerfilPaciente() throws Exception {
        mockMvc.perform(get("/pacientes"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(pacienteService);
    }
}