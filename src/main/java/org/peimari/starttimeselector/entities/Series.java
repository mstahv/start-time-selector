package org.peimari.starttimeselector.entities;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Series extends AbstractEntity {

    private String name;

    @ManyToOne(cascade = CascadeType.ALL)
    private SeriesGroup seriesGroup;

}
