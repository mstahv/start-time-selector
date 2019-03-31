package org.peimari.starttimeselector.entities;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @NoArgsConstructor
public class Competitor extends AbstractEntity {
    
    private String name;
    private String licenceId;
    private String emitNr;

    @ManyToOne
    private Series series;
    
    @OneToOne(mappedBy="competitor")
    private StartTime startTime;

}
