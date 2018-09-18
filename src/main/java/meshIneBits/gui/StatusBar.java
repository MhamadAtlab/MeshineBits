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

package meshIneBits.gui;

import meshIneBits.util.Logger;
import meshIneBits.util.LoggingInterface;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StatusBar extends JPanel implements LoggingInterface {
    private static final long serialVersionUID = 1L;
    private JLabel statusLabel;
    private JProgressBar progressBar;

    public StatusBar() {
        // Visual options
        setBackground(Color.white);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.lightGray), new EmptyBorder(3, 3, 3, 3)));
        setLayout(new BorderLayout());

        // Setting up
        Logger.register(this);

        progressBar = new JProgressBar(0, 2);
        progressBar.setVisible(false);

        statusLabel = new JLabel("Ready");
        statusLabel.setMinimumSize(new Dimension(200, statusLabel.getHeight()));

        add(statusLabel, BorderLayout.WEST);
        add(progressBar, BorderLayout.EAST);
    }

    @Override
    public void error(String error) {
        statusLabel.setText("ERROR :" + error);
    }

    @Override
    public void message(String message) {
        statusLabel.setText(message);
    }

    @Override
    public void setProgress(int value, int max) {
        if (value >= max) {
            progressBar.setVisible(false);
        } else {
            progressBar.setVisible(true);
            progressBar.setValue(value);
            progressBar.setMaximum(max);
        }
        StatusBar.this.repaint();
    }

    @Override
    public void updateStatus(String status) {
        statusLabel.setText(status);
    }

    @Override
    public void warning(String warning) {
    }
}