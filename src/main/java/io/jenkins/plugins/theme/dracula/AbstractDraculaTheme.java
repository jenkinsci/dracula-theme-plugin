package io.jenkins.plugins.theme.dracula;

import hudson.PluginWrapper;
import hudson.Util;
import io.jenkins.plugins.thememanager.ThemeManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import jenkins.model.Jenkins;

/**
 * Base class for the Dracula themes. All variants share one stylesheet, each one is scoped to its
 * own {@code data-theme} key.
 */
public abstract class AbstractDraculaTheme extends ThemeManagerFactory {

    static final String CSS = "dracula.css";

    private static volatile String cssVersion;

    /** Stylesheet URL with the content hash, see {@link #cssVersion()}. */
    String getVersionedCssUrl() {
        return getCssUrl() + "?v=" + cssVersion();
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
}
