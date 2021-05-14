package org.peimari.starttimeselector.adminview;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.peimari.starttimeselector.entities.Competition;
import org.peimari.starttimeselector.entities.SeriesGroup;
import org.peimari.starttimeselector.entities.StartTime;
import org.peimari.starttimeselector.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.firitin.components.DisclosurePanel;
import org.vaadin.firitin.components.button.VButton;

/**
 *
 * @author mstahv
 */
@SpringComponent
@UIScope
public class SeriesGroupStartTimeEditor extends VerticalLayout {

    @Autowired
    AdminService adminService;

    H3 header = new H3();
    Paragraph help = new Paragraph("Use this view to remove existing start times or add new start time ranges for a series (or group of series).");
    Grid<StartTime> grid = new Grid<>();
    Dialog dialog = new Dialog(this);
    Button close = new Button("Close", e-> dialog.close());
    Button deleteSelected = new VButton("Delete selected", this::deleteSelected);

    Paragraph newStartTimeHelp = new Paragraph("Add new start times for given time range. Note that the functionality doesn't check for existing start times, there may be duplicates if used improperly!");
    private DateTimePicker newStart;
    private DateTimePicker newEnd;
    IntegerField startIntervalSeconds = new IntegerField("Interval (seconds)");
    Button addNew = new VButton("Add new start times", this::addNew);

    VerticalLayout addNewLayout = new VerticalLayout();
    DisclosurePanel addNewStartTimes = new DisclosurePanel("Add new slots...", addNewLayout);

    private List<StartTime> startTimes;
    private SeriesGroup seriesGroup;
    private ClassesAndClassGroupsEditor editor;

    public SeriesGroupStartTimeEditor() {
    }

    private void addNew() {
        int count = 0;
        LocalDateTime start = newStart.getValue();
        LocalDateTime end = newEnd.getValue();
        while (start.isBefore(end)) {
            StartTime st = new StartTime();
            st.setTime(start);
            st.setSeriesGroup(seriesGroup);
            start = start.plusSeconds(startIntervalSeconds.getValue());
            st = adminService.save(st);
            startTimes.add(st);
            count++;
        }
        Notification.show(count + "new start times added!");
        addNewStartTimes.setOpen(false);
        listGrid();
    }

    private void deleteSelected() {
        Set<StartTime> toBeDeleted = grid.asMultiSelect().getSelectedItems();
        startTimes.removeAll(toBeDeleted);
        List<StartTime> reserved = adminService.deleteStartTimes(toBeDeleted);
        if(reserved.size() > 0) {
            Notification.show(reserved.size() + " starttimes were not deleted as they were already reserved!", 4000, Notification.Position.TOP_START);
            startTimes.addAll(reserved);
        }
        listGrid();
    }

    void setGroup(SeriesGroup sg, Competition competition, ClassesAndClassGroupsEditor editor) {
        lazyInit();
        this.seriesGroup = sg;
        this.editor = editor;
        header.setText("Edit start times for " + sg.getName());
        startTimes = new ArrayList(sg.getStartTimes());
        newStart.setValue(competition.getStart());
        newEnd.setValue(competition.getEnd());
        startIntervalSeconds.setValue(competition.getStartIntervalSeconds());

        listGrid();
        open();
    }

    protected void lazyInit() {
        if (newStart == null) {
            init();
        }
    }

    void open() {
        dialog.open();
    }

    private void listGrid() {
        Collections.sort(startTimes, (o1, o2) -> (int) (o1.getTime().toEpochSecond(ZoneOffset.UTC) - o2.getTime().toEpochSecond(ZoneOffset.UTC)));
        grid.setItems(startTimes);
    }

    private void init() {
        newStart = new DateTimePicker("First new start time");
        newEnd = new DateTimePicker("End of new start times (exclusive)");
        addNewLayout.add(newStartTimeHelp, newStart, newEnd, startIntervalSeconds, addNew);
        add(header, help,
                new HorizontalLayout(deleteSelected),
                grid, addNewStartTimes, close
        );
        grid.addColumn(st -> st.getTime());
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        
        dialog.setWidth("90%");
        dialog.setHeightFull();
        dialog.addDetachListener(e -> {
            editor.listGroups();
        });

    }

}
