package org.peimari.starttimeselector.adminview;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.RoutePrefix;
import com.vaadin.flow.shared.ui.Transport;
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
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        //getUI().get().getPushConfiguration().setTransport(Transport.WEBSOCKET);
    }
    
    

    @Override
    protected String getDrawerHeader() {
        return "STS - admin";
    }

}
