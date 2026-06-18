package com.servimedical.platform.repository;

import com.servimedical.platform.entity.Aph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AphRepository extends JpaRepository<Aph, Long> {
}
