package org.peimari.starttimeselector.entities;

import java.time.LocalDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class StartTime extends AbstractEntity {

    @ManyToOne
    private SeriesGroup seriesGroup;
    private LocalDateTime time;
    @OneToOne(cascade = CascadeType.ALL)
    private Competitor competitor;
    private boolean selfAssigned;

    @Override
    public String toString() {
        return time.getHour() + ":" + time.getMinute();
    }
}
