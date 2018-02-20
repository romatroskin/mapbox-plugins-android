package com.mapbox.mapboxsdk.plugins.locationlayer;

import android.content.Context;
import android.location.Location;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResourceTimeoutException;
import android.support.test.espresso.UiController;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.plugins.testapp.activity.location.LocationLayerModesActivity;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.utils.OnMapReadyIdlingResource;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import timber.log.Timber;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerConstants.ACCURACY_LAYER;
import static com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerConstants.BACKGROUND_LAYER;
import static com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerConstants.BEARING_LAYER;
import static com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerConstants.FOREGROUND_ICON;
import static com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerConstants.FOREGROUND_LAYER;
import static com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerConstants.FOREGROUND_STALE_ICON;
import static com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerConstants.LOCATION_SOURCE;
import static com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerConstants.NAVIGATION_LAYER;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@SuppressWarnings( {"MissingPermission"})
public class LocationLayerTest {

  @Rule
  public ActivityTestRule<LocationLayerModesActivity> rule = new ActivityTestRule<>(LocationLayerModesActivity.class);

  private OnMapReadyIdlingResource idlingResource;
  private LocationLayerPlugin locationLayerPlugin;
  private MapboxMap mapboxMap;
  private Location location;

  @Before
  public void beforeTest() {
    try {
      Timber.e("@Before: register idle resource");
      idlingResource = new OnMapReadyIdlingResource(rule.getActivity());
      Espresso.registerIdlingResources(idlingResource);
      onView(withId(android.R.id.content)).check(matches(isDisplayed()));
      mapboxMap = idlingResource.getMapboxMap();
      locationLayerPlugin = rule.getActivity().getLocationLayerPlugin();
    } catch (IdlingResourceTimeoutException idlingResourceTimeoutException) {
      Timber.e("Idling resource timed out. Couldn't not validate if map is ready.");
      throw new RuntimeException("Could not start executeLocationLayerTest for "
        + this.getClass().getSimpleName() + ".\n The ViewHierarchy doesn't contain a view with resource id ="
        + "R.id.mapView or \n the Activity doesn't contain an instance variable with a name equal to mapboxMap.\n");
    }
    location = new Location("test");
    location.setLatitude(1.0);
    location.setLongitude(2.0);
  }

  @Test
  public void sanity() throws Exception {
    assertTrue(mapboxMap != null);
    assertTrue(locationLayerPlugin != null);
  }

  @Test
  public void locationSourceAdded() throws Exception {
    executeLocationLayerTest(new LocationLayerPluginAction.onPerformLocationLayerAction() {
      @Override
      public void onLocationLayerAction(LocationLayerPlugin locationLayerPlugin, MapboxMap mapboxMap,
                                        UiController uiController, Context context) {
        locationLayerPlugin.setLocationLayerStyle(LocationLayerStyle.NORMAL);
        assertTrue(mapboxMap.getSource(LOCATION_SOURCE) != null);
      }
    });
  }

  @Test
  public void locationTrackingLayersAdded() throws Exception {
    executeLocationLayerTest(new LocationLayerPluginAction.onPerformLocationLayerAction() {
      @Override
      public void onLocationLayerAction(LocationLayerPlugin locationLayerPlugin, MapboxMap mapboxMap,
                                        UiController uiController, Context context) {
        locationLayerPlugin.setLocationLayerStyle(LocationLayerStyle.NORMAL);
        assertTrue(mapboxMap.getLayer(ACCURACY_LAYER) != null);
        assertTrue(mapboxMap.getLayer(BACKGROUND_LAYER) != null);
        assertTrue(mapboxMap.getLayer(FOREGROUND_LAYER) != null);
      }
    });
  }

  @Test
  public void locationBearingLayersAdded() throws Exception {
    executeLocationLayerTest(new LocationLayerPluginAction.onPerformLocationLayerAction() {
      @Override
      public void onLocationLayerAction(LocationLayerPlugin locationLayerPlugin, MapboxMap mapboxMap,
                                        UiController uiController, Context context) {
        locationLayerPlugin.setLocationLayerStyle(LocationLayerStyle.COMPASS);
        assertTrue(mapboxMap.getLayer(ACCURACY_LAYER) != null);
        assertTrue(mapboxMap.getLayer(BACKGROUND_LAYER) != null);
        assertTrue(mapboxMap.getLayer(FOREGROUND_LAYER) != null);
        assertTrue(mapboxMap.getLayer(BEARING_LAYER) != null);
      }
    });
  }

  @Test
  public void locationNavigationLayersAdded() throws Exception {
    executeLocationLayerTest(new LocationLayerPluginAction.onPerformLocationLayerAction() {
      @Override
      public void onLocationLayerAction(LocationLayerPlugin locationLayerPlugin, MapboxMap mapboxMap,
                                        UiController uiController, Context context) {
        locationLayerPlugin.setLocationLayerStyle(LocationLayerStyle.COMPASS);
        assertTrue(mapboxMap.getLayer(NAVIGATION_LAYER) != null);
      }
    });
  }

  @Test
  public void locationLayerModeCorrectlySetToNone() throws Exception {
    executeLocationLayerTest(new LocationLayerPluginAction.onPerformLocationLayerAction() {
      @Override
      public void onLocationLayerAction(LocationLayerPlugin locationLayerPlugin, MapboxMap mapboxMap,
                                        UiController uiController, Context context) {
        locationLayerPlugin.setLocationLayerStyle(LocationLayerStyle.NORMAL);
        assertTrue(mapboxMap.getLayer(FOREGROUND_LAYER) != null);
        locationLayerPlugin.setLocationLayerEnabled(false);
        assertTrue(mapboxMap.getLayer(FOREGROUND_LAYER).getVisibility().getValue()
          .equals(Property.NONE));
      }
    });
  }

  @Test
  public void onMapChangeLocationLayerRedrawn() throws Exception {
    executeLocationLayerTest(new LocationLayerPluginAction.onPerformLocationLayerAction() {
      @Override
      public void onLocationLayerAction(LocationLayerPlugin locationLayerPlugin, MapboxMap mapboxMap,
                                        UiController uiController, Context context) {
        locationLayerPlugin.setLocationLayerStyle(LocationLayerStyle.NORMAL);
        assertTrue(mapboxMap.getLayer(FOREGROUND_LAYER) != null);
        mapboxMap.setStyleUrl(Style.SATELLITE);
        uiController.loopMainThreadForAtLeast(500);
        assertEquals(locationLayerPlugin.getLocationLayerStyle(), LocationLayerStyle.NORMAL);
        assertTrue(mapboxMap.getLayer(FOREGROUND_LAYER) != null);
        assertTrue(mapboxMap.getLayer(FOREGROUND_LAYER).getVisibility().getValue()
          .equals(Property.VISIBLE));
      }
    });
  }

  //
  // Stale state test
  //

  @Test
  public void whenStaleTimeSet_iconsDoChangeAtAppropriateTime() throws Exception {
    executeLocationLayerTest(new LocationLayerPluginAction.onPerformLocationLayerAction() {
      @Override
      public void onLocationLayerAction(LocationLayerPlugin locationLayerPlugin, MapboxMap mapboxMap,
                                        UiController uiController, Context context) {
        locationLayerPlugin.setLocationLayerStyle(LocationLayerStyle.NORMAL);
        SymbolLayer symbolLayer = mapboxMap.getLayerAs(FOREGROUND_LAYER);
        assert symbolLayer != null;
        assertThat(symbolLayer.getIconImage().getValue(), equalTo(FOREGROUND_ICON));
        locationLayerPlugin.applyStyle(LocationLayerOptions.builder(context).staleStateDelay(400).build());
        locationLayerPlugin.forceLocationUpdate(location);
        uiController.loopMainThreadForAtLeast(500);
        assertThat(symbolLayer.getIconImage().getValue(), equalTo(FOREGROUND_STALE_ICON));
      }
    });
  }

  @Test
  public void whenDrawableChanged_continuesUsingStaleIcons() throws Exception {
    executeLocationLayerTest(new LocationLayerPluginAction.onPerformLocationLayerAction() {
      @Override
      public void onLocationLayerAction(LocationLayerPlugin locationLayerPlugin, MapboxMap mapboxMap,
                                        UiController uiController, Context context) {
        locationLayerPlugin.setLocationLayerStyle(LocationLayerStyle.NORMAL);
        locationLayerPlugin.applyStyle(LocationLayerOptions.builder(context).staleStateDelay(100).build());
        locationLayerPlugin.forceLocationUpdate(location);
        uiController.loopMainThreadForAtLeast(200);
        rule.getActivity().toggleStyle();
        SymbolLayer symbolLayer = mapboxMap.getLayerAs(FOREGROUND_LAYER);
        assert symbolLayer != null;
        assertThat(symbolLayer.getIconImage().getValue(), not(FOREGROUND_ICON));
      }});
  }

  @After
  public void afterTest() {
    Timber.e("@After: unregister idle resource");
    Espresso.unregisterIdlingResources(idlingResource);
  }

  public void executeLocationLayerTest(LocationLayerPluginAction.onPerformLocationLayerAction listener) {
    onView(withId(android.R.id.content)).perform(new LocationLayerPluginAction(mapboxMap, locationLayerPlugin,
      listener));
  }
}
