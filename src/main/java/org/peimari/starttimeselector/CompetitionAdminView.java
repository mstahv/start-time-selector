package org.peimari.starttimeselector;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import org.apache.commons.codec.digest.Crypt;
import org.peimari.starttimeselector.entities.Competition;
import org.peimari.starttimeselector.entities.Competitor;
import org.peimari.starttimeselector.entities.StartTime;
import org.peimari.starttimeselector.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.vaadin.firitin.components.DynamicFileDownloader;
import org.vaadin.firitin.components.orderedlayout.VHorizontalLayout;
import org.vaadin.firitin.components.upload.UploadFileHandler;
import org.vaadin.firitin.fields.LocalDateTimeField;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Route
@Push
public class CompetitionAdminView extends VerticalLayout {

    @Autowired
    AdminService adminService;
    @Autowired
    ClassesAndClassGroupsEditor classesAndClassGroupsEditor;

    @Value("${adminpassword}")
    private String cryptedPassword;

    Binder<Competition> binder = new Binder<>(Competition.class);

    Tabs tabs = new Tabs();
    Tab competitionDetails = new Tab("1. Competition details");
    Tab defineClasses = new Tab("2. Define classes and groups");
    Tab loadCompetitors = new Tab("3. Load competitors and open");
    Tab raffle = new Tab("4. Raffle, download and close");

    private TextField name = new TextField("Name");
    private LocalDateTimeField start = new LocalDateTimeField("Start");
    private LocalDateTimeField end = new LocalDateTimeField("End");
    private Checkbox open = new Checkbox("Open for public");
    private Button save = new Button("Save");
    // TODO confirm button
    private Button raffleRest = new Button("Raffle rest and close close");
    private DynamicFileDownloader download = new DynamicFileDownloader("Download CSV", "file.csv", os -> {
        adminService.writeCsvForPirila(binder.getBean(), os);
    });
    private Button delete = new Button("Delete competition", e -> delete());

    VerticalLayout form = new VerticalLayout(
            new Hr(),
            new H2("Competition details"),
            name, open, start, end, save
            );

    VerticalLayout content = new VerticalLayout();

    ComboBox<Competition> competitionSelector = new ComboBox<>();
    Button createNew = new Button("Create new competition");

    @PostConstruct
    void init() {

        createNew.addClickListener(e -> newCompetition());
        save.addClickListener(e -> save());

        competitionSelector.setLabel("Select edited competition");
        competitionSelector.setItemLabelGenerator(Competition::getName);
        listCompetititions();

        competitionSelector.addValueChangeListener(e -> setCompetition(e.getValue()));

        VHorizontalLayout vHorizontalLayout = new VHorizontalLayout(competitionSelector, createNew);
        vHorizontalLayout.setVerticalComponentAlignment(Alignment.END, createNew);
        add(vHorizontalLayout);

        tabs.add(competitionDetails, defineClasses, loadCompetitors, raffle);

        add(tabs, content);

        tabs.addSelectedChangeListener( e -> {
            content.removeAll();
            if(tabs.getSelectedTab() == competitionDetails) {
                content.add(form);
            } else if(tabs.getSelectedTab() == defineClasses) {
                content.add(classesAndClassGroupsEditor);
            } else if(tabs.getSelectedTab() == loadCompetitors) {

                content.add(new Paragraph("Be sure that previous step is properly configured. The file can be same IRMA csv file as used to load competitors. Uploading may take a while. Only competitors with valid series are saved."));

                Grid<Competitor> competitorGrid = new Grid<>(Competitor.class);
                competitorGrid.setColumns("name", "licenceId", "emitNr");
                Grid.Column<Competitor> competitorColumn = competitorGrid.addColumn(c -> {
                    StartTime st = c.getStartTime();
                    if(st == null) {
                        return "not set";
                    } else {
                        return st.getTime().toString();
                    }
                });
                content.add(competitorGrid);
                Competition competition = binder.getBean();
                competitorGrid.setItems(adminService.getCompetitors(competition));

                UploadFileHandler kilpailijoidenLataus = new UploadFileHandler((inputStream, s, s1) -> {
                    try {
                        int count = adminService.readInCompetitorsFromIrmaFile(inputStream, binder.getBean());
                        // TODO, due to Upload bug this don't work unless push is enabled :-(
                        getUI().get().access(() -> {
                            notify(count + " competitors loaded from IRMA file!");
                            competitorGrid.setItems(adminService.getCompetitors(binder.getBean()));
                        });
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });
                kilpailijoidenLataus.setUploadButton(new Button("Load competitors from IRM file..."));

                content.add(kilpailijoidenLataus);

                Button button = new Button("Open for start time selection", clickEvent -> {
                    open.setValue(true);
                    save();
                    notify("Competitors can now pick their start times. Log back in when you want to close the service.");
                });
                content.add(button);

            } else if(tabs.getSelectedTab() == raffle) {
                content.add("When deadline is done, you can close the competition and raffle start times for those who didn't pick a start time. You can also download currently defined start times using the download CSV link, any time you want. Deleting deletes the competition from the database.");
                content.add(raffleRest, download, delete);
            }

        });

        tabs.setVisible(false);
        content.setVisible(false);

        binder.bindInstanceFields(this);
        content.add(form);

        raffleRest.addClickListener(e -> raffleRest());

        setVisible(false);
        Dialog loginDialog = new Dialog();
        PasswordField pw = new PasswordField();
        pw.focus();
        Button login = new Button("Login");
        login.addClickListener(e -> {
            String crypted = Crypt.crypt(pw.getValue(), cryptedPassword);
            if (crypted.equals(cryptedPassword)) {
                loginDialog.close();
                setVisible(true);
            } else {
                Notification.show("Password did not match! If you want to use the service, contact matti ät tahvonen dot com");
            }
        });
        login.addClickShortcut(Key.ENTER);
        loginDialog.setCloseOnOutsideClick(false);
        loginDialog.setCloseOnEsc(false);
        loginDialog.add(new H1("Login to competition admin"), pw, login);
        loginDialog.open();

    }

    private void notify(String s) {
        Notification.show(s, 3000, Notification.Position.MIDDLE);
    }

    private void raffleRest() {
        Competition competition = binder.getBean();
        adminService.raffleRest(competition);
        listCompetititions();
        competitionSelector.setValue(null);
        competitionSelector.setValue(competition);
        notify("Remaining competitors raffled for free slots");
    }

    private void delete() {
        adminService.deleteCompetition(binder.getBean());
        listCompetititions();
    }

    private void listCompetititions() {
        competitionSelector.setItems(adminService.getCompetitions());
    }

    private void save() {
        Competition c = adminService.save(binder.getBean());
        listCompetititions();
        competitionSelector.setValue(c);
    }

    public void newCompetition() {
        Competition c = adminService.createNewCompetition();
        listCompetititions();
        competitionSelector.setValue(c);
    }

    public void setCompetition(Competition c) {
        if (c != null) {
            classesAndClassGroupsEditor.setCompetition(c);
            binder.setBean(c);
            tabs.setVisible(true);
            content.setVisible(true);
        } else {
            tabs.setVisible(false);
            content.setVisible(false);
        }
    }

}
