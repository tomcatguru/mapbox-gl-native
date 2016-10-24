package com.mapbox.mapboxsdk.testapp.annotations;

import android.graphics.Color;
import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.constants.MapboxConstants;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.testapp.R;
import com.mapbox.mapboxsdk.testapp.activity.camera.CameraTestActivity;
import com.mapbox.mapboxsdk.testapp.utils.OnMapReadyIdlingResource;
import com.mapbox.mapboxsdk.testapp.utils.ViewUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class AnnotationsTest {

    @Rule
    public final ActivityTestRule<CameraTestActivity> rule = new ActivityTestRule<>(CameraTestActivity.class);

    private OnMapReadyIdlingResource idlingResource;
    private Marker marker;
    private Polyline polyline;
    private Polygon polygon;


    @Before
    public void registerIdlingResource() {
        idlingResource = new OnMapReadyIdlingResource(rule.getActivity());
        Espresso.registerIdlingResources(idlingResource);
    }

    @Test
    @Ignore // TODO fix https://github.com/mapbox/mapbox-gl-native/issues/6802
    public void addMarkerTest() {
        ViewUtils.checkViewIsDisplayed(R.id.mapView);
        final MapboxMap mapboxMap = rule.getActivity().getMapboxMap();
        LatLng markerTarget = new LatLng();
        String markerSnippet = "Snippet";
        String markerTitle = "Title";

        assertEquals("Markers should be empty", 0, mapboxMap.getMarkers().size());

        final MarkerOptions options = new MarkerOptions();
        options.setPosition(markerTarget);
        options.setSnippet(markerSnippet);
        options.setTitle(markerTitle);

        try {
            rule.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    marker = mapboxMap.addMarker(options);
                }
            });
        } catch (Throwable t) {
            Log.e(MapboxConstants.TAG, "Could not run on ui-thread", t);
            assertTrue(false);
        } finally {
            assertEquals("Markers should be 1", 1, mapboxMap.getMarkers().size());
            assertEquals("Marker id should be 0", 0, marker.getId());
            assertEquals("Marker target should match", markerTarget, marker.getPosition());
            assertEquals("Marker snippet should match", markerSnippet, marker.getSnippet());
            assertEquals("Marker target should match", markerTitle, marker.getTitle());
            mapboxMap.clear();
            assertEquals("Markers should be empty", 0, mapboxMap.getMarkers().size());
        }
    }

    @Test
    @Ignore // TODO fix https://github.com/mapbox/mapbox-gl-native/issues/6802
    public void addPolygonTest() {
        ViewUtils.checkViewIsDisplayed(R.id.mapView);
        final MapboxMap mapboxMap = rule.getActivity().getMapboxMap();
        LatLng latLngOne = new LatLng();
        LatLng latLngTwo = new LatLng(1, 0);
        LatLng latLngThree = new LatLng(1, 1);

        assertEquals("Polygons should be empty", 0, mapboxMap.getPolygons().size());

        final PolygonOptions options = new PolygonOptions();
        options.strokeColor(Color.BLUE);
        options.fillColor(Color.RED);
        options.add(latLngOne);
        options.add(latLngTwo);
        options.add(latLngThree);

        try {
            rule.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    polygon = mapboxMap.addPolygon(options);
                }
            });
        } catch (Throwable t) {
            Log.e(MapboxConstants.TAG, "Could not run on ui-thread", t);
            assertTrue(false);
        } finally {
            assertEquals("Polygons should be 1", 1, mapboxMap.getPolygons().size());
            assertEquals("Polygon id should be 0", 0, polygon.getId());
            assertEquals("Polygon points size should match", 3, polygon.getPoints().size());
            assertEquals("Polygon stroke color should match", Color.BLUE, polygon.getStrokeColor());
            assertEquals("Polygon target should match", Color.RED, polygon.getFillColor());
            mapboxMap.clear();
            assertEquals("Polygons should be empty", 0, mapboxMap.getPolygons().size());
        }
    }

    @Test
    @Ignore // TODO fix https://github.com/mapbox/mapbox-gl-native/issues/6802
    public void addPolylineTest() {
        ViewUtils.checkViewIsDisplayed(R.id.mapView);
        final MapboxMap mapboxMap = rule.getActivity().getMapboxMap();
        LatLng latLngOne = new LatLng();
        LatLng latLngTwo = new LatLng(1, 0);

        assertEquals("Polygons should be empty", 0, mapboxMap.getPolygons().size());

        final PolylineOptions options = new PolylineOptions();
        options.color(Color.BLUE);
        options.add(latLngOne);
        options.add(latLngTwo);

        try {
            rule.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    polyline = mapboxMap.addPolyline(options);
                }
            });
        } catch (Throwable t) {
            Log.e(MapboxConstants.TAG, "Could not run on ui-thread", t);
            assertTrue(false);
        } finally {
            assertEquals("Polylines should be 1", 1, mapboxMap.getPolylines().size());
            assertEquals("Polyline id should be 0", 0, polyline.getId());
            assertEquals("Polyline points size should match", 2, polyline.getPoints().size());
            assertEquals("Polyline stroke color should match", Color.BLUE, polyline.getColor());
            mapboxMap.clear();
            assertEquals("Polyline should be empty", 0, mapboxMap.getPolylines().size());
        }
    }

    @After
    public void unregisterIdlingResource() {
        Espresso.unregisterIdlingResources(idlingResource);
    }
}
