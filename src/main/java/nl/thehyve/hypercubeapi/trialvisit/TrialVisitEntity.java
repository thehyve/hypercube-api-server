package nl.thehyve.hypercubeapi.trialvisit;

import lombok.*;
import nl.thehyve.hypercubeapi.study.StudyEntity;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;

@Entity
@Table(schema = "i2b2demodata", name = "trial_visit_dimension")
@Data @Builder @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(of = "id")
public class TrialVisitEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trial_visit_num")
    private Long id;

    @OneToOne
    @JoinColumn(name = "study_num", nullable=false)
    @Fetch(FetchMode.JOIN)
    private StudyEntity study;

    @Column(name = "rel_time_unit_cd")
    private String relativeTimeUnit;

    @Column(name = "rel_time_num")
    private Float relativeTime;

    @Column(name = "rel_time_label")
    private String relativeTimeLabel;

}
