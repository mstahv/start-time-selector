package org.peimari.starttimeselector.adminview;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.Route;
import org.apache.commons.codec.digest.Crypt;
import org.springframework.beans.factory.annotation.Value;

@Route
public class LoginView extends VerticalLayout {

    private AdminControl adminControl;

    @Value("${adminpassword}")
    private String cryptedPassword;

    public LoginView(AdminControl adminControl) {
        this.adminControl = adminControl;
        add(new H1("Login to competition admin"));

        PasswordField pw = new PasswordField();
        Button login = new Button("Login");
        add(pw, login);
        login.addClickListener(e -> {
            String crypted = Crypt.crypt(pw.getValue(), cryptedPassword);
            if (crypted.equals(cryptedPassword)) {
                adminControl.setLoggedIn(true);
                UI.getCurrent().navigate(ChooseCompetitionView.class);
                setVisible(true);
            } else {
                Notification.show("Password did not match! If you want to use the service, contact matti ät tahvonen dot com");
            }
        });
        login.addClickShortcut(Key.ENTER);
        pw.focus();
    }
}
