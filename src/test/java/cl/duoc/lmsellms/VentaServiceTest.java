package cl.duoc.lmsellms;

import cl.duoc.lmsellms.clients.ToAPICartFeign;
import cl.duoc.lmsellms.clients.ToAPICustomerFeign;
import cl.duoc.lmsellms.dtos.*;
import cl.duoc.lmsellms.exceptions.FailedAPICallResponseExeption;
import cl.duoc.lmsellms.exceptions.IdExisteException;
import cl.duoc.lmsellms.exceptions.IdNoExisteException;
import cl.duoc.lmsellms.mapper.*;
import cl.duoc.lmsellms.models.Descuento;
import cl.duoc.lmsellms.models.Venta;
import cl.duoc.lmsellms.repositories.DescuentoRepository;
import cl.duoc.lmsellms.repositories.VentaRepository;
import cl.duoc.lmsellms.services.VentaService;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VentaServiceTest {

    @Mock private VentaRepository ventaRepository;
    @Mock private DescuentoRepository descuentoRepository;
    @Mock private ToAPICartFeign toAPICartFeign;
    @Mock private ToAPICustomerFeign toAPICustomerFeign;
    @Mock private VentaResponseMapper ventaResponseMapper;
    @Mock private VentaInputMapper ventaInputMapper;
    @Mock private VentaUpdateMapper ventaUpdateMapper;
    @Mock private VentaUpdateOnCloseMapper ventaUpdateOnCloseMapper;
    @Mock private VentaResponseForPaymentMapper ventaResponseForPaymentMapper;

    @InjectMocks
    private VentaService ventaService;

    private Venta ventaSample;
    private Descuento descuentoSample;
    private VentaResponseDTO responseDTOSample;
    private VentaResponseForPaymentDTO paymentDTOSample;

    @BeforeEach
    void setUp() {
        ventaSample = new Venta();
        ventaSample.setId(1L);
        ventaSample.setClienteId(100L);
        ventaSample.setTotalFinal(15000.0);

        descuentoSample = new Descuento();
        descuentoSample.setId(1L);

        responseDTOSample = new VentaResponseDTO();
        responseDTOSample.setId(1L);

        paymentDTOSample = new VentaResponseForPaymentDTO();
        paymentDTOSample.setId(1L);
    }

    // ==========================================
    // PRUEBAS PARA EL MÉTODO SAVE()
    // ==========================================
    @Test
    void testSave_Exito() {
        VentaInputDTO input = new VentaInputDTO();
        input.setCarritoId(1L);
        input.setDireccionId(2L);
        input.setDescuentoId(1L);

        CarritoOrderResponseDTO cartMock = new CarritoOrderResponseDTO();
        cartMock.setClienteId(100L);
        cartMock.setTotal(15000.0);

        ClienteOrderResponseDTO clientMock = new ClienteOrderResponseDTO();
        DireccionResponseDTO dirMock = new DireccionResponseDTO();

        when(ventaRepository.existsByCarritoId(1L)).thenReturn(false);
        when(toAPICartFeign.findByIdForOrder(1L)).thenReturn(cartMock);
        when(toAPICustomerFeign.findById(100L)).thenReturn(clientMock);
        when(toAPICustomerFeign.findDireccionById(2L)).thenReturn(dirMock);
        when(descuentoRepository.existsById(1L)).thenReturn(true);
        when(descuentoRepository.findById(1L)).thenReturn(Optional.of(descuentoSample));
        when(ventaInputMapper.toEntity(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(ventaSample);
        when(ventaRepository.save(any(Venta.class))).thenReturn(ventaSample);
        when(ventaResponseMapper.toDto(any(Venta.class))).thenReturn(responseDTOSample);

        VentaResponseDTO result = ventaService.save(input);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testSave_IdExisteException_CarritoDuplicado() {
        VentaInputDTO input = new VentaInputDTO();
        input.setCarritoId(1L);
        when(ventaRepository.existsByCarritoId(1L)).thenReturn(true);

        assertThrows(IdExisteException.class, () -> ventaService.save(input));
    }

    @Test
    void testSave_IdNoExisteException_CarritoNoEncontrado() {
        VentaInputDTO input = new VentaInputDTO();
        input.setCarritoId(1L);

        when(ventaRepository.existsByCarritoId(1L)).thenReturn(false);
        Request request = Request.create(Request.HttpMethod.GET, "/url", Collections.emptyMap(), null, null, null);
        when(toAPICartFeign.findByIdForOrder(1L)).thenThrow(new FeignException.NotFound("Not Found", request, null, null));

        assertThrows(IdNoExisteException.class, () -> ventaService.save(input));
    }

    @Test
    void testSave_FailedAPICallResponseException_CarritoInaccesible() {
        VentaInputDTO input = new VentaInputDTO();
        input.setCarritoId(1L);

        when(ventaRepository.existsByCarritoId(1L)).thenReturn(false);
        when(toAPICartFeign.findByIdForOrder(1L)).thenThrow(new RuntimeException("Timeout"));

        assertThrows(FailedAPICallResponseExeption.class, () -> ventaService.save(input));
    }

    // ==========================================
    // PRUEBAS PARA MÉTODOS DE LECTURA (READ)
    // ==========================================
    @Test
    void testFindAll() {
        when(ventaRepository.findAll()).thenReturn(List.of(ventaSample));
        when(ventaResponseMapper.toDto(ventaSample)).thenReturn(responseDTOSample);

        List<VentaResponseDTO> list = ventaService.findAll();
        assertFalse(list.isEmpty());
        assertEquals(1, list.size());
    }

    @Test
    void testExistsById() {
        when(ventaRepository.existsById(1L)).thenReturn(true);
        assertTrue(ventaService.existsById(1L));
    }

    @Test
    void testFindById_Exito() {
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(ventaSample));
        when(ventaResponseMapper.toDto(ventaSample)).thenReturn(responseDTOSample);

        VentaResponseDTO result = ventaService.findById(1L);
        assertNotNull(result);
    }

    @Test
    void testFindById_IdNoExisteException() {
        when(ventaRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IdNoExisteException.class, () -> ventaService.findById(1L));
    }

    @Test
    void testFindByIdForPayment_Exito() {
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(ventaSample));
        when(ventaResponseForPaymentMapper.toDto(ventaSample)).thenReturn(paymentDTOSample);

        VentaResponseForPaymentDTO result = ventaService.findByIdForPayment(1L);
        assertNotNull(result);
    }

    @Test
    void testFindAllByMonto() {
        when(ventaRepository.findAllByTotalFinal(15000.0)).thenReturn(List.of(ventaSample));
        when(ventaResponseMapper.toDto(ventaSample)).thenReturn(responseDTOSample);

        List<VentaResponseDTO> result = ventaService.findAllByMonto(15000.0);
        assertFalse(result.isEmpty());
    }

    @Test
    void testFindByRun() {
        when(ventaRepository.findByClienteRun("12345678-9")).thenReturn(List.of(ventaSample));
        when(ventaResponseMapper.toDto(ventaSample)).thenReturn(responseDTOSample);

        List<VentaResponseDTO> result = ventaService.findByRun("12345678-9");
        assertFalse(result.isEmpty());
    }

    @Test
    void testFindAllByClienteId() {
        when(ventaRepository.findAllByClienteId(100L)).thenReturn(List.of(ventaSample));
        when(ventaResponseMapper.toDto(ventaSample)).thenReturn(responseDTOSample);

        List<VentaResponseDTO> result = ventaService.findAllByClienteId(100L);
        assertFalse(result.isEmpty());
    }

    // ==========================================
    // PRUEBAS PARA EL MÉTODO UPDATE()
    // ==========================================
    @Test
    void testUpdate_Exito() {
        VentaUpdateDTO updateDTO = new VentaUpdateDTO();
        updateDTO.setId(1L);
        updateDTO.setDireccionId(2L);
        updateDTO.setDescuentoId(1L);

        DireccionResponseDTO dirMock = new DireccionResponseDTO();
        dirMock.setClienteId(100L); 

        when(ventaRepository.findById(1L)).thenReturn(Optional.of(ventaSample));
        when(toAPICustomerFeign.findDireccionById(2L)).thenReturn(dirMock);
        when(descuentoRepository.existsById(1L)).thenReturn(true);
        when(descuentoRepository.findById(1L)).thenReturn(Optional.of(descuentoSample));
        when(ventaUpdateMapper.toEntity(any(), any(), any(), any())).thenReturn(ventaSample);
        when(ventaRepository.save(any(Venta.class))).thenReturn(ventaSample);
        when(ventaResponseMapper.toDto(any(Venta.class))).thenReturn(responseDTOSample);

        VentaResponseDTO result = ventaService.update(updateDTO);
        assertNotNull(result);
    }

    @Test
    void testUpdate_DireccionNoCorrespondeACliente() {
        VentaUpdateDTO updateDTO = new VentaUpdateDTO();
        updateDTO.setId(1L);
        updateDTO.setDireccionId(2L);

        DireccionResponseDTO dirMock = new DireccionResponseDTO();
        dirMock.setClienteId(999L); 

        when(ventaRepository.findById(1L)).thenReturn(Optional.of(ventaSample));
        when(toAPICustomerFeign.findDireccionById(2L)).thenReturn(dirMock);

        assertThrows(FailedAPICallResponseExeption.class, () -> ventaService.update(updateDTO));
    }

    @Test
    void testUpdateOnClose_Exito() {
        VentaUpdateOnCloseDTO closeDTO = new VentaUpdateOnCloseDTO();
        closeDTO.setId(1L);

        when(ventaRepository.findById(1L)).thenReturn(Optional.of(ventaSample));
        when(ventaUpdateOnCloseMapper.toEntity(any(), any())).thenReturn(ventaSample);
        when(ventaRepository.save(any(Venta.class))).thenReturn(ventaSample);
        when(ventaResponseMapper.toDto(any(Venta.class))).thenReturn(responseDTOSample);

        VentaResponseDTO result = ventaService.updateOnClose(closeDTO);
        assertNotNull(result);
    }

    // ==========================================
    // PRUEBAS PARA EL MÉTODO DELETE()
    // ==========================================
    @Test
    void testDeleteVentaById_Exito() {
        when(ventaRepository.existsById(1L)).thenReturn(true);
        doNothing().when(ventaRepository).deleteById(1L);

        Boolean result = ventaService.deleteVentaById(1L);
        assertTrue(result);
        verify(ventaRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteVentaById_Exception() {
        when(ventaRepository.existsById(1L)).thenReturn(false);
        assertThrows(IdNoExisteException.class, () -> ventaService.deleteVentaById(1L));
    }

    // ==========================================
    // EXTRA: COBERTURA ESPECÍFICA DE BLOQUES CATCH (JACOCO)
    // ==========================================
    @Test
    void testSave_IdNoExisteException_ClienteNoEncontrado() {
        VentaInputDTO input = new VentaInputDTO();
        input.setCarritoId(1L);

        CarritoOrderResponseDTO cartMock = new CarritoOrderResponseDTO();
        cartMock.setClienteId(100L);

        when(ventaRepository.existsByCarritoId(1L)).thenReturn(false);
        when(toAPICartFeign.findByIdForOrder(1L)).thenReturn(cartMock);
        
        Request request = Request.create(Request.HttpMethod.GET, "/url", Collections.emptyMap(), null, null, null);
        when(toAPICustomerFeign.findById(100L)).thenThrow(new FeignException.NotFound("Cliente Not Found", request, null, null));

        assertThrows(IdNoExisteException.class, () -> ventaService.save(input));
    }

    @Test
    void testSave_FailedAPICallResponseException_ClienteInaccesible() {
        VentaInputDTO input = new VentaInputDTO();
        input.setCarritoId(1L);

        CarritoOrderResponseDTO cartMock = new CarritoOrderResponseDTO();
        cartMock.setClienteId(100L);

        when(ventaRepository.existsByCarritoId(1L)).thenReturn(false);
        when(toAPICartFeign.findByIdForOrder(1L)).thenReturn(cartMock);
        when(toAPICustomerFeign.findById(100L)).thenThrow(new RuntimeException("Error Cliente"));

        assertThrows(FailedAPICallResponseExeption.class, () -> ventaService.save(input));
    }

    @Test
    void testSave_IdNoExisteException_DireccionNoEncontrada() {
        VentaInputDTO input = new VentaInputDTO();
        input.setCarritoId(1L);
        input.setDireccionId(2L);

        CarritoOrderResponseDTO cartMock = new CarritoOrderResponseDTO();
        cartMock.setClienteId(100L);
        ClienteOrderResponseDTO clientMock = new ClienteOrderResponseDTO();

        when(ventaRepository.existsByCarritoId(1L)).thenReturn(false);
        when(toAPICartFeign.findByIdForOrder(1L)).thenReturn(cartMock);
        when(toAPICustomerFeign.findById(100L)).thenReturn(clientMock);
        
        Request request = Request.create(Request.HttpMethod.GET, "/url", Collections.emptyMap(), null, null, null);
        when(toAPICustomerFeign.findDireccionById(2L)).thenThrow(new FeignException.NotFound("Dir Not Found", request, null, null));

        assertThrows(IdNoExisteException.class, () -> ventaService.save(input));
    }

    @Test
    void testSave_FailedAPICallResponseException_DireccionInaccesible() {
        VentaInputDTO input = new VentaInputDTO();
        input.setCarritoId(1L);
        input.setDireccionId(2L);

        CarritoOrderResponseDTO cartMock = new CarritoOrderResponseDTO();
        cartMock.setClienteId(100L);
        ClienteOrderResponseDTO clientMock = new ClienteOrderResponseDTO();

        when(ventaRepository.existsByCarritoId(1L)).thenReturn(false);
        when(toAPICartFeign.findByIdForOrder(1L)).thenReturn(cartMock);
        when(toAPICustomerFeign.findById(100L)).thenReturn(clientMock);
        when(toAPICustomerFeign.findDireccionById(2L)).thenThrow(new RuntimeException("Error Dir"));

        assertThrows(FailedAPICallResponseExeption.class, () -> ventaService.save(input));
    }

    @Test
    void testSave_IdNoExisteException_DescuentoNoExiste() {
        VentaInputDTO input = new VentaInputDTO();
        input.setCarritoId(1L);
        input.setDireccionId(2L);
        input.setDescuentoId(999L);

        CarritoOrderResponseDTO cartMock = new CarritoOrderResponseDTO();
        cartMock.setClienteId(100L);
        ClienteOrderResponseDTO clientMock = new ClienteOrderResponseDTO();
        DireccionResponseDTO dirMock = new DireccionResponseDTO();

        when(ventaRepository.existsByCarritoId(1L)).thenReturn(false);
        when(toAPICartFeign.findByIdForOrder(1L)).thenReturn(cartMock);
        when(toAPICustomerFeign.findById(100L)).thenReturn(clientMock);
        when(toAPICustomerFeign.findDireccionById(2L)).thenReturn(dirMock);
        when(descuentoRepository.existsById(999L)).thenReturn(false);

        assertThrows(IdNoExisteException.class, () -> ventaService.save(input));
    }

    @Test
    void testUpdate_IdNoExisteException_DescuentoNoExiste() {
        VentaUpdateDTO updateDTO = new VentaUpdateDTO();
        updateDTO.setId(1L);
        updateDTO.setDireccionId(2L);
        updateDTO.setDescuentoId(999L);

        DireccionResponseDTO dirMock = new DireccionResponseDTO();
        dirMock.setClienteId(100L);

        when(ventaRepository.findById(1L)).thenReturn(Optional.of(ventaSample));
        when(toAPICustomerFeign.findDireccionById(2L)).thenReturn(dirMock);
        when(descuentoRepository.existsById(999L)).thenReturn(false);

        assertThrows(IdNoExisteException.class, () -> ventaService.update(updateDTO));
    }

    @Test
    void testUpdate_IdNoExisteException_DireccionNoEncontrada() {
        VentaUpdateDTO updateDTO = new VentaUpdateDTO();
        updateDTO.setId(1L);
        updateDTO.setDireccionId(2L);

        when(ventaRepository.findById(1L)).thenReturn(Optional.of(ventaSample));
        Request request = Request.create(Request.HttpMethod.GET, "/url", Collections.emptyMap(), null, null, null);
        when(toAPICustomerFeign.findDireccionById(2L)).thenThrow(new FeignException.NotFound("Dir Not Found", request, null, null));

        assertThrows(IdNoExisteException.class, () -> ventaService.update(updateDTO));
    }

    // ==========================================
    // COBERTURA DE CASOS VACÍOS (.orElseThrow) EN SAVE Y UPDATE
    // ==========================================
    @Test
    void testSave_IdNoExisteException_DescuentoVacioEnFindById() {
        VentaInputDTO input = new VentaInputDTO();
        input.setCarritoId(1L);
        input.setDireccionId(2L);
        input.setDescuentoId(1L);

        CarritoOrderResponseDTO cartMock = new CarritoOrderResponseDTO();
        cartMock.setClienteId(100L);
        ClienteOrderResponseDTO clientMock = new ClienteOrderResponseDTO();
        DireccionResponseDTO dirMock = new DireccionResponseDTO();

        when(ventaRepository.existsByCarritoId(1L)).thenReturn(false);
        when(toAPICartFeign.findByIdForOrder(1L)).thenReturn(cartMock);
        when(toAPICustomerFeign.findById(100L)).thenReturn(clientMock);
        when(toAPICustomerFeign.findDireccionById(2L)).thenReturn(dirMock);
        when(descuentoRepository.existsById(1L)).thenReturn(true);
        when(descuentoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IdNoExisteException.class, () -> ventaService.save(input));
    }

    @Test
    void testFindByIdForPayment_IdNoExisteException() {
        when(ventaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IdNoExisteException.class, () -> ventaService.findByIdForPayment(1L));
    }

    @Test
    void testUpdate_IdNoExisteException_VentaVacia() {
        VentaUpdateDTO updateDTO = new VentaUpdateDTO();
        updateDTO.setId(1L);

        when(ventaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IdNoExisteException.class, () -> ventaService.update(updateDTO));
    }

    @Test
    void testUpdate_IdNoExisteException_DescuentoVacioEnFindById() {
        VentaUpdateDTO updateDTO = new VentaUpdateDTO();
        updateDTO.setId(1L);
        updateDTO.setDireccionId(2L);
        updateDTO.setDescuentoId(1L);

        DireccionResponseDTO dirMock = new DireccionResponseDTO();
        dirMock.setClienteId(100L);

        when(ventaRepository.findById(1L)).thenReturn(Optional.of(ventaSample));
        when(toAPICustomerFeign.findDireccionById(2L)).thenReturn(dirMock);
        when(descuentoRepository.existsById(1L)).thenReturn(true);
        when(descuentoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IdNoExisteException.class, () -> ventaService.update(updateDTO));
    }

    @Test
    void testUpdateOnClose_IdNoExisteException_VentaVacia() {
        VentaUpdateOnCloseDTO closeDTO = new VentaUpdateOnCloseDTO();
        closeDTO.setId(999L);

        when(ventaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IdNoExisteException.class, () -> ventaService.updateOnClose(closeDTO));
    }
}