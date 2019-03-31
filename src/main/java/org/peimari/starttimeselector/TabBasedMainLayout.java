package org.peimari.starttimeselector;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.router.RouterLink;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * This is the main layout for your application. It automatically registers all
 * your main views to a tabbar that can be used to navigate from view to view.
 * 
 * @author mstahv
 */
@Push
public class TabBasedMainLayout extends VerticalLayout implements RouterLayout {

    Tabs tabs = new Tabs();
    Map<Class<? extends Component>, NavTab> viewToTab = new HashMap<>();

    public class NavTab extends Tab {

        private final Class<? extends Component> target;

        public NavTab(Class<? extends Component> target) {
            this.target = target;
            viewToTab.put(target, this);
        }

        /**
         * @return the target
         */
        public Class<? extends Component> getTarget() {
            return target;
        }

    }

    public TabBasedMainLayout() {
        automaticallyRegisterAvailableViews();
        add(tabs);

        tabs.addSelectedChangeListener(e -> {
            if (e.isFromClient()) {
                NavTab t = (NavTab) tabs.getSelectedTab();
                UI.getCurrent().navigate(t.getTarget());
            }
        });

    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        attachEvent.getSession().setErrorHandler(e -> {
            e.getThrowable().printStackTrace();
            Notification error = new Notification();
            error.setText(e.getThrowable().getMessage());
            error.setPosition(Notification.Position.BOTTOM_CENTER);
            error.setOpened(true);
            getUI().get().add(error);
        });
    }

    private void automaticallyRegisterAvailableViews() {
        // Get all routes and register all views to tabbar that have this class
        // as parent layout
        UI.getCurrent().getRouter().getRoutes().stream().filter(r -> r.getParentLayout() == getClass())
                .forEach(r -> registerView(r.getNavigationTarget()));

        // The Vaadin 13+ way to do it:
        // RouteConfiguration.forApplicationScope()
        // .getAvailableRoutes().stream()
        // .filter(r -> r.getParentLayout() == getClass())
        // .forEach(r -> registerView(r.getNavigationTarget()));
    }

    private void registerView(Class<? extends Component> clazz) {
        Tab tab = new NavTab(clazz);
        tab.setLabel(createTabTitle(clazz));
        tabs.add(tab);
    }

    protected static String createTabTitle(Class<? extends Component> clazz) {
        return // Make somewhat sane title for the tab from class name
        StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(clazz.getSimpleName()), " ");
    }

    public void selectTab(Component view) {
        tabs.setSelectedTab(viewToTab.get(view.getClass()));
    }

}
