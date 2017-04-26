package meshIneBits.PatternTemplates;

import java.util.Vector;

import meshIneBits.Bit2D;
import meshIneBits.Layer;
import meshIneBits.Pattern;
import meshIneBits.Config.CraftConfig;
import meshIneBits.PatternTemplates.PatternTemplate;
import meshIneBits.util.Optimizer;
import meshIneBits.util.Vector2;

public class PatternTemplate2 extends PatternTemplate {

	public PatternTemplate2(double skirtRadius) {
		super(skirtRadius);
	}
	/**
	 * TODO
	 * Eliminate this
	 */
	public Pattern createPattern(int layerNumber) {
		Vector<Bit2D> bits = new Vector<Bit2D>();
		double xOffSet = Math.sqrt(2.0) / 2.0 * CraftConfig.bitLength + CraftConfig.yOffset;
		double yOffSet = Math.sqrt(2.0) / 2.0 * CraftConfig.bitWidth + CraftConfig.yOffset;
		for (double i = patternStart.x; i <= patternEnd.x; i = i + 2 * xOffSet){
			for (double j = patternStart.y; j <= patternEnd.y; j = j + 2 * yOffSet){
				Vector2 originBit;
				Vector2 orientationBit;
				double layerOffSet = 0; // In this pattern we apply an offset on 1 layer on 2
				if (layerNumber%2 == 0){
					layerOffSet = yOffSet;
				}
				originBit = new Vector2(i, j + layerOffSet);
				orientationBit = new Vector2(1, 1);
				bits.add(new Bit2D(originBit, orientationBit));	
			}
		}
		for (double i = patternStart.x + xOffSet; i <= patternEnd.x; i = i + 2 * xOffSet){
			for (double j = patternStart.y + yOffSet; j <= patternEnd.y; j = j + 2 * yOffSet){
				Vector2 originBit;
				Vector2 orientationBit;
				double layerOffSet = 0; // In this pattern we apply an offset on 1 layer on 2
				if (layerNumber%2 == 0){
					layerOffSet = yOffSet;
				}
				originBit = new Vector2(i, j + layerOffSet);
				orientationBit = new Vector2(-1, 1);
				bits.add(new Bit2D(originBit, orientationBit));	
			}
		}
		return new Pattern(bits, new Vector2(1,0), skirtRadius); //every pattern have no rotation in that template
	}
	@Override
	public void optimize(Layer realState) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public Vector2 moveBit(Pattern actualState, Vector2 keyOfBitToMove, Vector2 direction) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Vector2 moveBit(Pattern actualState, Vector2 keyOfBitToMove, Vector2 direction, double distance) {
		// TODO Auto-generated method stub
		return null;
	}

}