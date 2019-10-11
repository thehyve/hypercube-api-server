package nl.thehyve.hypercubeapi.patientset;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(schema = "i2b2demodata", name = "qt_query_instance")
@Data @Builder @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(of = "id")
public class QueryInstanceEntity {

    @Id
    @GeneratedValue
    @Column(name = "query_instance_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "query_master_id")
    private QueryMasterEntity queryMaster;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "group_id", nullable = false)
    private String groupId;

    @Column(name = "start_date", nullable = false)
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "delete_flag")
    private Boolean deleted;

    @OneToOne
    @JoinColumn(name = "status_type_id", nullable = false)
    private QueryStatusTypeEntity statusType;

    @Column(name = "message", columnDefinition = "text")
    private String message;

}
