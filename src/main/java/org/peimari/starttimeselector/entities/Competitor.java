package org.peimari.starttimeselector.entities;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

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
    private Competition competition;

    @ManyToOne
    private StartTime startTime;

}
