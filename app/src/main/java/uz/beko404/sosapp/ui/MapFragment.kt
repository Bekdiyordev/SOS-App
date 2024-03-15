package uz.beko404.sosapp.ui

import androidx.fragment.app.Fragment


import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color.rgb
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraBoundsOptions
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.style.atmosphere.generated.atmosphere
import com.mapbox.maps.extension.style.layers.properties.generated.ProjectionName
import com.mapbox.maps.extension.style.projection.generated.projection
import com.mapbox.maps.extension.style.sources.generated.rasterDemSource
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.extension.style.terrain.generated.terrain
import com.mapbox.maps.plugin.animation.MapAnimationOptions.Companion.mapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.attribution.attribution
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.logo.logo
import com.mapbox.maps.plugin.scalebar.scalebar
import uz.beko404.sosapp.R
import uz.beko404.sosapp.databinding.FragmentMapBinding
import uz.beko404.sosapp.viewBinding

class MapFragment : Fragment(R.layout.fragment_map), PermissionsListener {
    private val binding by viewBinding { FragmentMapBinding.bind(it) }
    private lateinit var mapView: MapView
    private lateinit var map: MapboxMap
    private lateinit var permissionsManager: PermissionsManager



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!::mapView.isInitialized) {
            mapView = binding.mapView
            onMapReady(mapboxMap = mapView.getMapboxMap())
        }
    }

    private fun onMapReady(mapboxMap: MapboxMap) {
        map = mapboxMap
        mapView.compass.enabled = false
        mapView.scalebar.enabled = false
        mapView.gestures.rotateEnabled = false
        mapView.gestures.scrollEnabled = true

        map.setCamera(
            CameraOptions.Builder().center(
                Point.fromLngLat(location.longitude, location.latitude)
            )
                .zoom(if (sharedPref.latitude == 41.841812f && sharedPref.longitude == 60.391438f) 12.0 else 15.0)
                .build()
        )

        val style = when (sharedPref.mode) {
            "night" -> Style.DARK
            "light" -> Style.MAPBOX_STREETS
            else -> {
                when (requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_YES -> Style.DARK
                    Configuration.UI_MODE_NIGHT_NO -> Style.MAPBOX_STREETS
                    else -> Style.MAPBOX_STREETS
                }
            }
        }

        map.loadStyle(
            (style(styleUri = style) {
                mapView.gestures.addOnMoveListener(onMoveListener)
                initLocationComponent()
            })
        )

        mapView.location2.puckBearingSource = PuckBearingSource.HEADING
        mapView.location2.puckBearingSource = PuckBearingSource.COURSE
        map.setBounds(CameraBoundsOptions.Builder().maxZoom(18.0).minZoom(8.0).build())
        mapView.attribution.enabled = false
        mapView.logo.enabled = false

        launchAndRepeatWithViewLifecycle {
            fromLongitude = map.cameraState.center.longitude()
            fromLatitude = map.cameraState.center.latitude()
            sharedViewModel.setStartAddress(LatLng(fromLatitude, fromLongitude))
            viewModel.getStartAddressNameFromOpenStreet(fromLatitude, fromLongitude)
        }
    }







    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            // Permission granted, fly to current location

        } else {
            // Permission not granted, handle accordingly
            Toast.makeText(
                requireContext(),
                "Location permission is required to show current location",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
