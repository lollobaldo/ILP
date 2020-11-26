package uk.ac.ed.inf.heatmap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.GeoJson;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

import uk.ac.ed.inf.heatmap.DataPoint;


/**
 * The class Heat map
 */
public class HeatMap {
    /** 2D matrix representing the data */
    private int[][] data;
    /** x and y coordinated for the top-left point */
    private double x;
    private double y;
    /** number of rectangles in the x and y dimensions */
    private double xSize;
    private double ySize;
    /** size of rectangles in the x and y dimensions */
    private double xStep;
    private double yStep;


    /**
     * HeatMap constructor.
     *
     * @param data  data for the HeatMap
     * @param topLeft  top-left Point
     * @param bottomRight  bottom-right Point
     */
    public HeatMap(int[][] data, Point topLeft, Point bottomRight) {
        this.data = data;
        this.x = topLeft.longitude();
        this.y = topLeft.latitude();
        this.xSize = data[0].length;
        this.ySize = data.length;
        this.xStep = (bottomRight.longitude() - topLeft.longitude()) / xSize;
        this.yStep = (bottomRight.latitude() - topLeft.latitude()) / xSize;
    }


    /**
     * Method to generate the GeoJson object
     *
     * @param fillOpacity  Fill opacity for the HeatMap layer
     * @return GeoJson
     */
    public GeoJson generateGeoJson(double fillOpacity) {
        List<Feature> heatmapRectangles = new ArrayList<>();

        // Iterate through all rectangles to generate them
        for(int i = 0; i < ySize; i++) {
            for(int j = 0; j < xSize; j++) {
                // Generate DataPoint and get its hex color
                String fillColor = new DataPoint(data[i][j]).getRgbString();
                Polygon rectangle = getRectangle(i, j);
                Feature feature = Feature.fromGeometry(rectangle);
                // Add properties for coloring
                feature.addNumberProperty("fill-opacity", fillOpacity);
                feature.addStringProperty("fill", fillColor);
                feature.addStringProperty("rgb-string", fillColor);
                heatmapRectangles.add(feature);
            }
        }
        return FeatureCollection.fromFeatures(heatmapRectangles);
    }


    /**
     * Private method to get the ith-jth rectangle
     *
     * @param i  the i
     * @param j  the j
     * @return Polygon
     */
    private Polygon getRectangle(int i, int j) { 

        // Generate all points to describe rectangle
        // NOTE: Need last and first point to be the same to close the shape
        List<Point> rectangle = Arrays.asList(
            Point.fromLngLat(this.x + this.xStep * j, this.y + this.yStep * i),
            Point.fromLngLat(this.x + this.xStep * j, this.y + this.yStep * (i + 1)),
            Point.fromLngLat(this.x + this.xStep * (j + 1), this.y + this.yStep * (i + 1)),
            Point.fromLngLat(this.x + this.xStep * (j + 1), this.y + this.yStep * i),
            Point.fromLngLat(this.x + this.xStep * j, this.y + this.yStep * i)
        );
        return Polygon.fromLngLats(Arrays.asList(rectangle));
    }
}
