package org.peimari.starttimeselector.adminview;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.peimari.starttimeselector.entities.SeriesGroup;
import org.peimari.starttimeselector.entities.StartTime;
import org.peimari.starttimeselector.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.firitin.components.orderedlayout.VHorizontalLayout;
import org.vaadin.firitin.components.textfield.VIntegerField;
import org.vaadin.firitin.components.timepicker.VTimePicker;
import org.vaadin.firitin.components.upload.UploadFileHandler;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Route(layout = AdminMainLayout.class)
@PageTitle("2. Define classes and groups")
public class ClassesAndClassGroupsEditor extends AbstractAdminView {

    private Grid<SeriesGroup> groups = new Grid<>();

    @Autowired
    private SeriesGroupStartTimeEditor startTimeEditor;

    TimePicker firstStart = new VTimePicker("First start").withWidth("9em");
    Checkbox singleQueue = new Checkbox("Single queue");
    IntegerField interval = new VIntegerField("Interval (s)").withWidth("8em");
    IntegerField slots = new VIntegerField("Slots").withWidth("8em");

    public ClassesAndClassGroupsEditor(AdminService adminService, AdminControl adminControl, SeriesGroupStartTimeEditor startTimeEditor) {
        super(adminControl, adminService);
        this.startTimeEditor = startTimeEditor;

        add(new Paragraph("Combining classes makes them use the same pool of start times (for example same first control). Competitors in deleted classes are ignored (E series without selectable times) "));

        // TreeGrid here

        groups.addColumn(SeriesGroup::getName).setHeader("Series").setSortable(true);
        groups.addColumn(sg -> sg.getStartTimes().size()).setHeader("Starttimes");
        groups.addComponentColumn(sg -> {
            long competitors = adminService.countCompetitors(sg);
            Span count = new Span("" + competitors);
            if(competitors > sg.getStartTimes().size()) {
                count.getStyle().set("color", "red");
                count.getStyle().set("font-weight", "bold");
                notify("Not enough start slots in group " + sg.getName());
            }
            return count;
        }).setHeader("Competitors");
        groups.addColumn(sg -> {
            List<StartTime> startTimes = sg.getStartTimes();
            startTimes.sort(Comparator.comparing(c -> c.getTime()));
            return startTimes.get(0);
        }).setHeader("First start");
        groups.addComponentColumn(sg -> new Button("Edit start times", e -> {
            startTimeEditor.setGroup(sg, adminControl.getCompetition(), this);
        }));
        groups.setSelectionMode(Grid.SelectionMode.MULTI);

        Button combine = new Button("Combine selected groups", e -> {
            Set<SeriesGroup> seriesGroupsToCombine = groups.asMultiSelect().getValue();
            adminService.combineSeriesGroups(seriesGroupsToCombine);
            listGroups();
        });
        Button uncombine = new Button("Break selected groups", e -> {
            Set<SeriesGroup> seriesGroupsToBreak = groups.asMultiSelect().getValue();
            adminService.breakSeriesGroups(seriesGroupsToBreak);
            listGroups();
        });
        Button addNew = new Button("Add single class...", e -> {
            new Dialog(new TextField("Class name", e1 -> {
                this.adminService.addClass(adminControl.getCompetition(), e1.getValue());
                e1.getSource().findAncestor(Dialog.class).close();
                listGroups();
            })).open();
        });
        
        Button delete = new Button("Delete selected groups", e -> {
            Set<SeriesGroup> seriesGroups = groups.asMultiSelect().getValue();
            adminService.deleteSeriesGroups(seriesGroups);
            listGroups();
        });

        UploadFileHandler ufh = new UploadFileHandler((inputStream, s, s1) -> {
            try {
                adminService.readInSeriesFromIrmaFile(inputStream,
                        adminControl.getCompetition(),
                        singleQueue.getValue(),
                        firstStart.getValue(),
                        interval.getValue(),
                        slots.getValue()
                );
                // TODO, due to Upload bug this don't work unless push is enabled :-(
                getUI().get().access(() -> {
                    listGroups();
                    notify("Classes loaded from IRMA file!");
                });
            } catch (IOException ex) {
                getUI().get().access(() -> {
                    Notification.show(ex.getMessage());
                });
                Logger.getLogger(ClassesAndClassGroupsEditor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                getUI().get().access(() -> {
                    Notification.show(ex.getMessage());
                });
                Logger.getLogger(ClassesAndClassGroupsEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        ufh.setUploadButton(new Button("Load new classes from IRMA file..."));

        singleQueue.addValueChangeListener(e -> {
            firstStart.setEnabled(e.getValue());
            interval.setEnabled(e.getValue());
            slots.setEnabled(e.getValue());
        });
        singleQueue.setValue(true);
        singleQueue.setValue(false);
        firstStart.setValue(adminControl.getCompetition().getStart().toLocalTime());
        interval.setValue(adminControl.getCompetition().getStartIntervalSeconds());
        long seconds = Duration.between(adminControl.getCompetition().getStart(), adminControl.getCompetition().getEnd()).toSeconds();
        int slots = (int) (seconds / interval.getValue());
        this.slots.setValue(slots);

        add(new VHorizontalLayout(ufh, singleQueue, firstStart, interval, this.slots).alignAll(Alignment.BASELINE));

        add(new VHorizontalLayout(combine, uncombine, delete, addNew).alignAll(Alignment.CENTER));
        addAndExpand(groups);
        listGroups();
    }

    void listGroups() {
        List<SeriesGroup> groups = adminService.getGroupsWithStartTimes(adminControl.getCompetition());
        this.groups.setItems(groups);
    }

}
