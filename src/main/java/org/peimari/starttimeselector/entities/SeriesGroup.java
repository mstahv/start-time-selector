package org.peimari.starttimeselector.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * By default all Series have one SeriesGroup, but series can be combined
 * together so that they share the same start time pool. For example if
 * two relatively equally fast series share same first control, it makes
 * sense to group them together -> only one participant is heading to 
 * same control.
 * 
 * @author mstahv
 */
@Entity
@Getter @Setter @NoArgsConstructor
public class SeriesGroup extends AbstractEntity {

    private String name;

    @ManyToOne
    private Competition competition;

    @OneToMany(mappedBy="seriesGroup", cascade = CascadeType.ALL)
    private List<Series> series = new ArrayList<>();
    
    @OneToMany(mappedBy="seriesGroup", cascade = CascadeType.ALL)
    private List<StartTime> startTimes = new ArrayList<>();
    
}
