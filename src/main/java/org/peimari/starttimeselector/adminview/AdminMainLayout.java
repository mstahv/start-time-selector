package org.peimari.starttimeselector.adminview;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.RoutePrefix;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.firitin.appframework.MainLayout;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * The main view is a top-level placeholder for other views.
 */
@RoutePrefix("admin")
@UIScope
@SpringComponent
public class AdminMainLayout extends MainLayout {

    @Autowired
    AdminControl adminControl;
    private Span cname;

    @Override
    protected String getDrawerHeader() {
        return "STS";
    }

    List<Class> showCompetitionName = Arrays.asList(
            ClassesAndClassGroupsEditor.class,
            CompetitorsView.class,
            RaffleAndCloseView.class
    );

    @Override
    public void setContent(Component content) {
        super.setContent(content);
        if(cname == null) {
            cname = new Span();
            cname.getStyle().setMargin("1em");
            addToNavbar(cname);
        }
        if(showCompetitionName.contains(content.getClass())) {
            cname.setText(adminControl.getCompetition().getName());
        } else {
            cname.setText("");
        }

    }
}
