package io.jenkins.plugins.theme.dracula;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import io.jenkins.plugins.thememanager.Theme;
import io.jenkins.plugins.thememanager.ThemeManagerFactory;
import java.util.List;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

/** Follows the system setting: Alucard in light mode, Dracula in dark mode. */
public class DraculaSystemTheme extends AbstractDraculaTheme {

    static final String KEY = "dracula-system";

    @DataBoundConstructor
    public DraculaSystemTheme() {
        // Stapler
    }

    @Override
    public Theme getTheme() {
        return Theme.builder()
                .withCssUrls(List.of(getVersionedCssUrl()))
                .respectSystemAppearance()
                // Theme Manager reads the -light or -dark property that matches the system setting
                .withProperty("bootstrap", "theme-light", "light")
                .withProperty("bootstrap", "theme-dark", "dark")
                .build();
    }

    @Extension
    @Symbol("draculaSystem")
    public static class DescriptorImpl extends DraculaThemeDescriptor {

        @Override
        public String getThemeKey() {
            return KEY;
        }

        @Override
        public ThemeManagerFactory getInstance() {
            return new DraculaSystemTheme();
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return "Dracula (System)";
        }
    }
}
