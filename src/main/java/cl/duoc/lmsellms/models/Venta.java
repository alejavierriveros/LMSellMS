package cl.duoc.lmsellms.models;


import com.fasterxml.jackson.annotation.JsonRawValue;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDate;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Positive
    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @NotBlank
    @Column(name = "cliente_nombre", nullable = false)
    private String clienteNombre;

    @NotBlank
    @Column(name = "cliente_run", nullable = false)
    private String clienteRun;

    @NotNull
    @Column(name = "carrito_id", nullable = false)
    private Long carritoId;

    @Column(name = "detalles_venta", columnDefinition = "LONGTEXT") //Convierte todos los detalles por separado en un solo texto plano tipo LONGTEXT.
    @JsonRawValue //Devuelve los detalles de texto plano como un JSON
    private String detallesVenta;

    @NotNull
    @Column(name = "direccion_id", nullable = false)
    private Long direccionId;

    @NotBlank
    @Column(name = "direccion_envio", nullable = false)
    private String direccionEnvio;

    @NotNull
    @PositiveOrZero
    private Double total;

    @ManyToOne
    @JoinColumn(name =  "descuento_id", nullable = false)
    private Descuento descuento;

    @NotNull
    @PositiveOrZero
    @Column(name = "total_final", nullable = false)
    private Double totalFinal;

    @Column(name = "fecha_creacion")
    private LocalDate fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDate fechaModificacion;

    @Column(name = "fecha_salida")
    private LocalDate fechaSalida;
    private Boolean realizada;

    public void aplicarDescuento(Descuento descuento){
        this.totalFinal =  Math.floor(this.total * (1 - descuento.getPorcentajeDcto()));
    }
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