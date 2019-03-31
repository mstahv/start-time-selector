package org.peimari.starttimeselector.service;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import org.peimari.starttimeselector.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.annotation.SessionScope;

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

    public List<List<String>> readIrmaFile(InputStream inputStream) throws IOException {
        List<List<String>> records = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = br.readLine()) != null) {
            String[] values = line.split(DELIMITER);
            records.add(Arrays.asList(values));
        }
        return records;
    }


    @Transactional
    public void readInSeriesFromIrmaFile(InputStream inputStream, Competition c) throws IOException {
        List<List<String>> input = readIrmaFile(inputStream);
        Set<String> seriesNames = input.stream().map(l -> l.get(0)).collect(Collectors.toSet());
        seriesNames.forEach(name -> addSeriesAndGroup(c, name));
    }

    private void addSeriesAndGroup(Competition competition, String s) {
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
        seriesRepository.save(series);
    }

    @Transactional
    public List<SeriesGroup> getGroups(Competition competition) {
        return seriesGroupRepository.findAllByCompetition(competition);
    }

    @Transactional
    public void combineSeriesGroups(Set<SeriesGroup> seriesGroupsToCombine) {
        if (seriesGroupsToCombine.size() > 1) {
            ArrayList<SeriesGroup> seriesGroups = new ArrayList<>(seriesGroupsToCombine);
            SeriesGroup master = seriesGroups.remove(0);
            SeriesGroup next;
            while (!seriesGroups.isEmpty()) {
                next = seriesGroups.remove(0);
                master.setName(master.getName() + "," + next.getName());
                next.getSeries().forEach(s -> {
                    master.getSeries().add(s);
                    s.setSeriesGroup(master);
                });
                next.getSeries().clear();
                seriesGroupRepository.deleteById(next.getId());
            }
            seriesGroupRepository.save(master);
        }
    }

    @Transactional
    public void deleteSeriesGroups(Set<SeriesGroup> seriesGroups) {
        seriesGroups.forEach(sg -> seriesGroupRepository.deleteById(sg.getId()));
    }

    @Transactional
    public void readInCompetitorsFromIrmaFile(InputStream inputStream, Competition competition) throws IOException {
        List<List<String>> input = readIrmaFile(inputStream);
        List<SeriesGroup> allByCompetition = seriesGroupRepository.findAllByCompetition(competition);
        Map<String, Series> seriesNameToEntity = new HashMap<>();
        allByCompetition.stream().forEach(group -> group.getSeries().stream().forEach(s -> seriesNameToEntity.put(s.getName(), s)));
        input.stream().forEach(line -> {
            //H21A;1504;500728;;Heikkilä Tero;PR;
            Competitor competitor = new Competitor();
            competitor.setEmitNr(line.get(2));
            competitor.setLicenceId(line.get(1));
            competitor.setName(line.get(4));
            Series s = seriesNameToEntity.get(line.get(0));
            if (s != null) {
                competitor.setSeries(s);
                competitorRepository.save(competitor);
            }
        });
    }

    @Transactional
    public void raffleRest(Competition competition) {
        competition = competitionRepository.getOne(competition.getId());

        competition.getSeriesGroups().forEach(seriesGroup -> {
            List<StartTime> startTimes = startTimeRepository.findAllBySeriesGroupAndCompetitorIsNull(seriesGroup);
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
    }

    @Transactional
    public void writeCsvForPirila(Competition competition, OutputStream os) {
        PrintWriter writer = new PrintWriter(os);

        writer.println("ArvLahto-1;Sarja;Nimi;KilpId;Emit");

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
                    writer.println();
                }
            });

        });
        writer.close();
    }

    @Transactional
    public void deleteCompetition(Competition competition) {
        competition = competitionRepository.getOne(competition.getId());
        competition.getSeriesGroups().forEach(seriesGroupRepository::delete);
        competitionRepository.delete(competition);
    }
}
