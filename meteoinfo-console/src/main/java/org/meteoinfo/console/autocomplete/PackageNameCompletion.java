package org.meteoinfo.console.autocomplete;

import org.fife.ui.autocomplete.CompletionProvider;
import org.meteoinfo.console.jython.AbstractJythonSourceCompletion;

import javax.swing.*;
import java.awt.*;

/**
 * A completion that represents a package name.
 *
 * @author Yaqiang Wang
 */
class PackageNameCompletion extends AbstractJythonSourceCompletion {

    public PackageNameCompletion(CompletionProvider provider, String text,
            String alreadyEntered) {
        super(provider, text.substring(text.lastIndexOf('.') + 1));
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof PackageNameCompletion)
                && ((PackageNameCompletion) obj).getReplacementText().equals(getReplacementText());
    }

    @Override
    public Icon getIcon() {
        return IconFactory.get().getIcon(IconFactory.PACKAGE_ICON);
    }

    @Override
    public int hashCode() {
        return getReplacementText().hashCode();
    }

    @Override
    public void rendererText(Graphics g, int x, int y, boolean selected) {
        g.drawString(getInputText(), x, y);
    }

}
