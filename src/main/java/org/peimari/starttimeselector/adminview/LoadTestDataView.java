package org.peimari.starttimeselector.adminview;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServlet;
import org.peimari.starttimeselector.entities.Competition;
import org.peimari.starttimeselector.service.AdminService;

import java.io.FileInputStream;
import java.io.InputStream;

@Route
public class LoadTestDataView extends VerticalLayout {

    private AdminService adminService;
    public LoadTestDataView(AdminService adminService) {
        this.adminService = adminService;

        boolean productionMode = VaadinServlet.getCurrent().getService().getDeploymentConfiguration().isProductionMode();
        if(!productionMode) {
            Button button = new Button("Add test competition");
            button.addClickListener(e -> {
                Competition newCompetition = adminService.createNewCompetition();
                try {
                    InputStream is = new FileInputStream("sample-data/irma-export.csv");
                    adminService.readInSeriesFromIrmaFile(is, newCompetition, false, null, null, null);
                    is = new FileInputStream("sample-data/irma-export.csv");
                    adminService.readInCompetitorsFromIrmaFile(is, newCompetition);
                    newCompetition.setOpen(true);
                    adminService.save(newCompetition);
                } catch (Exception ex) {throw new RuntimeException(ex);};
            });
            add(button);
        }
    }
}
