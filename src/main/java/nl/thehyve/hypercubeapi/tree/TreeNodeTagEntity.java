package nl.thehyve.hypercubeapi.tree;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(schema = "i2b2metadata", name = "i2b2_tags")
@Data @Builder @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(of = "id")
public class TreeNodeTagEntity {

    @Id
    @Column(name = "tag_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name="path", nullable=false)
    private TreeNodeEntity treeNode;

    @Column(name = "tag", columnDefinition = "text")
    private String tag;

    @NotBlank
    @Column(name = "tag_type", nullable = false, length = 400)
    private String type;

}
