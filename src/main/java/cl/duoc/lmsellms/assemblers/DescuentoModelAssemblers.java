package cl.duoc.lmsellms.assemblers;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import cl.duoc.lmsellms.controllers.DescuentoRESTControllerV2;
import cl.duoc.lmsellms.controllers.VentaRESTControllerV2;
import cl.duoc.lmsellms.dtos.DescuentoResponseDTO;
import cl.duoc.lmsellms.dtos.VentaResponseDTO;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class DescuentoModelAssemblers implements RepresentationModelAssembler<DescuentoResponseDTO, EntityModel<DescuentoResponseDTO>>{
    
    @Override
    public EntityModel<DescuentoResponseDTO> toModel(DescuentoResponseDTO Descuento) {
        return EntityModel.of(Descuento,
                linkTo(methodOn(VentaRESTControllerV2.class).findById(Descuento.getId())).withSelfRel(),
                linkTo(methodOn(VentaRESTControllerV2.class).findAll()).withRel("Descuento")
        );
    }
}
