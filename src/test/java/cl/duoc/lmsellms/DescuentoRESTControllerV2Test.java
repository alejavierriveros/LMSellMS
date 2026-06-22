package cl.duoc.lmsellms;

import cl.duoc.lmsellms.assemblers.DescuentoModelAssemblers;
import cl.duoc.lmsellms.controllers.DescuentoRESTControllerV2;
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
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
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

@ExtendWith(MockitoExtension.class)
public class DescuentoRESTControllerV2Test {

    private MockMvc mockMvc;

    @Mock
    private DescuentoService descuentoService;

    @Mock
    private DescuentoModelAssemblers assemblers;

    @InjectMocks
    private DescuentoRESTControllerV2 descuentoRESTControllerV2;

    // Se inicializa Jackson con soporte explícito para Java 8 java.time (LocalDate)
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            
    private DescuentoResponseDTO sampleDTO;
    private EntityModel<DescuentoResponseDTO> sampleEntityModel;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(descuentoRESTControllerV2).build();

        sampleDTO = new DescuentoResponseDTO();
        sampleDTO.setId(1L);
        sampleDTO.setNombre("CyberMonday");
        sampleDTO.setPorcentajeDcto(15.0);

        sampleEntityModel = EntityModel.of(sampleDTO, Link.of("http://localhost/api/v1/descuentos/1", "self"));
    }

    // ==========================================
    // TEST PARA POST (SAVE)
    // ==========================================
    @Test
    void testSave_Exito() throws Exception {
        DescuentoInputDTO input = new DescuentoInputDTO();
        input.setNombre("CyberMonday");
        input.setPorcentajeDcto(15.0);
        // Enviamos una fecha de expiración de 5 días en el futuro para que isFechaExpiracionValid() retorne TRUE
        input.setFechaExpiracion(LocalDate.now().plusDays(5));

        when(descuentoService.save(any(DescuentoInputDTO.class))).thenReturn(sampleDTO);
        when(assemblers.toModel(any(DescuentoResponseDTO.class))).thenReturn(sampleEntityModel);

        mockMvc.perform(post("/api/v1/descuentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/descuentos/1"));
    }

    // ==========================================
    // TESTS PARA GET (FIND ALL)
    // ==========================================
    @Test
    void testFindAll_ConElementos() throws Exception {
        when(descuentoService.findAll()).thenReturn(List.of(sampleDTO));
        when(assemblers.toModel(any(DescuentoResponseDTO.class))).thenReturn(sampleEntityModel);

        mockMvc.perform(get("/api/v1/descuentos"))
                .andExpect(status().isOk());
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

    // ==========================================
    // TESTS PARA GET (FIND BY ID)
    // ==========================================
    @Test
    void testFindById_Encontrado() throws Exception {
        when(descuentoService.findById(1L)).thenReturn(sampleDTO);
        when(assemblers.toModel(any(DescuentoResponseDTO.class))).thenReturn(sampleEntityModel);

        mockMvc.perform(get("/api/v1/descuentos/1"))
                .andExpect(status().isOk());
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
        when(descuentoService.findByNombre("CyberMonday")).thenReturn(sampleDTO);
        when(assemblers.toModel(any(DescuentoResponseDTO.class))).thenReturn(sampleEntityModel);

        mockMvc.perform(get("/api/v1/descuentos/by-nombre/CyberMonday"))
                .andExpect(status().isOk());
    }

    @Test
    void testFindByNombre_NoEncontrado() throws Exception {
        when(descuentoService.findByNombre("Inexistente")).thenReturn(null);

        mockMvc.perform(get("/api/v1/descuentos/by-nombre/Inexistente"))
                .andExpect(status().isNotFound());
    }

    // ==========================================
    // TESTS PARA GET (FIND VALUE BY ID FOR SELLING)
    // ==========================================
    @Test
    void testFindValueByIdForSelling_Encontrado() throws Exception {
        when(descuentoService.findValueByIdForSelling(1L)).thenReturn(15.0);

        mockMvc.perform(get("/api/v1/descuentos/value-by-id/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("15.0"));
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
        updateDTO.setId(1L);
        updateDTO.setNombre("CyberMonday Modificado");
        updateDTO.setPorcentajeDcto(20.0);

        when(descuentoService.update(any(DescuentoUpdateDTO.class))).thenReturn(sampleDTO);
        when(assemblers.toModel(any(DescuentoResponseDTO.class))).thenReturn(sampleEntityModel);

        mockMvc.perform(put("/api/v1/descuentos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(header().string("Location", "http://localhost/api/v1/descuentos/1"));
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