package org.peimari.starttimeselector;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.peimari.starttimeselector.entities.Competitor;
import org.peimari.starttimeselector.entities.SeriesGroup;
import org.peimari.starttimeselector.entities.StartTime;
import org.peimari.starttimeselector.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.vaadin.firitin.components.button.VButton;

import javax.annotation.PostConstruct;
import java.text.MessageFormat;
import java.util.List;

@Route
public class MainView extends VerticalLayout {

    @Autowired
    private UserService userService;

    @Autowired
    ApplicationContext springAppCtx;

    private H1 header = new H1();
    private H2 sectionHeader = new H2();

    private TextField license = new TextField();
    private Button login = new Button();
    private Anchor feedback = new Anchor("https://forms.gle/ddMmodokdDwU8apT7");

    @PostConstruct
    void init() {
        removeAll();
        header.setText(getTranslation("mainview.header"));
        sectionHeader.setText(getTranslation("mainview.sectionHeader"));
        license.setLabel(getTranslation("license"));
        login.setText(getTranslation("login"));
        feedback.setText(getTranslation("feedback"));

        add(header, sectionHeader, license, login, feedback);
        login.addClickListener(e -> login());
        login.addClickShortcut(Key.ENTER);
    }

    private void login() {
        List<Competitor> competitorList = userService.getCompetitorInfo(license.getValue());
        listCompetitions(competitorList);
        if(!competitorList.isEmpty()) {
            add(new Button(getTranslation("choose.foranoter"), e -> {
                MainView.this.init();
            }));
        }
    }

    private void listCompetitions(List<Competitor> competitorInfo) {
        if (competitorInfo.isEmpty()) {
            Notification.show(getTranslation("competitions-not-found"), 3000, Notification.Position.MIDDLE);
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
            add(new H2(seriesGroup.getCompetition().getName() + ": " + competitor.getName()));
            if (competitor.getStartTime() == null) {
                startTimeComboBox = new ComboBox<>(getTranslation("pic-start-time"));
                startTimeComboBox.setItemLabelGenerator(s -> s.getTime().toLocalTime().toString());
                startTimeComboBox.setItems(userService.findAvailableStartTimes(competitor.getSeries().getSeriesGroup()));
                startTimeComboBox.addValueChangeListener(e -> {
                    try {
                        userService.reserveStartTime(competitor, e.getValue());
                    } catch (IllegalStateException ex){
                        Notification.show(ex.getMessage(), 3000, Notification.Position.MIDDLE);
                    } catch (Exception ex) {
                        Notification.show(getTranslation("failed-to-reserve"), 3000, Notification.Position.MIDDLE);
                    }
                    login();
                });
                add(startTimeComboBox);
            } else {
                add(new H3(MessageFormat.format(getTranslation("preferre-start-time-is.0"), competitor.getStartTime().getTime().toLocalTime())));
                add(new Paragraph(getTranslation("whatnext")));
                add(new VButton(getTranslation("choose.new"), e -> {
                    userService.releaseStartTime(competitor.getStartTime());
                    login();
                }).withThemeVariants(ButtonVariant.LUMO_SMALL));
            }

        }
    }

}
