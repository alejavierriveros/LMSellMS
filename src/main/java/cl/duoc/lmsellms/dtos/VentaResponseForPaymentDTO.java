package cl.duoc.lmsellms.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VentaResponseForPaymentDTO {

    private Long Id;
    private Long clienteId;
    private String clienteNombre;
    private String clienteRun;
    private Double totalFinal;
    private Boolean realizado;

}
