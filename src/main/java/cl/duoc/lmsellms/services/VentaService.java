package cl.duoc.lmsellms.services;

import cl.duoc.lmsellms.clients.ToAPICartFeign;
import cl.duoc.lmsellms.clients.ToAPICustomerFeign;
import cl.duoc.lmsellms.controllers.DescuentoRESTController;
import cl.duoc.lmsellms.dtos.*;
import cl.duoc.lmsellms.exceptions.FailedAPICallResponseExeption;
import cl.duoc.lmsellms.exceptions.IdExisteException;
import cl.duoc.lmsellms.exceptions.IdNoExisteException;
import cl.duoc.lmsellms.exceptions.NombreDctoNoExisteException;
import cl.duoc.lmsellms.mapper.*;
import cl.duoc.lmsellms.models.Descuento;
import cl.duoc.lmsellms.models.Venta;
import cl.duoc.lmsellms.repositories.DescuentoRepository;
import cl.duoc.lmsellms.repositories.VentaRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class VentaService {

    private static final Logger logger = LoggerFactory.getLogger(DescuentoRESTController.class.getName());
    @Autowired
    private VentaRepository ventaRepository;


    @Autowired
    private VentaResponseMapper ventaResponseMapper;

    @Autowired
    private VentaInputMapper ventaInputMapper;
    @Autowired
    private VentaUpdateMapper ventaUpdateMapper;
    @Autowired
    private VentaUpdateOnCloseMapper ventaUpdateOnCloseMapper;
    @Autowired
    private ToAPICartFeign toAPICartFeign;
    @Autowired
    private ToAPICustomerFeign toAPICustomerFeign;
    @Autowired
    private DescuentoRepository descuentoRepository;
    @Autowired
    private VentaResponseForPaymentMapper ventaResponseForPaymentMapper;


    //CREATE:
    @Transactional
    public VentaResponseDTO save(VentaInputDTO dto) {
        if (ventaRepository.existsByCarritoId((dto.getCarritoId()))) {
            throw new IdExisteException("ID de carrito ya existe para el cliente.");
        }
        CarritoOrderResponseDTO carrito = null;
        try {

            carrito = toAPICartFeign.findByIdForOrder(dto.getCarritoId());
        } catch (FeignException.NotFound e) {
            String responseMessage = "ID de Carrito no existe." + e.getMessage();
            logger.info(responseMessage);
            throw new IdNoExisteException(responseMessage);
        } catch (Exception e) {
            String errorMessage = "Servicio de Carrito de compras inaccesible: " + e.getMessage();
            logger.error(errorMessage);
            throw new FailedAPICallResponseExeption(errorMessage);
        }


        //Obtener desde API REST.
        ClienteOrderResponseDTO cliente = null;
        try {
            cliente = toAPICustomerFeign.findById(carrito.getClienteId());
        } catch (FeignException.NotFound e) {
            String responseMessage = "ID de Cliente no existe." + e.getMessage();
            logger.info(responseMessage);
            throw new IdNoExisteException(responseMessage);
        } catch (Exception e) {
            String errorMessage = "Servicio de Cliente inaccesible: " + e.getMessage();
            logger.error(errorMessage);
            throw new FailedAPICallResponseExeption(errorMessage);
        }

        //Obtener desde API REST.
        DireccionResponseDTO direccion = null;
        try {
            direccion = toAPICustomerFeign.findDireccionById(dto.getDireccionId());
        } catch (FeignException.NotFound e) {
            String responseMessage = "ID de Dirección no existe." + e.getMessage();
            logger.info(responseMessage);
            throw new IdNoExisteException(responseMessage);
        } catch (Exception e) {
            String errorMessage = "Servicio de Dirección inaccesible: " + e.getMessage();
            logger.error(errorMessage);
            throw new FailedAPICallResponseExeption(errorMessage);
        }

        //Obtener desde LocalPackage:

        if (!descuentoRepository.existsById(dto.getDescuentoId())) {
            logger.info("Descuento con ID" + dto.getDescuentoId().toString() + "no encontrado");
            throw new IdNoExisteException("Descuento con ID" + dto.getDescuentoId().toString() + "no encontrado");
        }

        Descuento descuento = descuentoRepository.findById(dto.getDescuentoId()).orElseThrow(() ->new IdNoExisteException("Descuento con ID" + dto.getDescuentoId().toString() + "no encontrado"));

        return ventaResponseMapper.toDto(ventaRepository.save(ventaInputMapper.toEntity(carrito.getClienteId(), carrito.getNombreCliente(), carrito.getRunCliente(), carrito.getDetalles(), direccion, carrito.getTotal(), descuento, dto)));
    }


    //READ:
    @Transactional(readOnly = true)
    public List<VentaResponseDTO> findAll() {
        return ventaRepository.findAll().stream().map(ventaResponseMapper::toDto).toList();
    }

    @Transactional
    public Boolean existsById(Long id) {
        return ventaRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public VentaResponseDTO findById(Long id) {
        return ventaResponseMapper.toDto(ventaRepository.findById(id).orElseThrow(() -> new IdNoExisteException("ID de venta no existe.")));
    }

    @Transactional(readOnly = true)
    public VentaResponseForPaymentDTO findByIdForPayment(Long id) {
        return ventaResponseForPaymentMapper.toDto(ventaRepository.findById(id).orElseThrow(() -> new IdNoExisteException("ID de venta no existe.")));
    }

    @Transactional(readOnly = true)
    public List<VentaResponseDTO> findAllByClienteId(Long id) {
        return ventaRepository.findAllByClienteId(id).stream().map(ventaResponseMapper::toDto).toList();
    }


    @Transactional(readOnly = true)
    public List<VentaResponseDTO> findAllByMonto(Double monto) {
        return ventaRepository.findAllByTotalFinal(monto).stream().map(ventaResponseMapper::toDto).toList();
    }


    @Transactional(readOnly = true)
    public List<VentaResponseDTO> findByRun(String run) {
        return ventaRepository.findByClienteRun(run).stream().map(ventaResponseMapper::toDto).toList();
    }

    //UPDATE:
    @Transactional
    public VentaResponseDTO update(VentaUpdateDTO dto) {
        //Agregar los mismos validadores de .save"

        Venta ent = ventaRepository.findById(dto.getId()).orElseThrow(() -> new IdNoExisteException("ID de venta no existe."));

        //Obtener desde API REST.
        DireccionResponseDTO direccion = null;
        try {
            direccion = toAPICustomerFeign.findDireccionById(dto.getDireccionId());
            if (direccion.getClienteId() != ent.getClienteId()) {
                String responseMessage = "ID de dirección no corresponde a ID Cliente";
                logger.info(responseMessage);
                throw new IdNoExisteException(responseMessage);
            }
        } catch (FeignException.NotFound e) {
            String responseMessage = "ID de Dirección no existe." + e.getMessage();
            logger.info(responseMessage);
            throw new IdNoExisteException(responseMessage);
        } catch (Exception e) {
            String errorMessage = "Servicio de Dirección inaccesible: " + e.getMessage();
            logger.error(errorMessage);
            throw new FailedAPICallResponseExeption(errorMessage);
        }

        //Obtener desde LocalPackage:

        if (!descuentoRepository.existsById(dto.getDescuentoId())) {
            String responseMessage = "Descuento con ID: " + dto.getDescuentoId().toString() + " no encontrado.";
            logger.info(responseMessage);
            throw new IdNoExisteException(responseMessage);

        }

        Descuento descuento = descuentoRepository.findById(dto.getDescuentoId()).orElseThrow(() ->new IdNoExisteException("Descuento con ID" + dto.getDescuentoId().toString() + "no encontrado"));

        return ventaResponseMapper.toDto(ventaRepository.save(ventaUpdateMapper.toEntity(ent, direccion, descuento, dto)));
    }

    //Cambia estado de venta a realizado=true. Pago la invoca cuando se confirma transacción realizada.
    @Transactional
    public VentaResponseDTO updateOnClose(VentaUpdateOnCloseDTO dto) {
        Venta ent = ventaRepository.findById(dto.getId()).orElseThrow(() -> new IdNoExisteException("ID de venta no existe."));
        return ventaResponseMapper.toDto(ventaRepository.save(ventaUpdateOnCloseMapper.toEntity(ent, dto)));
    }

    //DELETE:
    @Transactional
    public Boolean deleteVentaById(Long id) {
        Boolean centinela = false;
        if (ventaRepository.existsById(id)) {
            ventaRepository.deleteById(id);
            centinela = true;
        } else {
            throw new IdNoExisteException("ID de venta no existe.");
        }
        return centinela;
    }

}
