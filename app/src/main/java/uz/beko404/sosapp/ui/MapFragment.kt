package uz.beko404.sosapp.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.os.Looper
import android.os.Vibrator
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraBoundsOptions
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.style.atmosphere.generated.atmosphere
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.extension.style.layers.properties.generated.ProjectionName
import com.mapbox.maps.extension.style.projection.generated.projection
import com.mapbox.maps.extension.style.sources.generated.rasterDemSource
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.extension.style.terrain.generated.terrain
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.MapAnimationOptions.Companion.mapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.attribution.attribution
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.logo.logo
import com.mapbox.maps.plugin.scalebar.scalebar
import com.vmadalin.easypermissions.EasyPermissions
import uz.beko404.sosapp.Constants
import uz.beko404.sosapp.Pref
import uz.beko404.sosapp.R
import uz.beko404.sosapp.WarningDialog
import uz.beko404.sosapp.databinding.FragmentMapBinding
import uz.beko404.sosapp.viewBinding

class MapFragment : Fragment(R.layout.fragment_map) {
    private val binding by viewBinding { FragmentMapBinding.bind(it) }
    private lateinit var mapView: MapView
    private lateinit var map: MapboxMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var lastLocation: Location? = null
    private lateinit var requestLocationFinePermissionLauncher: ActivityResultLauncher<String>

    private var SIGNIFICANT_DISTANCE_THRESHOLD = 0
    private var SIGNIFICANT_TIME_THRESHOLD = 0L
    private var time = 0L

    private var lastKnownLocation: Location? = null

    private lateinit var audioManager: AudioManager
    var mediaPlayer: MediaPlayer? = null

    override fun onPause() {
        super.onPause()
        if (mediaPlayer != null)
            mediaPlayer?.release()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        listenForLocationChanges()
        time = System.currentTimeMillis()
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        SIGNIFICANT_DISTANCE_THRESHOLD = Pref.distance
        SIGNIFICANT_TIME_THRESHOLD = Pref.time*1000*60
        audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (!::mapView.isInitialized) {
            mapView = binding.mapView
            onMapReady(mapboxMap = mapView.mapboxMap)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        requestLocationFinePermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            Pref.locationPermissionFine = isGranted
            if (!isGranted) {
                startActivity(Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", requireContext().packageName, null)
                })
            }
        }
    }

    private fun onMapReady(mapboxMap: MapboxMap) {
        map = mapboxMap
        mapView.compass.enabled = false
        mapView.scalebar.enabled = false
        mapView.gestures.rotateEnabled = false
        mapView.gestures.scrollEnabled = true
        mapView.compass.visibility = false


        map.setCamera(
            CameraOptions.Builder().center(
                Point.fromLngLat(Constants.DEFAULT_LON, Constants.DEFAULT_LAT)
            ).zoom(12.0).build()
        )

        val style = Style.MAPBOX_STREETS

        map.loadStyle(
            (style(style) {
                initLocationComponent()
            })
        )

        map.setBounds(CameraBoundsOptions.Builder().maxZoom(18.0).minZoom(2.0).build())
        mapView.attribution.enabled = false
        mapView.logo.enabled = false
    }

    private fun initLocationComponent() {
        val locationComponentPlugin = mapView.location
        locationComponentPlugin.updateSettings {
            this.enabled = true
            this.locationPuck = LocationPuck2D(
                bearingImage = ImageHolder.from(
                    R.drawable.ic_marker
                ), scaleExpression = interpolate {
                    linear()
                    zoom()
                    stop {
                        literal(0.0)
                        literal(0.6)
                    }
                    stop {
                        literal(20.0)
                        literal(1.0)
                    }
                }.toJson()
            )
        }
    }

    private fun hasLocationPermissionFine() = EasyPermissions.hasPermissions(
        requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
    )

    private fun currentLocation() {
        if (!hasLocationPermissionFine()) {
            if (::requestLocationFinePermissionLauncher.isInitialized) {
                requestLocationFinePermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        } else if (isLocationEnabled(requireContext())) {
            flyTo(lastLocation!!.longitude, lastLocation!!.latitude)
            mapView.setPadding(0, 0, 0, 0)
        } else {
            getLocation()
        }
    }

    private fun flyTo(longitude: Double, latitude: Double) {
        map.flyTo(CameraOptions.Builder().center(
            Point.fromLngLat(
                longitude, latitude
            )
        ).zoom(15.0).build(), mapAnimationOptions {
            duration(1200)
        })
    }

    private fun isLocationEnabled(context: Context): Boolean {
        val locationMode = try {
            Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE)
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
            return false
        }
        return locationMode != Settings.Secure.LOCATION_MODE_OFF
    }

    private fun listenForLocationChanges() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
            .setWaitForAccurateLocation(true).build()

        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper()
            )
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            for (location in result.locations) {
                lastLocation = location
            }
            currentLocation()
            if (Pref.isLocationEnabled) {
                if (lastKnownLocation == null)
                    lastKnownLocation = lastLocation
                else {
                    if (System.currentTimeMillis() - time >= SIGNIFICANT_TIME_THRESHOLD)
                        isLocationChangedSignificantly()
                }
            }
        }
    }

    private fun isLocationChangedSignificantly() {
        time = System.currentTimeMillis()
        if (lastKnownLocation?.distanceTo(lastLocation!!)!! <= SIGNIFICANT_DISTANCE_THRESHOLD) {
            Toast.makeText(requireContext(), lastKnownLocation?.distanceTo(lastLocation!!)!!.toString(), Toast.LENGTH_SHORT).show()
            if (!Pref.isFind) {
                Pref.isFind = true
                playSound()
            }
        }
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    lastLocation = location
                    Pref.latitude = location.latitude.toFloat()
                    Pref.longitude = location.longitude.toFloat()
                } else {
                    Pref.latitude = Constants.DEFAULT_LAT.toFloat()
                    Pref.longitude = Constants.DEFAULT_LON.toFloat()
                    lastLocation = Location("").apply {
                        latitude = Constants.DEFAULT_LAT
                        longitude = Constants.DEFAULT_LAT
                    }
                }
            }
        }
    }

    private fun playSound() {
        val vibrator = requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(2000)
        showMessage()
            mediaPlayer = MediaPlayer.create(context, R.raw.sos_sound)
            mediaPlayer?.start()


        // Set maximum volume
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume / 2, 0)

        mediaPlayer?.setOnCompletionListener {
            mediaPlayer?.start()
            Toast.makeText(requireContext(), "Finish", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showMessage() {
        val dialog = WarningDialog.newInstance()
        dialog.onClickListener = {
            dialog.dismiss()
            mediaPlayer?.release()
            Pref.isFind = false
            lastKnownLocation = lastLocation
        }
        dialog.show(childFragmentManager, "error_dialog")
    }
}
