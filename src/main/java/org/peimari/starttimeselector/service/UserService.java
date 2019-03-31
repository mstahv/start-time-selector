package org.peimari.starttimeselector.service;

import org.peimari.starttimeselector.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    public List<Competitor> getCompetitorInfo(String id, String emit) {
        Competitor sample = new Competitor();
        sample.setEmitNr(emit);
        sample.setLicenceId(id);
        List<Competitor> all = competitorRepository.findAll(Example.of(sample));
        all.forEach(c -> {
            // lazy load possible start time and competition
            c.getStartTime();
            c.getSeries().getSeriesGroup().getCompetition();
        });
        return all;
    }

    public List<StartTime> findAvailableStartTimes(SeriesGroup seriesGroup) {
        return startTimeRepository.findAllBySeriesGroupAndCompetitorIsNull(seriesGroup);
    }

    @Transactional
    public void reserveStartTime(Competitor competitor, StartTime startTime) {
        startTime.setCompetitor(competitor);
        startTimeRepository.save(startTime);
    }

    @Transactional
    public void releaseStartTime(StartTime startTime) {
        startTime.setCompetitor(null);
        startTimeRepository.save(startTime);
    }
}
