package cl.duoc.lmsellms.assemblers;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import cl.duoc.lmsellms.controllers.DescuentoRESTControllerV2;
import cl.duoc.lmsellms.dtos.DescuentoResponseDTO;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class DescuentoModelAssemblers implements RepresentationModelAssembler<DescuentoResponseDTO, EntityModel<DescuentoResponseDTO>>{
    
    @Override
    public EntityModel<DescuentoResponseDTO> toModel(DescuentoResponseDTO descuento) {
        return EntityModel.of(descuento,
                // Corregido: Ahora apunta a DescuentoRESTControllerV2
                linkTo(methodOn(DescuentoRESTControllerV2.class).findById(descuento.getId())).withSelfRel(),
                linkTo(methodOn(DescuentoRESTControllerV2.class).findAll()).withRel("descuentos")
        );
    }
}