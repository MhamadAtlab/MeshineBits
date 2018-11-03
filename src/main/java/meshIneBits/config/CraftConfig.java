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

package meshIneBits.config;

import meshIneBits.patterntemplates.*;
import meshIneBits.scheduler.AScheduler;

/**
 * The CraftConfig class contains the configurable
 * settings for the slicer. Reflection and annotations
 * are used to make it easy to generate the configuration
 * dialog.
 * NOTE: Do not auto format this file. Manual format keeps it readable!
 */
public class CraftConfig {
    static final String VERSION = "Dev-Prerelease";

    // Slicer options

    @Setting(title = "Space between layers",
            description = "The vertical gap between each layers (in mm)",
            step = 0.01, minValue = 0.01, maxValue = 100.0
    )
    public static double layersOffset = 0.25;

    @Setting(title = "First slice height",
            description = "Starting height of the first slice in the model (in % of a bit's thickness). 50% is the default.",
            minValue = 1.0, maxValue = 99.0
    )
    public static double firstSliceHeightPercent = 50;

    @Setting(title = "Minimal line segment cosine value",
            description = "If the cosine of the line angle difference is higher then this value then 2 lines are joined into 1.\nSpeeding up the slicing, and creating less gcode commands. Lower values makes circles less round,\nfor a faster slicing and less GCode. A value of 1.0 leaves every line intact.",
            minValue = 0.95, maxValue = 1.0
    )
    public static double joinMinCosAngle = 0.995;

    // Bits options

    @Setting(title = "Bit thickness",
            description = "Thickness of the bits (in mm)",
            minValue = 1.0, maxValue = 1000.0
    )
    public static double bitThickness = 8.0;

    @Setting(title = "Bit width",
            description = "Width of the bits (in mm)",
            minValue = 1.0, maxValue = 1000.0
    )
    public static double bitWidth = 24.0;

    @Setting(title = "Bit length",
            description = "Length of the bits (in mm)",
            minValue = 1.0, maxValue = 1000.0
    )
    public static double bitLength = 120.0;

    // Pattern choices

    /**
     * The provided templates
     */
    public static PatternTemplate[] templatesPreloaded = {
            new ManualPattern(),
            new ClassicBrickPattern(),
            new DiagonalHerringbonePattern(),
            new ImprovedBrickPattern(),
            new EconomicPattern(),
            new UnitSquarePattern()
    };

    /**
     * @return new instance of each pattern builder
     */
    public static PatternTemplate[] clonePreloadedPatterns() {
        PatternTemplate[] patternsList = new PatternTemplate[CraftConfig.templatesPreloaded.length];
        for (int i = 0; i < CraftConfig.templatesPreloaded.length; i++) {
            try {
                patternsList[i] = CraftConfig.templatesPreloaded[i].getClass().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return patternsList;
    }

    /**
     * The chosen pattern
     */
    public static PatternTemplate templateChoice = templatesPreloaded[0];

    // Deprecated pattern parameters

    // Layer computation


    @Setting(title = "Suction cup diameter",
            description = "Diameter of the suction cup which lifts the bits (in mm)",
            minValue = 1.0, maxValue = 100.0
    )
    public static double suckerDiameter = 10.0;

    // Other parameters
    /**
     * To know the file in process
     */
    @Setting()
    public static String lastSlicedFile = "";

    /**
     * To know the lastly selected pattern configuration file
     */
    @Setting()
    public static String lastPatternConfigFile = "";

    @Setting(title = "Acceptable error",
            description = "Equivalent to 10^(-errorAccepted). Describing the maximum error accepted for accelerating the calculation")
    public static int errorAccepted = 5;

    @Setting(title = "Printer X",
            description = "Length of printer in mm")
    public static float printerX = 8000f;

    @Setting(title = "Printer Y",
            description = "Width of printer in mm")
    public static float printerY = 4000f;

    @Setting(title = "Printer Z",
            description = "Height of printer in mm")
    public static float printerZ = 2000f;

    /**
     * The provided templates
     */
    public static AScheduler[] schedulerPreloaded = {};
}