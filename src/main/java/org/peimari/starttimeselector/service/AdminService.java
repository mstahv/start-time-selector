package org.peimari.starttimeselector.service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

import org.apache.commons.lang3.mutable.MutableInt;
import org.peimari.starttimeselector.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    public static final String DELIMITER = ";";
    @Autowired
    CompetitionRepository competitionRepository;
    @Autowired
    CompetitorRepository competitorRepository;
    @Autowired
    StartTimeRepository startTimeRepository;

    public Collection<Competition> getCompetitions() {
        return competitionRepository.findAll();
    }

    @Transactional
    public Competition createNewCompetition() {
        Competition c = new Competition();
        c.setName("Your competition name here");
        c.setStart(LocalDateTime.now().withNano(0).withSecond(0));
        c.setEnd(LocalDateTime.now().plusHours(5).withNano(0).withSecond(0));
        return competitionRepository.save(c);
    }

    public Competition save(Competition bean) {
        return competitionRepository.save(bean);
    }

    public List<List<String>> readIrmaFile(InputStream inputStream, boolean validate) throws IOException, Exception {
        List<List<String>> records = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line;
        while ((line = br.readLine()) != null) {
            String[] values = line.split(DELIMITER);
            records.add(Arrays.asList(values));
        }
        if(validate) {
            validateRecord(records.get(0));
        }
        return records;
    }


    @Transactional
    public int readInCompetitorsFromIrmaFile(InputStream inputStream, Competition competition) throws IOException, Exception {
        MutableInt mutableInt = new MutableInt(0);
        List<List<String>> input = readIrmaFile(inputStream, true);
        input.stream().forEach(line -> {
            //H21A;1504;500728;;Heikkilä Tero;PR;
            Competitor competitor = new Competitor();
            competitor.setEmitNr(line.get(2));
            competitor.setLicenceId(line.get(1));
            competitor.setName(line.get(4));
            competitor.setClub(line.get(5));
            competitor.setCompetition(competition);
            competitorRepository.save(competitor);
            mutableInt.increment();
        });
        return mutableInt.intValue();
    }

    @Transactional
    public Competition raffleRest(Competition competition) {
        competition = competitionRepository.getOne(competition.getId());
        competition.setOpen(false);
        competition = competitionRepository.save(competition);

        // TODO
        /*
        competition.getSeriesGroups().forEach(seriesGroup -> {
            List<StartTime> startTimes = startTimeRepository.findAllBySeriesGroupAndCompetitorIsNullOrderByTimeAsc(seriesGroup);
            Collections.shuffle(startTimes);
            seriesGroup.getSeries().forEach(series -> {
                List<Competitor> competitors = competitorRepository.findAllBySeries(series);
                competitors.forEach(competitor -> {
                    if (competitor.getStartTime() == null) {
                        StartTime startTime = startTimes.remove(0);
                        startTime.setCompetitor(competitor);
                        competitor.setStartTime(startTime);
                        startTimeRepository.save(startTime);
                        competitorRepository.save(competitor);
                    }
                });
            });
        });
         */
        return competition;
    }

    @Transactional
    public void writeCsvForPirila(Competition competition, OutputStream os) {
        PrintWriter writer = new PrintWriter(os);

        writer.println("ArvLahto-1;Nimi;KilpId;Emit;Seura;SelfAssigned");

        competition = competitionRepository.getOne(competition.getId());
        competition.getStartTimes().forEach(st -> {
            st.getCompetitors().forEach( competitor -> {
                StartTime startTime = competitor.getStartTime();
                if(startTime != null) {
                    writer.print(startTime.getTime().toLocalTime().toString());
                }
                writer.print(DELIMITER);
                writer.print(competitor.getName());
                writer.print(DELIMITER);
                writer.print(competitor.getLicenceId());
                writer.print(DELIMITER);
                writer.print(competitor.getEmitNr());
                writer.print(DELIMITER);
                writer.print(competitor.getClub());
                writer.print(DELIMITER);
                writer.println();
            });
        });
        writer.close();
    }

    @Transactional
    public void deleteCompetition(Competition competition) {
        competition = competitionRepository.getOne(competition.getId());
        competitorRepository.findAllByCompetition(competition).forEach(competitorRepository::delete);
        competition.getStartTimes().forEach(st -> {
            startTimeRepository.delete(st);
        });
        competitionRepository.delete(competition);
    }

    @Transactional
    public void deleteCompetitor(Competitor competitor) {
        competitor = competitorRepository.findById(competitor.getId()).get();
        competitorRepository.delete(competitor);
    }

    @Transactional
    public void addCompetitor(Competition c, String licence, String name) {
        Competitor competitor = new Competitor();
        competitor.setCompetition(c);
        competitor.setLicenceId(licence);
        competitor.setName(name);
        competitorRepository.save(competitor);
    }

    public List<Competitor> getCompetitors(Competition c) {
        return competitorRepository.findAllByCompetition(c);
    }

    public StartTime save(StartTime st) {
        return startTimeRepository.save(st);
    }

    @Transactional
    public List<StartTime> deleteStartTimes(Set<StartTime> toBeDeleted) {
        ArrayList<StartTime> reservedStartTimes = new ArrayList<>();
        toBeDeleted.forEach(st -> {
            StartTime startTime = startTimeRepository.getOne(st.getId());
        });
        return reservedStartTimes;
    }

    @Transactional
    public void releaseStartTime(Competitor c) {
        Competitor competitor = competitorRepository.getOne(c.getId());
        StartTime startTime = competitor.getStartTime();
        competitor.setStartTime(null);
        competitorRepository.save(competitor);
        competitor.setStartTime(null);
        startTimeRepository.save(startTime);
    }

    private void validateRecord(List<String> record) throws Exception {
        // Sarja;Lisenssinumero;Emit;EmiTag;Nimi;Seura
        // series;licenseid;emit;emittag;Name;club;
        // Only first two fields and name are relevant
        if(record.size() < 5
                ||
                record.get(0).isEmpty()
                ||
                record.get(4).isEmpty()
                ) {
            throw new Exception("The file is not in the format: series;licenseid;emit;emittag;Name;club;");
        }
        try {
            Integer.parseInt(record.get(1));
        } catch (Exception e) {
            throw new Exception("The file is not in the format: series;licenseid;emit;emittag;Name;club;");
        }
    }

    @Transactional
    public List<StartTime> getStartTimes(Competition competition) {
        return new ArrayList<>(competitionRepository.findById(competition.getId()).get().getStartTimes());
    }
}
