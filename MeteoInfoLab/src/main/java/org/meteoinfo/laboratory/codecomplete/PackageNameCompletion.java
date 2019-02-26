package org.meteoinfo.laboratory.codecomplete;

import java.awt.Graphics;
import javax.swing.Icon;

import org.fife.ui.autocomplete.CompletionProvider;

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
