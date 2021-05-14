package org.peimari.starttimeselector.adminview;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.peimari.starttimeselector.entities.SeriesGroup;
import org.peimari.starttimeselector.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.firitin.components.orderedlayout.VHorizontalLayout;
import org.vaadin.firitin.components.orderedlayout.VVerticalLayout;
import org.vaadin.firitin.components.upload.UploadFileHandler;

import java.io.IOException;
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
        groups.addComponentColumn(sg -> new Button("Edit start times", e -> {
            startTimeEditor.setGroup(sg, adminControl.getCompetition(), this);
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

        UploadFileHandler ufh = new UploadFileHandler((inputStream, s, s1) -> {
            try {
                adminService.readInSeriesFromIrmaFile(inputStream, adminControl.getCompetition());
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

        add(new VHorizontalLayout(combine, delete, ufh).alignAll(Alignment.CENTER));
        addAndExpand(groups);
        listGroups();
    }

    void listGroups() {
        List<SeriesGroup> groups = adminService.getGroupsWithStartTimes(adminControl.getCompetition());
        this.groups.setItems(groups);
    }

}
