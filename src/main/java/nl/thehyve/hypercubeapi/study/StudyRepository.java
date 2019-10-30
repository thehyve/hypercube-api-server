package nl.thehyve.hypercubeapi.study;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudyRepository extends JpaRepository<StudyEntity, Long> {

    List<StudyEntity> findAllByStudyId(List<String> studyIds);

}
