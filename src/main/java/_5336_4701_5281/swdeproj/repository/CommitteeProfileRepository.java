package _5336_4701_5281.swdeproj.repository;

import _5336_4701_5281.swdeproj.model.CommitteeProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommitteeProfileRepository extends JpaRepository<CommitteeProfile, Long> {
    CommitteeProfile findByUserId(Long userId);
}
