package _5336_4701_5281.swdeproj.repository;

import _5336_4701_5281.swdeproj.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Company findByUserId(Long userId);
} 