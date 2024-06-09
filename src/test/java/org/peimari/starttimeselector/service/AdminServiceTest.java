package org.peimari.starttimeselector.service;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.peimari.starttimeselector.entities.Competition;
import org.peimari.starttimeselector.entities.CompetitionRepository;
import org.peimari.starttimeselector.entities.SeriesGroup;
import org.peimari.starttimeselector.entities.StartTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;


@ExtendWith(SpringExtension.class)
@SpringBootTest
class AdminServiceTest {

    @Autowired
    AdminService adminService;

    @Autowired
    CompetitionRepository competitionRepository;

    @Test
    public void testReadCompetitorsFromIrmaNoClub() throws Exception {
        Competition comp = new Competition();
        comp.setName("Test");
        comp.setStart(LocalDateTime.now());
        comp.setEnd(LocalDateTime.now().plusHours(2));
        comp = competitionRepository.save(comp);
        adminService.readInSeriesFromIrmaFile(getClass().getResourceAsStream("/irma-no-club.csv"), comp, false, null ,null, null);
        int i = adminService.readInCompetitorsFromIrmaFile(getClass().getResourceAsStream("/irma-no-club.csv"), comp);
        Assertions.assertThat(i).describedAs("Expected competitor count").isEqualTo(4);
    }

    @Test
    public void testReadCompetitorsToSingleQueue() throws Exception {
        Competition comp = new Competition();
        comp.setName("Test");
        comp.setStart(LocalDateTime.now());
        comp.setEnd(LocalDateTime.now().plusHours(2));
        comp = competitionRepository.save(comp);
        adminService.readInSeriesFromIrmaFile(getClass().getResourceAsStream("/irma-no-club.csv"), comp, true, LocalTime.of(9,0), 30, 300);
        List<SeriesGroup> groupsWithStartTimes = adminService.getGroupsWithStartTimes(comp);
        Assertions.assertThat(groupsWithStartTimes.size()).describedAs("Group count").isEqualTo(1);
        long startSlots = groupsWithStartTimes.get(0).getStartTimes().stream().count();
        Assertions.assertThat(startSlots).describedAs("Slots count").isEqualTo(300);
        StartTime startTime = groupsWithStartTimes.get(0).getStartTimes().get(1);
        Assertions.assertThat(startTime.getTime().getSecond()).describedAs("Second start is at at 30s").isEqualTo(30);

    }


}