package nl.thehyve.hypercubeapi.observation;

import lombok.*;
import nl.thehyve.hypercubeapi.patient.PatientEntity;
import nl.thehyve.hypercubeapi.trialvisit.TrialVisitEntity;
import nl.thehyve.hypercubeapi.type.ValueType;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@IdClass(ObservationEntity.ObservationId.class)
@Table(schema = "i2b2demodata", name = "observation_fact")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(of = {"patient", "concept", "encounterId", "startDate", "providerId", "instance", "modifierCode"})
public class ObservationEntity {

    public static final Timestamp EMPTY_DATE = Timestamp.valueOf("0001-01-01 00:00:00");

    public static String observationFactValueField(ValueType valueType) {
        switch(valueType) {
            case Text:
                return "textValue";
            case Number:
            case Date:
                return "numericalValue";
            case RawText:
                return "rawTextValue";
            default:
                throw new RuntimeException("Unsupported value type: " + valueType.toString());
        }
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ObservationId implements Serializable {
        private long patient;
        private String concept;
        private long encounterId;
        private LocalDateTime startDate;
        private String providerId;
        private int instance;
        private String modifierCode;
    }

    @Id
    @ManyToOne
    @JoinColumn(name = "patient_num", nullable=false)
    private PatientEntity patient;

    @Id
    @Column(name = "concept_cd")
    private String concept;

    @Id
    @Column(name = "encounter_num")
    private Long encounterId;

    @Id
    @Column(name = "start_date")
    @Builder.Default
    private LocalDateTime startDate = EMPTY_DATE.toLocalDateTime();

    @Id
    @Column(name = "provider_id")
    @Builder.Default
    private String providerId = "@";

    @Id
    @Column(name = "instance_num")
    private Integer instance;

    @Id
    @Column(name = "modifier_cd")
    @Builder.Default
    private String modifierCode = "@";

    @ManyToOne
    @JoinColumn(name = "trial_visit_num", nullable=false)
    private TrialVisitEntity trialVisit;

    @Column(name = "valtype_cd")
    private ValueType valueType;

    @Column(name = "tval_char")
    private String textValue;

    @Column(name = "nval_num")
    private BigDecimal numericalValue;

    @Column(name = "observation_blob", columnDefinition = "text")
    private String rawTextValue;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "location_cd", length = 50)
    private String location;

}
