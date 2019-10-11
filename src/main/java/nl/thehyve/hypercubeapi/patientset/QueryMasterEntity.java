package nl.thehyve.hypercubeapi.patientset;

import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(schema = "i2b2demodata", name = "qt_query_master")
@Data @Builder @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(of = "id")
public class QueryMasterEntity {

    @Id
    @GeneratedValue
    @Column(name = "query_master_id")
    private Long id;

    @Column(name = "name", nullable = false, length = 250)
    private String name;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "group_id", nullable = false)
    private String groupId;

    @Column(name = "create_date", nullable = false)
    private Date createDate;

    @Column(name = "delete_date")
    private Date deleteDate;

    @Column(name = "delete_flag")
    private Boolean deleted;

    @Column(name = "generated_sql", columnDefinition = "text")
    private String generatedSql;

    @Column(name = "request_xml", columnDefinition = "text")
    private String requestXml;

    @Column(name = "i2b2_request_xml", columnDefinition = "text")
    private String i2b2RequestXml;

    @Column(name = "request_constraints", columnDefinition = "text")
    private String requestConstraints;

    @Column(name = "api_version", columnDefinition = "text")
    private String apiVersion;

}
