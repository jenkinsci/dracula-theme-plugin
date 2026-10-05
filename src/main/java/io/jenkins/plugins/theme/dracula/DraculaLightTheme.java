package io.jenkins.plugins.theme.dracula;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import io.jenkins.plugins.thememanager.Theme;
import io.jenkins.plugins.thememanager.ThemeManagerFactory;
import java.util.List;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

/** Alucard Classic, the light Dracula theme. */
public class DraculaLightTheme extends AbstractDraculaTheme {

    static final String KEY = "dracula-alucard";

    @DataBoundConstructor
    public DraculaLightTheme() {
        // Stapler
    }

    @Override
    public Theme getTheme() {
        return Theme.builder()
                .withCssUrls(List.of(getVersionedCssUrl()))
                // Bootstrap 5 pages read this to keep the light color mode
                .withProperty("bootstrap", "theme", "light")
                .build();
    }

    @Extension
    @Symbol("draculaAlucard")
    public static class DescriptorImpl extends DraculaThemeDescriptor {

        @Override
        public String getThemeKey() {
            return KEY;
        }

        @Override
        public ThemeManagerFactory getInstance() {
            return new DraculaLightTheme();
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return "Dracula (Alucard)";
        }
    }
}
