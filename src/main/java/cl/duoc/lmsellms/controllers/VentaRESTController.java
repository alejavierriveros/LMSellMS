package cl.duoc.lmsellms.controllers;

import cl.duoc.lmsellms.dtos.*;
import cl.duoc.lmsellms.services.VentaService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ventas")
public class VentaRESTController {

    private static final Logger logger = LoggerFactory.getLogger(VentaRESTController.class.getName());

    @Autowired
    private VentaService ventaService;

    //CREATE:
    @PostMapping
    public ResponseEntity<VentaResponseDTO> save(@Valid @RequestBody VentaInputDTO dto){
        String logMsgRequest = "Recibiendo solicitud para crear/guardar venta.";
        String logMsg = "Solicitud para crear/guardar venta.";
        logger.info(logMsgRequest);
        VentaResponseDTO created = ventaService.save(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.getId()).toUri();
        //de componentes de constructor URI // de la actual request //ruta de id // sacar la id del obj creado // transformar a URI.
        logger.info(logMsg + "=> creado con ID: {}, ID cliente {}, Total: ${}, Descuento aplicado: {}% Dirección de envío: {}.", created.getId(), created.getClienteId(), created.getTotal(), created.getDescuento(), created.getDireccionEnvio());
        return ResponseEntity.created(location).body(created);
        //devuelve el estado y la locación //devuelve el objeto creado
    }


    //READ:
    @GetMapping
    public ResponseEntity<List<VentaResponseDTO>> findAll(){
        String logMsgRequest = "Recibiendo solicitud para buscar listado de ventas.";
        String logMsg = "Solicitud para buscar listado de ventas.";
        logger.info(logMsgRequest);
        List<VentaResponseDTO> listadoDTO = ventaService.findAll();

        if (!listadoDTO.isEmpty()){
            logger.info(logMsg + "=> encontrado(s) y enlistado(s).");
            return ResponseEntity.ok(listadoDTO);
        }
        logger.info(logMsg + "=> sin coincidencias (vacío).");
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exists-by-id/{id}")
    public ResponseEntity<Boolean> existsById(@PathVariable Long id) {
        String logMsgRequest = "Recibiendo solicitud para verificar existencia de venta con ID: " + id + ".";
        String logMsg = "Solicitud para verificar existencia de venta con ID: " + id + ".";
        logger.info(logMsgRequest);
        if (ventaService.existsById(id)) {
            logger.info(logMsg + " => encontrado.");
            return ResponseEntity.ok(true);
        }
        logger.info(logMsg + " => no encontrado.");
        return ResponseEntity.ok(false);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VentaResponseDTO> findById(@PathVariable Long id){
        String logMsgRequest = "Recibiendo solicitud para buscar venta por ID: " + id + ".";
        String logMsg = "Solicitud para buscar venta por ID: " + id + ".";
        logger.info(logMsgRequest);
        VentaResponseDTO dto = ventaService.findById(id);
        if (dto != null){
            logger.info(logMsg + "=> encontrado.");
            return ResponseEntity.ok(dto);
        }
        logger.info(logMsg + "=> no encontrado.");
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/-by-id-for-payment/{id}")
    public ResponseEntity<VentaResponseForPaymentDTO> findByIdForPayment(@PathVariable Long id){
        String logMsgRequest = "Recibiendo solicitud para buscar venta por ID: " + id + ".";
        String logMsg = "Solicitud para buscar venta por ID: " + id + ".";
        logger.info(logMsgRequest);
        VentaResponseForPaymentDTO dto = ventaService.findByIdForPayment(id);
        if (dto != null){
            logger.info(logMsg + "=> encontrado.");
            return ResponseEntity.ok(dto);
        }
        logger.info(logMsg + "=> no encontrado.");
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/list-all-by-cliente-id/{id}")
    public ResponseEntity<List<VentaResponseDTO>> findAllByClienteId(Long id){
        String logMsgRequest = "Recibiendo solicitud para buscar listado de ventas.";
        String logMsg = "Solicitud para buscar listado de ventas.";
        logger.info(logMsgRequest);
        List<VentaResponseDTO> listadoDTO = ventaService.findAllByClienteId(id);

        if (!listadoDTO.isEmpty()){
            logger.info(logMsg + "=> encontrado(s) y enlistado(s).");
            return ResponseEntity.ok(listadoDTO);
        }
        logger.info(logMsg + "=> sin coincidencias (vacío).");
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/list-all-by-cliente-run/{run}")
    public ResponseEntity<List<VentaResponseDTO>> findyByRun(String run){
        String logMsgRequest = "Recibiendo solicitud para buscar listado de ventas.";
        String logMsg = "Solicitud para buscar listado de ventas.";
        logger.info(logMsgRequest);
        List<VentaResponseDTO> listadoDTO = ventaService.findByRun(run);

        if (!listadoDTO.isEmpty()){
            logger.info(logMsg + "=> encontrado(s) y enlistado(s).");
            return ResponseEntity.ok(listadoDTO);
        }
        logger.info(logMsg + "=> sin coincidencias (vacío).");
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/list-all-by-monto/{monto}")
    public ResponseEntity<List<VentaResponseDTO>> findAllByMonto(Double monto){
        String logMsgRequest = "Recibiendo solicitud para buscar listado de ventas.";
        String logMsg = "Solicitud para buscar listado de ventas.";
        logger.info(logMsgRequest);
        List<VentaResponseDTO> listadoDTO = ventaService.findAllByMonto(monto);

        if (!listadoDTO.isEmpty()){
            logger.info(logMsg + "=> encontrado(s) y enlistado(s).");
            return ResponseEntity.ok(listadoDTO);
        }
        logger.info(logMsg + "=> sin coincidencias (vacío).");
        return ResponseEntity.noContent().build();
    }




    //UPDATE:
    @PutMapping("/{id}")
    public ResponseEntity<VentaResponseDTO> update(@Valid @RequestBody VentaUpdateDTO dto, @PathVariable Long id){
        String logMsgRequest = "Recibiendo solicitud para actualizar venta con ID: " + id + ".";
        String logMsg = "Solicitud para actualizar venta con ID: " + id + ".";
        logger.info(logMsgRequest);
        dto.setId(id);
        VentaResponseDTO updated = ventaService.update(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(updated.getId()).toUri();
        //de componentes de constructor URI // de la actual request //ruta de id // sacar la id del obj creado // transformar a URI.
        logger.info(logMsg + " => actualizado.");
        return ResponseEntity.status(200).location(location).body(updated);
        //devuelve el estado y la locación //devuelve el objeto creado
    }

    @PutMapping("/close-venta/{id}")
    public ResponseEntity<VentaResponseDTO> updateOnClose(@Valid @RequestBody VentaUpdateOnCloseDTO dto, @PathVariable Long id){
        String logMsgRequest = "Recibiendo solicitud para actualizar venta con ID: " + id + ".";
        String logMsg = "Solicitud para actualizar venta con ID: " + id + ".";
        logger.info(logMsgRequest);
        dto.setId(id);
        VentaResponseDTO updated = ventaService.updateOnClose(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(updated.getId()).toUri();
        //de componentes de constructor URI // de la actual request //ruta de id // sacar la id del obj creado // transformar a URI.
        logger.info(logMsg + " => actualizado.");
        return ResponseEntity.status(200).location(location).body(updated);
        //devuelve el estado y la locación //devuelve el objeto creado
    }


    //DELETE:
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id){
        String logMsgRequest = "Recibiendo solicitud para borrar venta con ID: " + id + ".";
        String logMsg = "Solicitud para borrar venta con ID: " + id + ".";
        logger.info(logMsgRequest);
        if(ventaService.deleteVentaById(id)){
            logger.info(logMsg + " => encontrado y borrado.");
            return ResponseEntity.noContent().build();
        }
        logger.info(logMsg + " => no encontrado.");
        return ResponseEntity.notFound().build();
    }
}
