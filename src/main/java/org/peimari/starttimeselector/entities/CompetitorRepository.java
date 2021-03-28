package org.peimari.starttimeselector.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface CompetitorRepository extends JpaRepository<Competitor, Long> {

    List<Competitor> findAllBySeries(Series s);

    long countAllBySeries(Series s);

    List<Competitor> findAllBySeriesAndStartTimeIsNull(Series s);

    @Query("select c from Competitor c join c.series s join s.seriesGroup sg join sg.competition comp where comp = ?1")
    List<Competitor> findAllByCompetition(Competition competition);

}
