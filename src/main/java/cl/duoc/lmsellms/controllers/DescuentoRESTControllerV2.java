package cl.duoc.lmsellms.controllers;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.media.ArraySchema;

import cl.duoc.lmsellms.assemblers.DescuentoModelAssemblers;
import cl.duoc.lmsellms.dtos.DescuentoInputDTO;
import cl.duoc.lmsellms.dtos.DescuentoResponseDTO;
import cl.duoc.lmsellms.dtos.DescuentoUpdateDTO;
import cl.duoc.lmsellms.services.DescuentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v2/descuentos")
@Tag(name = "Descuento", description = "Gestión de descuentos")
public class DescuentoRESTControllerV2 {

    private static final Logger logger = LoggerFactory.getLogger(DescuentoRESTControllerV2.class.getName());

    @Autowired
    private DescuentoService descuentoService;

    
    @Autowired
    private DescuentoModelAssemblers assemblers;

    // CREATE
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Descuento creado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EntityModel.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(summary = "Crear descuento", description = "Registra un nuevo descuento")
    @PostMapping
    public ResponseEntity<EntityModel<DescuentoResponseDTO>> save(@Valid @RequestBody DescuentoInputDTO dto) {
        logger.info("Recibiendo solicitud para crear/guardar descuento.");

        DescuentoResponseDTO created = descuentoService.save(dto);
        EntityModel<DescuentoResponseDTO> entityModel = assemblers.toModel(created);
        
        // Obtenemos la URI directamente del link "self" provisto por HATEOAS
        URI location = entityModel.getRequiredLink("self").toUri();

        logger.info("Solicitud para crear/guardar descuento => creado con ID: {}, Nombre: {}, %dcto: {}", 
                created.getId(), created.getNombre(), created.getPorcentajeDcto());

        return ResponseEntity.created(location).body(entityModel);
    }

    // READ - LISTAR
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de descuentos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CollectionModel.class))
            ),
            @ApiResponse(responseCode = "204", description = "No existen descuentos", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(summary = "Listar descuentos", description = "Obtiene todos los descuentos registrados")
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<DescuentoResponseDTO>>> findAll() {
        logger.info("Recibiendo solicitud para buscar listado de descuentos.");

        List<DescuentoResponseDTO> listadoDTO = descuentoService.findAll();

        if (!listadoDTO.isEmpty()) {
            List<EntityModel<DescuentoResponseDTO>> descuentos = listadoDTO.stream()
                    .map(assemblers::toModel)
                    .collect(Collectors.toList());

            CollectionModel<EntityModel<DescuentoResponseDTO>> collectionModel = CollectionModel.of(descuentos,
                    linkTo(methodOn(DescuentoRESTControllerV2.class).findAll()).withSelfRel());

            logger.info("Solicitud para buscar listado de descuentos => encontrado(s) y enlistado(s).");
            return ResponseEntity.ok(collectionModel);
        }

        logger.info("Solicitud para buscar listado de descuentos => sin coincidencias (vacío).");
        return ResponseEntity.noContent().build();
    }

    // READ - EXISTE POR ID (Se mantiene primitivo/Boolean simple ya que no es un recurso completo)
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Resultado de la búsqueda") })
    @Operation(summary = "Validar existencia", description = "Verifica si existe un descuento según su ID")
    @GetMapping("/exists-by-id/{id}")
    public ResponseEntity<Boolean> existsById(@PathVariable Long id) {
        logger.info("Recibiendo solicitud para verificar existencia de descuento con ID: {}", id);
        return ResponseEntity.ok(descuentoService.existsById(id));
    }

    // READ - BUSCAR POR ID
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Descuento encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EntityModel.class))
            ),
            @ApiResponse(responseCode = "404", description = "Descuento no encontrado", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(summary = "Buscar descuento por ID", description = "Obtiene un descuento según su identificador")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<DescuentoResponseDTO>> findById(@PathVariable Long id) {
        logger.info("Recibiendo solicitud para buscar descuento por ID: {}", id);

        DescuentoResponseDTO dto = descuentoService.findById(id);

        if (dto != null) {
            logger.info("Solicitud para buscar descuento por ID: {} => encontrado.", id);
            return ResponseEntity.ok(assemblers.toModel(dto));
        }

        logger.info("Solicitud para buscar descuento por ID: {} => no encontrado.", id);
        return ResponseEntity.notFound().build();
    }

    // READ - BUSCAR POR NOMBRE
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Descuento encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EntityModel.class))
            ),
            @ApiResponse(responseCode = "404", description = "Descuento no encontrado", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(summary = "Buscar descuento por nombre", description = "Obtiene un descuento según su nombre")
    @GetMapping("/by-nombre/{nombre}")
    public ResponseEntity<EntityModel<DescuentoResponseDTO>> findByNombre(@PathVariable String nombre) {
        logger.info("Recibiendo solicitud para buscar descuento por nombre: {}", nombre);

        DescuentoResponseDTO dto = descuentoService.findByNombre(nombre);

        if (dto != null) {
            logger.info("Solicitud para buscar descuento por nombre: {} => encontrado con ID: {}.", nombre, dto.getId());
            return ResponseEntity.ok(assemblers.toModel(dto));
        }

        logger.info("Solicitud para buscar descuento por nombre: {} => no encontrado.", nombre);
        return ResponseEntity.notFound().build();
    }

    // READ - OBTENER PORCENTAJE (Se mantiene Double simple porque solo extrae un valor primitivo específico)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Porcentaje encontrado"),
            @ApiResponse(responseCode = "404", description = "Descuento no encontrado", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(summary = "Obtener porcentaje de descuento", description = "Retorna el porcentaje de descuento para utilizar en ventas")
    @GetMapping("/value-by-id/{id}")
    public ResponseEntity<Double> findValueByIdForSelling(@PathVariable Long id) {
        logger.info("Recibiendo solicitud para buscar porcentaje de descuento según ID: {}", id);

        Double porcentajeDcto = descuentoService.findValueByIdForSelling(id);

        if (porcentajeDcto != null) {
            logger.info("Solicitud para buscar descuento según ID: {} => encontrado.", id);
            return ResponseEntity.ok(porcentajeDcto);
        }

        logger.info("Solicitud para buscar descuento según ID: {} => no encontrado.", id);
        return ResponseEntity.notFound().build();
    }

    // UPDATE
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Descuento actualizado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EntityModel.class))
            ),
            @ApiResponse(responseCode = "404", description = "Descuento no encontrado", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(summary = "Actualizar descuento", description = "Actualiza la información de un descuento existente")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<DescuentoResponseDTO>> update(@Valid @RequestBody DescuentoUpdateDTO objAux, @PathVariable Long id) {
        logger.info("Recibiendo solicitud para actualizar descuento con ID: {}", id);

        objAux.setId(id);
        DescuentoResponseDTO updated = descuentoService.update(objAux);

        EntityModel<DescuentoResponseDTO> entityModel = assemblers.toModel(updated);
        URI location = entityModel.getRequiredLink("self").toUri();

        logger.info("Solicitud para actualizar descuento con ID: {} => actualizado.", id);
        return ResponseEntity.ok().location(location).body(entityModel);
    }

    // DELETE
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Descuento eliminado"),
            @ApiResponse(responseCode = "404", description = "Descuento no encontrado", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(summary = "Eliminar descuento", description = "Elimina un descuento según su ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        logger.info("Recibiendo solicitud para borrar descuento con ID: {}", id);

        if (descuentoService.deleteDescuentoById(id)) {
            logger.info("Solicitud para borrar descuento con ID: {} => encontrado y borrado.", id);
            return ResponseEntity.noContent().build();
        }

        logger.info("Solicitud para borrar descuento con ID: {} => no encontrado.", id);
        return ResponseEntity.notFound().build();
    }
}