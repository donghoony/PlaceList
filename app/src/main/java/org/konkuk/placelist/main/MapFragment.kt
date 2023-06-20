package org.konkuk.placelist.main

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
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
import com.google.android.gms.tasks.CancellationTokenSource
import org.konkuk.placelist.MyViewModel
import org.konkuk.placelist.R
import org.konkuk.placelist.databinding.FragmentMapBinding
import org.konkuk.placelist.domain.Place

class MapFragment : Fragment(), OnMapReadyCallback {
    lateinit var mMap: GoogleMap
    private val model : MyViewModel by activityViewModels()
    var binding : FragmentMapBinding? = null
    var marker : Marker? = null
    var radius = 100.0
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var cancellationTokenSource: CancellationTokenSource? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(layoutInflater,container,false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        radius = model.detectRange.value!!.toDouble()
        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mMap.clear()
        model.setRange(100f)
        binding = null
    }

    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        radius = model.detectRange.value!!.toDouble()
        Log.i("Radius", "Map radius $radius")
        val iconBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_pin_red)
        val resizedIcon = Bitmap.createScaledBitmap(iconBitmap, 60, 72, false)
        val pinIcon: BitmapDescriptor = BitmapDescriptorFactory.fromBitmap(resizedIcon)
        val circleOptions = CircleOptions()
            .radius(radius)
            .fillColor(0x22ff5959)
            .strokeColor(0xffff5959.toInt())
            .strokeWidth(5f)
        val markerOptions = MarkerOptions().icon(pinIcon)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.requireActivity())
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        cancellationTokenSource = CancellationTokenSource()
        Log.i("Mapfrag", "TAG : ${requireParentFragment().tag}")
        if (requireParentFragment().tag != "EditPlace")
            fusedLocationProviderClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, cancellationTokenSource!!.token)
                .addOnSuccessListener { location ->
                    val lat = location.latitude
                    val lng = location.longitude
                    Log.d("GmapViewFragment", "Lat: $lat, lon: $lng")
                    val currentLocation = LatLng(lat, lng)
                    marker = mMap.addMarker(markerOptions.position(currentLocation))
                    mMap.addCircle(circleOptions.center(currentLocation).radius(radius))
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                    model.setLiveData(currentLocation)
                }
        else{
            val place = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                requireParentFragment().requireArguments().getSerializable("place", Place::class.java)!!
            else
                requireParentFragment().requireArguments().getSerializable("place")!! as Place
            val currentLocation = LatLng(place.latitude.toDouble(), place.longitude.toDouble())
            marker = mMap.addMarker(markerOptions.position(currentLocation))
            mMap.addCircle(circleOptions.center(currentLocation).radius(radius))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
            model.setLiveData(currentLocation)
            model.setRange(place.detectRange)
        }

        model.location.observe(viewLifecycleOwner) {
            mMap.clear()
            marker = mMap.addMarker(markerOptions.position(it))
            mMap.addCircle(circleOptions.center(it).radius(radius))
            if(mMap.cameraPosition.zoom>=15.0f) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker!!.position, mMap.cameraPosition.zoom))
            }else {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker!!.position, 15.0f))
            }
        }

        mMap.setOnMapClickListener {
            Log.d(ContentValues.TAG, "onMapClick :" + it.latitude + it.longitude)
            mMap.clear()
            marker = mMap.addMarker(markerOptions.position(it))
            mMap.addCircle(circleOptions.center(it).radius(radius))
            model.setLiveData(it)
            if(mMap.cameraPosition.zoom>=15.0f) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker!!.position, mMap.cameraPosition.zoom))
            }else {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker!!.position, 15.0f))
            }
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

        model.detectRange.observe(viewLifecycleOwner){
            mMap.clear()
            radius = it.toDouble()
            val pos = model.location.value
            if (pos != null){
                mMap.addCircle(circleOptions.center(pos).radius(radius))
                mMap.addMarker(markerOptions.position(pos))
            }
        }
    }

}