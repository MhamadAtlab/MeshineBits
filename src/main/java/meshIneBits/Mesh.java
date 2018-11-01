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

package meshIneBits;

import meshIneBits.patterntemplates.PatternTemplate;
import meshIneBits.slicer.Slice;
import meshIneBits.slicer.SliceTool;
import meshIneBits.util.*;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * This object is the equivalent of the piece which will be printed
 */
public class Mesh extends Observable implements Observer, Serializable {

    private static final long serialVersionUID = 20180000400L;

    private Vector<Layer> layers = new Vector<>();
    private Vector<Slice> slices = new Vector<>();
    @Deprecated
    private transient PatternTemplate patternTemplate = null;
    private double skirtRadius;
    private transient SliceTool slicer;
    // private boolean sliced = false;
    @Deprecated
    private transient Optimizer optimizer = null;
    private Model model;
    private MeshEvents state;
    /**
     * Regroup of irregularities by index of layers and keys of bits
     */
    private Map<Layer, List<Vector2>> irregularBits;

    /**
     * Update the current state of mesh and notify observers with that state
     *
     * @param state a value from predefined list
     */
    private void setState(MeshEvents state) {
        this.state = state;
        setChanged();
        notifyObservers(state);
    }

    @Deprecated
    public Mesh(Model model) {
        slicer = new SliceTool(this);
        slicer.sliceModel();
        this.model = model;
    }

    /**
     * Set the new mesh to ready
     */
    public Mesh() {
        setState(MeshEvents.READY);
    }

    /**
     * Register a model given a path
     *
     * @param filepath model file to load
     * @throws Exception when an other action is currently executing
     */
    public void importModel(String filepath) throws Exception {
        if (state.isWorking()) throw new SimultaneousOperationsException(this);

        setState(MeshEvents.IMPORTING);
        this.model = new Model(filepath);
        this.model.center();

        // Crash all slices and layers
        slices.clear();
        layers.clear();

        // Signal to update
        setState(MeshEvents.IMPORTED);
    }

    /**
     * Start slicing the registered model
     *
     * @throws Exception when an other action is currently executing
     */
    public void slice() throws Exception {
        if (state.isWorking()) throw new SimultaneousOperationsException(this);

        setState(MeshEvents.SLICING);
        // clean before executing
        slices.clear();
        layers.clear();
        // start
        double zMin = this.model.getMin().z;
        if (zMin != 0) this.model.center(); // recenter before slicing
        slicer = new SliceTool(this);
        slicer.sliceModel();
        // MeshEvents.SLICED will be sent in update() after receiving
        // signal from slicer
    }

    /**
     * Given a certain template, pave the whole mesh sequentially
     *
     * @param template an automatic builder
     * @throws Exception when an other action is currently executing
     */
    public void pave(PatternTemplate template) throws Exception {
        pavementSafetyCheck();

        setState(MeshEvents.PAVING_MESH);
        // MeshEvents.PAVED_MESH will be sent in update() after receiving
        // enough signals from layers
        Logger.updateStatus("Ready to generate bits");
        template.ready(this);
        // Remove all current layers
        layers.clear();
        // New worker
        if (template.isInterdependent()) {
            SequentialPavingWorker pavingWorker = new SequentialPavingWorker(template);
            pavingWorker.addObserver(this);
            (new Thread(pavingWorker)).start();
        } else {
            PavingWorkerMaster pavingWorkerMaster = new PavingWorkerMaster(template);
            pavingWorkerMaster.addObserver(this);
            (new Thread(pavingWorkerMaster)).start();
        }
    }

    // TODO pave a certain layer

    private void pavementSafetyCheck() throws Exception {
        if (state.isWorking()) throw new SimultaneousOperationsException(this);
        if (!isSliced())
            throw new Exception("The mesh cannot be paved until it is sliced");
    }

    /**
     * Start the auto optimizer embedded in each template of each layer
     * if presenting
     *
     * @throws Exception when an other action is currently executing
     */
    public void optimize() throws Exception {
        optimizationSafetyCheck();

        setState(MeshEvents.OPTIMIZING_MESH);
        // MeshEvents.OPTIMIZED_MESH will be sent in update() after receiving
        // enough signals from layers
        MeshOptimizer meshOptimizer = new MeshOptimizer();
        Thread t = new Thread(meshOptimizer);
        t.start();
    }

    /**
     * Run the optimizing algorithm of the layer
     *
     * @param layer layer of optimization
     * @throws Exception when an other action is currently executing
     */
    public void optimize(Layer layer) throws Exception {
        optimizationSafetyCheck();

        setState(MeshEvents.OPTIMIZING_LAYER);
        LayerOptimizer layerOptimizer = new LayerOptimizer(layer);
        // MeshEvents.OPTIMIZED_LAYER will be sent after completed the task
        Thread t = new Thread(layerOptimizer);
        t.start();
    }

    private void optimizationSafetyCheck() throws Exception {
        if (state.isWorking()) throw new SimultaneousOperationsException(this);
        if (!isPaved())
            throw new Exception("The mesh cannot be auto optimized until it is fully paved.");
    }

    /**
     * Calculate glue points and/or areas between layers
     */
    public void glue() {
        setState(MeshEvents.GLUING);
        // TODO run glue inserting in each layer
        // MeshEvents.GLUED will be sent in update() after receiving
        // enough signals from layers
    }

    public void export() {
        // TODO export instructions
    }

    public Vector<Layer> getLayers() {
        if (!isPaved()) {
            try {
                throw new Exception("Mesh not paved!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return this.layers;
    }

    @Deprecated
    PatternTemplate getPatternTemplate() {
        return patternTemplate;
    }

    public double getSkirtRadius() {
        return skirtRadius;
    }

    public Vector<Slice> getSlices() {
        if (!isSliced()) {
            try {
                throw new Exception("Mesh not sliced!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return slices;
    }

    public boolean isPaved() {
        return state.getCode() >= MeshEvents.PAVED_MESH.getCode();
    }

    public boolean isSliced() {
        return state.getCode() >= MeshEvents.SLICED.getCode();
    }

    private void detectIrregularBits() {
        irregularBits = layers.parallelStream()
                .filter(Objects::nonNull)
                .collect(Collectors.toConcurrentMap(
                        layer -> layer,
                        layer -> DetectorTool.detectIrregularBits(layer.getFlatPavement()),
                        (u, v) -> u,
                        ConcurrentHashMap::new
                ));
    }

    /**
     * @param layer target
     * @return keys of irregularities in layer
     */
    public List<Vector2> getIrregularBitKeysOf(Layer layer) {
        return irregularBits.get(layer);
    }

    /**
     * skirtRadius is the radius of the cylinder that fully contains the part.
     */
    private void setSkirtRadius() {

        double radius = 0;

        for (Slice s : slices) {
            for (Segment2D segment : s.getSegmentList()) {
                if (segment.start.vSize2() > radius) {
                    radius = segment.start.vSize2();
                }
                if (segment.end.vSize2() > radius) {
                    radius = segment.end.vSize2();
                }
            }
        }
        skirtRadius = Math.sqrt(radius);
        Logger.updateStatus("Skirt's radius: " + ((int) skirtRadius + 1) + " mm");
    }

    public Model getModel() {
        return model;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof MeshEvents) {
            switch ((MeshEvents) arg) {
                case READY:
                    break;
                case IMPORTING:
                    break;
                case IMPORTED:
                    break;
                case SLICING:
                    break;
                case SLICED:
                    // Slice job has been done
                    // Slicer sends messages
                    this.slices = slicer.getSlices();
                    // sliced = true;
                    setSkirtRadius();
                    setState(MeshEvents.SLICED);
                    break;
                case PAVING_MESH:
                    break;
                case PAVED_MESH:
                    setState(MeshEvents.PAVED_MESH);
                    break;
                case PAVED_MESH_FAILED:
                    break;
                case OPTIMIZING_LAYER:
                    break;
                case OPTIMIZED_LAYER:
                    setState(MeshEvents.OPTIMIZED_MESH);
                    break;
                case OPTIMIZING_MESH:
                    break;
                case OPTIMIZED_MESH:
                    setState(MeshEvents.OPTIMIZED_MESH);
                    break;
                case GLUING:
                    break;
                case GLUED:
                    break;
                case OPENED:
                    break;
                case OPEN_FAILED:
                    break;
                case SAVED:
                    break;
                case SAVE_FAILED:
                    break;
                case EXPORTED:
                    break;
            }
        }
    }

    /**
     * @return the optimizer
     * @deprecated
     */
    public Optimizer getOptimizer() {
        return optimizer;
    }

    public MeshEvents getState() {
        return state;
    }

    /**
     * Determine all empty or null layers to notify users
     */
    public List<Integer> getEmptyLayers() {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < slices.size(); i++) {
            if (layers.get(i) == null
                    || layers.get(i).getFlatPavement().getBitsKeys().size() == 0)
                indexes.add(i);
        }
        return indexes;
    }

    /**
     * In charge of paving layers sequentially
     */
    private class SequentialPavingWorker extends Observable implements Runnable {

        private PatternTemplate patternTemplate;

        /**
         * Pave the layer following a certain template
         *
         * @param patternTemplate how we want to pave a layer
         */
        SequentialPavingWorker(PatternTemplate patternTemplate) {
            this.patternTemplate = patternTemplate;
        }

        @Override
        public void run() {
            buildLayers();
            detectIrregularBits();
            setChanged();
            notifyObservers(MeshEvents.PAVED_MESH);
        }

        /**
         * Construct layers from slices then pave them
         */
        private void buildLayers() {
            Logger.updateStatus("Paving layers sequentially with " + patternTemplate.getCommonName());
            int jobsize = slices.size();
            for (int i = 0; i < jobsize; i++) {
                layers.add(new Layer(i, slices.get(i), patternTemplate));
                Logger.setProgress(i + 1, jobsize);
            }
            Logger.updateStatus(layers.size() + " layers have been generated and paved");
        }
    }

    /**
     * Simultaneously pave all layers
     */
    private class PavingWorkerMaster extends Observable implements Observer, Runnable {

        private Map<Integer, PavingWorkerSlave> jobsMap = new ConcurrentHashMap<>();
        private int jobsTotalCount = 0;
        private int finishedJobsCount = 0;
        private PatternTemplate originalPatternTemplate;

        public PavingWorkerMaster(PatternTemplate patternTemplate) {
            layers.clear();
            originalPatternTemplate = patternTemplate;
            for (int i = 0; i < slices.size(); i++) {
                try {
                    PavingWorkerSlave pavingWorkerSlave = new PavingWorkerSlave(
                            (PatternTemplate) patternTemplate.clone(),
                            i,
                            slices.get(i)
                    );
                    pavingWorkerSlave.addObserver(this);
                    jobsMap.put(i, pavingWorkerSlave);
                    jobsTotalCount++;
                    layers.add(null);
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void run() {
            Logger.updateStatus("Paving mesh parallelly " + originalPatternTemplate.getCommonName());
            setState(MeshEvents.PAVING_MESH);
            jobsMap.forEach((integer, pavingWorkerSlave)
                    -> (new Thread(pavingWorkerSlave)).start());
        }

        @Override
        public synchronized void update(Observable o, Object arg) {
            if (o instanceof PavingWorkerSlave) {
                Layer resultLayer = ((PavingWorkerSlave) o).getLayer();
                layers.set(resultLayer.getLayerNumber(), resultLayer);
                finishedJobsCount++;
                Logger.setProgress(finishedJobsCount, jobsTotalCount);
            }
            if (finishedJobsCount == jobsTotalCount) {
                // Finished all
                Logger.updateStatus(layers.size() + " layers have been generated and paved");
                // Check irregularities
                detectIrregularBits();
                // Notify
                setChanged();
                notifyObservers(MeshEvents.PAVED_MESH);
            }
        }
    }

    /**
     * Separated thread to pave a certain layer
     */
    private class PavingWorkerSlave extends Observable implements Runnable {

        private PatternTemplate patternTemplate;
        private int index;
        private Slice slice;
        private Layer layer;

        public PavingWorkerSlave(PatternTemplate patternTemplate, int index, Slice slice) {
            this.patternTemplate = patternTemplate;
            this.index = index;
            this.slice = slice;
        }

        public Layer getLayer() {
            return layer;
        }

        @Override
        public void run() {
            try {
                layer = new Layer(index, slice, patternTemplate);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            setChanged();
            notifyObservers();
        }
    }

    /**
     * Managing optimization of a layer, then reporting to {@link MeshOptimizer} or {@link Mesh}
     */
    private class LayerOptimizer extends Observable implements Runnable {

        private Layer layer;
        private int irregularitiesRest;

        LayerOptimizer(Layer layer) {
            this.layer = layer;
        }

        public Layer getLayer() {
            return layer;
        }

        @Override
        public void run() {
            Logger.updateStatus("Auto-optimizing layer " + layer.getLayerNumber());
            irregularitiesRest = layer.getPatternTemplate().optimize(layer);
            if (irregularitiesRest <= 0) {
                switch (irregularitiesRest) {
                    case 0:
                        Logger.updateStatus("Auto-optimization succeeded on layer " + layer.getLayerNumber());
                        break;
                    case -1:
                        Logger.updateStatus("Auto-optimization failed on layer " + layer.getLayerNumber());
                        break;
                    case -2:
                        Logger.updateStatus("No optimizing algorithm implemented on layer " + layer.getLayerNumber());
                        break;
                }
            } else {
                Logger.updateStatus("Auto-optimization for layer " + layer.getLayerNumber() + " done. " + irregularitiesRest + " unsolved irregularities.");
            }
            setChanged();
            notifyObservers(MeshEvents.OPTIMIZED_LAYER);
        }
    }

    /**
     * Managing process of optimizing consequently all layers, then reporting to {@link Mesh}
     */
    private class MeshOptimizer extends Observable implements Runnable {
        @Override
        public void run() {
            int progressGoal = layers.size();
            int irregularitiesRest = 0;
            ArrayList<Integer> unsolvedLayers = new ArrayList<>();
            Logger.updateStatus("Optimizing the current mesh.");
            for (int j = 0; j < layers.size(); j++) {
                Logger.setProgress(j + 1, progressGoal);
                int ir = layers.get(j).getPatternTemplate().optimize(layers.get(j));
                if (ir > 0) {
                    irregularitiesRest += ir;
                } else if (ir < 0) {
                    unsolvedLayers.add(layers.get(j).getLayerNumber());
                }
            }
            Logger.updateStatus("Auto-optimization complete. Still has " + irregularitiesRest + " irregularities not solved yet.");
            if (!unsolvedLayers.isEmpty()) {
                StringBuilder str = new StringBuilder();
                for (Integer integer : unsolvedLayers) {
                    str.append(" ").append(integer);
                }
                Logger.updateStatus("Unsolvable layers: " + str.toString());
            }
            setChanged();
            notifyObservers(MeshEvents.OPTIMIZED_MESH);
        }
    }

}