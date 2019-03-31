package org.peimari.starttimeselector.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface CompetitorRepository extends JpaRepository<Competitor, Long> {

    List<Competitor> findAllBySeries(Series s);

    List<Competitor> findAllBySeriesAndStartTimeIsNull(Series s);

}
