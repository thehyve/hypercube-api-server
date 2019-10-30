package nl.thehyve.hypercubeapi.visit;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@IdClass(EncounterMappingEntity.EncounterMappingId.class)
@Table(schema = "i2b2demodata", name = "encounter_mapping")
@Data @Builder @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(of = {"encryptedId", "encryptedIdSource"})
public class EncounterMappingEntity {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class EncounterMappingId implements Serializable {
        String encryptedId;
        String encryptedIdSource;
    }

    @Id
    @Column(name = "encounter_ide", length = 200)
    private String encryptedId;

    @Id
    @Column(name = "encounter_ide_source", length = 50)
    private String encryptedIdSource;

    @ManyToOne
    @JoinColumn(name="encounter_num", nullable=false)
    private VisitEntity visit;

    @Column(name = "encounter_ide_status", length = 50)
    private String encryptedIdStatus;

}
