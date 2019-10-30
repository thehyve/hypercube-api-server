package nl.thehyve.hypercubeapi.visit;

import lombok.*;
import nl.thehyve.hypercubeapi.patient.PatientEntity;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

    @OneToMany(mappedBy="visit")
    private List<EncounterMappingEntity> encounterIds;

    @Column(name="active_status_cd", length = 50)
    String activeStatus;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name="inout_cd", length = 50)
    String inOut;

    @Column(name="location_cd", length = 50)
    String location;

    @Column(name="length_of_stay")
    BigDecimal lengthOfStay;

}
