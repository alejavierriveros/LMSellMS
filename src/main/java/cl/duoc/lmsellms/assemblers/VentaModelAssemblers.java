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
    public EntityModel<VentaResponseDTO> toModel(VentaResponseDTO Venta) {
        return EntityModel.of(Venta,
                linkTo(methodOn(VentaRESTControllerV2.class).findById(Venta.getId())).withSelfRel(),
                linkTo(methodOn(VentaRESTControllerV2.class).findAll()).withRel("venta")
        );
    }

}
