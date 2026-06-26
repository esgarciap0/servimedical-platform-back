package com.servimedical.platform.aph.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface AphJpaRepository extends JpaRepository<AphJpaEntity, Long> {
}
