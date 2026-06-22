package cl.duoc.lmsellms;

import cl.duoc.lmsellms.dtos.DescuentoInputDTO;
import cl.duoc.lmsellms.dtos.DescuentoResponseDTO;
import cl.duoc.lmsellms.dtos.DescuentoUpdateDTO;
import cl.duoc.lmsellms.exceptions.IdNoExisteException;
import cl.duoc.lmsellms.exceptions.NombreDctoNoExisteException;
import cl.duoc.lmsellms.mapper.DescuentoInputMapper;
import cl.duoc.lmsellms.mapper.DescuentoResponseMapper;
import cl.duoc.lmsellms.mapper.DescuentoUpdateMapper;
import cl.duoc.lmsellms.models.Descuento;
import cl.duoc.lmsellms.repositories.DescuentoRepository;
import cl.duoc.lmsellms.services.DescuentoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DescuentoServiceTest {

    @Mock private DescuentoRepository descuentoRepository;
    @Mock private DescuentoResponseMapper descuentoResponseMapper;
    @Mock private DescuentoInputMapper descuentoInputMapper;
    @Mock private DescuentoUpdateMapper descuentoUpdateMapper;

    @InjectMocks
    private DescuentoService descuentoService;

    private Descuento descuentoSample;
    private DescuentoResponseDTO responseDTOSample;

    @BeforeEach
    void setUp() {
        descuentoSample = new Descuento();
        descuentoSample.setId(1L);
        descuentoSample.setNombre("VERANO2026");
        descuentoSample.setPorcentajeDcto(15.0);

        responseDTOSample = new DescuentoResponseDTO();
        responseDTOSample.setId(1L);
        responseDTOSample.setNombre("VERANO2026");
    }

    // ==========================================
    // PRUEBAS PARA EL MÉTODO SAVE()
    // ==========================================
    @Test
    void testSave_Exito() {
        DescuentoInputDTO input = new DescuentoInputDTO();
        input.setNombre("VERANO2026");

        when(descuentoRepository.existsByNombre("VERANO2026")).thenReturn(false);
        when(descuentoInputMapper.toEntity(any(DescuentoInputDTO.class))).thenReturn(descuentoSample);
        when(descuentoRepository.save(any(Descuento.class))).thenReturn(descuentoSample);
        when(descuentoResponseMapper.toDto(any(Descuento.class))).thenReturn(responseDTOSample);

        DescuentoResponseDTO result = descuentoService.save(input);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testSave_NombreDctoNoExisteException_NombreYaExiste() {
        DescuentoInputDTO input = new DescuentoInputDTO();
        input.setNombre("VERANO2026");

        when(descuentoRepository.existsByNombre("VERANO2026")).thenReturn(true);

        assertThrows(NombreDctoNoExisteException.class, () -> descuentoService.save(input));
    }

    // ==========================================
    // PRUEBAS PARA MÉTODOS DE LECTURA (READ)
    // ==========================================
    @Test
    void testFindAll() {
        when(descuentoRepository.findAll()).thenReturn(List.of(descuentoSample));
        when(descuentoResponseMapper.toDto(descuentoSample)).thenReturn(responseDTOSample);

        List<DescuentoResponseDTO> list = descuentoService.findAll();
        assertFalse(list.isEmpty());
        assertEquals(1, list.size());
    }

    @Test
    void testExistsById() {
        when(descuentoRepository.existsById(1L)).thenReturn(true);
        assertTrue(descuentoService.existsById(1L));
    }

    @Test
    void testFindById_Exito() {
        when(descuentoRepository.findById(1L)).thenReturn(Optional.of(descuentoSample));
        when(descuentoResponseMapper.toDto(descuentoSample)).thenReturn(responseDTOSample);

        DescuentoResponseDTO result = descuentoService.findById(1L);
        assertNotNull(result);
    }

    @Test
    void testFindById_IdNoExisteException() {
        when(descuentoRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IdNoExisteException.class, () -> descuentoService.findById(1L));
    }

    @Test
    void testFindValueByIdForSelling_Exito() {
        when(descuentoRepository.findById(1L)).thenReturn(Optional.of(descuentoSample));
        
        Double value = descuentoService.findValueByIdForSelling(1L);
        assertEquals(15.0, value);
    }

    @Test
    void testFindValueByIdForSelling_IdNoExisteException() {
        when(descuentoRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IdNoExisteException.class, () -> descuentoService.findValueByIdForSelling(1L));
    }

    @Test
    void testFindByNombre_Exito() {
        when(descuentoRepository.findByNombre("VERANO2026")).thenReturn(descuentoSample);
        when(descuentoResponseMapper.toDto(descuentoSample)).thenReturn(responseDTOSample);

        DescuentoResponseDTO result = descuentoService.findByNombre("VERANO2026");
        assertNotNull(result);
    }

    @Test
    void testFindByNombre_NombreDctoNoExisteException() {
        when(descuentoRepository.findByNombre("INVENTADO")).thenReturn(null);
        assertThrows(NombreDctoNoExisteException.class, () -> descuentoService.findByNombre("INVENTADO"));
    }

    // ==========================================
    // PRUEBAS PARA EL MÉTODO UPDATE()
    // ==========================================
    @Test
    void testUpdate_Exito() {
        DescuentoUpdateDTO updateDTO = new DescuentoUpdateDTO();
        updateDTO.setId(1L);

        when(descuentoRepository.findById(1L)).thenReturn(Optional.of(descuentoSample));
        when(descuentoUpdateMapper.toEntity(any(Descuento.class), any(DescuentoUpdateDTO.class))).thenReturn(descuentoSample);
        when(descuentoRepository.save(any(Descuento.class))).thenReturn(descuentoSample);
        when(descuentoResponseMapper.toDto(any(Descuento.class))).thenReturn(responseDTOSample);

        DescuentoResponseDTO result = descuentoService.update(updateDTO);
        assertNotNull(result);
    }

    @Test
    void testUpdate_IdNoExisteException() {
        DescuentoUpdateDTO updateDTO = new DescuentoUpdateDTO();
        updateDTO.setId(1L);

        when(descuentoRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IdNoExisteException.class, () -> descuentoService.update(updateDTO));
    }

    // ==========================================
    // PRUEBAS PARA EL MÉTODO DELETE()
    // ==========================================
    @Test
    void testDeleteDescuentoById_Exito() {
        when(descuentoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(descuentoRepository).deleteById(1L);

        Boolean result = descuentoService.deleteDescuentoById(1L);
        assertTrue(result);
        verify(descuentoRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteDescuentoById_IdNoExisteException() {
        when(descuentoRepository.existsById(1L)).thenReturn(false);
        assertThrows(IdNoExisteException.class, () -> descuentoService.deleteDescuentoById(1L));
    }
}