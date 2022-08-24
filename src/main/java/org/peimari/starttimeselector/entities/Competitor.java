package org.peimari.starttimeselector.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Competitor extends AbstractEntity {
    
    private String name;
    private String licenceId;
    private String emitNr;
    private String club;

    @ManyToOne
    private Series series;
    
    @OneToOne(mappedBy="competitor")
    private StartTime startTime;

}
