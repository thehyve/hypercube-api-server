package nl.thehyve.hypercubeapi.patientset;

import lombok.*;
import nl.thehyve.hypercubeapi.patient.PatientEntity;

import javax.persistence.*;

@Entity
@Table(schema = "i2b2demodata", name = "qt_patient_set_collection")
@Data @Builder @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(of = "id")
public class PatientSetCollectionEntity {

    @Id
    @GeneratedValue
    @Column(name = "patient_set_coll_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "result_instance_id")
    private QueryResultInstanceEntity queryResultInstance;

    @ManyToOne
    @JoinColumn(name = "patient_num")
    private PatientEntity patient;

    @Column(name = "set_index")
    private Integer setIndex;

}
