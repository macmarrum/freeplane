/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.features.styles.mindmapmode.styleeditorpanel;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.border.Border;

import org.freeplane.core.resources.components.IPropertyControl;
import org.freeplane.core.resources.components.PropertyBean;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;

class RevertingProperty extends PropertyBean implements IPropertyControl {

    static final String NAME = "revert";
    private static final int PADDING = (int) (UITools.FONT_SCALE_FACTOR * 2);
    private static final Border BORDER = BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING);
    private final static String REVERT_RESOURCE = "reset_property_text";
    private static final String TEXT = TextUtils.getText(REVERT_RESOURCE);
    private final JButton revertButton;
	
	RevertingProperty() {
        this(NAME);
	}
	
	RevertingProperty(String name) {
		super(name);
		revertButton = IconFont.createIconButton();
		revertButton.setText(IconFont.REVERT_CHARACTER);
		revertButton.setToolTipText(TEXT);
		revertButton.setBorder(BORDER);
		revertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setValue(! getBooleanValue());
			}
		});
	}

	@Override
	public String getValue() {
		return revertButton.isVisible() ? Boolean.TRUE.toString() : Boolean.FALSE.toString();
	}

	public void appendToForm(final DefaultFormBuilder builder) {
	    builder.append(revertButton);
	}

	public void setEnabled(final boolean pEnabled) {
		revertButton.setEnabled(pEnabled);
		super.setEnabled(pEnabled);
	}
	
	public boolean isEnabled() {
	    return revertButton.isEnabled();
	}

	@Override
	public void setValue(final String value) {
		final boolean booleanValue = Boolean.parseBoolean(value);
		setValue(booleanValue);
	}

	public void setValue(final boolean booleanValue) {
	    if(revertButton.isVisible() != booleanValue) {
	    	if(booleanValue == false && revertButton.isFocusOwner()) {
	    		moveFocusToPreviousFocusableComponent();
	    	}
	        revertButton.setVisible(booleanValue);
	        firePropertyChangeEvent();
	    }
	}

	private void moveFocusToPreviousFocusableComponent() {
		Container parent = revertButton.getParent();
		int i = parent.getComponentCount() - 1;
		for (; i >= 0 ; i--) {
			Component component = parent.getComponent(i);
			if (revertButton == component)
					break;
		}
		for (i--; i >= 0 ; i--) {
			Component component = parent.getComponent(i);
			if(requestFocusInWindow(component)) {
				break;
			}
		}
	}

	private boolean requestFocusInWindow(Component component) {
		if(!(component instanceof Container) || ((Container) component).getComponentCount() == 0) {
			boolean isFocusable = component.isFocusable();
			if(isFocusable)
				component.requestFocusInWindow();
			return isFocusable;
		}
		Container container = (Container) component;
		int i = container.getComponentCount() - 1;
		for (; i >= 0 ; i--) {
			if(requestFocusInWindow(container.getComponent(i)))
				return true;
		}
		return false;
	}

	public boolean getBooleanValue() {
		return revertButton.isVisible();
	}
}
