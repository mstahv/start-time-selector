package org.peimari.starttimeselector.service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    SeriesRepository seriesRepository;
    @Autowired
    SeriesGroupRepository seriesGroupRepository;
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
    public void readInSeriesFromIrmaFile(InputStream inputStream, Competition c) throws IOException, Exception {
        List<List<String>> input = readIrmaFile(inputStream, false);
        Set<String> seriesNames = input.stream().map(l -> l.get(0)).collect(Collectors.toSet());
        seriesNames.forEach(name -> addSeriesAndGroup(c, name));
    }

    @Transactional
    private void addSeriesAndGroup(Competition competition, String s) {
        if(s == null || s.isEmpty()) {
            return;
        }
        Series series = new Series();
        series.setName(s);
        SeriesGroup group = new SeriesGroup();
        group.setName(s);
        group.setCompetition(competition);
        series.setSeriesGroup(group);
        LocalDateTime start = competition.getStart();
        LocalDateTime end = competition.getEnd();
        while (start.isBefore(end)) {
            StartTime st = new StartTime();
            st.setSeriesGroup(group);
            st.setTime(start);
            group.getStartTimes().add(st);
            start = start.plusSeconds(competition.getStartIntervalSeconds());
        }
        seriesGroupRepository.save(group);
        seriesRepository.save(series);
    }

    @Transactional
    public List<SeriesGroup> getGroups(Competition competition) {
        return seriesGroupRepository.findAllByCompetition(competition);
    }

    @Transactional
    public List<SeriesGroup> getGroupsWithStartTimes(Competition competition) {
        List<SeriesGroup> groups = seriesGroupRepository.findAllByCompetitionOrderByName(competition);
        groups.forEach(g -> g.getStartTimes().size());
        return groups;
    }
    
    public long countCompetitors(SeriesGroup sg) {
        long count = 0;
        for (Series series : sg.getSeries()) {
            count = count + competitorRepository.countAllBySeries(series);
        }
        return count;
    }
    
    @Transactional
    public List<Series> getSeries(Competition competition) {
        ArrayList<Series> series = new ArrayList<>();
        seriesGroupRepository.findAllByCompetitionOrderByName(competition).stream().forEach(sg -> series.addAll(sg.getSeries()));
        return series;
    }

    @Transactional
    public void combineSeriesGroups(Set<SeriesGroup> seriesGroupsToCombine) {
        if (seriesGroupsToCombine.size() > 1) {
            ArrayList<SeriesGroup> seriesGroups = new ArrayList<>(seriesGroupsToCombine);
            SeriesGroup master = seriesGroupRepository.findById(seriesGroups.remove(0).getId()).get();
            SeriesGroup next;
            while (!seriesGroups.isEmpty()) {
                next = seriesGroupRepository.findById(seriesGroups.remove(0).getId()).get();
                master.setName(master.getName() + "," + next.getName());
                next.getSeries().forEach(s -> {
                    master.getSeries().add(s);
                    s.setSeriesGroup(master);
                    seriesRepository.save(s);
                });
                next.getSeries().clear();
                seriesGroupRepository.deleteById(next.getId());
            }
            seriesGroupRepository.save(master);
        }
    }

    @Transactional
    public void deleteSeriesGroups(Set<SeriesGroup> seriesGroups) {
        seriesGroups.forEach(sg -> {
            sg.getSeries().forEach(series -> competitorRepository.deleteBySeries(series));
            seriesGroupRepository.deleteById(sg.getId());
        });
    }

    @Transactional
    public int readInCompetitorsFromIrmaFile(InputStream inputStream, Competition competition) throws IOException, Exception {
        MutableInt mutableInt = new MutableInt(0);
        List<List<String>> input = readIrmaFile(inputStream, true);
        List<SeriesGroup> allByCompetition = seriesGroupRepository.findAllByCompetition(competition);
        Map<String, Series> seriesNameToEntity = new HashMap<>();
        allByCompetition.stream().forEach(group -> group.getSeries().stream().forEach(s -> seriesNameToEntity.put(s.getName(), s)));
        input.stream().forEach(line -> {
            //H21A;1504;500728;;Heikkilä Tero;PR;
            Competitor competitor = new Competitor();
            competitor.setEmitNr(line.get(2));
            competitor.setLicenceId(line.get(1));
            competitor.setName(line.get(4));
            competitor.setClub(line.get(5));
            Series s = seriesNameToEntity.get(line.get(0));
            if (s != null) {
                competitor.setSeries(s);
                competitorRepository.save(competitor);
                mutableInt.increment();
            }
        });
        return mutableInt.intValue();
    }

    @Transactional
    public Competition raffleRest(Competition competition) {
        competition = competitionRepository.getOne(competition.getId());
        competition.setOpen(false);
        competition = competitionRepository.save(competition);

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
        return competition;
    }

    @Transactional
    public void writeCsvForPirila(Competition competition, OutputStream os) {
        PrintWriter writer = new PrintWriter(os);

        writer.println("ArvLahto-1;Sarja;Nimi;KilpId;Emit;Seura;SelfAssigned");

        competition = competitionRepository.getOne(competition.getId());

        competition.getSeriesGroups().forEach(seriesGroup -> {
            seriesGroup.getStartTimes().forEach(startTime -> {
                Competitor competitor = startTime.getCompetitor();
                if (competitor != null) {
                    writer.print(startTime.getTime().toLocalTime().toString());
                    writer.print(DELIMITER);
                    writer.print(competitor.getSeries().getName());
                    writer.print(DELIMITER);
                    writer.print(competitor.getName());
                    writer.print(DELIMITER);
                    writer.print(competitor.getLicenceId());
                    writer.print(DELIMITER);
                    writer.print(competitor.getEmitNr());
                    writer.print(DELIMITER);
                    writer.print(competitor.getClub());
                    writer.print(DELIMITER);
                    writer.print(startTime.isSelfAssigned());
                    writer.print(DELIMITER);
                    writer.println();
                }
            });

        });
        writer.close();
    }

    @Transactional
    public void deleteCompetition(Competition competition) {
        competition = competitionRepository.getOne(competition.getId());
        competitorRepository.findAllByCompetition(competition).forEach(competitorRepository::delete);
        competition.getSeriesGroups().forEach(sg -> {
            sg.getStartTimes().forEach(startTimeRepository::delete);
            sg.getSeries().forEach(seriesRepository::delete);
            seriesGroupRepository.delete(sg);
        });
        competitionRepository.delete(competition);
    }

    @Transactional
    public void deleteCompetitor(Competitor competitor) {
        competitor = competitorRepository.findById(competitor.getId()).get();
        StartTime startTime = competitor.getStartTime();
        if (startTime != null) {
            startTime.setCompetitor(null);
            startTime.setSelfAssigned(false);
            startTimeRepository.save(startTime);
        }
        competitorRepository.delete(competitor);
    }

    @Transactional
    public void addCompetitor(Series s, String licence, String name) {
        Competitor competitor = new Competitor();
        competitor.setSeries(s);
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
            // don't delete reserved start times
            if(startTime.getCompetitor() != null) {
                reservedStartTimes.add(startTime);
            } else {
                startTimeRepository.delete(startTime);
            }
        });
        return reservedStartTimes;
    }

    @Transactional
    public void releaseStartTime(Competitor c) {
        Competitor competitor = competitorRepository.getOne(c.getId());
        StartTime startTime = competitor.getStartTime();
        startTime.setSelfAssigned(false);
        startTime.setCompetitor(null);
        competitor.setStartTime(null);
        startTimeRepository.save(startTime);
        competitorRepository.save(competitor);
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
    public void removeAllCompetitors(Competition competition) {
        competition = competitionRepository.getOne(competition.getId());
        competition.getSeriesGroups()
                .forEach(seriesGroup -> seriesGroup.getSeries()
                        .forEach(series -> competitorRepository.deleteBySeries(series)));
    }

}
