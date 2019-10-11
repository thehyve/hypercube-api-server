package nl.thehyve.hypercubeapi.patient;

import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.transmartproject.common.type.Sex;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(schema = "i2b2demodata", name = "patient_dimension")
@Data @Builder @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(of = "id")
public class PatientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "patient_num")
    private Long id;

    @Column(name = "sex_cd", length = 50)
    private Sex sex;

    @OneToMany(mappedBy="patient")
    private List<PatientMappingEntity> subjectIds;

}
