package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Tasks
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentRemindersBinding
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragmentDirections
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    //Objects of maps fragments
    private lateinit var map: GoogleMap
    private val TAG = SelectLocationFragment::class.java.simpleName
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var poi: Marker

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity!!)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        binding.continueButton.setOnClickListener { button ->
            onLocationSelected()
        }

        return binding.root
    }

    private fun onLocationSelected() {
        if(this::poi.isInitialized){
            _viewModel.latitude.value = poi.position.latitude
            _viewModel.longitude.value = poi.position.longitude
            _viewModel.reminderSelectedLocationStr.value = poi.title
            _viewModel.navigationCommand.value = NavigationCommand.Back
        }else{
            _viewModel.showToast.value = "No location selected"
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, "MAP READY")
        map = googleMap

        setMapStyle(map)
        enableMyLocation()
        setPoiClick(map)
    }

    private fun setPoiClick(map: GoogleMap){
        map.setOnPoiClickListener { it ->
            if(this::poi.isInitialized){
                poi.remove()
            }
            poi = map.addMarker(MarkerOptions()
                .position(it.latLng)
                .title(it.name)
            )
            poi.showInfoWindow()
        }

        map.setOnMapClickListener { it ->
            if(this::poi.isInitialized){
                poi.remove()
            }
            poi = map.addMarker(MarkerOptions()
                .position(it)
                .title("My location")
            )
        }
    }

    private fun isPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(
            context!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
            getDeviceLocation()
        }
        else {
            requestPermissions(
                arrayOf<String>(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        // Check if location permissions are granted and if so enable the
        // location data layer.
        Log.d(TAG, "onRequestPermissionsResult")
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation")
        try {
            val locationResult = fusedLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener(activity!!) { task ->
                if (task.isSuccessful) {
                    // Set the map's camera position to the current location of the device.
                    val lastKnownLocation = task.result
                    if (lastKnownLocation != null) {
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            LatLng(lastKnownLocation.latitude,
                                lastKnownLocation.longitude), 15f))
                    }else{
                        Log.d(TAG, "LastKnownLocation is null")
                        _viewModel.showToast.value = "Can not find current location. Please reload the map"
                    }
                } else {
                    Log.d(TAG, "Current location is null. Using defaults.")
                    _viewModel.showToast.value = "Can not find current location. Please reload the map"
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Exception: %s" , e)
        }
    }

    private fun setMapStyle(map: GoogleMap){
        try{
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    context,
                    R.raw.map_style
                )
            )
            if (!success){
                Log.e(TAG, "Style parsing failed")
            }
        }catch (e: Resources.NotFoundException){
            Log.e(TAG, "Can not find style. Error: ", e)
        }
    }

}

private const val REQUEST_LOCATION_PERMISSION = 1
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29