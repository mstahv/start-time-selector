package org.peimari.starttimeselector.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Competition extends AbstractEntity {
    
    private String name;
    private LocalDateTime start;
    private LocalDateTime end;
    private boolean open;
    private Integer startIntervalSeconds = 60*15;
    
    @OneToMany(mappedBy="competition", cascade = CascadeType.ALL)
    private List<StartTime> startTimes = new ArrayList<StartTime>();

    @OneToMany(mappedBy="competition", cascade = CascadeType.ALL)
    private List<Competitor> competitors = new ArrayList<Competitor>();

}
