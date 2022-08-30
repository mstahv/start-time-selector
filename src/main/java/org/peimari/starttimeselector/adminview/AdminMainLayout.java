package org.peimari.starttimeselector.adminview;

import com.vaadin.flow.router.RoutePrefix;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.vaadin.firitin.appframework.MainLayout;

/**
 * The main view is a top-level placeholder for other views.
 */
@RoutePrefix("admin")
@UIScope
@SpringComponent
public class AdminMainLayout extends MainLayout {

    @Override
    protected String getDrawerHeader() {
        return "STS";
    }

}
