package _5336_4701_5281.swdeproj.repository;

import _5336_4701_5281.swdeproj.model.TraineeProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TraineeProfileRepository extends JpaRepository<TraineeProfile, Long> {
    TraineeProfile findByUserId(Long userId);
    TraineeProfile findByStudentId(String studentId);
}
