package cl.duoc.lmsellms.controllers;

import cl.duoc.lmsellms.dtos.DescuentoInputDTO;
import cl.duoc.lmsellms.dtos.DescuentoResponseDTO;
import cl.duoc.lmsellms.dtos.DescuentoUpdateDTO;
import cl.duoc.lmsellms.services.DescuentoService;
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
@RequestMapping("/api/v1/descuentos")
@Tag(name = "Descuento", description = "Gestión de descuentos")
public class DescuentoRESTController {

    private static final Logger logger = LoggerFactory.getLogger(DescuentoRESTController.class.getName());

    @Autowired
    private DescuentoService descuentoService;

    // CREATE
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Descuento creado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DescuentoResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @Operation(summary = "Crear descuento", description = "Registra un nuevo descuento")
    @PostMapping
    public ResponseEntity<DescuentoResponseDTO> save(@Valid @RequestBody DescuentoInputDTO dto) {

        String logMsgRequest = "Recibiendo solicitud para crear/guardar descuento.";
        String logMsg = "Solicitud para crear/guardar descuento.";

        logger.info(logMsgRequest);

        DescuentoResponseDTO created = descuentoService.save(dto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        logger.info(
                logMsg + "=> creado con ID: {}, Nombre: {}, %dcto: {}, fecha de expiración: {}.",
                created.getId(),
                created.getNombre(),
                created.getPorcentajeDcto(),
                created.getFechaExpiracion()
        );

        return ResponseEntity.created(location).body(created);
    }

    // READ - LISTAR
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de descuentos",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = DescuentoResponseDTO.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "204",
                    description = "No existen descuentos",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @Operation(summary = "Listar descuentos", description = "Obtiene todos los descuentos registrados")
    @GetMapping
    public ResponseEntity<List<DescuentoResponseDTO>> findAll() {

        String logMsgRequest = "Recibiendo solicitud para buscar listado de descuentos.";
        String logMsg = "Solicitud para buscar listado de descuentos.";

        logger.info(logMsgRequest);

        List<DescuentoResponseDTO> listadoDTO = descuentoService.findAll();

        if (!listadoDTO.isEmpty()) {
            logger.info(logMsg + "=> encontrado(s) y enlistado(s).");
            return ResponseEntity.ok(listadoDTO);
        }

        logger.info(logMsg + "=> sin coincidencias (vacío).");
        return ResponseEntity.noContent().build();
    }

    // READ - EXISTE POR ID
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Resultado de la búsqueda"
            )
    })
    @Operation(summary = "Validar existencia", description = "Verifica si existe un descuento según su ID")
    @GetMapping("/exists-by-id/{id}")
    public ResponseEntity<Boolean> existsById(@PathVariable Long id) {

        String logMsgRequest = "Recibiendo solicitud para verificar existencia de descuento con ID: " + id + ".";
        String logMsg = "Solicitud para verificar existencia de descuento con ID: " + id + ".";

        logger.info(logMsgRequest);

        if (descuentoService.existsById(id)) {
            logger.info(logMsg + " => encontrado.");
            return ResponseEntity.ok(true);
        }

        logger.info(logMsg + " => no encontrado.");
        return ResponseEntity.ok(false);
    }

    // READ - BUSCAR POR ID
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Descuento encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DescuentoResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Descuento no encontrado",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @Operation(summary = "Buscar descuento por ID", description = "Obtiene un descuento según su identificador")
    @GetMapping("/{id}")
    public ResponseEntity<DescuentoResponseDTO> findById(@PathVariable Long id) {

        String logMsgRequest = "Recibiendo solicitud para buscar descuento por ID: " + id + ".";
        String logMsg = "Solicitud para buscar descuento por ID: " + id + ".";

        logger.info(logMsgRequest);

        DescuentoResponseDTO dto = descuentoService.findById(id);

        if (dto != null) {
            logger.info(logMsg + "=> encontrado.");
            return ResponseEntity.ok(dto);
        }

        logger.info(logMsg + "=> no encontrado.");
        return ResponseEntity.notFound().build();
    }

    // READ - BUSCAR POR NOMBRE
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Descuento encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DescuentoResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Descuento no encontrado",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @Operation(summary = "Buscar descuento por nombre", description = "Obtiene un descuento según su nombre")
    @GetMapping("/by-nombre/{nombre}")
    public ResponseEntity<DescuentoResponseDTO> findByNombre(@PathVariable String nombre) {

        String logMsgRequest = "Recibiendo solicitud para buscar descuento por nombre: " + nombre + ".";
        String logMsg = "Solicitud para buscar descuento por nombre: " + nombre + ".";

        logger.info(logMsgRequest);

        DescuentoResponseDTO dto = descuentoService.findByNombre(nombre);

        if (dto != null) {
            logger.info(logMsg + "=> encontrado con ID:{}.", dto.getId());
            return ResponseEntity.ok(dto);
        }

        logger.info(logMsg + "=> no encontrado.");
        return ResponseEntity.notFound().build();
    }

    // READ - OBTENER PORCENTAJE
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Porcentaje encontrado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Descuento no encontrado",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @Operation(
            summary = "Obtener porcentaje de descuento",
            description = "Retorna el porcentaje de descuento para utilizar en ventas"
    )
    @GetMapping("/value-by-id/{id}")
    public ResponseEntity<Double> findValueByIdForSelling(@PathVariable Long id) {

        String logMsgRequest = "Recibiendo solicitud para buscar descuento según ID: " + id + ".";
        String logMsg = "Solicitud para buscar descuento según ID: " + id + ".";

        logger.info(logMsgRequest);

        Double porcentajeDcto = descuentoService.findValueByIdForSelling(id);

        if (porcentajeDcto != null) {
            logger.info(logMsg + "=> encontrado con ID:{}.", id);
            return ResponseEntity.ok(porcentajeDcto);
        }

        logger.info(logMsg + "=> no encontrado.");
        return ResponseEntity.notFound().build();
    }

    // UPDATE
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Descuento actualizado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DescuentoResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Descuento no encontrado",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @Operation(
            summary = "Actualizar descuento",
            description = "Actualiza la información de un descuento existente"
    )
    @PutMapping("/{id}")
    public ResponseEntity<DescuentoResponseDTO> update(
            @Valid @RequestBody DescuentoUpdateDTO objAux,
            @PathVariable Long id) {

        String logMsgRequest = "Recibiendo solicitud para actualizar descuento con ID: " + id + ".";
        String logMsg = "Solicitud para actualizar descuento con ID: " + id + ".";

        logger.info(logMsgRequest);

        objAux.setId(id);

        DescuentoResponseDTO updated = descuentoService.update(objAux);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(updated.getId())
                .toUri();

        logger.info(logMsg + " => actualizado.");

        return ResponseEntity.status(200)
                .location(location)
                .body(updated);
    }

    // DELETE
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Descuento eliminado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Descuento no encontrado",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @Operation(
            summary = "Eliminar descuento",
            description = "Elimina un descuento según su ID"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {

        String logMsgRequest = "Recibiendo solicitud para borrar descuento con ID: " + id + ".";
        String logMsg = "Solicitud para borrar descuento con ID: " + id + ".";

        logger.info(logMsgRequest);

        if (descuentoService.deleteDescuentoById(id)) {
            logger.info(logMsg + " => encontrado y borrado.");
            return ResponseEntity.noContent().build();
        }

        logger.info(logMsg + " => no encontrado.");
        return ResponseEntity.notFound().build();
    }
}