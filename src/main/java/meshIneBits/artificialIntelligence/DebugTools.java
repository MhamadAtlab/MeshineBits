package meshIneBits.artificialIntelligence;

import meshIneBits.Bit2D;
import meshIneBits.util.Polygon;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.Vector;
//todo @Etienne @Andre javadoc
@SuppressWarnings("unused")
public final class DebugTools {

    public static AffineTransform transformArea = new AffineTransform();

    public static Vector<Vector2> pointsToDrawRED = new Vector<>();
    public static Vector<Vector2> pointsToDrawGREEN = new Vector<>();
    public static Vector<Vector2> pointsToDrawBLUE = new Vector<>();
    public static Vector<Vector2> pointsToDrawORANGE = new Vector<>();

    public static Path2D cutPathToDraw = new Path2D.Double();

    public static Vector<Segment2D> segmentsToDraw = new Vector<>();
    public static Segment2D currentSegToDraw = new Segment2D(new Vector2(0,0), new Vector2(0,0));
    public static Segment2D currentSegToDraw2 = new Segment2D(new Vector2(0,0), new Vector2(0,0));

    public static Polygon poly = new Polygon();

    public static Area area = new Area();
    public static Area areaToDraw = null;

    public static Vector<Bit2D> Bits = new Vector<>();
}