package org.peimari.starttimeselector.adminview;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.peimari.starttimeselector.entities.StartTime;
import org.peimari.starttimeselector.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.firitin.components.DisclosurePanel;
import org.vaadin.firitin.components.button.VButton;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.PageTitle;

/**
 *
 * @author mstahv
 */
@PageTitle("2.Start times")
public class StartTimeEditor extends AbstractAdminView {

    @Autowired
    AdminService adminService;

    Paragraph help = new Paragraph("Use this view to remove existing start times or add new start time ranges for a series (or group of series).");
    Grid<StartTime> grid = new Grid<>();
    Button deleteSelected = new VButton("Delete selected", this::deleteSelected);

    Paragraph newStartTimeHelp = new Paragraph("Add new start times for given time range. Note that the functionality doesn't check for existing start times, there may be duplicates if used improperly!");
    private DateTimePicker newStart;
    private DateTimePicker newEnd;
    IntegerField startIntervalSeconds = new IntegerField("Interval (seconds)");
    IntegerField maximumCompetitors = new IntegerField("Max competitors");
    Button addNew = new VButton("Add new start times", this::addNew);

    VerticalLayout addNewLayout = new VerticalLayout();
    DisclosurePanel addNewStartTimes = new DisclosurePanel("Add new slots...", addNewLayout);

    private List<StartTime> startTimes;

    public StartTimeEditor(AdminControl adminControl, AdminService adminService) {
        super(adminControl, adminService);
        
        newStart = new DateTimePicker("First new start time");
        newEnd = new DateTimePicker("End of new start times (exclusive)");
        
        addNewLayout.add(newStartTimeHelp, newStart, newEnd, startIntervalSeconds, maximumCompetitors, addNew);
        
        add(help,
                new HorizontalLayout(deleteSelected),
                grid, addNewStartTimes
        );
        grid.addColumn(st -> st.toString());
        grid.addColumn(st -> st.getMaximumCompetitors()).setHeader("max competitors");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        

    }
    
    @Override
    	protected void onAttach(AttachEvent attachEvent) {
    		// TODO Auto-generated method stub
    		super.onAttach(attachEvent);
    		if(adminControl.getCompetition() != null) {

                startTimes = adminService.getStartTimes(adminControl.getCompetition());
                newStart.setValue(adminControl.getCompetition().getStart());
                newEnd.setValue(adminControl.getCompetition().getEnd());
                startIntervalSeconds.setValue(adminControl.getCompetition().getStartIntervalSeconds());
                maximumCompetitors.setValue(50);
                listGrid();
    		}
    	}

    private void addNew() {
        int count = 0;
        LocalDateTime start = newStart.getValue();
        LocalDateTime end = newEnd.getValue();
        while (start.isBefore(end)) {
            StartTime st = new StartTime();
            st.setTime(start);
            st.setCompetition(adminControl.getCompetition());
            start = start.plusSeconds(startIntervalSeconds.getValue());
            st.setSeconds(startIntervalSeconds.getValue());
            st.setMaximumCompetitors(maximumCompetitors.getValue());
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

    private void listGrid() {
        Collections.sort(startTimes, (o1, o2) -> (int) (o1.getTime().toEpochSecond(ZoneOffset.UTC) - o2.getTime().toEpochSecond(ZoneOffset.UTC)));
        grid.setItems(startTimes);
    }


}
