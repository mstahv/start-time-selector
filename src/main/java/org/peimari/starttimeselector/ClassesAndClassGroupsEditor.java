package org.peimari.starttimeselector;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.peimari.starttimeselector.entities.Competition;
import org.peimari.starttimeselector.entities.SeriesGroup;
import org.peimari.starttimeselector.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.firitin.components.upload.UploadFileHandler;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Set;

@SpringComponent
@UIScope
public class ClassesAndClassGroupsEditor extends VerticalLayout {


    @Autowired
    AdminService adminService;

    Competition competition;

    private Grid<SeriesGroup> groups = new Grid<>();
    
    @Autowired
    private SeriesGroupStartTimeEditor startTimeEditor;

    @PostConstruct
    void init() {
        add(new Hr(), new H3("Classes and Class groups"));

        add(new Paragraph("Combining classes makes them use the same pool of start times (for example same first control). Competitors in deleted classes are ignored (E series without selectable times) "));

        // TreeGrid here

        groups.addColumn(SeriesGroup::getName).setHeader("Series");
        groups.addColumn(sg -> sg.getStartTimes().size()).setHeader("Starttimes");
        groups.addColumn(sg -> adminService.countCompetitors(sg)).setHeader("Competitors");
        groups.addComponentColumn(sg -> new Button("Edit start times", e -> {
            startTimeEditor.setGroup(sg, competition, this);
        }));
        groups.setSelectionMode(Grid.SelectionMode.MULTI);

        Button combine = new Button("Combine selected groups", e -> {
            Set<SeriesGroup> seriesGroupsToCombine = groups.asMultiSelect().getValue();
            adminService.combineSeriesGroups(seriesGroupsToCombine);
            listGroups();
        });
        Button delete = new Button("Delete selected groups", e -> {
            Set<SeriesGroup> seriesGroups = groups.asMultiSelect().getValue();
            adminService.deleteSeriesGroups(seriesGroups);
            listGroups();
        });

        add(new HorizontalLayout(combine, delete), groups);
        listGroups();

        UploadFileHandler ufh = new UploadFileHandler((inputStream, s, s1) -> {
            try {
                adminService.readInSeriesFromIrmaFile(inputStream, competition);
                // TODO, due to Upload bug this don't work unless push is enabled :-(
                getUI().get().access(() -> {
                    listGroups();
                    notify("Classes loaded from IRMA file!");
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        ufh.setUploadButton(new Button("Load classes from IRMA file..."));

        add(new HorizontalLayout(ufh));
    }

    private void notify(String s) {
        Notification.show(s, 3000, Notification.Position.MIDDLE);
    }

    void listGroups() {
        List<SeriesGroup> groups = adminService.getGroupsWithStartTimes(competition);
        this.groups.setItems(groups);
    }

    void setCompetition(Competition c) {
        this.competition = c;
        if (competition != null) {
            listGroups();
        }
    }
}
