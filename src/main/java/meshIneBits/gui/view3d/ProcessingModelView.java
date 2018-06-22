/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016  Thibault Cassard & Nicolas Gouju.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
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

package meshIneBits.gui.view3d;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import com.jogamp.nativewindow.WindowClosingProtocol.WindowClosingMode;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;

import meshIneBits.GeneratedPart;
import meshIneBits.Model;
import meshIneBits.util.Logger;
import meshIneBits.util.Triangle;
import meshIneBits.util.Vector3;
import meshIneBits.gui.SubWindow;

import processing.core.PApplet;
import processing.core.PShape;
import processing.opengl.PJOGL;

import remixlab.dandelion.geom.Quat;
import remixlab.dandelion.geom.Rotation;
import remixlab.proscene.InteractiveFrame;
import remixlab.proscene.Scene;
import remixlab.dandelion.geom.Vec;

import controlP5.*;

import static java.awt.event.KeyEvent.VK_SPACE;
import static remixlab.proscene.MouseAgent.WHEEL_ID;


/**
 * 
 * @author Nicolas
 *
 */
public class ProcessingModelView extends PApplet implements Observer, SubWindow {

	private final int BACKGROUND_COLOR = color(150, 150, 150);
	private final int MODEL_COLOR = color(219, 100, 50);
	private int height = 400;
	private int width = 700;
	private float printerX;
	private float printerY;
	private float printerZ;

	private static boolean visible = false;
	private static boolean initialized = false;

	private static ProcessingModelView currentInstance = null;
	private static Model MODEL;
	private static Controller controller = null;

	private PShape shape;
	private Scene scene;
	private InteractiveFrame frame;
	private ControlP5 cp5;
	private Textlabel txt;
	private Textarea tooltipGrav;
	private Textarea tooltipReset;
	private Textarea tooltipCamera;
	private Textarea tooltipApply;

	private DecimalFormat df;

    private boolean[] borders;

	/**
	 *
	 */
	public static void startProcessingModelView(){
		if (currentInstance == null){
			PApplet.main("meshIneBits.gui.view3d.ProcessingModelView");
		}
	}

	/**
	 *
	 */
	public static void closeProcessingModelView(){
		if (currentInstance != null) {
			currentInstance.destroyGLWindow();
		}
	}

	/**
	 *
	 */
	private void destroyGLWindow(){
		((com.jogamp.newt.opengl.GLWindow) surface.getNative()).destroy();
	}

	/**
	 *
	 */
	public void settings(){
		PJOGL.setIcon(this.getClass().getClassLoader().getResource("resources/icon.png").getPath());
		currentInstance = this;
		size(width, height, P3D);
	}

	/**
	 *
	 */
	private void setCloseOperation(){
		//Removing close listeners
		com.jogamp.newt.opengl.GLWindow win = ((com.jogamp.newt.opengl.GLWindow) surface.getNative());
		for (com.jogamp.newt.event.WindowListener wl : win.getWindowListeners()){
			win.removeWindowListener(wl);
		}

		win.setDefaultCloseOperation(WindowClosingMode.DISPOSE_ON_CLOSE);

		win.addWindowListener(new WindowAdapter() {
			public void windowDestroyed(WindowEvent e) {
				controller.deleteObserver(currentInstance);
				dispose();
				currentInstance = null;
				MODEL = null;
				initialized = false;
				visible = false;
			}
		});
	}

	public ProcessingModelView() {
		controller = Controller.getInstance();
		controller.addObserver(this);
	}

	/**
	 *
	 */
	public void setup(){

		this.surface.setResizable(true);
		this.surface.setTitle("MeshIneBits - Model view");
		try{
			MODEL = controller.getModel();
		} catch (Exception e){
			System.out.print(" Model loading failed");
		}

        borders = new boolean[6];                            // each bool corresponds to 1 face of the workspace. is true if the the shape is crossing the associate face.
		for (int i = 0; i < 6; i++){ borders[i] = false;}    // borders[0] =  xmin border / borders[1] = xmax border ...

		scene = new Scene(this);
		scene.eye().setPosition(new Vec(0, 1, 1));
		scene.eye().lookAt(scene.eye().sceneCenter());
		scene.setRadius(2500);
		scene.showAll();
		scene.toggleGridVisualHint();
		scene.disableKeyboardAgent();

		buildModel();
		cp5 = new ControlP5(this);
		createButtons(cp5);

		df = new DecimalFormat("#.##");
		df.setRoundingMode(RoundingMode.CEILING);

		setCloseOperation();
		frame = new InteractiveFrame(scene,shape);
		frame.setMotionBinding(WHEEL_ID, null);
	}

	private void buildModel(){
		Logger.updateStatus("Start building STL model");

		Vector<Triangle> stlTriangles = MODEL.getTriangles();

		shapeMode(CORNER);
		shape = createShape(GROUP);

		for (Triangle t : stlTriangles){
			shape.addChild(getPShapeFromTriangle(t));
		}

		Logger.updateStatus("STL model built.");
	}

	/**
	 *
	 *
	 */
	private PShape getPShapeFromTriangle(Triangle t){
		PShape face = createShape();
		face.beginShape();

		for (Vector3 p : t.point){
			face.vertex((float) p.x, (float) p.y, (float) p.z);
		}

		face.endShape(CLOSE);

		face.setStroke(false);
		face.setFill(MODEL_COLOR);

		return face;
	}
	/**
	 *
	 */

	public void keyPressed(){
		if (keyCode == VK_SPACE){
			//this.surface.setSize(1280,720);
		}
	}

	public void draw(){
		background(BACKGROUND_COLOR);
		lights();
		ambientLight(255,255,255);
		drawWorkspace();
		mouseConstraints();
		scene.drawFrames();
		scene.beginScreenDrawing();
		txt.setText("Current position :\n" + " x : " + df.format(frame.position().x()) + "\n y : " + df.format(frame.position().y()) + "\n z : " + df.format(frame.position().z()));
		cp5.draw();
		displayTooltips();
		scene.endScreenDrawing();
	}

	private void drawWorkspace() {
		try {
			File filename = new File(this.getClass().getClassLoader().getResource("resources/PrinterConfig.txt").getPath());
			FileInputStream file = new FileInputStream(filename);
			BufferedReader br = new BufferedReader(new InputStreamReader(file));
			String strline;
			int linenumber = 0;
			while ((strline = br.readLine()) != null){
				linenumber++;
				br.skip(3);
				if (linenumber == 3){
					printerX = Float.valueOf(strline);
				}
				else if (linenumber == 4){
					printerY = Float.valueOf(strline);
				}
				else if (linenumber == 5){
					printerZ = Float.valueOf(strline);
				}

			}
			br.close();
			file.close();

		}
		catch(Exception e){
			System.out.println("Error :" + e.getMessage());
		}
		pushMatrix();
		noFill();

		stroke(255,255,0);
		translate(0,0,printerZ/2);
		box(printerX,printerY,printerZ);
		popMatrix();
		stroke(80);
		scene.pg().pushStyle();
		scene.pg().beginShape(LINES);
		for (int i = -(int)printerX/2; i <= printerX/2; i+=100) {
			vertex(i,printerY/2,0);
			vertex(i,-printerY/2,0);

		}
		for (int i = -(int)printerY/2; i <= printerY/2; i+=100) {
			vertex(printerX/2,i,0);
			vertex(-printerX/2,i,0);
		}
		scene.pg().endShape();
		scene.pg().popStyle();

	}

	private void createButtons(ControlP5 cp5){
		cp5.addTextfield("RotationX").setPosition(20,40).setSize(30,20)
				.setInputFilter(0).setColorBackground(color(255,250))
				.setColor(0).setColorLabel(255).setAutoClear(false).setColorCursor(0);

		cp5.addTextfield("RotationY").setPosition(20,80).setSize(30,20)
				.setInputFilter(0).setColorBackground(color(255,250))
				.setColor(0).setColorLabel(255).setAutoClear(false).setColorCursor(0);

		cp5.addTextfield("RotationZ").setPosition(20,120).setSize(30,20)
				.setInputFilter(0).setColorBackground(color(255,250))
				.setColor(0).setColorLabel(255).setAutoClear(false).setColorCursor(0);

		cp5.addTextfield("PositionX").setPosition(70,40).setSize(30,20)
				.setInputFilter(0).setColorBackground(color(255,250))
				.setColor(0).setColorLabel(255).setAutoClear(false).setColorCursor(0);

		cp5.addTextfield("PositionY").setPosition(70,80).setSize(30,20).setInputFilter(0)
				.setColorBackground(color(255,250))
				.setColor(0).setColorLabel(255).setAutoClear(false).setColorCursor(0);

		cp5.addTextfield("PositionZ").setPosition(70,120).setSize(30,20).setInputFilter(0)
				.setColorBackground(color(255,250))
				.setColor(0).setColorLabel(255).setAutoClear(false).setColorCursor(0);


		cp5.addButton("ApplyGravity").setPosition(20,250).setSize(80,20).setColorLabel(255);
		cp5.addButton("Reset").setPosition(20,280).setSize(80,20).setColorLabel(255);
		cp5.addButton("CenterCamera").setPosition(20,310).setSize(80,20).setColorLabel(255);
		cp5.addButton("Apply").setPosition(20,340).setSize(80,20).setColorLabel(255);

		tooltipGrav =  cp5.addTextarea("tooltipGrav").setPosition(100,250).setText("Pose le modèle").setSize(90,18)
					.setColorBackground(color(220))
					.setColor(color(50)).setFont(createFont("arial",10)).setLineHeight(12).hide()
					.hideScrollbar();
		tooltipGrav.getValueLabel().getStyle().setMargin(1,0,0,5);

		tooltipReset =  cp5.addTextarea("tooltipReset").setPosition(100,280).setText("Remise à zero").setSize(85,18)
				.setColorBackground(color(220))
				.setColor(color(50)).setFont(createFont("arial",10)).setLineHeight(12).hide()
				.hideScrollbar();
		tooltipReset.getValueLabel().getStyle().setMargin(1,0,0,5);

		tooltipCamera =  cp5.addTextarea("tooltipCamera").setPosition(100,310).setText("Centre le modèle").setSize(105,18)
				.setColorBackground(color(220))
				.setColor(color(50)).setFont(createFont("arial",10)).setLineHeight(12).hide()
				.hideScrollbar();
		tooltipCamera.getValueLabel().getStyle().setMargin(1,0,0,5);

		tooltipApply =  cp5.addTextarea("tooltipApply").setPosition(100,340).setText("Applique les modifications").setSize(145,18)
				.setColorBackground(color(220))
				.setColor(color(50)).setFont(createFont("arial",10)).setLineHeight(12).hide()
				.hideScrollbar();
		tooltipApply.getValueLabel().getStyle().setMargin(1,0,0,5);

		txt = cp5.addTextlabel("label").setText("Current Position : (0,0,0)").setPosition(570,350)
				.setSize(80,40).setColor(255);

		cp5.addTextlabel("model size", "Model Size :\n Depth:" + shape.getDepth() + "\n Height :" + shape.getHeight() + "\n Width : " + shape.getWidth())
				.setPosition(570,10).setColor(255);

		cp5.setAutoDraw(false);
	}

	private void displayTooltips(){
		tooltipGrav.hide();
		tooltipReset.hide();
		tooltipCamera.hide();
		tooltipApply.hide();
		tooltipGrav.setPosition(mouseX+20 ,mouseY-20);
		tooltipReset.setPosition(mouseX+20 ,mouseY-20);
		tooltipCamera.setPosition(mouseX+20 ,mouseY-20);
		tooltipApply.setPosition(mouseX+20 ,mouseY-20);

		if ((mouseX > 20) && (mouseX < 100) && (mouseY > 250) && (mouseY < 270)){
			if ((pmouseX - mouseX) == 0 && (pmouseY - mouseY) == 0) {
				tooltipGrav.show();
			}
		}
		if ((mouseX > 20) && (mouseX < 100) && (mouseY > 280) && (mouseY < 300)){
			if ((pmouseX - mouseX) == 0 && (pmouseY - mouseY) == 0) {
				tooltipReset.show();
			}
		}
		if ((mouseX > 20) && (mouseX < 100) && (mouseY > 310) && (mouseY < 330)){
			if ((pmouseX - mouseX) == 0 && (pmouseY - mouseY) == 0) {
				tooltipCamera.show();
			}
		}
		if ((mouseX > 20) && (mouseX < 100) && (mouseY > 340) && (mouseY < 360)){
			if ((pmouseX - mouseX) == 0 && (pmouseY - mouseY) == 0) {
				tooltipApply.show();
			}
		}
	}

	void myDelay(int ms)
	{
		try
		{
			Thread.sleep(ms);
		}
		catch(Exception e){}
	}

	private void rotateShape(float angleX, float angleY, float angleZ){
		Quat r = new Quat();
		float angXRad = (float)Math.toRadians((double)angleX);
		float angYRad = (float)Math.toRadians((double)angleY);
		float angZRad = (float)Math.toRadians((double)angleZ);

		r.fromEulerAngles(angXRad,angYRad,angZRad);
		frame.rotate(r);
		//applyRotation(MODEL);
		applyGravity();
	}

    private void translateShape(float transX, float transY, float transZ){
        boolean checkIn = checkShapeInWorkspace();
        frame.translate(transX, transY, transZ);
        if (!checkIn) {
            if (borders[0]){
                print(borders[0]);
                frame.setPosition(-printerX / 2 + shape.getWidth() / 2, frame.position().y(), frame.position().z());
            }
            if (borders[1]) {
                frame.setPosition(printerX / 2 - shape.getWidth() / 2, frame.position().y(), frame.position().z());
            }
            if (borders[2]){
                frame.setPosition(frame.position().x(),- printerY/2 + shape.getHeight()/2,frame.position().z());
            }
            if (borders[3]){
                frame.setPosition(frame.position().x(),printerY/2 - shape.getHeight()/2,frame.position().z());
            }
            if (borders[4]){
                frame.setPosition(frame.position().x(),frame.position().y(),0);
            }
            if(borders[5]){
                frame.setPosition(frame.position().x(),frame.position().y(),printerZ - shape.getDepth());
            }
        }
        //applyTranslation(MODEL);
    }

    public void applyRotation(Model m){
		Rotation r = frame.orientation();
		m.rotate(r);
	}

	public void applyTranslation(Model m){
		Vec trans = frame.position();
		Vector3 v = new Vector3(trans.x(), trans.y(),trans.z());
		MODEL.setPos(v);
	}

	private float getMinShape() {
		float minz = Float.MAX_VALUE;
		int size = shape.getChildCount();
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < 3; j++){
				Vec vertex = new Vec(shape.getChild(i).getVertex(j).x,shape.getChild(i).getVertex(j).y, shape.getChild(i).getVertex(j).z);
                Vec v = frame.inverseCoordinatesOf(vertex);
				if (minz > v.z()){
					minz = v.z();
				}
			}
		}
		return minz;
	}

	private void applyGravity(){
		float minz = getMinShape();
		if (minz != 0) {
			frame.translate(0, 0, - getMinShape());
		}
	}

	private void resetModel(){
		MODEL.rotate(frame.rotation().inverse());
		frame.setPosition(new Vec(0,0,0));
		frame.rotate(frame.rotation().inverse());
	}

    private boolean checkShapeInWorkspace(){
        for (int i = 0; i < 6; i++) { borders[i] = false;}
        float minX = -printerX/2;
        float maxX = printerX/2;
        float minY = -printerY/2;
        float maxY = printerY/2;
        float minZ = 0;
        float maxZ = printerZ;
        Vec pos = frame.position();
        Vec minPos = new Vec(pos.x() - shape.getWidth()/2,pos.y() - shape.getHeight()/2, pos.z());
        Vec maxPos = new Vec(pos.x() + shape.getWidth()/2,pos.y() + shape.getHeight()/2,pos.z() + shape.getDepth());
        boolean inWorkspace = true;
        if (minPos.x() < minX){
            inWorkspace = false;
            borders[0] = true;
        }
        if (maxPos.x() >= maxX){
            inWorkspace = false;
            borders[1] = true;
        }
        if (minPos.y() < minY){
            inWorkspace = false;
            borders[2] = true;
        }
        if (maxPos.y() >= maxY){
            inWorkspace = false;
            borders[3] = true;
        }
        if (minPos.z() < minZ){
            inWorkspace = false;
            borders[4] = true;
        }
        if (maxPos.z() >= maxZ){
            inWorkspace = false;
            borders[5] = true;
        }
        return inWorkspace;
    }

    private void mouseConstraints(){
        boolean checkIn = checkShapeInWorkspace();
        if (!checkIn) {
            if (borders[0]) {
                frame.setPosition(-printerX / 2 + shape.getWidth() / 2, frame.position().y(), frame.position().z());
            }
            if (borders[1]) {
                frame.setPosition(printerX / 2 - shape.getWidth() / 2, frame.position().y(), frame.position().z());
            }
            if (borders[2]) {
                frame.setPosition(frame.position().x(), -printerY / 2 + shape.getHeight() / 2, frame.position().z());
            }
            if (borders[3]){
                frame.setPosition(frame.position().x(),printerY/2 - shape.getHeight()/2,frame.position().z());
            }
            if (borders[4]){
                frame.setPosition(frame.position().x(),frame.position().y(),0);
            }
            if (borders[5]){
                frame.setPosition(frame.position().x(),frame.position().y(),printerZ - shape.getDepth());
            }
        }
    }

    private void centerCamera(){
		float y = scene.eye().position().y();
		float z = scene.eye().position().z();
		scene.eye().setPosition(new Vec(frame.position().x(), y, z));
		scene.eye().lookAt(frame.position());
	}

	public void RotationX(String theValue){
		float angle = Float.valueOf(theValue);
		rotateShape(angle,0,0);
	}

	public void RotationY(String theValue){
		float angle = Float.valueOf(theValue);
		rotateShape(0, angle, 0);
	}

	public void RotationZ(String theValue){
		float angle = Float.valueOf(theValue);
		rotateShape(0,0, angle);
	}

	public void PositionX(String theValue) {
		float pos = Float.valueOf(theValue);
		translateShape(pos,0,0);
	}

	public void PositionY(String theValue){
		float pos = Float.valueOf(theValue);
		translateShape(0, pos, 0);
	}

	public void PositionZ(String theValue){
		float pos = Float.valueOf(theValue);
		translateShape(0, 0, pos);
	}

	public void Reset(float theValue){
		resetModel();
		centerCamera();
	}

	public void ApplyGravity(float theValue) {
		applyGravity();

	}

	public void CenterCamera(float theValue){
		centerCamera();
	}

	public void Apply(float theValue){
			applyGravity();
			applyTranslation(MODEL);
			applyRotation(MODEL);
	}

    @Override
    public void update(Observable o, Object arg){

    }

	public void open(){
		// TODO Auto-generated method stub
		if (!initialized){
			ProcessingModelView.startProcessingModelView();
			visible = true;
			initialized = true;
		} else{
			setVisible(true);
		}
	}

	public void hide(){
		// TODO Auto-generated method stub
		setVisible(false);
	}

	@Override
	public void toggle(){
		if (visible){
			hide();
		} else{
			open();
		}
	}

	@Override
	public void setCurrentPart(GeneratedPart currentPart) {
		controller.setCurrentPart(currentPart);
	}

	private void setVisible(boolean b){
		// TODO Auto-generated method stub
		currentInstance.getSurface().setVisible(b);
		visible = b;
	}

	public void setModel(Model m) {
		controller.setModel(m);
	}
}

