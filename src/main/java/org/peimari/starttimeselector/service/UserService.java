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
    StartTimeRepository startTimeRepository;

    public Collection<Competition> getCompetitions() {
        return competitionRepository.findAllByOpen(true);
    }

    @Transactional
    public List<Competitor> getCompetitorInfo(String licenceId) {
    	
        List<Competitor> all = new ArrayList<>(competitorRepository.findAllByLicenceId(licenceId));

        for(Iterator<Competitor> it = all.iterator(); it.hasNext();) {
            Competitor c = it.next();
            Competition competition = c.getCompetition();
            if(competition.isOpen()) {
                // lazy load possible start time and competition
                c.getStartTime();
            } else {
                it.remove();
            }
        }
        return all;
    }


    @Transactional
    public void reserveStartTime(Competitor competitor, StartTime startTime) {
        competitor = competitorRepository.getOne(competitor.getId());
        startTime = startTimeRepository.getOne(startTime.getId());
        if(competitor.getStartTime() != null) {
            throw new IllegalStateException("You already have a start time reserved. Probably another session picked a one for you.");
        } else {
            startTime.getCompetitors().add(competitor);
            competitor.setStartTime(startTime);
            competitorRepository.save(competitor);
            startTimeRepository.save(startTime);
        }
    }

    @Transactional
    public void releaseStartTime(Competitor competitor) {
    	StartTime startTime = competitor.getStartTime();
    	competitor = competitorRepository.getOne(competitor.getId());
    	competitor.setStartTime(null);
    	competitorRepository.save(competitor);
    }

	public List<StartTime> findAvailableStartTimes(Competitor competitor) {
		Competition competition = competitor.getCompetition();
		return startTimeRepository.findAllByCompetition(competition);
	}
}
