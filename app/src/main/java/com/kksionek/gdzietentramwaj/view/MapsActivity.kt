package com.kksionek.gdzietentramwaj.view

import android.Manifest
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.annotation.RequiresApi
import android.support.annotation.UiThread
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.ShareActionProvider
import android.support.v7.widget.Toolbar
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast

import com.crashlytics.android.Crashlytics
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.JsonSyntaxException
import com.google.maps.android.ui.IconGenerator
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException
import com.kksionek.gdzietentramwaj.BuildConfig
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.TramApplication
import com.kksionek.gdzietentramwaj.dataSource.TramData
import com.kksionek.gdzietentramwaj.dataSource.TramDataWrapper
import com.kksionek.gdzietentramwaj.viewModel.MainActivityViewModel
import com.kksionek.gdzietentramwaj.viewModel.ViewModelFactory

import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.atomic.AtomicBoolean

import javax.inject.Inject

private const val MY_PERMISSIONS_REQUEST_LOCATION = 1234

private const val MAX_VISIBLE_MARKERS = 50

private const val BUILD_VERSION_WELCOME_WINDOW_ADDED = 23
private const val PREF_LAST_VERSION = "LAST_VERSION"

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private val tramMarkerHashMap = mutableMapOf<String, TramMarker>()

    private var menuItemRefresh: MenuItemRefreshCtrl? = null
    private lateinit var menuItemFavoriteSwitch: MenuItem

    @Inject
    internal lateinit var adProviderInterface: AdProviderInterface

    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory

    private val shareIntent: Intent by lazy {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(
            Intent.EXTRA_TEXT,
            "https://play.google.com/store/apps/details?id=$packageName"
        )
        shareIntent
    }

    private lateinit var iconGenerator: IconGenerator

    private val polylineGenerator = PolylineGenerator()
    private val tramPathAnimator = TramPathAnimator(polylineGenerator)

    private lateinit var viewModel: MainActivityViewModel
    private var favoriteView: LiveData<Boolean>? = null
    private var favoriteTrams = listOf<String>()
    private val cameraMoveInProgress = AtomicBoolean(false)


    private fun showSuccessToast(text: Int) {
        Toast.makeText(
            this,
            text,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showSuccessToast(text: String) {
        Toast.makeText(
            this,
            text,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showErrorToast(text: Int) {
        Toast.makeText(
            this,
            text,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showErrorToast(text: String) {
        Toast.makeText(
            this,
            text,
            Toast.LENGTH_LONG
        ).show()
    }

    private val tramDataObserver = Observer<TramDataWrapper?> { tramDataWrapper ->
        tramDataWrapper!!
        if (tramDataWrapper is TramDataWrapper.InProgress) {
            menuItemRefresh?.startAnimation()
        } else {
            menuItemRefresh?.endAnimation()
        }

        when (tramDataWrapper) {
            is TramDataWrapper.Error -> {
                val throwable = tramDataWrapper.throwable
                when (throwable) {
                    is UnknownHostException, is SocketTimeoutException -> showErrorToast(R.string.error_internet)
                    else -> {
                        if (!BuildConfig.DEBUG
                            && throwable !is JsonSyntaxException
                            && throwable !is IllegalStateException
                            && throwable !is HttpException
                        ) {
                            Crashlytics.log("Error handled with a toast.")
                            Crashlytics.logException(throwable)
                        }
                        showErrorToast(
                            if (BuildConfig.DEBUG)
                                throwable.javaClass.simpleName + ": " + throwable.message
                            else
                                getString(R.string.error_ztm)
                        )
                    }
                }
            }
            is TramDataWrapper.Success -> {
                val tramDataHashMap = tramDataWrapper.tramDataHashMap
                if (tramDataHashMap.isEmpty()) {
                    showErrorToast(R.string.none_position_is_up_to_date)
                    return@Observer
                }
                showSuccessToast(
                    if (BuildConfig.DEBUG)
                        getString(
                            R.string.position_update_successful_amount,
                            tramDataHashMap.size
                        )
                    else
                        getString(R.string.position_update_sucessful)
                )
                updateExistingMarkers(tramDataHashMap)
                addNewMarkers(tramDataHashMap)
            }
        }
    }

    private val favoriteModeObserver = Observer<Boolean?> {
        if (it != null) {
            menuItemFavoriteSwitch.setIcon(
                if (it) R.drawable.fav_on else R.drawable.fav_off
            )
            updateMarkersVisibility()
        }
    }

    private val favoriteTramsObserver = Observer<List<String>> { strings ->
        favoriteTrams = strings ?: emptyList()
        updateMarkersVisibility()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        (application as TramApplication).appComponent.inject(this)

        val myToolbar = findViewById<Toolbar>(R.id.my_toolbar)
        setSupportActionBar(myToolbar)

        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(MainActivityViewModel::class.java)

        viewModel.tramData.observe(this, tramDataObserver)
        viewModel.getFavoriteTramsLiveData().observe(this, favoriteTramsObserver)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        adProviderInterface.initialize(this, getString(R.string.adMobAppId))
        adProviderInterface.showAd(findViewById(R.id.adView))
        if (checkLocationPermission(true)) {
            applyLastKnownLocation(true, false)
        } else {
            reloadAds(null)
        }

        iconGenerator = IconGenerator(this)

        handleWelcomeDialog()
    }

    private fun handleWelcomeDialog() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val lastVersion = sharedPreferences.getInt(PREF_LAST_VERSION, 0)
        if (lastVersion < BUILD_VERSION_WELCOME_WINDOW_ADDED) {
            showAboutAppDialog()
        }
        sharedPreferences.edit().putInt(PREF_LAST_VERSION, BuildConfig.VERSION_CODE).apply()
    }

    private fun applyLastKnownLocation(adView: Boolean, map: Boolean) {
        viewModel.getLastKnownLocation()
            .addOnSuccessListener(this) { location ->
                location?.let {
                    if (adView) {
                        reloadAds(it)
                    }
                    if (map) {
                        val latLng = LatLng(it.latitude, it.longitude)
                        this.map.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                    }
                }
            }
    }

    override fun onResume() {
        super.onResume()
        adProviderInterface.resume()
    }

    override fun onPause() {
        adProviderInterface.pause()
        super.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        menu.findItem(R.id.menu_item_refresh)?.also {
            menuItemRefresh = MenuItemRefreshCtrl(this, it)
            if (favoriteView?.value == true) {
                menuItemRefresh?.startAnimation()
            }
        }

        @Suppress("ConstantConditionIf")
        if (BuildConfig.FLAVOR == "paid") {
            val removeAds = menu.findItem(R.id.menu_item_remove_ads)
            removeAds.isVisible = false
        }

        val menuShare = menu.findItem(R.id.menu_item_share)
        val shareActionProvider =
            MenuItemCompat.getActionProvider(menuShare) as ShareActionProvider

        shareActionProvider.setShareIntent(shareIntent)

        menuItemFavoriteSwitch = menu.findItem(R.id.menu_item_favorite_switch)
        favoriteView = viewModel.isFavoriteViewEnabled().also {
            it.observe(this, favoriteModeObserver)
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when {
            item.itemId == R.id.menu_item_info -> showAboutAppDialog()
            item.itemId == R.id.menu_item_refresh -> viewModel.forceReload()
            item.itemId == R.id.menu_item_remove_ads -> removeAds()
            item.itemId == R.id.menu_item_rate -> rateApp()
            item.itemId == R.id.menu_item_favorite -> {
                val intent = Intent(applicationContext, FavoriteLinesActivity::class.java)
                startActivity(intent)
            }
            item.itemId == R.id.menu_item_favorite_switch -> viewModel.toggleFavorite()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    @UiThread
    private fun updateMarkersVisibility() {
        val onlyFavorites = favoriteView?.value ?: false
        tramMarkerHashMap.values.forEach {
            it.isFavoriteVisible = !onlyFavorites || it.tramLine in favoriteTrams
        }
        showOrZoom()
    }

    @UiThread
    private fun updateExistingMarkers(tramDataHashMap: Map<String, TramData>) {
        val visibleRegion: LatLngBounds = map.projection.visibleRegion.latLngBounds
        val iter = tramMarkerHashMap.entries.iterator()
        tramPathAnimator.removeAllAnimatedMarkers()
        while (iter.hasNext()) {
            val element = iter.next()
            val tramMarker = element.value
            val updatedData = tramDataHashMap[element.key]
            if (updatedData != null) {
                val prevPosition = tramMarker.finalPosition
                val newPosition = updatedData.latLng
                if (prevPosition == newPosition) {
                    continue
                }

                tramMarker.updatePosition(newPosition)

                if (!cameraMoveInProgress.get() && tramMarker.isFavoriteVisible) {
                    if (tramMarker.isOnMap(visibleRegion)) {
                        if (tramMarker.marker == null) {
                            tramMarker.marker = map.addMarker(
                                MarkerOptions()
                                    .position(tramMarker.prevPosition!!)
                                    .icon(
                                        TramMarker.getBitmap(
                                            tramMarker.tramLine,
                                            iconGenerator
                                        )
                                    )
                            )
                        }
                        if (tramMarker.polyline == null) {
                            tramMarker.polyline = map.addPolyline(
                                PolylineOptions()
                                    .add(tramMarker.prevPosition)
                                    .color(Color.argb(255, 236, 57, 57))
                                    .width(TramMarker.POLYLINE_WIDTH)
                            )
                        }
                        tramPathAnimator.addMarker(tramMarker)
                    } else {
                        tramMarker.remove()
                    }
                }
            } else {
                tramMarker.remove()
                iter.remove()
            }
        }
        tramPathAnimator.startAnimation()
    }

    @UiThread
    private fun addNewMarkers(tramDataHashMap: Map<String, TramData>) {
        val visibleRegion: LatLngBounds = map.projection.visibleRegion.latLngBounds
        val onlyFavorites = favoriteView?.value ?: false
        for ((key, value) in tramDataHashMap) {
            if (key !in tramMarkerHashMap) {
                val marker = TramMarker(value)
                tramMarkerHashMap[key] = marker
                marker.isFavoriteVisible = !onlyFavorites || marker.tramLine in favoriteTrams
                if (!cameraMoveInProgress.get()
                    && marker.isFavoriteVisible
                    && marker.isOnMap(visibleRegion)
                ) {
                    createNewFullMarker(marker)
                }
            }
        }
    }

    private fun createNewFullMarker(tramMarker: TramMarker) {
        if (tramMarker.marker == null) {
            tramMarker.marker = map.addMarker(
                MarkerOptions()
                    .position(tramMarker.finalPosition)
                    .icon(TramMarker.getBitmap(tramMarker.tramLine, iconGenerator))
            )
        }
        if (tramMarker.polyline == null) {
            val finalPosition = tramMarker.finalPosition
            val prevPosition = tramMarker.prevPosition
            val pointsList = polylineGenerator.generatePolylinePoints(finalPosition, prevPosition)

            tramMarker.polyline = map.addPolyline(
                PolylineOptions()
                    .color(Color.argb(255, 236, 57, 57))
                    .width(TramMarker.POLYLINE_WIDTH)
            ).apply { points = pointsList }
        }
    }

    @UiThread
    private fun checkLocationPermission(doRequest: Boolean): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (doRequest) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_LOCATION
                )
            }
            return false
        }
        return true
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.apply {
            moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(52.231841, 21.005940), 15f
                )
            )
            setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    applicationContext,
                    R.raw.map_style
                )
            )
            uiSettings?.apply {
                isTiltGesturesEnabled = false
                isZoomControlsEnabled = true
            }
            isBuildingsEnabled = false
            isIndoorEnabled = false
            isTrafficEnabled = false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            map.isMyLocationEnabled =
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            map.isMyLocationEnabled = true
        }

        if (checkLocationPermission(false)) {
            applyLastKnownLocation(false, true)
        }
        map.apply {
            setOnMarkerClickListener { true }
            setOnCameraMoveStartedListener { cameraMoveInProgress.set(true) }
            setOnCameraIdleListener {
                cameraMoveInProgress.set(false)
                showOrZoom()
            }
        }
    }

    private fun showOrZoom() {
        val markersToBeCreated = mutableListOf<TramMarker>()
        val visibleRegion = map.projection.visibleRegion.latLngBounds
        for (marker in tramMarkerHashMap.values) {
            if (marker.isFavoriteVisible && marker.isOnMap(visibleRegion)) {
                markersToBeCreated.add(marker)
                if (markersToBeCreated.size > MAX_VISIBLE_MARKERS) {
                    map.animateCamera(CameraUpdateFactory.zoomIn())
                    return
                }
            } else {
                marker.remove()
            }
        }
        markersToBeCreated.forEach { createNewFullMarker(it) }
    }

    private fun reloadAds(location: Location?) {
        fun createDefaultLocation(): Location {
            val loc = Location("")
            loc.latitude = 52.231841
            loc.longitude = 21.005940
            return loc
        }

        val adLocation = location ?: createDefaultLocation()
        adProviderInterface.loadAd(applicationContext, adLocation)
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    map.run { isMyLocationEnabled = true }
                    applyLastKnownLocation(true, true)
                } else {
                    map.run { isMyLocationEnabled = false }
                }
            }
        }
    }

    private fun showAboutAppDialog() {
        val marginSize = resources.getDimensionPixelSize(R.dimen.dialog_margin)
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            leftMargin = marginSize
            rightMargin = marginSize
            topMargin = marginSize
            bottomMargin = marginSize
        }

        val builder = AlertDialog.Builder(this)
        val text = TextView(this).apply {
            setText(R.string.disclaimer)
            movementMethod = LinkMovementMethod.getInstance()
            layoutParams = params
        }

        val container = FrameLayout(this).apply { addView(text) }

        builder.setTitle(R.string.about_app) // TODO Move this view creation to xml
            .setView(container)
            .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun removeAds() {
        val marginSize = resources.getDimensionPixelSize(R.dimen.dialog_margin)
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            leftMargin = marginSize
            rightMargin = marginSize
            topMargin = marginSize
            bottomMargin = marginSize
        }

        val builder = AlertDialog.Builder(this)
        val text = TextView(this).apply {
            setText(R.string.remove_info)
            movementMethod = LinkMovementMethod.getInstance()
            layoutParams = params
        }

        val container = FrameLayout(this).apply { addView(text) }

        builder.setTitle(R.string.remove_title) // TODO Move this view creation to xml
            .setView(container)
            .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun rateApp() {
        val uri = Uri.parse("market://details?id=$packageName")
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }
}
