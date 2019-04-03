package org.peimari.starttimeselector;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import org.apache.commons.codec.digest.Crypt;
import org.peimari.starttimeselector.entities.Competition;
import org.peimari.starttimeselector.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.vaadin.firitin.components.html.DynamicFileDownloader;
import org.vaadin.firitin.components.orderedlayout.VHorizontalLayout;
import org.vaadin.firitin.fields.LocalDateTimeField;

import javax.annotation.PostConstruct;

@Route
@Push
public class CompetitionAdminView extends VerticalLayout {

    @Autowired
    AdminService adminService;
    @Autowired
    ClassesAndClassGroupsEditor classesAndClassGroupsEditor;

    @Value("${adminpassword}")
    private String password;

    Binder<Competition> binder = new Binder<>(Competition.class);

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
            name, open, start, end,
            new HorizontalLayout(save, raffleRest, download, delete));

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

        form.add(classesAndClassGroupsEditor);
        form.setVisible(false);
        binder.bindInstanceFields(this);
        add(form);

        raffleRest.addClickListener(e -> raffleRest());

        setVisible(false);
        Dialog loginDialog = new Dialog();
        PasswordField pw = new PasswordField();
        Button login = new Button("Login");
        login.addClickListener(e -> {
            Crypt.crypt("sdfsd");
            if (Crypt.crypt(pw.getValue(), password).equals(password)) {
                loginDialog.close();
                setVisible(true);
            } else {
                Notification.show("Password did not match! If you want to use the service, contact matti ät tahvonen dot com");
            }
        });
        login.addClickShortcut(Key.ENTER);
        loginDialog.add(new H1("Login to competition admin"), pw, login);
        loginDialog.open();

    }

    private void notify(String s) {
        Notification.show(s, 3000, Notification.Position.MIDDLE);
    }

    private void raffleRest() {
        Competition competition = binder.getBean();
        adminService.raffleRest(competition);
        competition.setOpen(false);
        save();
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
            form.setVisible(true);
        } else {
            form.setVisible(false);
        }
    }

}
