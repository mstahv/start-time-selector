package org.peimari.starttimeselector.adminview;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.RoutePrefix;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.vaadin.firitin.appframework.MainLayout;

/**
 * The main view is a top-level placeholder for other views.
 */
@Push
@RoutePrefix("admin")
@UIScope
@SpringComponent
public class AdminMainLayout extends MainLayout {

    @Override
    protected Component[] getDrawerHeader() {
        return new Component[] {
                new Icon(VaadinIcon.VAADIN_V),
                new H1("STS - admin")};
    }


}
