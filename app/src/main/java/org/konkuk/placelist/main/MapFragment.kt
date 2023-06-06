package org.konkuk.placelist.main

import android.Manifest
import android.content.ContentValues
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.konkuk.placelist.MyViewModel
import org.konkuk.placelist.R
import org.konkuk.placelist.databinding.FragmentMapBinding

class MapFragment : Fragment(), OnMapReadyCallback {
    lateinit var mMap: GoogleMap
    private val model : MyViewModel by activityViewModels()
    var binding : FragmentMapBinding? = null
    var mLocationManager : LocationManager? = null
    var mLocationListener : LocationListener? = null
    var marker : Marker? = null
    var radius = 100.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(layoutInflater,container,false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mMap.clear()
        binding = null
    }

    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        val iconBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_pin_red)
        val resizedIcon = Bitmap.createScaledBitmap(iconBitmap, 60, 72, false)
        val pinIcon: BitmapDescriptor = BitmapDescriptorFactory.fromBitmap(resizedIcon)

        val circleOptions = CircleOptions()
            .radius(radius)
            .fillColor(0x22ff5959)
            .strokeColor(0xffff5959.toInt())
            .strokeWidth(5f)
        val markerOptions = MarkerOptions().icon(pinIcon)

        mLocationManager = this.requireActivity().getSystemService(LOCATION_SERVICE) as LocationManager
        mMap.setOnMarkerClickListener {
            when(radius){
                100.0 -> radius = 200.0
                200.0 -> radius = 500.0
                500.0 -> radius = 100.0
            }
            mMap.clear()
            val pos = LatLng(it.position.latitude, it.position.longitude)
            mMap.addCircle(circleOptions.center(pos).radius(radius))
            mMap.addMarker(markerOptions.position(pos))
            true
        }
        mLocationListener = LocationListener { location ->
            val lat = location.latitude
            val lng = location.longitude
            Log.d("GmapViewFragment", "Lat: $lat, lon: $lng")
            val currentLocation = LatLng(lat, lng)
            marker = mMap.addMarker(markerOptions.position(currentLocation))
            mMap.addCircle(circleOptions.center(currentLocation))

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
            model.setLiveData(currentLocation)
        }

        model.location.observe(viewLifecycleOwner) {
            mMap.clear()
            marker = mMap.addMarker(markerOptions.position(it))
            mMap.addCircle(circleOptions.center(it))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker!!.position, 15.0f))
        }


        mMap.setOnMapClickListener {
            Log.d(ContentValues.TAG, "onMapClick :" + it.latitude + it.longitude)
            mMap.clear()
            marker = mMap.addMarker(markerOptions.position(it))
            mMap.addCircle(circleOptions.center(it))
            model.setLiveData(it)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker!!.position, 15.0f))
        }

        if (ActivityCompat.checkSelfPermission(
                this.requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this.requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mLocationManager!!.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            3000L,
            30f,
            mLocationListener!!
        )

    }
}
