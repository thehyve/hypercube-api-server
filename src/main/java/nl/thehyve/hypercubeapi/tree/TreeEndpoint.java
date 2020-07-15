package nl.thehyve.hypercubeapi.tree;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.transmartproject.common.dto.Forest;
import org.transmartproject.common.resource.TreeResource;

@RestController
@Validated
@CrossOrigin
public class TreeEndpoint implements TreeResource {

    private final TreeNodeService treeNodeService;

    @Autowired
    TreeEndpoint(TreeNodeService treeNodeService) {
        this.treeNodeService = treeNodeService;
    }

    @Override
    public ResponseEntity<Forest> getForest(String root, Integer depth, Boolean constraints, Boolean counts, Boolean tags) {
        return ResponseEntity.ok(this.treeNodeService.getForest(root, depth, tags));
    }

}
