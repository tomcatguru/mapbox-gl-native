package com.mapbox.mapboxsdk.testapp.camera;

import android.graphics.PointF;
import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;

import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.MapboxConstants;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CameraMoveTest {

    @Rule
    public final ActivityTestRule<CameraTestActivity> rule = new ActivityTestRule<>(CameraTestActivity.class);

    private OnMapReadyIdlingResource idlingResource;

    @Before
    public void registerIdlingResource() {
        idlingResource = new OnMapReadyIdlingResource(rule.getActivity());
        Espresso.registerIdlingResources(idlingResource);
    }

    @Test
    public void testMoveToCameraPositionTarget() {
        ViewUtils.checkViewIsDisplayed(R.id.mapView);
        final MapboxMap mapboxMap = rule.getActivity().getMapboxMap();

        /*TODO remove zoom #6474*/
        float zoom = 1.0f;

        CameraPosition initialPosition = new CameraPosition.Builder().target(new LatLng()).zoom(zoom).bearing(0).tilt(0).build();
        CameraPosition cameraPosition = mapboxMap.getCameraPosition();
        assertEquals("Default camera position should match default", cameraPosition, initialPosition);

        final LatLng moveTarget = new LatLng(1, 1);

        try {
            rule.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mapboxMap.moveCamera(CameraUpdateFactory.newLatLng(moveTarget));
                }
            });
        } catch (Throwable t) {
            Log.e(MapboxConstants.TAG, "Could not run on ui-thread");
            assertTrue(false);
        } finally {
            cameraPosition = mapboxMap.getCameraPosition();
            assertEquals("Moved camera position target should match", cameraPosition.target, moveTarget);
            Log.e(MapboxConstants.TAG, getClass().getSimpleName() + " successfully executed with " + cameraPosition.target);
        }
    }

    @Test
    public void testMoveToCameraPositionTargetZoom() {
        ViewUtils.checkViewIsDisplayed(R.id.mapView);
        final MapboxMap mapboxMap = rule.getActivity().getMapboxMap();

        final float moveZoom = 15.5f;
        final LatLng moveTarget = new LatLng(1.0000000001, 1.0000000003);
        try {
            rule.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(moveTarget, moveZoom));
                }
            });
        } catch (Throwable t) {
            Log.e(MapboxConstants.TAG, "Could not run on ui-thread", t);
            assertTrue(false);
        } finally {
            CameraPosition cameraPosition = mapboxMap.getCameraPosition();
            assertEquals("Moved camera position target should match", cameraPosition.target, moveTarget);
            assertEquals("Moved zoom should match", cameraPosition.zoom, moveZoom, 0.0f);
            Log.e(MapboxConstants.TAG, getClass().getSimpleName() + " successfully executed with " + cameraPosition.target);
        }
    }

    @Test
    public void testMoveToCameraPosition() {
        ViewUtils.checkViewIsDisplayed(R.id.mapView);
        final MapboxMap mapboxMap = rule.getActivity().getMapboxMap();

        final LatLng moveTarget = new LatLng(1.0000000001, 1.0000000003);
        final float moveZoom = 15.5f;
        final float moveTilt = 45.5f;
        final float moveBearing = 12.5f;

        try {
            rule.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                            .target(moveTarget)
                            .zoom(moveZoom)
                            .tilt(moveTilt)
                            .bearing(moveBearing)
                            .build()
                    ));
                }
            });
        } catch (Throwable t) {
            Log.e(MapboxConstants.TAG, "Could not run on ui-thread", t);
            assertTrue(false);
        } finally {
            CameraPosition cameraPosition = mapboxMap.getCameraPosition();
            assertEquals("Moved camera position target should match", cameraPosition.target, moveTarget);
            assertEquals("Moved zoom should match", cameraPosition.zoom, moveZoom, 0.0f);
            assertEquals("Moved zoom should match", cameraPosition.tilt, moveTilt, 0.0f);
            assertEquals("Moved bearing should match", cameraPosition.bearing, moveBearing, 0.0f);
            Log.e(MapboxConstants.TAG, getClass().getSimpleName() + " successfully executed with " + cameraPosition.target);
        }
    }

    // TODO fix https://github.com/mapbox/mapbox-gl-native/issues/6800
    @Test
    @Ignore
    public void testMoveToBounds() {
        ViewUtils.checkViewIsDisplayed(R.id.mapView);
        final MapboxMap mapboxMap = rule.getActivity().getMapboxMap();

        final LatLng centerBounds = new LatLng(1, 1);
        LatLng cornerOne = new LatLng();
        LatLng cornerTwo = new LatLng(2, 2);

        final LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(cornerOne);
        builder.include(cornerTwo);

        try {
            rule.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mapboxMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 0));
                }
            });
        } catch (Throwable t) {
            Log.e(MapboxConstants.TAG, "Could not run on ui-thread", t);
            assertTrue(false);
        } finally {
            CameraPosition cameraPosition = mapboxMap.getCameraPosition();
            assertEquals("Moved camera position target should match center bounds", cameraPosition.target, centerBounds);
            Log.e(MapboxConstants.TAG, getClass().getSimpleName() + " successfully executed with " + cameraPosition.target);
        }
    }

    // TODO fix https://github.com/mapbox/mapbox-gl-native/issues/6800
    @Test
    @Ignore
    public void testMoveToMoveBy() {
        ViewUtils.checkViewIsDisplayed(R.id.mapView);
        final MapboxMap mapboxMap = rule.getActivity().getMapboxMap();

        final PointF centerPoint = mapboxMap.getProjection().toScreenLocation(mapboxMap.getCameraPosition().target);
        final LatLng moveTarget = new LatLng(2, 2);
        final PointF moveTargetPoint = mapboxMap.getProjection().toScreenLocation(moveTarget);
        try {
            rule.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mapboxMap.moveCamera(CameraUpdateFactory.scrollBy(
                            moveTargetPoint.x - centerPoint.x, moveTargetPoint.y - centerPoint.y));
                }
            });
        } catch (Throwable t) {
            Log.e(MapboxConstants.TAG, "Could not run on ui-thread", t);
            assertTrue(false);
        } finally {
            CameraPosition cameraPosition = mapboxMap.getCameraPosition();
            assertEquals("Moved camera should match calculated moved camera", cameraPosition.target, moveTarget);
            Log.e(MapboxConstants.TAG, getClass().getSimpleName() + " successfully executed with " + cameraPosition.target);
        }
    }

    @Test
    public void testMoveToZoomIn() {
        ViewUtils.checkViewIsDisplayed(R.id.mapView);
        final MapboxMap mapboxMap = rule.getActivity().getMapboxMap();

        /*TODO fix zoom #6474*/
        float zoom = 1.0f;

        try {
            rule.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mapboxMap.moveCamera(CameraUpdateFactory.zoomIn());
                }
            });
        } catch (Throwable t) {
            Log.e(MapboxConstants.TAG, "Could not run on ui-thread", t);
            assertTrue(false);
        } finally {
            CameraPosition cameraPosition = mapboxMap.getCameraPosition();
            assertEquals("Moved camera zoom should match moved camera zoom", cameraPosition.zoom, zoom + 1, 0.0f);
            Log.e(MapboxConstants.TAG, getClass().getSimpleName() + " successfully executed with " + cameraPosition.target);
        }
    }

    @Test
    public void testMoveToZoomOut() {
        ViewUtils.checkViewIsDisplayed(R.id.mapView);
        final MapboxMap mapboxMap = rule.getActivity().getMapboxMap();

        /*TODO fix zoom #6474*/
        float zoom = 1.0f;

        try {
            rule.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mapboxMap.moveCamera(CameraUpdateFactory.zoomOut());
                }
            });
        } catch (Throwable t) {
            Log.e(MapboxConstants.TAG, "Could not run on ui-thread", t);
            assertTrue(false);
        } finally {
            CameraPosition cameraPosition = mapboxMap.getCameraPosition();
            assertEquals("Moved camera zoom should match moved camera zoom", cameraPosition.zoom, zoom - 1, 0.0f);
            Log.e(MapboxConstants.TAG, getClass().getSimpleName() + " successfully executed with " + cameraPosition.target);
        }
    }

    @Test
    public void testMoveToZoomBy() {
        ViewUtils.checkViewIsDisplayed(R.id.mapView);
        final MapboxMap mapboxMap = rule.getActivity().getMapboxMap();

        /*TODO fix zoom #6474*/
        float zoom = 1.0f;
        final float zoomBy = 2.45f;

        try {
            rule.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mapboxMap.moveCamera(CameraUpdateFactory.zoomBy(zoomBy));
                }
            });
        } catch (Throwable t) {
            Log.e(MapboxConstants.TAG, "Could not run on ui-thread", t);
            assertTrue(false);
        } finally {
            CameraPosition cameraPosition = mapboxMap.getCameraPosition();
            assertEquals("Moved camera zoom should match moved camera zoom", cameraPosition.zoom, zoom + zoomBy, 0.0f);
            Log.e(MapboxConstants.TAG, getClass().getSimpleName() + " successfully executed with " + cameraPosition.target);
        }
    }

    @Test
    public void testMoveToZoomTo(){
        ViewUtils.checkViewIsDisplayed(R.id.mapView);
        final MapboxMap mapboxMap = rule.getActivity().getMapboxMap();

        /*TODO fix zoom #6474*/
        final float zoomTo = 2.45f;

        try {
            rule.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mapboxMap.moveCamera(CameraUpdateFactory.zoomTo(zoomTo));
                }
            });
        } catch (Throwable t) {
            Log.e(MapboxConstants.TAG, "Could not run on ui-thread", t);
            assertTrue(false);
        } finally {
            CameraPosition cameraPosition = mapboxMap.getCameraPosition();
            assertEquals("Moved camera zoom should match moved camera zoom", cameraPosition.zoom, zoomTo, 0.0f);
            Log.e(MapboxConstants.TAG, getClass().getSimpleName() + " successfully executed with " + cameraPosition.target);
        }
    }

    @After
    public void unregisterIdlingResource() {
        Espresso.unregisterIdlingResources(idlingResource);
    }
}
