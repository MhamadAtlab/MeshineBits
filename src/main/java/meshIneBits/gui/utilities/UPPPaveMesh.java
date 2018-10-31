/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016  Thibault Cassard & Nicolas Gouju.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 Vallon BENJAMIN.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package meshIneBits.gui.utilities;

import meshIneBits.config.CraftConfig;
import meshIneBits.gui.MeshController;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class UPPPaveMesh extends UtilityParametersPanel {
    public UPPPaveMesh(MeshController meshController) {
        super("Pave Mesh");
        // Init components
        JPanel parametersPanel = new JPanel();
        parametersPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        parametersPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

        PatternComboBox patternComboBox = new PatternComboBox(
                Arrays.asList(CraftConfig.clonePreloadedPatterns()),
                parametersPanel
        );

        JButton startButton = new JButton("Start");
        startButton.addActionListener(e -> {
            try {
                meshController.paveMesh(patternComboBox.getCurrentChoice());
            } catch (Exception e1) {
                meshController.handleException(e1);
            }
        });

        // Layout
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0;
        c.weightx = 0;
        c.anchor = GridBagConstraints.LINE_START;
        add(patternComboBox, c);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.weighty = 0;
        c.weightx = 0;
        c.anchor = GridBagConstraints.CENTER;
        add(startButton, c);

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 2;
        c.gridy = 0;
        c.weighty = 1;
        c.weightx = 1;
        c.anchor = GridBagConstraints.LINE_START;
        add(parametersPanel, c);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(getWidth(), 100);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(getWidth(), 100);
    }
}