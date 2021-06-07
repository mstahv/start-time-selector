package org.peimari.starttimeselector.entities;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class StartTime extends AbstractEntity {

    @ManyToOne
    Competition competition;
    private LocalDateTime time;
    private int seconds = 900;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "startTime")
    private List<Competitor> competitors;
    private int maximumCompetitors = 50;


    @Override
    public String toString() {
        return time.toLocalTime() + "-" + time.plus(seconds, ChronoUnit.SECONDS).toLocalTime();
    }
    
}
