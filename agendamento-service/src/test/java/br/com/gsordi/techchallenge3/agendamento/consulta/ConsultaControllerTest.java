package br.com.gsordi.techchallenge3.agendamento.consulta;

import br.com.gsordi.techchallenge3.agendamento.consulta.dto.ConsultaDTO;
import br.com.gsordi.techchallenge3.agendamento.infra.security.SecurityFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ConsultaController.class)
@EnableMethodSecurity
class ConsultaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @MockitoBean
    private ConsultaService consultaService;

    @MockitoBean
    private ConsultaMapper consultaMapper;

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
    @WithMockUser(roles = "MEDICO")
    @DisplayName("Deve agendar consulta com sucesso e retornar status 200")
    void deveAgendarConsultaComSucesso() throws Exception {
        var idMedico = UUID.randomUUID();
        var idPaciente = UUID.randomUUID();
        var data = LocalDate.now()
                .with(TemporalAdjusters.next(DayOfWeek.TUESDAY))
                .atTime(14, 0);

        var postRequest = new ConsultaDTO.PostRequest(idMedico, idPaciente, data);
        var consultaMock = mock(Consulta.class);
        var responseMock = new ConsultaDTO.Response(
                UUID.randomUUID(),
                idMedico,
                idPaciente,
                data,
                StatusConsulta.AGENDADA
        );



        when(consultaService.agendar(eq(idMedico), eq(idPaciente), eq(data))).thenReturn(consultaMock);
        when(consultaMapper.toResponse(consultaMock)).thenReturn(responseMock);

        mockMvc.perform(post("/consultas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseMock.id().toString()))
                .andExpect(jsonPath("$.idMedico").value(idMedico.toString()))
                .andExpect(jsonPath("$.idPaciente").value(idPaciente.toString()));

        verify(consultaService, times(1)).agendar(eq(idMedico), eq(idPaciente), eq(data));
        verify(consultaMapper, times(1)).toResponse(consultaMock);
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    @DisplayName("Deve editar consulta com sucesso e retornar status 200")
    void deveEditarConsultaComSucesso() throws Exception {
        var idConsulta = UUID.randomUUID();
        var novaData = LocalDate.now()
                .with(TemporalAdjusters.next(DayOfWeek.THURSDAY))
                .atTime(15, 0);

        var putRequest = new ConsultaDTO.PutRequest(novaData);
        var consultaMock = mock(Consulta.class);
        var responseMock = new ConsultaDTO.Response(
                idConsulta,
                UUID.randomUUID(),
                UUID.randomUUID(),
                novaData,
                StatusConsulta.AGENDADA
        );

        when(consultaService.editar(eq(idConsulta), eq(novaData))).thenReturn(consultaMock);
        when(consultaMapper.toResponse(consultaMock)).thenReturn(responseMock);

        mockMvc.perform(put("/consultas/{id}", idConsulta)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(putRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(idConsulta.toString()));

        verify(consultaService, times(1)).editar(eq(idConsulta), eq(novaData));
        verify(consultaMapper, times(1)).toResponse(consultaMock);
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    @DisplayName("Deve retornar 403 Forbidden ao tentar agendar com perfil de PACIENTE")
    void deveNegarAgendamentoParaPerfilPaciente() throws Exception {
        var idMedico = UUID.randomUUID();
        var idPaciente = UUID.randomUUID();
        var data = LocalDate.now()
                .with(TemporalAdjusters.next(DayOfWeek.TUESDAY))
                .atTime(14, 0);

        var postRequest = new ConsultaDTO.PostRequest(idMedico, idPaciente, data);

        mockMvc.perform(post("/consultas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(consultaService);
    }

    @Test
    @WithMockUser(roles = "MEDICO")
    @DisplayName("Deve retornar status 400 Bad Request ao agendar com data retroativa")
    void deveRetornarBadRequestComDataPassada() throws Exception {
        var idMedico = UUID.randomUUID();
        var idPaciente = UUID.randomUUID();
        var dataPassada = LocalDateTime.now().minusDays(1);

        var postRequestInvalido = new ConsultaDTO.PostRequest(idMedico, idPaciente, dataPassada);

        mockMvc.perform(post("/consultas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequestInvalido)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(consultaService);
        verifyNoInteractions(consultaMapper);
    }
}