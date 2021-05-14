package org.peimari.starttimeselector.adminview;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.peimari.starttimeselector.entities.Competition;
import org.peimari.starttimeselector.service.AdminService;
import org.vaadin.firitin.components.button.VButton;

@Route(layout = AdminMainLayout.class)
@PageTitle("1. Competition details")
public class CompetitionDetailsView extends AbstractAdminView {

    private TextField name = new TextField("Name");
    private DateTimePicker start = new DateTimePicker();
    private DateTimePicker end = new DateTimePicker();
    private TextField startIntervalSeconds = new TextField("Default start time interval (seconds)");
    private Checkbox open = new Checkbox("Open for public");
    private VButton save = new VButton("Save", this::save);

    Binder<Competition> binder = new Binder<>(Competition.class);

    public CompetitionDetailsView(AdminControl adminControl, AdminService adminService) {
        super(adminControl, adminService);

        add(name, open, start, end, startIntervalSeconds, save);

        startIntervalSeconds.setPattern("[0-9]");
        binder.forMemberField(startIntervalSeconds).withConverter(this::parseIntOrDefault, i -> Integer.toString(i));
        binder.bindInstanceFields(this);

        binder.setBean(adminControl.getCompetition());

    }

    private void save() {
        Competition c = adminService.save(binder.getBean());
        adminControl.setCompetition(c);
    }

    private Integer parseIntOrDefault(String toParse) {
        try {
            return Integer.parseInt(toParse);
        } catch (NumberFormatException e) {
            return 60;
        }
    }

}
