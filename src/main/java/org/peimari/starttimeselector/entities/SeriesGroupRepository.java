package org.peimari.starttimeselector.entities;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeriesGroupRepository extends JpaRepository<SeriesGroup, Long> {

    @EntityGraph(attributePaths = { "series" })
    List<SeriesGroup> findAllByCompetition(Competition competition);
}
