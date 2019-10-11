package nl.thehyve.hypercubeapi.relation;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(schema = "i2b2demodata", name = "relation_type")
@Data @Builder @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(of = "id")
public class RelationTypeEntity {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @NotBlank
    @Column(name = "label", length = 200, nullable = false, unique = true)
    private String label;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "symmetrical")
    private Boolean symmetrical;

    @Column(name = "biological")
    private Boolean biological;

}
