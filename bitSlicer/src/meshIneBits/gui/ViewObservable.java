package meshIneBits.gui;

import java.util.Observable;

import meshIneBits.GeneratedPart;
import meshIneBits.util.Vector2;

public class ViewObservable extends Observable {
	
	private GeneratedPart part = null;
	private int layerNumber = 0;
	private Vector2 selectedBitKey = null;
	private double zoom = 1;
	
	public enum Component {
		PART, LAYER, SELECTED_BIT, ZOOM, ME
	}
	
	public ViewObservable(){

	}
	
	public void letObserversKnowMe(){
		setChanged();
		notifyObservers(Component.ME);
	}
	
	public void setPart(GeneratedPart part){
		this.part = part;
		layerNumber = 0;
		selectedBitKey = null;
		setChanged();
		notifyObservers(Component.PART);
	}
	
	public void setLayer(int nbrLayer){
		if(part == null)
			return;
		layerNumber = nbrLayer;
		selectedBitKey = null;
		setChanged();
		notifyObservers(Component.LAYER);
	}
	
	public void setSelectedBitKey(Vector2 bitKey){
		if(part == null)
			return;
		selectedBitKey = bitKey;
		setChanged();
		notifyObservers(Component.SELECTED_BIT);
	}
	
	public void setZoom(double zoomValue){
		if(part == null)
			return;
		zoom = zoomValue;
		if (zoom < 0.2)
			zoom = 0.2;
		setChanged();
		notifyObservers(Component.ZOOM);
	}
	
	public GeneratedPart getCurrentPart(){
		return part;
	}
	
	public int getCurrentLayerNumber(){
		return layerNumber;
	}
	
	public Vector2 getSelectedBitKey(){
		return selectedBitKey;
	}
	
	public double getZoom(){
		return zoom;
	}
}