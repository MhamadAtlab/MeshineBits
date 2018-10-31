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

import meshIneBits.Mesh;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MeshWindowLayerSelector extends JPanel {
    private JSlider layerSlider;
    private JSpinner layerSpinner;
    private Mesh mesh;

    public MeshWindowLayerSelector(MeshController meshController) {
        mesh = meshController.getMesh();
        layerSlider = new JSlider(
                SwingConstants.VERTICAL,
                0,
                mesh.getLayers().size() - 1,
                0);
        layerSpinner = new JSpinner(
                new SpinnerNumberModel(
                        0,
                        0,
                        mesh.getLayers().size() - 1,
                        1));
        layerSlider.setMaximumSize(new Dimension(40, 500));
        layerSlider.setFocusable(false);
        layerSpinner.setMaximumSize(new Dimension(40, 40));

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(layerSlider);
        add(layerSpinner);
        setBorder(new EmptyBorder(0, 5, 5, 0));

        layerSpinner.addChangeListener(e ->
        {
            meshController.setLayer((Integer) layerSpinner.getValue());
            layerSlider.setValue((Integer) layerSpinner.getValue());
        });

        layerSlider.addChangeListener(e ->
        {
            meshController.setLayer(layerSlider.getValue());
            layerSpinner.setValue(layerSlider.getValue());
        });
        setVisible(true);
    }

    public Mesh getMesh() {
        return mesh;
    }
}
