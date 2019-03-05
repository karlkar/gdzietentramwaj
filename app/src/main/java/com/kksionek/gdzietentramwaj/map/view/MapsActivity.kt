package com.kksionek.gdzietentramwaj.map.view

import android.Manifest
import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.annotation.RequiresApi
import android.support.annotation.StringRes
import android.support.annotation.UiThread
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.util.DiffUtil
import android.support.v7.util.DiffUtil.DiffResult.NO_POSITION
import android.support.v7.widget.ShareActionProvider
import android.support.v7.widget.Toolbar
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.ui.IconGenerator
import com.kksionek.gdzietentramwaj.BuildConfig
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.TramApplication
import com.kksionek.gdzietentramwaj.WARSAW_LATLNG
import com.kksionek.gdzietentramwaj.base.viewModel.ViewModelFactory
import com.kksionek.gdzietentramwaj.favorite.view.FavoriteLinesActivity
import com.kksionek.gdzietentramwaj.makeExhaustive
import com.kksionek.gdzietentramwaj.map.viewModel.MapsViewModel
import kotlinx.android.synthetic.main.bottom_sheet_difficulties.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

private const val MY_PERMISSIONS_REQUEST_LOCATION = 1234

private const val BUILD_VERSION_WELCOME_WINDOW_ADDED = 23
private const val PREF_LAST_VERSION = "LAST_VERSION"

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    //TODO change fav icon to vector drawable
    private lateinit var map: GoogleMap

    private var menuItemRefresh: MenuItemRefreshCtrl? = null
    private lateinit var menuItemFavoriteSwitch: MenuItem

    private var currentlyDisplayedTrams = emptyList<TramMarker>()

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

    private val iconGenerator by lazy { IconGenerator(this) }
    private val polylineGenerator = PolylineGenerator()
    private val tramPathAnimator = TramPathAnimator(polylineGenerator)

    private lateinit var viewModel: MapsViewModel
    private var favoriteView: LiveData<Boolean>? = null
    private val cameraMoveInProgress = AtomicBoolean(false)

    private fun showSuccessToast(text: String) {
        Toast.makeText(
            this,
            text,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showErrorToast(text: String) {
        Toast.makeText(
            this,
            text,
            Toast.LENGTH_LONG
        ).show()
    }

    private val tramDataObserver =
        Observer<UiState<BusTramLoading>> { uiState: UiState<BusTramLoading>? ->
            when (uiState) {
                is UiState.InProgress -> {
                    menuItemRefresh?.startAnimation()
                }
                is UiState.Error -> {
                    menuItemRefresh?.endAnimation()
                    showErrorToast(getString(uiState.message, *uiState.args.toTypedArray()))
                }
                is UiState.Success -> {
                    menuItemRefresh?.endAnimation()
                    val tramMarkerList = uiState.data.data
                    if (uiState.data.newData) {
                        showSuccessToast(getString(R.string.position_update_sucessful))
                    }
                    updateExistingMarkers(tramMarkerList, uiState.data.animate)
                }
                null -> {
                }
            }.makeExhaustive
        }

    private val favoriteModeObserver = Observer<Boolean?> { favorite ->
        favorite?.let {
            menuItemFavoriteSwitch.setIcon(
                if (it) R.drawable.fav_on else R.drawable.fav_off
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        (application as TramApplication).appComponent.inject(this)

        val myToolbar = findViewById<Toolbar>(R.id.toolbar_maps_toolbar)
        setSupportActionBar(myToolbar)

        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(MapsViewModel::class.java)

        viewModel.tramData.observe(this, tramDataObserver)

        viewModel.lastLocation.observe(this, Observer { location ->
            location?.let {
                reloadAds(it)
                if (this::map.isInitialized) {
                    val latLng = LatLng(it.latitude, it.longitude)
                    map.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                    map.isMyLocationEnabled = checkLocationPermission(false)
                    viewModel.lastLocation.removeObservers(this)
                }
            }
        })

        recyclerview_difficulties_difficulties.adapter = DifficultiesAdapter {
            startActivity(Intent(ACTION_VIEW, Uri.parse(it.link)))
        }

        imagebutton_difficulties_refresh_button.setOnClickListener {
            viewModel.forceReloadDifficulties()
        }

        viewModel.difficulties.observe(this, Observer { difficulties ->
            when (difficulties) {
                is UiState.Success -> {
                    stopDifficultiesLoading()
                    if (difficulties.data.isEmpty()) {
                        //TODO implement empty view
                    } else {
                        (recyclerview_difficulties_difficulties.adapter as DifficultiesAdapter).submitList(
                            difficulties.data
                        )
                    }
                }
                is UiState.Error -> {
                    stopDifficultiesLoading()
                }
                is UiState.InProgress -> {
                    startDifficultiesLoading()
                }
                null -> {
                }
            }.makeExhaustive
        })

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_maps_content) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        adProviderInterface.initialize(this, getString(R.string.adMobAppId))
        adProviderInterface.showAd(findViewById(R.id.adview_maps_adview))
        checkLocationPermission(true)

        handleWelcomeDialog()
    }

    private val reloadAnimation: Animation by lazy {
        AnimationUtils.loadAnimation(this, R.anim.anim_rotate)
    }

    private fun startDifficultiesLoading() {
        imagebutton_difficulties_refresh_button.isEnabled = false
        imagebutton_difficulties_refresh_button.startAnimation(reloadAnimation)
    }

    private fun stopDifficultiesLoading() {
        imagebutton_difficulties_refresh_button.isEnabled = true
        imagebutton_difficulties_refresh_button.clearAnimation()
    }

    private fun handleWelcomeDialog() {
        // TODO move this logic to ViewModel
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val lastVersion = sharedPreferences.getInt(PREF_LAST_VERSION, 0)
        if (lastVersion < BUILD_VERSION_WELCOME_WINDOW_ADDED) {
            showAboutAppDialog()
        }
        sharedPreferences.edit()
            .putInt(PREF_LAST_VERSION, BuildConfig.VERSION_CODE)
            .apply()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
        adProviderInterface.resume()
    }

    override fun onPause() {
        adProviderInterface.pause()
        viewModel.onPause()
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
        favoriteView = viewModel.favoriteView.also {
            it.observe(this, favoriteModeObserver)
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_info -> showAboutAppDialog()
            R.id.menu_item_refresh -> viewModel.forceReloadTrams()
            R.id.menu_item_remove_ads -> removeAds()
            R.id.menu_item_rate -> rateApp()
            R.id.menu_item_favorite -> {
                val intent = Intent(applicationContext, FavoriteLinesActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_item_favorite_switch -> viewModel.toggleFavorite()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    @UiThread
    private fun updateExistingMarkers(
        tramMarkerList: List<TramMarker>,
        animate: Boolean
    ) {
        if (cameraMoveInProgress.get()) {
            return
        }
        if (animate) {
            tramPathAnimator.removeAllAnimatedMarkers()
        }
        val diffCallback = MapsViewModel.DiffCallback(currentlyDisplayedTrams, tramMarkerList)
        val diffResult = DiffUtil.calculateDiff(diffCallback, false)

        for (i in (currentlyDisplayedTrams.size - 1) downTo 0) {
            if (diffResult.convertOldPositionToNew(i) == NO_POSITION) {
                currentlyDisplayedTrams[i].apply {
                    tramPathAnimator.removeMarker(this)
                    remove()
                }
            }
        }

        for (i in 0..(tramMarkerList.size - 1)) {
            val tramMarker = tramMarkerList[i]
            if (diffResult.convertNewPositionToOld(i) == NO_POSITION) {
                if (tramMarker.marker == null) {
                    tramMarker.marker = map.addMarker(
                        MarkerOptions()
                            .position(tramMarker.finalPosition) // if the markers blink - this is the reason - prevPosition should be here, but then new markers appear at the previous position instead of final
                            .icon(
                                TramMarker.getBitmap(
                                    tramMarker.tramLine,
                                    iconGenerator
                                )
                            )
                    )
                }
                if (tramMarker.polyline == null) {
                    val newPoints = polylineGenerator.generatePolylinePoints(
                        tramMarker.finalPosition,
                        tramMarker.prevPosition
                    )
                    tramMarker.polyline = map.addPolyline(
                        PolylineOptions()
                            .color(Color.argb(255, 236, 57, 57))
                            .width(TramMarker.POLYLINE_WIDTH)
                    ).apply { points = newPoints }
                }
            }
            tramPathAnimator.addMarker(tramMarker)
        }

        currentlyDisplayedTrams = tramMarkerList
        if (animate) {
            tramPathAnimator.startAnimation()
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
                CameraUpdateFactory.newLatLngZoom(WARSAW_LATLNG, 15f)
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

            setOnMarkerClickListener { true }
            setOnCameraMoveStartedListener { cameraMoveInProgress.set(true) }
            setOnCameraIdleListener {
                viewModel.visibleRegion = projection.visibleRegion.latLngBounds
                cameraMoveInProgress.set(false)
            }
            viewModel.mapControls.observe(this@MapsActivity, Observer {
                when (it) {
                    is MapControls.ZoomIn -> animateCamera(CameraUpdateFactory.zoomIn())
                    is MapControls.MoveTo -> animateCamera(CameraUpdateFactory.newLatLng(it.location))
                }
            })
        }

        viewModel.forceReloadLastLocation()
    }

    private fun reloadAds(location: Location) {
        adProviderInterface.loadAd(applicationContext, location)
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        viewModel.forceReloadLastLocation()
    }

    private fun showAboutAppDialog() {
        val view = createDialogView(R.string.disclaimer) ?: return
        AlertDialog.Builder(this)
            .setTitle(R.string.about_app)
            .setView(view)
            .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun removeAds() {
        val view = createDialogView(R.string.remove_info) ?: return
        AlertDialog.Builder(this)
            .setTitle(R.string.remove_title)
            .setView(view)
            .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    @SuppressLint("InflateParams")
    @Suppress("DEPRECATION")
    private fun createDialogView(@StringRes textId: Int): View? {
        val view = LayoutInflater.from(this)
            .inflate(R.layout.dialog_info, null)
        view.findViewById<TextView>(R.id.info_dialog_text)?.apply {
            movementMethod = LinkMovementMethod.getInstance()
            text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(getString(textId), Html.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(getString(textId))
            }
        }
        return view
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

    companion object {
        private const val TAG = "MapsActivity"
    }
}
