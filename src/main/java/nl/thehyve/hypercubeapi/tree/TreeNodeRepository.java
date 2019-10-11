package nl.thehyve.hypercubeapi.tree;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TreeNodeRepository extends JpaRepository<TreeNodeEntity, Long> {

    List<TreeNodeEntity> getAllByFullNameStartingWithAndLevelLessThan(String root, Integer maxLevel);

    List<TreeNodeEntity> getAllByFullNameStartingWith(String root);

}
