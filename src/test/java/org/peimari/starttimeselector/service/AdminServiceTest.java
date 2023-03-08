package org.peimari.starttimeselector.service;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.peimari.starttimeselector.entities.Competition;
import org.peimari.starttimeselector.entities.CompetitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;


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
        adminService.readInSeriesFromIrmaFile(getClass().getResourceAsStream("/irma-no-club.csv"), comp);
        int i = adminService.readInCompetitorsFromIrmaFile(getClass().getResourceAsStream("/irma-no-club.csv"), comp);
        Assertions.assertThat(i).describedAs("Expected competitor count").isEqualTo(4);
    }

}