package meshIneBits.IA.IA_util;

import meshIneBits.util.Vector2;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Vector;
import java.util.stream.Stream;

public final class DataLog {
    private final static String dataLogFilePath = "storedBits.txt";

    static public long getNumberOfEntries() throws IOException {
        return Files.lines(Paths.get(dataLogFilePath)).count();
    }


    static public void saveEntry(DataLogEntry dataLogEntry) throws IOException {
        String line = dataLogEntry.getBitPosition().x + ","
                + dataLogEntry.getBitPosition().y + ","
                + dataLogEntry.getBitOrientation().x + ","
                + dataLogEntry.getBitOrientation().y;

        for (Vector2 point : dataLogEntry.getPoints()) {
            line += "," + point.x + ";" + point.y;
        }
        FileWriter fw = new FileWriter(dataLogFilePath, true);
        fw.write(line + "\n");
        fw.close();
    }


    static public void saveAllEntries(Vector<DataLogEntry> dataSetEntries) throws IOException {
        for (DataLogEntry dataLogEntry : dataSetEntries) {
            saveEntry(dataLogEntry);
        }
    }


    static public DataLogEntry getEntryFromFile(long lineNumber) {
        String line;
        try (Stream<String> lines = Files.lines(Paths.get(dataLogFilePath))) {
            line = lines.skip(lineNumber - 1).findFirst().get();
        } catch (IOException e) {
            System.out.println(e);
            return null;
        }
        return decodeLine(line);
    }


    static public Vector<DataLogEntry> getAllEntriesFromFile() {

        Vector<String> lines = new Vector<>();

        try {
            lines.addAll(Files.readAllLines(Paths.get(dataLogFilePath)));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        Vector<DataLogEntry> dataSetEntries = new Vector<>();
        for (String line : lines) {
            dataSetEntries.add(decodeLine(line));
        }

        return dataSetEntries;
    }


    static private DataLogEntry decodeLine(String line) {

        String[] dataStr = line.split(",");
        Vector<String> data = new Vector<>();
        Collections.addAll(data, dataStr);

        double bitPosX = Double.parseDouble(data.remove(0));
        double bitPosY = Double.parseDouble(data.remove(0));
        Vector2 bitPos = new Vector2(bitPosX, bitPosY);

        double bitOrientationX = Double.parseDouble(data.remove(0));
        double bitOrientationY = Double.parseDouble(data.remove(0));
        Vector2 bitOrientation = new Vector2(bitOrientationX, bitOrientationY);

        Vector<Vector2> points = new Vector<>();
        for (String pointStr : data) {
            String[] pointStrSplit = pointStr.split(";");
            points.add(new Vector2(
                    Double.parseDouble(pointStrSplit[0]),
                    Double.parseDouble(pointStrSplit[1])));
        }

        return new DataLogEntry(bitPos, bitOrientation, points);
    }

}