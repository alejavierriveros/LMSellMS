package cl.duoc.lmsellms;

import cl.duoc.lmsellms.assemblers.VentaModelAssemblers;
import cl.duoc.lmsellms.controllers.VentaRESTControllerV2;
import cl.duoc.lmsellms.dtos.VentaInputDTO;
import cl.duoc.lmsellms.dtos.VentaResponseDTO;
import cl.duoc.lmsellms.dtos.VentaUpdateDTO;
import cl.duoc.lmsellms.dtos.VentaResponseForPaymentDTO;
import cl.duoc.lmsellms.dtos.VentaUpdateOnCloseDTO;
import cl.duoc.lmsellms.services.VentaService;
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

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class VentaRESTControllerV2Test {

    private MockMvc mockMvc;

    @Mock
    private VentaService ventaService;

    @Mock
    private VentaModelAssemblers assemblers;

    @InjectMocks
    private VentaRESTControllerV2 ventaRESTControllerV2;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private VentaResponseDTO sampleDTO;
    private EntityModel<VentaResponseDTO> sampleEntityModel;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(ventaRESTControllerV2).build();

        sampleDTO = new VentaResponseDTO();
        sampleDTO.setId(1L);
        sampleDTO.setTotal(5000.0);

        // HATEOAS requiere el enlace "self" ya que el controlador hace .getRequiredLink("self") en SAVE y UPDATE
        sampleEntityModel = EntityModel.of(sampleDTO, Link.of("http://localhost/api/v2/ventas/1", "self"));
    }

    // ==========================================
    // TEST PARA POST (SAVE)
    // ==========================================
    @Test
    void testSave_Exito() throws Exception {
        VentaInputDTO input = new VentaInputDTO();
        // Si tu DTO posee campos obligatorios como @NotBlank o @NotNull, inicialízalos aquí.

        when(ventaService.save(any(VentaInputDTO.class))).thenReturn(sampleDTO);
        when(assemblers.toModel(any(VentaResponseDTO.class))).thenReturn(sampleEntityModel);

        mockMvc.perform(post("/api/v2/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v2/ventas/1"));
    }

    // ==========================================
    // TESTS PARA GET (FIND ALL)
    // ==========================================
    @Test
    void testFindAll_ConElementos() throws Exception {
        when(ventaService.findAll()).thenReturn(List.of(sampleDTO));
        when(assemblers.toModel(any(VentaResponseDTO.class))).thenReturn(sampleEntityModel);

        mockMvc.perform(get("/api/v2/ventas"))
                .andExpect(status().isOk());
    }

    @Test
    void testFindAll_Vacio() throws Exception {
        when(ventaService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v2/ventas"))
                .andExpect(status().isNoContent());
    }

    // ==========================================
    // TESTS PARA GET (EXISTS BY ID)
    // ==========================================
    @Test
    void testExistsById() throws Exception {
        when(ventaService.existsById(1L)).thenReturn(true);

        mockMvc.perform(get("/api/v2/ventas/exists-by-id/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    // ==========================================
    // TESTS PARA GET (FIND BY ID)
    // ==========================================
    @Test
    void testFindById_Encontrado() throws Exception {
        when(ventaService.findById(1L)).thenReturn(sampleDTO);
        when(assemblers.toModel(any(VentaResponseDTO.class))).thenReturn(sampleEntityModel);

        mockMvc.perform(get("/api/v2/ventas/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testFindById_NoEncontrado() throws Exception {
        when(ventaService.findById(1L)).thenReturn(null);

        mockMvc.perform(get("/api/v2/ventas/1"))
                .andExpect(status().isNotFound());
    }

    // ==========================================
    // TESTS PARA GET (FIND BY ID FOR PAYMENT)
    // ==========================================
    @Test
    void testFindByIdForPayment_Encontrado() throws Exception {
        VentaResponseForPaymentDTO paymentDTO = new VentaResponseForPaymentDTO();

        when(ventaService.findByIdForPayment(1L)).thenReturn(paymentDTO);

        mockMvc.perform(get("/api/v2/ventas/-by-id-for-payment/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testFindByIdForPayment_NoEncontrado() throws Exception {
        when(ventaService.findByIdForPayment(1L)).thenReturn(null);

        mockMvc.perform(get("/api/v2/ventas/-by-id-for-payment/1"))
                .andExpect(status().isNotFound());
    }

    // ==========================================
    // TESTS PARA GET (FIND ALL BY CLIENTE ID)
    // ==========================================
    @Test
    void testFindAllByClienteId_ConElementos() throws Exception {
        when(ventaService.findAllByClienteId(1L)).thenReturn(List.of(sampleDTO));
        when(assemblers.toModel(any(VentaResponseDTO.class))).thenReturn(sampleEntityModel);

        mockMvc.perform(get("/api/v2/ventas/list-all-by-cliente-id/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testFindAllByClienteId_Vacio() throws Exception {
        when(ventaService.findAllByClienteId(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v2/ventas/list-all-by-cliente-id/1"))
                .andExpect(status().isNoContent());
    }

    // ==========================================
    // TESTS PARA GET (FIND BY RUN)
    // ==========================================
    @Test
    void testFindByRun_ConElementos() throws Exception {
        when(ventaService.findByRun("12345678-9")).thenReturn(List.of(sampleDTO));
        when(assemblers.toModel(any(VentaResponseDTO.class))).thenReturn(sampleEntityModel);

        mockMvc.perform(get("/api/v2/ventas/list-all-by-cliente-run/12345678-9"))
                .andExpect(status().isOk());
    }

    @Test
    void testFindByRun_Vacio() throws Exception {
        when(ventaService.findByRun("12345678-9")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v2/ventas/list-all-by-cliente-run/12345678-9"))
                .andExpect(status().isNoContent());
    }

    // ==========================================
    // TESTS PARA GET (FIND ALL BY MONTO)
    // ==========================================
    @Test
    void testFindAllByMonto_ConElementos() throws Exception {
        when(ventaService.findAllByMonto(5000.0)).thenReturn(List.of(sampleDTO));
        when(assemblers.toModel(any(VentaResponseDTO.class))).thenReturn(sampleEntityModel);

        mockMvc.perform(get("/api/v2/ventas/list-all-by-monto/5000.0"))
                .andExpect(status().isOk());
    }

    @Test
    void testFindAllByMonto_Vacio() throws Exception {
        when(ventaService.findAllByMonto(5000.0)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v2/ventas/list-all-by-monto/5000.0"))
                .andExpect(status().isNoContent());
    }

    // ==========================================
    // TEST PARA PUT (UPDATE)
    // ==========================================
    @Test
    void testUpdate_Exito() throws Exception {
        VentaUpdateDTO updateDTO = new VentaUpdateDTO();

        when(ventaService.update(any(VentaUpdateDTO.class))).thenReturn(sampleDTO);
        when(assemblers.toModel(any(VentaResponseDTO.class))).thenReturn(sampleEntityModel);

        mockMvc.perform(put("/api/v2/ventas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(header().string("Location", "http://localhost/api/v2/ventas/1"));
    }

    // ==========================================
    // TEST PARA PUT (UPDATE ON CLOSE)
    // ==========================================
    @Test
    void testUpdateOnClose_Exito() throws Exception {
        VentaUpdateOnCloseDTO closeDTO = new VentaUpdateOnCloseDTO();

        when(ventaService.updateOnClose(any(VentaUpdateOnCloseDTO.class))).thenReturn(sampleDTO);
        when(assemblers.toModel(any(VentaResponseDTO.class))).thenReturn(sampleEntityModel);

        mockMvc.perform(put("/api/v2/ventas/close-venta/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(closeDTO)))
                .andExpect(status().isOk())
                .andExpect(header().string("Location", "http://localhost/api/v2/ventas/1"));
    }

    // ==========================================
    // TESTS PARA DELETE (DELETE BY ID)
    // ==========================================
    @Test
    void testDeleteById_Exito() throws Exception {
        when(ventaService.deleteVentaById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/v2/ventas/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteById_NoEncontrado() throws Exception {
        when(ventaService.deleteVentaById(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/v2/ventas/1"))
                .andExpect(status().isNotFound());
    }
}