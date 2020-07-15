package nl.thehyve.hypercubeapi.observation;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(schema = "i2b2demodata", name = "modifier_dimension")
@Data @Builder @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(of = "id")
public class ModifierEntity {

    @Id
    @Column(name = "modifier_path", length = 700, nullable = false)
    private String id;

    @Column(name = "modifier_cd", length = 50)
    private String modifierCode;

    @Column(name = "name_char", length = 2000, nullable = false)
    private String name;

}
