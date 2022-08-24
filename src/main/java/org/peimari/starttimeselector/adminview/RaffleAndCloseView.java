package org.peimari.starttimeselector.adminview;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import org.peimari.starttimeselector.service.AdminService;
import org.vaadin.firitin.components.DynamicFileDownloader;
import org.vaadin.firitin.components.button.DeleteButton;
import org.vaadin.firitin.components.button.VButton;

@PageTitle("4. Raffle, download, close")
public class RaffleAndCloseView extends AbstractAdminView implements BeforeEnterObserver {

    private Button raffleRest = new VButton("Raffle rest and close close", this::raffleRest);
    private DynamicFileDownloader download = new DynamicFileDownloader("Download CSV", "file.csv", os -> {
        adminService.writeCsvForPirila(adminControl.getCompetition(), os);
    });

    private Button delete = new DeleteButton(this::delete)
            .withConfirmationPrompt("Do you really want to delete the competition form the system?")
            .withText("Delete competition");

    public RaffleAndCloseView(AdminControl adminControl, AdminService adminService) {
        super(adminControl, adminService);

        add("When deadline is done, you can close the competition and raffle start times for those who didn't pick a start time. You can also download currently defined start times using the download CSV link, any time you want. Deleting deletes the competition from the database.");
        add(raffleRest, download, delete);

    }

    private void delete() {
        adminService.deleteCompetition(adminControl.getCompetition());
        adminControl.setCompetition(null);
        UI.getCurrent().navigate(ChooseCompetitionView.class);
    }

    private void raffleRest() {
        adminControl.setCompetition(adminService.raffleRest(adminControl.getCompetition()));
        notify("Remaining competitors raffled for free slots");
    }

}
