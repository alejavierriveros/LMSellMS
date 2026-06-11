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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import cl.duoc.lmsellms.assemblers.VentaModelAssemblers;
import cl.duoc.lmsellms.dtos.VentaInputDTO;
import cl.duoc.lmsellms.dtos.VentaResponseDTO;
import cl.duoc.lmsellms.dtos.VentaUpdateDTO;
import cl.duoc.lmsellms.dtos.VentaResponseForPaymentDTO;
import cl.duoc.lmsellms.dtos.VentaUpdateOnCloseDTO;
import cl.duoc.lmsellms.services.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v2/ventas")
@Tag(name = "Venta v2", description = "Gestión de ventas")
public class VentaRESTControllerV2 {
    
    private static final Logger logger = LoggerFactory.getLogger(VentaRESTControllerV2.class.getName());

    @Autowired
    private VentaService ventaService;

    @Autowired
    private VentaModelAssemblers assemblers;

    // CREATE
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Venta creada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EntityModel.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(summary = "Crear venta", description = "Registra una nueva venta")
    @PostMapping
    public ResponseEntity<EntityModel<VentaResponseDTO>> save(@Valid @RequestBody VentaInputDTO dto) {
        logger.info("Recibiendo solicitud para crear/guardar venta.");

        VentaResponseDTO created = ventaService.save(dto);
        EntityModel<VentaResponseDTO> entityModel = assemblers.toModel(created);
        
        // Genera la URI de localización basada en el link "self" del recurso HATEOAS
        URI location = entityModel.getRequiredLink("self").toUri();
        
        logger.info("Solicitud para crear/guardar venta => creado con ID: {}", created.getId());
        return ResponseEntity.created(location).body(entityModel);
    }

    // LISTAR
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de ventas",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CollectionModel.class))
            ),
            @ApiResponse(responseCode = "204", description = "No existen ventas", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(summary = "Listar ventas", description = "Obtiene todas las ventas registradas")
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<VentaResponseDTO>>> findAll() {
        logger.info("Recibiendo solicitud para buscar listado de ventas.");

        List<VentaResponseDTO> listadoDTO = ventaService.findAll();

        if (!listadoDTO.isEmpty()) {
            // Convertimos la lista de DTOs en una lista de EntityModel mediante el assembler
            List<EntityModel<VentaResponseDTO>> ventas = listadoDTO.stream()
                    .map(assemblers::toModel)
                    .collect(Collectors.toList());

            // Envolvemos todo en un CollectionModel y agregamos el link al listado general (self)
            CollectionModel<EntityModel<VentaResponseDTO>> collectionModel = CollectionModel.of(ventas,
                    linkTo(methodOn(VentaRESTControllerV2.class).findAll()).withSelfRel());

            logger.info("Solicitud para buscar listado de ventas => encontrado(s) y enlistado(s).");
            return ResponseEntity.ok(collectionModel);
        }

        logger.info("Solicitud para buscar listado de ventas => sin coincidencias (vacío).");
        return ResponseEntity.noContent().build();
    }

    // EXISTE POR ID (Se mantiene booleano simple, ya que no representa una entidad/recurso directo)
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Resultado de la validación") })
    @Operation(summary = "Validar existencia", description = "Verifica si una venta existe según su ID")
    @GetMapping("/exists-by-id/{id}")
    public ResponseEntity<Boolean> existsById(@PathVariable Long id) {
        logger.info("Recibiendo solicitud para verificar existencia de venta con ID: {}", id);
        return ResponseEntity.ok(ventaService.existsById(id));
    }

    // BUSCAR POR ID
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Venta encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EntityModel.class))
            ),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(summary = "Buscar venta por ID", description = "Obtiene una venta según su identificador")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<VentaResponseDTO>> findById(@PathVariable Long id) {
        logger.info("Recibiendo solicitud para buscar venta por ID: {}", id);

        VentaResponseDTO dto = ventaService.findById(id);

        if (dto != null) {
            return ResponseEntity.ok(assemblers.toModel(dto));
        }

        logger.info("Solicitud para buscar venta por ID: {} => no encontrado.", id);
        return ResponseEntity.notFound().build();
    }

    // DATOS PARA PAGO 
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Venta encontrada para pago",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VentaResponseForPaymentDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(summary = "Obtener venta para pago", description = "Obtiene los datos de una venta para procesamiento de pago")
    @GetMapping("/-by-id-for-payment/{id}")
    public ResponseEntity<VentaResponseForPaymentDTO> findByIdForPayment(@PathVariable Long id) {
        logger.info("Recibiendo solicitud para buscar datos de pago por ID: {}", id);

        VentaResponseForPaymentDTO dto = ventaService.findByIdForPayment(id);

        if (dto != null) {
            return ResponseEntity.ok(dto);
        }

        return ResponseEntity.notFound().build();
    }

    // VENTAS POR CLIENTE ID
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de ventas encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CollectionModel.class))
            )
    })
    @Operation(summary = "Buscar ventas por cliente", description = "Obtiene todas las ventas de un cliente por ID")
    @GetMapping("/list-all-by-cliente-id/{id}")
    public ResponseEntity<CollectionModel<EntityModel<VentaResponseDTO>>> findAllByClienteId(@PathVariable Long id) {
        List<VentaResponseDTO> listadoDTO = ventaService.findAllByClienteId(id);

        if (!listadoDTO.isEmpty()) {
            List<EntityModel<VentaResponseDTO>> ventas = listadoDTO.stream().map(assemblers::toModel).collect(Collectors.toList());
            CollectionModel<EntityModel<VentaResponseDTO>> collectionModel = CollectionModel.of(ventas,
                    linkTo(methodOn(VentaRESTControllerV2.class).findAllByClienteId(id)).withSelfRel());
            return ResponseEntity.ok(collectionModel);
        }

        return ResponseEntity.noContent().build();
    }

    // VENTAS POR RUN
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de ventas encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CollectionModel.class))
            )
    })
    @Operation(summary = "Buscar ventas por RUN", description = "Obtiene todas las ventas asociadas a un RUN")
    @GetMapping("/list-all-by-cliente-run/{run}")
    public ResponseEntity<CollectionModel<EntityModel<VentaResponseDTO>>> findyByRun(@PathVariable String run) {
        List<VentaResponseDTO> listadoDTO = ventaService.findByRun(run);

        if (!listadoDTO.isEmpty()) {
            List<EntityModel<VentaResponseDTO>> ventas = listadoDTO.stream().map(assemblers::toModel).collect(Collectors.toList());
            CollectionModel<EntityModel<VentaResponseDTO>> collectionModel = CollectionModel.of(ventas,
                    linkTo(methodOn(VentaRESTControllerV2.class).findyByRun(run)).withSelfRel());
            return ResponseEntity.ok(collectionModel);
        }

        return ResponseEntity.noContent().build();
    }

    // VENTAS POR MONTO
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de ventas encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CollectionModel.class))
            )
    })
    @Operation(summary = "Buscar ventas por monto", description = "Obtiene ventas filtradas por monto")
    @GetMapping("/list-all-by-monto/{monto}")
    public ResponseEntity<CollectionModel<EntityModel<VentaResponseDTO>>> findAllByMonto(@PathVariable Double monto) {
        List<VentaResponseDTO> listadoDTO = ventaService.findAllByMonto(monto);

        if (!listadoDTO.isEmpty()) {
            List<EntityModel<VentaResponseDTO>> ventas = listadoDTO.stream().map(assemblers::toModel).collect(Collectors.toList());
            CollectionModel<EntityModel<VentaResponseDTO>> collectionModel = CollectionModel.of(ventas,
                    linkTo(methodOn(VentaRESTControllerV2.class).findAllByMonto(monto)).withSelfRel());
            return ResponseEntity.ok(collectionModel);
        }

        return ResponseEntity.noContent().build();
    }

    // ACTUALIZAR
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Venta actualizada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EntityModel.class))
            ),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(summary = "Actualizar venta", description = "Actualiza la información de una venta")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<VentaResponseDTO>> update(@Valid @RequestBody VentaUpdateDTO dto, @PathVariable Long id) {
        dto.setId(id);
        VentaResponseDTO updated = ventaService.update(dto);
        
        EntityModel<VentaResponseDTO> entityModel = assemblers.toModel(updated);
        URI location = entityModel.getRequiredLink("self").toUri();

        return ResponseEntity.ok().location(location).body(entityModel);
    }

    // CERRAR VENTA
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Venta cerrada correctamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = EntityModel.class))
            )
    })
    @Operation(summary = "Cerrar venta", description = "Finaliza el proceso de venta")
    @PutMapping("/close-venta/{id}")
    public ResponseEntity<EntityModel<VentaResponseDTO>> updateOnClose(@Valid @RequestBody VentaUpdateOnCloseDTO dto, @PathVariable Long id) {
        dto.setId(id);
        VentaResponseDTO updated = ventaService.updateOnClose(dto);

        EntityModel<VentaResponseDTO> entityModel = assemblers.toModel(updated);
        URI location = entityModel.getRequiredLink("self").toUri();

        return ResponseEntity.ok().location(location).body(entityModel);
    }

    // DELETE
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Venta eliminada"),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada", content = @Content(schema = @Schema(hidden = true)))
    })
    @Operation(summary = "Eliminar venta", description = "Elimina una venta según su ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        logger.info("Recibiendo solicitud para borrar venta con ID: {}", id);

        if (ventaService.deleteVentaById(id)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}