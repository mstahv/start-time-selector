package org.peimari.starttimeselector.entities;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeriesRepository extends JpaRepository<Series, Long> {

    public List<Series> findAllBySeriesGroup(SeriesGroup sg);

    public void deleteBySeriesGroup(SeriesGroup sg);

}
