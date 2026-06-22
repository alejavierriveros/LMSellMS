package cl.duoc.lmsellms;

import cl.duoc.lmsellms.controllers.DescuentoRESTController;
import cl.duoc.lmsellms.dtos.DescuentoInputDTO;
import cl.duoc.lmsellms.dtos.DescuentoResponseDTO;
import cl.duoc.lmsellms.dtos.DescuentoUpdateDTO;
import cl.duoc.lmsellms.services.DescuentoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class) // Inicialización ultra rápida con Mockito puro, sin levantar Spring completo
public class DescuentoRESTControllerTest {

    private MockMvc mockMvc;

    @Mock // Mock estándar de Mockito
    private DescuentoService descuentoService;

    @InjectMocks // Inyecta automáticamente el servicio mockeado dentro del controlador
    private DescuentoRESTController descuentoRESTController;

    // Se registra el JavaTimeModule para que Jackson sepa cómo serializar LocalDate sin ayuda de Spring
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private DescuentoResponseDTO responseSample;

    @BeforeEach
    void setUp() {
        // Configuramos MockMvc de forma aislada apuntando directo a nuestro controlador independiente
        this.mockMvc = MockMvcBuilders.standaloneSetup(descuentoRESTController).build();

        responseSample = new DescuentoResponseDTO();
        responseSample.setId(1L);
        responseSample.setNombre("WINTER26");
        responseSample.setPorcentajeDcto(10.0);
        responseSample.setFechaExpiracion(null);
    }

    // ==========================================
    // TEST PARA POST (SAVE)
    // ==========================================
    @Test
    void testSave_Exito() throws Exception {
        DescuentoInputDTO input = new DescuentoInputDTO();
        input.setNombre("WINTER26");
        input.setPorcentajeDcto(10.0);
        // Seteamos una fecha futura (+5 días) para satisfacer la validación fechaExpiracionValid
        input.setFechaExpiracion(LocalDate.now().plusDays(5)); 

        when(descuentoService.save(any(DescuentoInputDTO.class))).thenReturn(responseSample);

        mockMvc.perform(post("/api/v1/descuentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("WINTER26"));
    }

    // ==========================================
    // TESTS PARA GET (FIND ALL)
    // ==========================================
    @Test
    void testFindAll_ConElementos() throws Exception {
        when(descuentoService.findAll()).thenReturn(List.of(responseSample));

        mockMvc.perform(get("/api/v1/descuentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void testFindAll_Vacio() throws Exception {
        when(descuentoService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/descuentos"))
                .andExpect(status().isNoContent());
    }

    // ==========================================
    // TESTS PARA GET (EXISTS BY ID)
    // ==========================================
    @Test
    void testExistsById_True() throws Exception {
        when(descuentoService.existsById(1L)).thenReturn(true);

        mockMvc.perform(get("/api/v1/descuentos/exists-by-id/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void testExistsById_False() throws Exception {
        when(descuentoService.existsById(1L)).thenReturn(false);

        mockMvc.perform(get("/api/v1/descuentos/exists-by-id/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    // ==========================================
    // TESTS PARA GET (FIND BY ID)
    // ==========================================
    @Test
    void testFindById_Encontrado() throws Exception {
        when(descuentoService.findById(1L)).thenReturn(responseSample);

        mockMvc.perform(get("/api/v1/descuentos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void testFindById_NoEncontrado() throws Exception {
        when(descuentoService.findById(1L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/descuentos/1"))
                .andExpect(status().isNotFound());
    }

    // ==========================================
    // TESTS PARA GET (FIND BY NOMBRE)
    // ==========================================
    @Test
    void testFindByNombre_Encontrado() throws Exception {
        when(descuentoService.findByNombre("WINTER26")).thenReturn(responseSample);

        mockMvc.perform(get("/api/v1/descuentos/by-nombre/WINTER26"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("WINTER26"));
    }

    @Test
    void testFindByNombre_NoEncontrado() throws Exception {
        when(descuentoService.findByNombre("ERROR")).thenReturn(null);

        mockMvc.perform(get("/api/v1/descuentos/by-nombre/ERROR"))
                .andExpect(status().isNotFound());
    }

    // ==========================================
    // TESTS PARA GET (FIND VALUE BY ID)
    // ==========================================
    @Test
    void testFindValueByIdForSelling_Encontrado() throws Exception {
        when(descuentoService.findValueByIdForSelling(1L)).thenReturn(10.0);

        mockMvc.perform(get("/api/v1/descuentos/value-by-id/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("10.0"));
    }

    @Test
    void testFindValueByIdForSelling_NoEncontrado() throws Exception {
        when(descuentoService.findValueByIdForSelling(1L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/descuentos/value-by-id/1"))
                .andExpect(status().isNotFound());
    }

    // ==========================================
    // TEST PARA PUT (UPDATE)
    // ==========================================
    @Test
    void testUpdate_Exito() throws Exception {
        DescuentoUpdateDTO updateDTO = new DescuentoUpdateDTO();
        updateDTO.setId(1L);             // Cumple con @NotNull en el ID de actualización
        updateDTO.setNombre("WINTER26"); // Cumple con @NotBlank en el nombre
        updateDTO.setPorcentajeDcto(10.0);
        
        when(descuentoService.update(any(DescuentoUpdateDTO.class))).thenReturn(responseSample);

        mockMvc.perform(put("/api/v1/descuentos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    // ==========================================
    // TESTS PARA DELETE (DELETE BY ID)
    // ==========================================
    @Test
    void testDeleteById_Exito() throws Exception {
        when(descuentoService.deleteDescuentoById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/descuentos/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteById_NoEncontrado() throws Exception {
        when(descuentoService.deleteDescuentoById(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/v1/descuentos/1"))
                .andExpect(status().isNotFound());
    }
}