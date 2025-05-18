package _5336_4701_5281.swdeproj.repository;

import _5336_4701_5281.swdeproj.model.CompanyProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyProfileRepository extends JpaRepository<CompanyProfile, Long> {
    CompanyProfile findByUserId(Long userId);
}
