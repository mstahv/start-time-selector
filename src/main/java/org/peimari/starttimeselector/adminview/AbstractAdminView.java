package org.peimari.starttimeselector.adminview;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import org.peimari.starttimeselector.service.AdminService;

@Route(layout = AdminMainLayout.class)
public abstract class AbstractAdminView extends VerticalLayout implements BeforeEnterObserver {

    final AdminService adminService;
    final AdminControl adminControl;

    public AbstractAdminView(AdminControl adminControl, AdminService adminService) {
        this.adminControl = adminControl;
        this.adminService = adminService;
    }

    protected AdminMainLayout getAdminMainLayout() {
        return (AdminMainLayout) getParent().get();
    }

    protected void notify(String s) {
        Notification.show(s, 3000, Notification.Position.MIDDLE);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if(adminControl.getCompetition() == null) {
            beforeEnterEvent.rerouteTo(ChooseCompetitionView.class);
        }
    }
}
