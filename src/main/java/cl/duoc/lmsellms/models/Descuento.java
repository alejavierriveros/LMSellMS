package cl.duoc.lmsellms.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Descuento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Length(min = 2, max = 20)
    private String nombre;

    @NotNull
    @Min(0)
    @Max(1)
    @Column(name = "porcentaje_dcto", nullable = false)
    private Double porcentajeDcto;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDate fechaCreacion;

    @Column(name = "fecha_modificacion", nullable = false)
    private LocalDate fechaModificacion;

    @NotNull
    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDate fechaExpiracion;

    @PrePersist
    protected void fechaOnCreate(){
        this.fechaCreacion = LocalDate.now();
        this.fechaModificacion = LocalDate.now();
    }

    @PreUpdate
    protected void fechaOnUpdate(){
        this.fechaModificacion = LocalDate.now();
    }
}
