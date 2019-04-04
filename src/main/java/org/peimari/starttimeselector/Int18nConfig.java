package org.peimari.starttimeselector;

import com.vaadin.flow.i18n.I18NProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Component
public class Int18nConfig implements I18NProvider {

    private static final List<Locale> AVAILABLE_LOCALES = Arrays.asList(Locale.getAvailableLocales());

    @Autowired
    private MessageSource messageSource;

    @Override
    public List<Locale> getProvidedLocales() {
        return AVAILABLE_LOCALES;
    }

    @Override
    public String getTranslation(String key, Locale locale, Object... params) {
        return messageSource.getMessage(key, params, locale);
    }

}
