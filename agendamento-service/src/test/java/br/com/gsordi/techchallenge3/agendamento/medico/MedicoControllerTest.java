package br.com.gsordi.techchallenge3.agendamento.medico;

import br.com.gsordi.techchallenge3.agendamento.infra.security.SecurityFilter;
import br.com.gsordi.techchallenge3.agendamento.medico.dto.MedicoDTO;
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

@WebMvcTest(MedicoController.class)
@EnableMethodSecurity
class MedicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private MedicoService medicoService;

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
    @DisplayName("Deve cadastrar médico com sucesso e retornar status 201 Created")
    void deveCadastrarMedicoComSucesso() throws Exception {
        var idMedico = UUID.randomUUID();
        var crm = "123456/SP";
        var nome = "Dr. Carlos Eduardo";
        var email = "carlos.medico@clinica.com";
        var senha = "senhaSegura123";

        var postRequest = new MedicoDTO.PostRequest(nome, crm, email, senha);

        var usuarioMock = mock(Usuario.class);
        when(usuarioMock.getEmail()).thenReturn(email);

        var medicoMock = mock(Medico.class);
        when(medicoMock.getId()).thenReturn(idMedico);
        when(medicoMock.getNome()).thenReturn(nome);
        when(medicoMock.getCrm()).thenReturn(crm);
        when(medicoMock.getUsuario()).thenReturn(usuarioMock);

        when(medicoService.criarMedico(any(Medico.class), eq(email), eq(senha)))
                .thenReturn(medicoMock);

        mockMvc.perform(post("/medicos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(idMedico.toString()))
                .andExpect(jsonPath("$.nome").value(nome))
                .andExpect(jsonPath("$.crm").value(crm))
                .andExpect(jsonPath("$.email").value(email));

        verify(medicoService, times(1)).criarMedico(any(Medico.class), eq(email), eq(senha));
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    @DisplayName("Deve listar médicos com status 200 quando autenticado com perfil autorizado")
    void deveListarMedicosComSucesso() throws Exception {
        var idMedico = UUID.randomUUID();
        var crm = "123456/SP";
        var nome = "Dr. Carlos Eduardo";
        var email = "carlos.medico@clinica.com";

        var usuarioMock = mock(Usuario.class);
        when(usuarioMock.getEmail()).thenReturn(email);

        var medicoMock = mock(Medico.class);
        when(medicoMock.getId()).thenReturn(idMedico);
        when(medicoMock.getNome()).thenReturn(nome);
        when(medicoMock.getCrm()).thenReturn(crm);
        when(medicoMock.getUsuario()).thenReturn(usuarioMock);

        when(medicoService.listarTodos()).thenReturn(List.of(medicoMock));

        mockMvc.perform(get("/medicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(idMedico.toString()))
                .andExpect(jsonPath("$[0].nome").value(nome))
                .andExpect(jsonPath("$[0].crm").value(crm))
                .andExpect(jsonPath("$[0].email").value(email));

        verify(medicoService, times(1)).listarTodos();
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    @DisplayName("Deve retornar status 403 Forbidden ao listar médicos com perfil PACIENTE")
    void deveNegarListagemParaPerfilPaciente() throws Exception {
        mockMvc.perform(get("/medicos"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(medicoService);
    }
}