package org.peimari.starttimeselector.service;

import org.peimari.starttimeselector.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserService {

    @Autowired
    CompetitionRepository competitionRepository;
    @Autowired
    CompetitorRepository competitorRepository;
    @Autowired
    SeriesRepository seriesRepository;
    @Autowired
    SeriesGroupRepository seriesGroupRepository;
    @Autowired
    StartTimeRepository startTimeRepository;

    public Collection<Competition> getCompetitions() {
        return competitionRepository.findAllByOpen(true);
    }

    @Transactional
    public List<Competitor> getCompetitorInfo(String licenceId) {
        Competitor sample = new Competitor();
        sample.setLicenceId(licenceId);
        List<Competitor> all = competitorRepository.findAll(Example.of(sample));
        for(Iterator<Competitor> it = all.iterator(); it.hasNext();) {
            Competitor c = it.next();
            Competition competition = c.getSeries().getSeriesGroup().getCompetition();
            if(competition.isOpen()) {
                // lazy load possible start time and competition
                c.getStartTime();
            } else {
                it.remove();
            }
        }
        all.sort(Comparator.comparing(c -> c.getSeries().getSeriesGroup().getCompetition().getStartDate()));
        return all;
    }

    public List<StartTime> findAvailableStartTimes(SeriesGroup seriesGroup) {
        return startTimeRepository.findAllBySeriesGroupAndCompetitorIsNullOrderByTimeAsc(seriesGroup);
    }

    @Transactional
    public void reserveStartTime(Competitor competitor, StartTime startTime) {
        competitor = competitorRepository.getOne(competitor.getId());
        if(competitor.getStartTime() != null) {
            throw new IllegalStateException("You already have a start time reserved. Probably another session picked a one for you.");
        } else {
            startTime.setCompetitor(competitor);
            startTime.setSelfAssigned(true);
            startTimeRepository.save(startTime);
        }
    }

    @Transactional
    public void releaseStartTime(StartTime startTime) {
        startTime.setCompetitor(null);
        startTime.setSelfAssigned(false);
        startTimeRepository.save(startTime);
    }
}
