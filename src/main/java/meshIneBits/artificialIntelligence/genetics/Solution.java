package meshIneBits.artificialIntelligence.genetics;


import meshIneBits.Bit2D;
import meshIneBits.artificialIntelligence.DataPreparation;
import meshIneBits.config.CraftConfig;
import meshIneBits.util.CalculateAreaSurface;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.Comparator;
import java.util.Vector;

public class Solution {

    /**
     * The coefficient associated to the mutation.
     */
    private static final double MUTATION_MAX_STRENGTH = 0.2;


    private final int LENGTH_COEFF_STRENGTH;
    private final Vector<Vector2> bound;
    private final Vector2 startPoint;
    private final Generation generation;
    private final Area layerAvailableArea;
    private boolean hasBeenEvaluated = false;
    private boolean hasBeenCheckedBad = false;
    private boolean bad = false;
    private Bit2D bit;
    private double bitPos;
    private Vector2 bitAngle;
    private double score = 0;


    /**
     * A Solution is a Bit2D with position parameter in a local coordinate system. Its position is measured from its startPoint.
     */
    public Solution(double pos, Vector2 bitAngle, Vector2 startPoint, Generation generation, Vector<Vector2> bound, int length_coeff, Area layerAvailableArea) {
        this.bitPos = pos;
        this.bitAngle = bitAngle;
        this.startPoint = startPoint;
        this.generation = generation;
        this.bound = bound;
        this.LENGTH_COEFF_STRENGTH = length_coeff;
        this.layerAvailableArea = layerAvailableArea;
    }


    /**
     * Evaluates the current solution according to its lost surface.
     * Applies penalties to the score.
     *
     */
    public void evaluate() {
        if (hasBeenEvaluated)
            return;
        score = getAreaScore(); //the used area
        try {
            score += getLengthScore();
            hasBeenEvaluated = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Calculates the area of the Bit2D.
     * It is the area of the part of the Bit2D inside the Slice.
     *
     * @return the area.
     */
    private double getAreaScore() {
        bit = getBit(startPoint);
        Area availableBitArea = bit.getArea();
        availableBitArea.intersect(layerAvailableArea);

        if (availableBitArea.isEmpty()) {// || DetectorTool.checkIrregular(availableBitArea)) { // Outside of border or irregular
            this.generation.solutions.remove(this);
            return 0;
        } else {

            bit.updateBoundaries(availableBitArea);
            bit.calcCutPath();

            Rectangle2D rectangle2D = new Rectangle2D.Double(0, 0, CraftConfig.bitLength, CraftConfig.bitWidth);
            double maxArea = CalculateAreaSurface.approxArea(new Area(rectangle2D), 0);
            double area = CalculateAreaSurface.approxArea(bit.getArea(), 0);
            return ((1 - LENGTH_COEFF_STRENGTH / 100.0) * area / maxArea);


        }
    }

    /**
     * Mutates parameters of the solution.
     */
    public void mutate() {
        if (Math.random() < 0.5) { // mutate bitPos
            bitPos += (Math.random() * 2 - 1) * MUTATION_MAX_STRENGTH;
            if (bitPos < 0)
                bitPos = 0;
            else if (bitPos > CraftConfig.bitWidth)
                bitPos = CraftConfig.bitWidth;
        } else { // mutate bitAngle
            bitAngle = new Vector2(bitAngle.x + (Math.random() * 2 - 1) * MUTATION_MAX_STRENGTH,
                    bitAngle.y + (Math.random() * 2 - 1) * MUTATION_MAX_STRENGTH)
                    .normal();
        }
        hasBeenEvaluated = false;
        hasBeenCheckedBad = false;
    }

    public boolean isBad() {
        if (hasBeenCheckedBad)
            return bad;

        /*
        Adding getNumberOfIntersections usage will improve the performances.
        Needs to be debugged. Works elsewhere but not here... :(

        if (getNumberOfIntersections(bound) != 2)
            bad = true;
         */
        try {
            DataPreparation.getNextBitStartPoint(getBit(startPoint), bound);
        } catch (Exception e) {
            bad = true;
        }
        hasBeenCheckedBad = true;
        return bad;
    }

    /**
     * Returns the number of intersections between bit's edges and the given bound.
     *
     * @param boundPoints the bound of the Slice
     * @return the number of intersections
     */
    @SuppressWarnings("unused")
    private int getNumberOfIntersections(Vector<Vector2> boundPoints) {
        Vector<Segment2D> segmentsSlice = DataPreparation.getSegment2DS(boundPoints);
        Vector<Segment2D> bitSides = Bit2D.getBitSidesSegments(getBit(startPoint));

        int intersectionCount = 0;

        for (Segment2D segmentSlice : segmentsSlice)
            for (Segment2D bitSide : bitSides)
                if (Segment2D.doSegmentsIntersect(segmentSlice, bitSide))
                    intersectionCount++;

        return intersectionCount;
    }


    /**
     * get the Solution's Bit2D according to the startPoint.
     *
     * @param startPoint the point where the bit's edge should be placed
     * @return the related Bit
     */
    private Bit2D getBit(Vector2 startPoint) {
        Vector2 collinear = this.bitAngle.normal();
        Vector2 orthogonal = collinear
                .rotate(new Vector2(0, -1)); // 90deg anticlockwise rotation
        Vector2 position = startPoint
                .add(orthogonal.mul(this.bitPos))
                .add(collinear.mul(CraftConfig.bitLength / 2))
                .sub(orthogonal.mul(CraftConfig.bitWidth / 2));

        return new Bit2D(position, this.bitAngle);
    }


    /**
     * Add a penalty to the score.
     * The less the Bit2D follows the bound of the Slice,
     * the more the score will be decreased.
     * Depends of <code>LENGTH_PENALTY_STRENGTH</code>
     */
    private double getLengthScore() throws Exception {
        Bit2D bit2D = getBit(startPoint);
        Vector2 nextBitStartPoint = DataPreparation.getNextBitStartPoint(bit2D, bound);
        double coveredDistance = Vector2.dist(startPoint, nextBitStartPoint);
        return LENGTH_COEFF_STRENGTH / 100.0 * coveredDistance / CraftConfig.bitLength;
    }


    /**
     * @return the position and the angle of the solution in a String.
     */
    public String toString() {
        return "pos: " + this.bitPos + " , angle: " + this.bitAngle;
    }

    public Bit2D getBit() {
        return bit;
    }

    public double getBitPos() {
        return bitPos;
    }

    public Vector2 getBitAngle() {
        return bitAngle;
    }

    public double getScore() {
        return score;
    }
}

/**
 * Let the <code>Generation</code> compare two Solution by their scores.
 */
class SolutionComparator implements Comparator<Solution> {
    public int compare(Solution s1, Solution s2) {
        return Double.compare(s2.getScore(), s1.getScore());
    }
}