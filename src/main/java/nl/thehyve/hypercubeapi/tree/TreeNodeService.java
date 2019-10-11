package nl.thehyve.hypercubeapi.tree;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.transmartproject.common.dto.Forest;
import org.transmartproject.common.dto.TreeNode;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
@Transactional
public class TreeNodeService {

    private final TreeNodeRepository treeNodeRepository;
    private final TreeNodeMapper treeNodeMapper;

    @Autowired
    TreeNodeService(TreeNodeRepository treeNodeRepository, TreeNodeMapper treeNodeMapper) {
        this.treeNodeRepository = treeNodeRepository;
        this.treeNodeMapper = treeNodeMapper;
    }

    public Forest getForest(@NotNull String root, Integer depth, Boolean tags) {
        List<TreeNode> nodes;
        if (depth == null) {
            nodes = this.treeNodeRepository.getAllByFullNameStartingWith(root).stream()
            .map(treeNodeMapper::treeNodeToTreeNodeDto).collect(Collectors.toList());
        } else {
            int startLevel = root == null ? 0 : root.split("\\\\").length;
            nodes = this.treeNodeRepository.getAllByFullNameStartingWithAndLevelLessThan(
                root, depth + startLevel).stream()
                .map(treeNodeMapper::treeNodeToTreeNodeDto).collect(Collectors.toList());
        }
        return TreeAlgorithms.buildForest(root, nodes);
    }

}
