package nl.thehyve.hypercubeapi.tree;

import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.transmartproject.common.type.*;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.util.*;

@Entity
@Table(schema = "i2b2metadata", name = "i2b2_secure")
@Data @Builder @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode(of = "fullName")
public class TreeNodeEntity {

    @Id
    @Column(name = "c_fullname", length = 900)
    private String fullName;

    @Column(name = "c_hlevel")
    private Integer level;

    @NotBlank
    @Column(name = "c_name", nullable = false, length = 2000)
    private String name;

    @Column(name = "c_visualattributes")
    private String visualAttributeCodes;

    @Column(name = "c_basecode", length = 450)
    private String baseCode;

    @Column(name = "c_facttablecolumn", length = 50)
    private String factTableColumn;

    @Column(name = "c_tablename", length = 150)
    private String tableName;

    @Column(name = "c_columnname", length = 50)
    private String columnName;

    @Column(name = "c_columndatatype", length = 50)
    private String columnDataType;

    @Column(name = "c_operator", length = 10)
    private String operator;

    @Column(name = "c_dimcode", length = 900)
    private String dimensionCode;

    @NotNull
    @Column(name = "secure_obj_token", nullable = false)
    private String secureObjectToken;

    @OneToMany(mappedBy="treeNode")
    @Fetch(FetchMode.JOIN)
    @OrderColumn(name="tags_idx")
    private List<TreeNodeTagEntity> tags;

    public EnumSet<VisualAttribute> getVisualAttributes() {
        // FIXME: parse codes
        return EnumSet.of(VisualAttribute.Folder);
    }

    public String getStudyId() {
        return null;
    }

    public String getConceptCode() {
        return null;
    }

    public String getConceptPath() {
        return null;
    }

    public TreeNodeType getType() {
        return null;
    }

}
