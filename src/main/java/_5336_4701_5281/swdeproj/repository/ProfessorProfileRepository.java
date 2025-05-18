package _5336_4701_5281.swdeproj.repository;

import _5336_4701_5281.swdeproj.model.ProfessorProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorProfileRepository extends JpaRepository<ProfessorProfile, Long> {
    ProfessorProfile findByUserId(Long userId);
}
