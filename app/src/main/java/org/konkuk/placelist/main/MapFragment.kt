package org.konkuk.placelist.main

import android.Manifest
import android.content.ContentValues
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
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
    lateinit var mLayout : View
    var mLocationManager : LocationManager? = null
    var mLocationListener : LocationListener? = null
    var marker : Marker? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding= FragmentMapBinding.inflate(layoutInflater,container,false)
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
        mLocationManager = this.requireActivity().getSystemService(LOCATION_SERVICE) as LocationManager
        mLocationListener = LocationListener { location ->
            val lat = location.latitude
            val lng = location.longitude
            Log.d("GmapViewFragment", "Lat: $lat, lon: $lng")
            val currentLocation = LatLng(lat, lng)
            marker = mMap.addMarker(MarkerOptions().position(currentLocation))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
            model.setLiveData(currentLocation)
        }

        model.location.observe(viewLifecycleOwner) {
            mMap.clear()
            marker = mMap.addMarker(MarkerOptions().position(LatLng(it.latitude, it.longitude)))!!
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker!!.position, 15.0f))
        }

        var mark : LatLng
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mark, 15.0f))

        mMap.setOnMapClickListener {
            Log.d(ContentValues.TAG, "onMapClick :" + it.latitude + it.longitude)
            mark = LatLng(it.latitude, it.longitude)
            mMap.clear()
            marker = mMap.addMarker(MarkerOptions().position(mark).title("이름"))!!
            model.setLiveData(mark)
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
