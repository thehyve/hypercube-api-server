package nl.thehyve.hypercubeapi.concept;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(schema = "i2b2demodata", name = "concept_dimension")
@Data @Builder @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(of = "id")
public class ConceptEntity {

    @Id
    @Column(name = "concept_cd", length = 50, updatable = false)
    private String id;

    @Column(name = "concept_path", length = 700, nullable = false)
    private String conceptPath;

    @Column(name = "name_char", length = 2000, nullable = false)
    private String name;

}
