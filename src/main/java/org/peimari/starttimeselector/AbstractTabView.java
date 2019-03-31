package org.peimari.starttimeselector;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

/**
 * All views extended from this class will automatically function properly as
 * views in the tabbar in TabBasedMainLayout.
 *
 * @author mstahv
 */
@Route(layout = TabBasedMainLayout.class)
public abstract class AbstractTabView extends VerticalLayout {

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        ensureSelectTab();
    }

    /**
     * Ensure the correct tab is selected when landing straight to this view.
     *
     * If you have a DI framework like Spring in use, you could also have a
     * separate aspect to do this, for example with BeanPostProcessor.
     */
    private void ensureSelectTab() {
        TabBasedMainLayout ml = (TabBasedMainLayout) getParent().get();
        ml.selectTab(this);
    }

}
