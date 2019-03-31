package org.peimari.starttimeselector.entities;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StartTimeRepository extends JpaRepository<StartTime, Long> {

    List<StartTime> findAllBySeriesGroupAndCompetitorIsNull(SeriesGroup seriesGroup);

}
