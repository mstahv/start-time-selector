package org.peimari.starttimeselector.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Series extends AbstractEntity {

    private String name;

    @ManyToOne
    private SeriesGroup seriesGroup;

}
