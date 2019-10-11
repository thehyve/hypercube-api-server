package nl.thehyve.hypercubeapi.tree;

import org.transmartproject.common.dto.Forest;
import org.transmartproject.common.dto.TreeNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TreeAlgorithms {

    private static String getParentPath(String path) {
        if (path.endsWith("\\")) {
            path = path.substring(0, path.length() - 1);
        }
        int lastSeparator = path.lastIndexOf("\\");
        if (lastSeparator < 0) {
            return null;
        }
        return path.substring(0, lastSeparator + 1);
    }

    private static void buildTree(List<TreeNode> nodes, Map<String, List<TreeNode>> childNodes) {
        for (TreeNode node: nodes) {
            List<TreeNode> children = childNodes.get(node.getFullName());
            if (children == null) {
                continue;
            }
            buildTree(children, childNodes);
            node.setChildren(children);
        }
    }

    static Forest buildForest(String root, List<TreeNode> nodes) {
        List<TreeNode> roots = new ArrayList<>();
        Map<String, List<TreeNode>> childNodes = new HashMap<>();
        for (TreeNode node: nodes) {
            String parentPath = getParentPath(node.getFullName());
            if (parentPath == null) {
                throw new RuntimeException("No parent path for node: " + node.getFullName());
            } else if (parentPath.equals(root)) {
                roots.add(node);
            } else {
                if (!childNodes.containsKey(parentPath)) {
                    childNodes.put(parentPath, new ArrayList<>());
                }
                childNodes.get(parentPath).add(node);
            }
        }
        buildTree(roots, childNodes);
        return Forest.builder().treeNodes(roots).build();
    }

}
