package org.peimari.starttimeselector;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.peimari.starttimeselector.entities.Competition;
import org.peimari.starttimeselector.entities.Competitor;
import org.peimari.starttimeselector.entities.SeriesGroup;
import org.peimari.starttimeselector.entities.StartTime;
import org.peimari.starttimeselector.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.util.List;

public class MainView extends AbstractTabView {

    @Autowired
    private UserService userService;

    private H1 header = new H1("Start time selector");
    private H2 sectionHeader = new H2("Type in your license id and emit number (can be empty if you rent one):");

    private TextField license = new TextField("License");
    private TextField emit = new TextField("Emit");
    private Button login = new Button("Login");

    @PostConstruct
    void init() {
        add(header, sectionHeader, license, emit, login);
        login.addClickListener(e -> login());
    }

    private void login() {
        listCompetitions(userService.getCompetitorInfo(license.getValue(), emit.getValue()));
    }

    private void listCompetitions(List<Competitor> competitorInfo) {
        if (competitorInfo.isEmpty()) {
            Notification.show("No competitions found for your details!", 3000, Notification.Position.MIDDLE);
        } else {
            removeAll();
            add(header);

            competitorInfo.forEach(c -> {
                add(new StartTimeSelector(c));
            });
        }
    }

    private class StartTimeSelector extends VerticalLayout {

        private ComboBox<StartTime> startTimeComboBox;
        private Competitor competitor;

        public StartTimeSelector(Competitor competitor) {
            init(competitor);
        }

        private void init(Competitor competitor) {
            this.competitor = competitor;

            SeriesGroup seriesGroup = competitor.getSeries().getSeriesGroup();
            add(new H2(seriesGroup.getCompetition().getName()));
            if (competitor.getStartTime() == null) {
                startTimeComboBox = new ComboBox<>("Pick from available start times");
                startTimeComboBox.setItemLabelGenerator(s -> s.getTime().toLocalTime().toString());
                startTimeComboBox.setItems(userService.findAvailableStartTimes(competitor.getSeries().getSeriesGroup()));
                startTimeComboBox.addValueChangeListener(e -> {
                    try {
                        userService.reserveStartTime(competitor, e.getValue());
                    } catch (Exception ex) {
                        Notification.show("Failed to reserve start time, probably somebody " +
                                "just picked to time before your. Please try again!", 3000, Notification.Position.MIDDLE);
                    }
                    login();
                });
                add(startTimeComboBox);
            } else {
                    add(new H3("Your preferred start time is: " + competitor.getStartTime().getTime().toLocalTime()));
                add(new Button("Choose new", e -> {
                    userService.releaseStartTime(competitor.getStartTime());
                    login();
                }));
            }

        }
    }

}
