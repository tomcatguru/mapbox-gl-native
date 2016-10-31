package com.mapbox.mapboxsdk.testapp.annotations;

import android.graphics.Color;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;
import android.view.View;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.MapboxConstants;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.testapp.R;
import com.mapbox.mapboxsdk.testapp.activity.camera.CameraTestActivity;
import com.mapbox.mapboxsdk.testapp.camera.CameraEaseTest;
import com.mapbox.mapboxsdk.testapp.utils.OnMapReadyIdlingResource;
import com.mapbox.mapboxsdk.testapp.utils.ViewUtils;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
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

        onView(withId(R.id.mapView)).perform(new AddMarkerAction(mapboxMap, options));
        assertEquals("Markers should be 1", 1, mapboxMap.getMarkers().size());
        assertEquals("Marker id should be 0", 0, marker.getId());
        assertEquals("Marker target should match", markerTarget, marker.getPosition());
        assertEquals("Marker snippet should match", markerSnippet, marker.getSnippet());
        assertEquals("Marker target should match", markerTitle, marker.getTitle());
        mapboxMap.clear();
        assertEquals("Markers should be empty", 0, mapboxMap.getMarkers().size());
    }

    private class AddMarkerAction implements ViewAction {

        private MapboxMap mapboxMap;
        private MarkerOptions options;

        AddMarkerAction(MapboxMap map, MarkerOptions markerOptions) {
            mapboxMap = map;
            options = markerOptions;
        }

        @Override
        public Matcher<View> getConstraints() {
            return isDisplayed();
        }

        @Override
        public String getDescription() {
            return getClass().getSimpleName();
        }

        @Override
        public void perform(UiController uiController, View view) {
            marker = mapboxMap.addMarker(options);
        }
    }

    @Test
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

        onView(withId(R.id.mapView)).perform(new AddPolygonAction(mapboxMap, options));

        assertEquals("Polygons should be 1", 1, mapboxMap.getPolygons().size());
        assertEquals("Polygon id should be 0", 0, polygon.getId());
        assertEquals("Polygon points size should match", 3, polygon.getPoints().size());
        assertEquals("Polygon stroke color should match", Color.BLUE, polygon.getStrokeColor());
        assertEquals("Polygon target should match", Color.RED, polygon.getFillColor());
        mapboxMap.clear();
        assertEquals("Polygons should be empty", 0, mapboxMap.getPolygons().size());
    }

    private class AddPolygonAction implements ViewAction {

        private MapboxMap mapboxMap;
        private PolygonOptions options;

        AddPolygonAction(MapboxMap map, PolygonOptions polygonOptions) {
            mapboxMap = map;
            options = polygonOptions;
        }

        @Override
        public Matcher<View> getConstraints() {
            return isDisplayed();
        }

        @Override
        public String getDescription() {
            return getClass().getSimpleName();
        }

        @Override
        public void perform(UiController uiController, View view) {
            polygon = mapboxMap.addPolygon(options);
        }
    }

    @Ignore
    @Test
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

        onView(withId(R.id.mapView)).perform(new AddPolyLineAction(mapboxMap, options));

        assertEquals("Polylines should be 1", 1, mapboxMap.getPolylines().size());
        assertEquals("Polyline id should be 0", 0, polyline.getId());
        assertEquals("Polyline points size should match", 2, polyline.getPoints().size());
        assertEquals("Polyline stroke color should match", Color.BLUE, polyline.getColor());
        mapboxMap.clear();
        assertEquals("Polyline should be empty", 0, mapboxMap.getPolylines().size());
    }

    private class AddPolyLineAction implements ViewAction {

        private MapboxMap mapboxMap;
        private PolylineOptions options;

        AddPolyLineAction(MapboxMap map, PolylineOptions polylineOptions) {
            mapboxMap = map;
            options = polylineOptions;
        }

        @Override
        public Matcher<View> getConstraints() {
            return isDisplayed();
        }

        @Override
        public String getDescription() {
            return getClass().getSimpleName();
        }

        @Override
        public void perform(UiController uiController, View view) {
            polyline = mapboxMap.addPolyline(options);
        }
    }


    @After
    public void unregisterIdlingResource() {
        Espresso.unregisterIdlingResources(idlingResource);
    }
}
