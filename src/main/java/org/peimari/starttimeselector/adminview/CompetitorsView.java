package org.peimari.starttimeselector.adminview;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import org.peimari.starttimeselector.entities.Competition;
import org.peimari.starttimeselector.entities.Competitor;
import org.peimari.starttimeselector.entities.Series;
import org.peimari.starttimeselector.entities.StartTime;
import org.peimari.starttimeselector.service.AdminService;
import org.vaadin.firitin.components.button.DeleteButton;
import org.vaadin.firitin.components.button.VButton;
import org.vaadin.firitin.components.orderedlayout.VHorizontalLayout;
import org.vaadin.firitin.components.textfield.VTextField;
import org.vaadin.firitin.components.upload.UploadFileHandler;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@PageTitle("3. Competitors")
public class CompetitorsView extends AbstractAdminView {

    private final Competition competition;

    private Grid<Competitor> competitorGrid = new Grid<>(Competitor.class);
    private Paragraph compatitorCount = new Paragraph();
    Button openForStartTimeSelection = new VButton("Open for start time selection", this::openForSelection);

    public CompetitorsView(AdminControl adminControl, AdminService adminService) {
        super(adminControl, adminService);
        this.competition = adminControl.getCompetition();

        Competition competition = adminControl.getCompetition();

        competitorGrid.setColumns("name", "licenceId", "emitNr");
        competitorGrid.addColumn(c -> {
            StartTime st = c.getStartTime();
            if (st == null) {
                return "not set";
            } else {
                return st.getTime().toString();
            }
        });

        competitorGrid.addComponentColumn(c -> {
            final HorizontalLayout actions = new HorizontalLayout(
                    new DeleteButton()
                            .withIcon(VaadinIcon.TRASH.create())
                            .withConfirmText("Are you sure you want to delete the competitor " + c.getName() + "?")
                            .withConfirmHandler(() -> {
                                adminService.deleteCompetitor(c);
                                listCompetitors();
                            })
            );
            if (c.getStartTime() != null) {
                actions.add(new VButton("Release start time", e -> {
                    adminService.releaseStartTime(c);
                    listCompetitors();
                }));
            }
            return actions;
        });


        add(new Paragraph("Be sure that previous step is properly configured. The file can be same IRMA csv file as used to load competitors. Uploading may take a while. Only competitors with valid series are saved."));

        DeleteButton removeAllCompetitors = new DeleteButton()
                .withText("Remove all competitors")
                .withConfirmHandler(() -> {
                    adminService.removeAllCompetitors(competition);
                    listCompetitors();
                });

        Select<Series> seriesSelect = new Select<>();
        seriesSelect.setItems(adminService.getSeries(competition));
        seriesSelect.setTextRenderer(Series::getName);
        TextField license = new VTextField().withPlaceholder("LicenceNr");
        TextField name = new VTextField().withPlaceholder("Name");
        Button addNewCompetitor = new Button("Add new");
        addNewCompetitor.addClickListener(click -> {
            if (seriesSelect.getValue() != null && !license.getValue().isEmpty() && !name.getValue().isEmpty()) {
                adminService.addCompetitor(seriesSelect.getValue(), license.getValue(), name.getValue());
                license.clear();
                name.clear();
                Notification.show("New competitor added");
                listCompetitors();
            } else {
                Notification.show("All fields must be filled to add a new competitor!");
            }
        });
        add(new VHorizontalLayout(new Span("Add single competitor:"), license, name, seriesSelect, addNewCompetitor).alignAll(Alignment.CENTER));

        UploadFileHandler competitorUpload = new UploadFileHandler((inputStream, s, s1) -> {
            try {
                int count = adminService.readInCompetitorsFromIrmaFile(inputStream, competition);
                // TODO, due to Upload bug this don't work unless push is enabled :-(
                getUI().get().access(() -> {
                    notify(count + " competitors loaded from IRMA file!");
                    listCompetitors();
                });
            } catch (IOException ex) {
                getUI().get().access(() -> {
                    notify(ex.getMessage());
                });
                Logger.getLogger(CompetitorsView.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                getUI().get().access(() -> {
                    notify(ex.getMessage());
                });
                Logger.getLogger(CompetitorsView.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        competitorUpload.setUploadButton(new Button("Load new competitors from IRM file..."));
        add(competitorUpload);
        addAndExpand(competitorGrid);
        add(new VHorizontalLayout(openForStartTimeSelection, compatitorCount, removeAllCompetitors));
        listCompetitors();

    }

    private void listCompetitors() {
        competitorGrid.setItems(adminService.getCompetitors(competition));
    }

    private void openForSelection() {
        competition.setOpen(true);
        adminService.save(competition);
        notify("Competitors can now pick their start times. Log back in when you want to close the service.");
    }

}
