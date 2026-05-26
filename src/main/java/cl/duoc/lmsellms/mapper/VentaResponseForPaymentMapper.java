package cl.duoc.lmsellms.mapper;

import cl.duoc.lmsellms.dtos.VentaResponseForPaymentDTO;
import cl.duoc.lmsellms.models.Venta;
import org.springframework.stereotype.Component;

@Component
public class VentaResponseForPaymentMapper {
    public VentaResponseForPaymentDTO toDto(Venta ent){

        if(ent==null) return null;

        return new VentaResponseForPaymentDTO(
                ent.getId(),
                ent.getClienteId(),
                ent.getClienteNombre(),
                ent.getClienteRun(),
                ent.getTotalFinal(),
                ent.getRealizada()
        );
    }
}
