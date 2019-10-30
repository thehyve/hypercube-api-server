package nl.thehyve.hypercubeapi.dimension;

import lombok.*;
import nl.thehyve.hypercubeapi.type.Density;
import nl.thehyve.hypercubeapi.type.Packable;
import nl.thehyve.hypercubeapi.type.Size;
import nl.thehyve.hypercubeapi.type.ValueType;
import org.transmartproject.common.type.DimensionType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

@Entity
@Table(schema = "i2b2metadata", name = "dimension_description")
@Data @Builder @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(of = "id")
public class DimensionEntity {

    @Id
    @Column(name = "id")
    private Long id;

    @NotBlank
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "density")
    private Density density;

    @Column(name = "modifier_code")
    private String modifierCode;

    @Column(name = "value_type")
    private ValueType valueType;

    @Column(name = "packable")
    private Packable packable;

    @Column(name = "size_cd")
    private Size size;

    @Column(name = "dimension_type")
    private DimensionType dimensionType;

    @Column(name = "sort_index")
    private Integer sortIndex;

}
