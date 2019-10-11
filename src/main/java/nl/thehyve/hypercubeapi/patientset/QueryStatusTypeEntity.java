package nl.thehyve.hypercubeapi.patientset;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(schema = "i2b2demodata", name = "qt_query_status_type")
@Data @Builder @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(of = "id")
public class QueryStatusTypeEntity {

    @Id
    @GeneratedValue
    @Column(name = "status_type_id")
    private Long id;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "description", length = 200)
    private String description;

}
