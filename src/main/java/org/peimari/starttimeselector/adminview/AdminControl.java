package org.peimari.starttimeselector.adminview;

import com.vaadin.flow.spring.annotation.UIScope;
import org.peimari.starttimeselector.entities.Competition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@UIScope
@Component
public class AdminControl {

    private boolean loggedIn;

    private Competition competition;

    @Autowired
    private AdminMainLayout adminMainLayout;

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public Competition getCompetition() {
        return competition;
    }

    public void setCompetition(Competition competition) {
        this.competition = competition;
    }

    public AdminMainLayout getAdminMainLayout() {
        return adminMainLayout;
    }
}
