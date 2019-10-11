package nl.thehyve.hypercubeapi.study;

import lombok.*;
import nl.thehyve.hypercubeapi.dimension.DimensionEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Entity
@Table(schema = "i2b2demodata", name = "study")
@Data @Builder @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(of = "id")
public class StudyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_num")
    private Long id;

    @NotBlank
    @Column(name = "study_id", length = 100, nullable = false, unique = true)
    private String studyId;

    @OneToMany
    @JoinTable(schema = "i2b2metadata", name = "study_dimension_descriptions",
        joinColumns = @JoinColumn(name="study_id", referencedColumnName="study_num"),
        inverseJoinColumns = @JoinColumn(name="dimension_description_id", referencedColumnName="id"))
    private List<DimensionEntity> dimensions;

    @NotBlank
    @Column(name = "secure_obj_token", nullable = false)
    private String secureObjectToken;

}
