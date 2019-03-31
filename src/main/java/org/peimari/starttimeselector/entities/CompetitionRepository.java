package org.peimari.starttimeselector.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface CompetitionRepository extends JpaRepository<Competition, Long> {

    Collection<Competition> findAllByOpen(boolean isOpen);

}
