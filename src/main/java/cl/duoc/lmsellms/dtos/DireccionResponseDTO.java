package cl.duoc.lmsellms.dtos;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"id", "numero", "nroDepto", "calle", "comuna", "region", "esDefault"})
public class DireccionResponseDTO {

    private Long id;
    private Long clienteId;
    private Integer numero;
    private Integer nroDepto;
    private String calle;
    private String comuna;
    private String region;
    private Boolean esDefault;

}
