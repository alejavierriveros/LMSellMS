package cl.duoc.lmsellms.assemblers;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import cl.duoc.lmsellms.controllers.VentaRESTControllerV2;
import cl.duoc.lmsellms.dtos.VentaResponseDTO;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class VentaModelAssemblers implements RepresentationModelAssembler<VentaResponseDTO, EntityModel<VentaResponseDTO>> {

    @Override
    public EntityModel<VentaResponseDTO> toModel(VentaResponseDTO venta) {
        return EntityModel.of(venta,
                linkTo(methodOn(VentaRESTControllerV2.class).findById(venta.getId())).withSelfRel(),
                // Corregido de "venta" a "ventas" para seguir el estándar de colecciones
                linkTo(methodOn(VentaRESTControllerV2.class).findAll()).withRel("ventas")
        );
    }
}