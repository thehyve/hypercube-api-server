package nl.thehyve.hypercubeapi.relation;

import lombok.*;
import nl.thehyve.hypercubeapi.patient.PatientEntity;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@IdClass(RelationEntity.RelationId.class)
@Table(schema = "i2b2demodata", name = "relation")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(of = {"leftSubject", "rightSubject", "relationType"})
public class RelationEntity {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RelationId implements Serializable {
        private long leftSubject;
        private long rightSubject;
        private long relationType;
    }

    @Id
    @OneToOne
    @JoinColumn(name = "left_subject_id", nullable=false)
    @Fetch(FetchMode.JOIN)
    private PatientEntity leftSubject;

    @Id
    @OneToOne
    @JoinColumn(name = "right_subject_id", nullable=false)
    @Fetch(FetchMode.JOIN)
    private PatientEntity rightSubject;

    @Id
    @OneToOne
    @JoinColumn(name = "relation_type_id", nullable=false)
    @Fetch(FetchMode.JOIN)
    private RelationTypeEntity relationType;

    @Column(name = "biological")
    private Boolean biological;

    @Column(name = "share_household")
    private Boolean shareHousehold;

}
