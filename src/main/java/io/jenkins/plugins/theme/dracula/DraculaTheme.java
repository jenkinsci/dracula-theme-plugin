package io.jenkins.plugins.theme.dracula;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.PluginWrapper;
import hudson.Util;
import io.jenkins.plugins.thememanager.Theme;
import io.jenkins.plugins.thememanager.ThemeManagerFactory;
import io.jenkins.plugins.thememanager.ThemeManagerFactoryDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

public class DraculaTheme extends ThemeManagerFactory {

    static final String CSS = "dracula.css";
    static final String ID = "dracula";

    private static volatile String cssVersion;

    @DataBoundConstructor
    public DraculaTheme() {
        // Stapler
    }

    @Override
    public Theme getTheme() {
        return Theme.builder()
                .withCssUrls(List.of(getCssUrl() + "?v=" + cssVersion()))
                // Plugins with their own light styles read these to load a dark variant, dracula.css recolors it
                .withProperty("prism-api", "theme", "tomorrow")
                .withProperty("bootstrap", "theme", "dark")
                .build();
    }

    /**
     * Short hash of the stylesheet. The CSS is served with a fixed Last-Modified, so browsers
     * revalidate, get 304 and keep the old file unless the URL changes with the content.
     */
    static String cssVersion() {
        String version = cssVersion;
        if (version == null) {
            version = computeCssVersion();
            cssVersion = version;
        }
        return version;
    }

    private static String computeCssVersion() {
        PluginWrapper plugin = Jenkins.get().getPluginManager().getPlugin("dracula-theme");
        if (plugin == null) {
            return "0";
        }
        try (InputStream in =
                plugin.baseResourceURL.toURI().resolve(CSS).toURL().openStream()) {
            return Util.getDigestOf(in).substring(0, 8);
        } catch (IOException | URISyntaxException | IllegalArgumentException e) {
            return plugin.getVersion();
        }
    }

    @Extension
    @Symbol("dracula")
    public static class DescriptorImpl extends ThemeManagerFactoryDescriptor {

        @Override
        public String getThemeKey() {
            return ID;
        }

        @Override
        public ThemeManagerFactory getInstance() {
            return new DraculaTheme();
        }

        @Override
        public String getThemeCssSuffix() {
            return CSS;
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return "Dracula";
        }

        @Override
        public String getIconClassName() {
            return "symbol-dracula plugin-dracula-theme";
        }

        @Override
        public String getThemeId() {
            return ID;
        }

        @Override
        public boolean isNamespaced() {
            return true;
        }
    }
}
