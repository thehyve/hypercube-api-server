package nl.thehyve.hypercubeapi.tree;

import org.mapstruct.Mapper;
import org.transmartproject.common.dto.TreeNode;

@Mapper(componentModel = "spring")
public interface TreeNodeMapper {

    TreeNode treeNodeToTreeNodeDto(TreeNodeEntity treeNode);

}
