package io.jenkins.plugins.theme.dracula;

import io.jenkins.plugins.thememanager.ThemeManagerFactoryDescriptor;

/** Settings shared by the Dracula themes, they are all served from {@code theme-dracula}. */
public abstract class DraculaThemeDescriptor extends ThemeManagerFactoryDescriptor {

    static final String ID = "dracula";

    @Override
    public String getThemeId() {
        return ID;
    }

    @Override
    public String getThemeCssSuffix() {
        return AbstractDraculaTheme.CSS;
    }

    @Override
    public String getIconClassName() {
        return "symbol-dracula plugin-dracula-theme";
    }

    @Override
    public boolean isNamespaced() {
        return true;
    }
}
