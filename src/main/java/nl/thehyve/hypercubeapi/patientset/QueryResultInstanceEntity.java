package nl.thehyve.hypercubeapi.patientset;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(schema = "i2b2demodata", name = "qt_query_result_instance")
@Data @Builder @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(of = "id")
public class QueryResultInstanceEntity {

    @Id
    @GeneratedValue
    @Column(name = "result_instance_id")
    private Long id;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "message", columnDefinition = "text")
    private String message;

    @Column(name = "description", length = 200)
    private String description;

    @ManyToOne
    @JoinColumn(name = "query_instance_id")
    private QueryInstanceEntity queryInstance;

    @OneToOne
    @JoinColumn(name = "result_type_id", nullable = false)
    private QueryResultTypeEntity resultType;

    @OneToOne
    @JoinColumn(name = "status_type_id", nullable = false)
    private QueryStatusTypeEntity statusType;

    @Column(name = "set_size")
    private Integer setSize;

    @Column(name = "start_date", nullable = false)
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "delete_flag")
    private Boolean deleted;

}
