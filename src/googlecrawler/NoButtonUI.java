/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package googlecrawler;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.BasicComboBoxUI;

/**
 *
 * @author ninja
 */
class NoButtonUI extends BasicComboBoxUI {

    public static ComboBoxUI createUI(JComponent c) {
        return new NoButtonUI();
    }

    @Override protected JButton createArrowButton() {
        JButton noButton = new JButton();
        noButton.setVisible(false);
        noButton.setEnabled(false);
        return noButton;
    }
}
