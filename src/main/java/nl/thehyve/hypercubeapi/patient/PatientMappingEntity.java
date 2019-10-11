package nl.thehyve.hypercubeapi.patient;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@IdClass(PatientMappingEntity.PatientMappingId.class)
@Table(schema = "i2b2demodata", name = "patient_mapping")
@Data @Builder @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(of = {"encryptedId", "encryptedIdSource"})
public class PatientMappingEntity {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PatientMappingId implements Serializable {
        String encryptedId;
        String encryptedIdSource;
    }

    @Id
    @Column(name = "patient_ide", length = 200)
    private String encryptedId;

    @Id
    @Column(name = "patient_ide_source", length = 50)
    private String encryptedIdSource;

    @ManyToOne
    @JoinColumn(name="patient_num", nullable=false)
    private PatientEntity patient;

    @Column(name = "patient_ide_status", length = 50)
    private String encryptedIdStatus;

}
