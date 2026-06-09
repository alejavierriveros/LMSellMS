package cl.duoc.lmsellms.controllers;

import cl.duoc.lmsellms.dtos.*;
import cl.duoc.lmsellms.services.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Venta", description = "Gestión de ventas")
public class VentaRESTController {

    private static final Logger logger = LoggerFactory.getLogger(VentaRESTController.class.getName());

    @Autowired
    private VentaService ventaService;

    // CREATE
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Venta creada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = VentaResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @Operation(summary = "Crear venta", description = "Registra una nueva venta")
    @PostMapping
    public ResponseEntity<VentaResponseDTO> save(@Valid @RequestBody VentaInputDTO dto) {

        String logMsgRequest = "Recibiendo solicitud para crear/guardar venta.";
        String logMsg = "Solicitud para crear/guardar venta.";

        logger.info(logMsgRequest);

        VentaResponseDTO created = ventaService.save(dto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        logger.info(
                logMsg + "=> creado con ID: {}, ID cliente {}, Total: ${}, Descuento aplicado: {}% Dirección de envío: {}.",
                created.getId(),
                created.getClienteId(),
                created.getTotal(),
                created.getDescuento(),
                created.getDireccionEnvio()
        );

        return ResponseEntity.created(location).body(created);
    }

    // LISTAR
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de ventas",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = VentaResponseDTO.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "No existen ventas",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @Operation(summary = "Listar ventas", description = "Obtiene todas las ventas registradas")
    @GetMapping
    public ResponseEntity<List<VentaResponseDTO>> findAll() {

        String logMsgRequest = "Recibiendo solicitud para buscar listado de ventas.";
        String logMsg = "Solicitud para buscar listado de ventas.";

        logger.info(logMsgRequest);

        List<VentaResponseDTO> listadoDTO = ventaService.findAll();

        if (!listadoDTO.isEmpty()) {
            logger.info(logMsg + "=> encontrado(s) y enlistado(s).");
            return ResponseEntity.ok(listadoDTO);
        }

        logger.info(logMsg + "=> sin coincidencias (vacío).");
        return ResponseEntity.noContent().build();
    }

    // EXISTE POR ID
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Resultado de la validación"
            )
    })
    @Operation(summary = "Validar existencia", description = "Verifica si una venta existe según su ID")
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

    // BUSCAR POR ID
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Venta encontrada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = VentaResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Venta no encontrada",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @Operation(summary = "Buscar venta por ID", description = "Obtiene una venta según su identificador")
    @GetMapping("/{id}")
    public ResponseEntity<VentaResponseDTO> findById(@PathVariable Long id) {

        String logMsgRequest = "Recibiendo solicitud para buscar venta por ID: " + id + ".";
        String logMsg = "Solicitud para buscar venta por ID: " + id + ".";

        logger.info(logMsgRequest);

        VentaResponseDTO dto = ventaService.findById(id);

        if (dto != null) {
            logger.info(logMsg + "=> encontrado.");
            return ResponseEntity.ok(dto);
        }

        logger.info(logMsg + "=> no encontrado.");
        return ResponseEntity.notFound().build();
    }

    // DATOS PARA PAGO
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Venta encontrada para pago",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = VentaResponseForPaymentDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Venta no encontrada",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @Operation(summary = "Obtener venta para pago", description = "Obtiene los datos de una venta para procesamiento de pago")
    @GetMapping("/-by-id-for-payment/{id}")
    public ResponseEntity<VentaResponseForPaymentDTO> findByIdForPayment(@PathVariable Long id) {

        String logMsgRequest = "Recibiendo solicitud para buscar venta por ID: " + id + ".";
        String logMsg = "Solicitud para buscar venta por ID: " + id + ".";

        logger.info(logMsgRequest);

        VentaResponseForPaymentDTO dto = ventaService.findByIdForPayment(id);

        if (dto != null) {
            logger.info(logMsg + "=> encontrado.");
            return ResponseEntity.ok(dto);
        }

        logger.info(logMsg + "=> no encontrado.");
        return ResponseEntity.notFound().build();
    }
        // VENTAS POR CLIENTE ID
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de ventas encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = VentaResponseDTO.class))
                    )
            )
    })
    @Operation(summary = "Buscar ventas por cliente", description = "Obtiene todas las ventas de un cliente por ID")
    @GetMapping("/list-all-by-cliente-id/{id}")
    public ResponseEntity<List<VentaResponseDTO>> findAllByClienteId(@PathVariable Long id) {

        List<VentaResponseDTO> listadoDTO = ventaService.findAllByClienteId(id);

        if (!listadoDTO.isEmpty()) {
            return ResponseEntity.ok(listadoDTO);
        }

        return ResponseEntity.noContent().build();
    }

    // VENTAS POR RUN
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de ventas encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = VentaResponseDTO.class))
                    )
            )
    })
    @Operation(summary = "Buscar ventas por RUN", description = "Obtiene todas las ventas asociadas a un RUN")
    @GetMapping("/list-all-by-cliente-run/{run}")
    public ResponseEntity<List<VentaResponseDTO>> findyByRun(@PathVariable String run) {

        List<VentaResponseDTO> listadoDTO = ventaService.findByRun(run);

        if (!listadoDTO.isEmpty()) {
            return ResponseEntity.ok(listadoDTO);
        }

        return ResponseEntity.noContent().build();
    }

    // VENTAS POR MONTO
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de ventas encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = VentaResponseDTO.class))
                    )
            )
    })
    @Operation(summary = "Buscar ventas por monto", description = "Obtiene ventas filtradas por monto")
    @GetMapping("/list-all-by-monto/{monto}")
    public ResponseEntity<List<VentaResponseDTO>> findAllByMonto(@PathVariable Double monto) {

        List<VentaResponseDTO> listadoDTO = ventaService.findAllByMonto(monto);

        if (!listadoDTO.isEmpty()) {
            return ResponseEntity.ok(listadoDTO);
        }

        return ResponseEntity.noContent().build();
    }

    // ACTUALIZAR
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Venta actualizada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = VentaResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Venta no encontrada",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @Operation(summary = "Actualizar venta", description = "Actualiza la información de una venta")
    @PutMapping("/{id}")
    public ResponseEntity<VentaResponseDTO> update(
            @Valid @RequestBody VentaUpdateDTO dto,
            @PathVariable Long id) {

        dto.setId(id);

        VentaResponseDTO updated = ventaService.update(dto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(updated.getId())
                .toUri();

        return ResponseEntity.status(200)
                .location(location)
                .body(updated);
    }

    // CERRAR VENTA
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Venta cerrada correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = VentaResponseDTO.class)
                    )
            )
    })
    @Operation(summary = "Cerrar venta", description = "Finaliza el proceso de venta")
    @PutMapping("/close-venta/{id}")
    public ResponseEntity<VentaResponseDTO> updateOnClose(
            @Valid @RequestBody VentaUpdateOnCloseDTO dto,
            @PathVariable Long id) {

        dto.setId(id);

        VentaResponseDTO updated = ventaService.updateOnClose(dto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(updated.getId())
                .toUri();

        return ResponseEntity.status(200)
                .location(location)
                .body(updated);
    }

    // DELETE
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Venta eliminada"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Venta no encontrada",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @Operation(summary = "Eliminar venta", description = "Elimina una venta según su ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {

        String logMsgRequest = "Recibiendo solicitud para borrar venta con ID: " + id + ".";
        String logMsg = "Solicitud para borrar venta con ID: " + id + ".";

        logger.info(logMsgRequest);

        if (ventaService.deleteVentaById(id)) {
            logger.info(logMsg + " => encontrado y borrado.");
            return ResponseEntity.noContent().build();
        }

        logger.info(logMsg + " => no encontrado.");
        return ResponseEntity.notFound().build();
    }
}