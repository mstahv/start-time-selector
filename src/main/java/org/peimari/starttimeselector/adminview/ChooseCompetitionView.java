package org.peimari.starttimeselector.adminview;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import org.peimari.starttimeselector.entities.Competition;
import org.peimari.starttimeselector.service.AdminService;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

@Route(value = "", layout = AdminMainLayout.class)
@RouteAlias(value = "competitionadmin", layout = AdminMainLayout.class, absolute = true)
@PageTitle("0. Choose competition")
public class ChooseCompetitionView extends AbstractAdminView {

    private final Environment env;
    private Select<Competition> competitionSelector = new Select<>();
    private Button createNew = new Button("Create new competition");

    public ChooseCompetitionView(AdminControl adminControl, AdminService adminService, Environment environment) {
        super(adminControl, adminService);
        this.env = environment;

        add(
                competitionSelector,
                createNew
        );

        createNew.addClickListener(e -> newCompetition());

        competitionSelector.setLabel("Select existing competition");
        competitionSelector.setItemLabelGenerator(Competition::getName);
        listCompetititions();
        competitionSelector.addValueChangeListener(e -> {
            if (e.isFromClient()) setCompetition(e.getValue());
        });
        competitionSelector.setEmptySelectionAllowed(false);
    }

    public void setCompetition(Competition c) {
        if (c != null) {
            adminControl.setCompetition(c);
            // Enable all options from the menu
            getAdminMainLayout().getNavigationItems().stream()
                    .forEach(ni -> ni.setEnabled(true));
            getAdminMainLayout().buildMenu();

            UI.getCurrent().navigate(CompetitionDetailsView.class);
        }
    }

    private void listCompetititions() {
        competitionSelector.setItems(adminService.getCompetitions());
    }

    public void newCompetition() {
        Competition c = adminService.createNewCompetition();
        setCompetition(c);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if (env.acceptsProfiles(Profiles.of("prod")) && !adminControl.isLoggedIn()) {
            beforeEnterEvent.rerouteTo(LoginView.class);
        } else {
            listCompetititions();
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // Disable all but this from the menu until competition is chosen
        getAdminMainLayout().getNavigationItems().stream()
                .filter(ni -> ni.getNavigationTarget() != ChooseCompetitionView.class)
                .forEach(ni -> ni.setEnabled(false));
        getAdminMainLayout().buildMenu();
    }
}
