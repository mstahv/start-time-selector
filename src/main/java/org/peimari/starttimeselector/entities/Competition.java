package org.peimari.starttimeselector.entities;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Competition extends AbstractEntity {
    
    private String name;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean open;
    private Integer startIntervalSeconds = 60;
    
    @OneToMany(mappedBy="competition", cascade = CascadeType.ALL)
    private List<SeriesGroup> seriesGroups;
    
    
    
}
