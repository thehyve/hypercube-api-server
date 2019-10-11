package nl.thehyve.hypercubeapi.visit;

import lombok.*;
import nl.thehyve.hypercubeapi.patient.PatientEntity;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;

@Entity
@Table(schema = "i2b2demodata", name = "visit_dimension")
@Data @Builder @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(of = "id")
public class VisitEntity {

    @Id
    @Column(name = "encounter_num")
    private Long id;

    @OneToOne
    @JoinColumn(name = "patient_num", nullable=false)
    @Fetch(FetchMode.JOIN)
    private PatientEntity patient;

}
