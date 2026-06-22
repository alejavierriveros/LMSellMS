package cl.duoc.lmsellms;

import cl.duoc.lmsellms.controllers.VentaRESTController;
import cl.duoc.lmsellms.dtos.*;
import cl.duoc.lmsellms.services.VentaService;
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

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class VentaRESTControllerTest {

    private MockMvc mockMvc;

    @Mock
    private VentaService ventaService;

    @InjectMocks
    private VentaRESTController ventaRESTController;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private VentaResponseDTO responseSample;
    private VentaResponseForPaymentDTO paymentSample;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(ventaRESTController).build();

        responseSample = new VentaResponseDTO();
        responseSample.setId(1L);
        responseSample.setClienteId(10L);
        responseSample.setTotal(5000.0);
        responseSample.setDescuento("10%");
        responseSample.setDireccionEnvio("Av. Concha y Toro 1234");
        responseSample.setTotalFinal(4500.0);
        responseSample.setRealizado(true);

        paymentSample = new VentaResponseForPaymentDTO();
    }

    // ==========================================
    // TEST PARA POST (SAVE)
    // ==========================================
    @Test
    void testSave_Exito() throws Exception {
        VentaInputDTO input = new VentaInputDTO();

        when(ventaService.save(any(VentaInputDTO.class))).thenReturn(responseSample);

        mockMvc.perform(post("/api/v1/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(5000.0)); // Validamos por total para evitar el conflicto del Id capitalizado
    }

    // ==========================================
    // TESTS PARA GET (FIND ALL)
    // ==========================================
    @Test
    void testFindAll_ConElementos() throws Exception {
        when(ventaService.findAll()).thenReturn(List.of(responseSample));

        mockMvc.perform(get("/api/v1/ventas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].total").value(5000.0));
    }

    @Test
    void testFindAll_Vacio() throws Exception {
        when(ventaService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/ventas"))
                .andExpect(status().isNoContent());
    }

    // ==========================================
    // TESTS PARA GET (EXISTS BY ID)
    // ==========================================
    @Test
    void testExistsById_True() throws Exception {
        when(ventaService.existsById(1L)).thenReturn(true);

        mockMvc.perform(get("/api/v1/ventas/exists-by-id/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void testExistsById_False() throws Exception {
        when(ventaService.existsById(1L)).thenReturn(false);

        mockMvc.perform(get("/api/v1/ventas/exists-by-id/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    // ==========================================
    // TESTS PARA GET (FIND BY ID)
    // ==========================================
    @Test
    void testFindById_Encontrado() throws Exception {
        when(ventaService.findById(1L)).thenReturn(responseSample);

        mockMvc.perform(get("/api/v1/ventas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(5000.0));
    }

    @Test
    void testFindById_NoEncontrado() throws Exception {
        when(ventaService.findById(1L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/ventas/1"))
                .andExpect(status().isNotFound());
    }

    // ==========================================
    // TESTS PARA GET (FIND BY ID FOR PAYMENT)
    // ==========================================
    @Test
    void testFindByIdForPayment_Encontrado() throws Exception {
        when(ventaService.findByIdForPayment(1L)).thenReturn(paymentSample);

        mockMvc.perform(get("/api/v1/ventas/-by-id-for-payment/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testFindByIdForPayment_NoEncontrado() throws Exception {
        when(ventaService.findByIdForPayment(1L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/ventas/-by-id-for-payment/1"))
                .andExpect(status().isNotFound());
    }

    // ==========================================
    // TESTS PARA GET (FIND ALL BY CLIENTE ID)
    // ==========================================
    @Test
    void testFindAllByClienteId_ConElementos() throws Exception {
        when(ventaService.findAllByClienteId(10L)).thenReturn(List.of(responseSample));

        mockMvc.perform(get("/api/v1/ventas/list-all-by-cliente-id/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clienteId").value(10L));
    }

    @Test
    void testFindAllByClienteId_Vacio() throws Exception {
        when(ventaService.findAllByClienteId(10L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/ventas/list-all-by-cliente-id/10"))
                .andExpect(status().isNoContent());
    }

    // ==========================================
    // TESTS PARA GET (FIND BY RUN)
    // ==========================================
    @Test
    void testFindByRun_ConElementos() throws Exception {
        when(ventaService.findByRun("12345678-9")).thenReturn(List.of(responseSample));

        mockMvc.perform(get("/api/v1/ventas/list-all-by-cliente-run/12345678-9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].total").value(5000.0));
    }

    @Test
    void testFindByRun_Vacio() throws Exception {
        when(ventaService.findByRun("12345678-9")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/ventas/list-all-by-cliente-run/12345678-9"))
                .andExpect(status().isNoContent());
    }

    // ==========================================
    // TESTS PARA GET (FIND ALL BY MONTO)
    // ==========================================
    @Test
    void testFindAllByMonto_ConElementos() throws Exception {
        when(ventaService.findAllByMonto(5000.0)).thenReturn(List.of(responseSample));

        mockMvc.perform(get("/api/v1/ventas/list-all-by-monto/5000.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].total").value(5000.0));
    }

    @Test
    void testFindAllByMonto_Vacio() throws Exception {
        when(ventaService.findAllByMonto(5000.0)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/ventas/list-all-by-monto/5000.0"))
                .andExpect(status().isNoContent());
    }

    // ==========================================
    // TEST PARA PUT (UPDATE)
    // ==========================================
    @Test
    void testUpdate_Exito() throws Exception {
        VentaUpdateDTO updateDTO = new VentaUpdateDTO();

        when(ventaService.update(any(VentaUpdateDTO.class))).thenReturn(responseSample);

        mockMvc.perform(put("/api/v1/ventas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(5000.0));
    }

    // ==========================================
    // TEST PARA PUT (UPDATE ON CLOSE)
    // ==========================================
    @Test
    void testUpdateOnClose_Exito() throws Exception {
        VentaUpdateOnCloseDTO closeDTO = new VentaUpdateOnCloseDTO();

        when(ventaService.updateOnClose(any(VentaUpdateOnCloseDTO.class))).thenReturn(responseSample);

        mockMvc.perform(put("/api/v1/ventas/close-venta/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(closeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(5000.0));
    }

    // ==========================================
    // TESTS PARA DELETE (DELETE BY ID)
    // ==========================================
    @Test
    void testDeleteById_Exito() throws Exception {
        when(ventaService.deleteVentaById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/ventas/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteById_NoEncontrado() throws Exception {
        when(ventaService.deleteVentaById(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/v1/ventas/1"))
                .andExpect(status().isNotFound());
    }
}