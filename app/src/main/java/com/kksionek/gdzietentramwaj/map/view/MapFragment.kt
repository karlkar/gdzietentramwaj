package com.kksionek.gdzietentramwaj.map.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.ShareActionProvider
import androidx.core.content.ContextCompat
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.kksionek.gdzietentramwaj.BuildConfig
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.TramApplication
import com.kksionek.gdzietentramwaj.WARSAW_LATLNG
import com.kksionek.gdzietentramwaj.base.view.ImageLoader
import com.kksionek.gdzietentramwaj.base.viewModel.ViewModelFactory
import com.kksionek.gdzietentramwaj.favorite.view.FavoriteLinesActivity
import com.kksionek.gdzietentramwaj.makeExhaustive
import com.kksionek.gdzietentramwaj.map.viewModel.MapsViewModel
import kotlinx.android.synthetic.main.bottom_sheet_difficulties.*
import kotlinx.android.synthetic.main.fragment_map.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

private const val MY_PERMISSIONS_REQUEST_LOCATION = 1234

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var viewModel: MapsViewModel

    private lateinit var menuItemFavoriteSwitch: MenuItem
    private var menuItemRefresh: MenuItemRefreshCtrl? = null

    private val polylineGenerator = PolylineGenerator()
    private val tramPathAnimator = TramPathAnimator(polylineGenerator)
    private lateinit var difficultiesBottomSheet: DifficultiesBottomSheet

    private val cameraMoveInProgress = AtomicBoolean(false)

    private var currentlyDisplayedTrams = emptyList<TramMarker>()

    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory

    @Inject
    internal lateinit var adProviderInterface: AdProviderInterface

    @Inject
    internal lateinit var imageLoader: ImageLoader

    private val shareIntent: Intent by lazy {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(
            Intent.EXTRA_TEXT,
            "https://play.google.com/store/apps/details?id=${activity!!.packageName}"
        )
        shareIntent
    }

    private fun showSuccessToast(text: String) {
        Toast.makeText(
            context!!.applicationContext,
            text,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showErrorToast(text: String) {
        Toast.makeText(
            context!!.applicationContext,
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

    @Suppress("MissingPermission")
    private val lastLocationObserver = Observer { location: Location? ->
        location?.let {
            reloadAds(it)
            if (this::map.isInitialized) {
                val latLng = LatLng(it.latitude, it.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                map.isMyLocationEnabled = checkLocationPermission(false)
            }
        }
    }

    private fun reloadAds(location: Location) {
        adProviderInterface.loadAd(context!!.applicationContext, location)
    }

    private val favoriteModeObserver = Observer<Boolean?> { favorite ->
        favorite?.let {
            setFavoriteButtonIcon(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity!!.application as TramApplication).appComponent.inject(this)
        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(MapsViewModel::class.java) // TODO make it fragment's viewmodel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!this::map.isInitialized) {
            (childFragmentManager.findFragmentById(R.id.map_fragment) as? SupportMapFragment)
                ?.getMapAsync(this)
        }

        viewModel.tramData.observe(this, tramDataObserver)
        viewModel.lastLocation.observe(this, lastLocationObserver)
        viewModel.favoriteView.observe(this, favoriteModeObserver)

        difficultiesBottomSheet = DifficultiesBottomSheet(
            constraintlayout_bottomsheet_rootview,
            context!!,
            this,
            viewModel,
            imageLoader
        )

        adProviderInterface.initialize(context!!, getString(R.string.adMobAppId))
        adProviderInterface.showAd(adview_maps_adview)
        checkLocationPermission(true)

        if (viewModel.shouldShowWelcomeDialog()) {
            showAboutAppDialog()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.main_menu, menu)
        menu.findItem(R.id.menu_item_refresh)?.also {
            menuItemRefresh = MenuItemRefreshCtrl(context!!, it)
            if (viewModel.tramData.value is UiState.InProgress) {
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
        setFavoriteButtonIcon(viewModel.favoriteView.value ?: false)
        return super.onCreateOptionsMenu(menu, menuInflater)
    }

    private fun setFavoriteButtonIcon(favoriteEnabled: Boolean) {
        if (::menuItemFavoriteSwitch.isInitialized) {
            menuItemFavoriteSwitch.setIcon(
                if (favoriteEnabled) R.drawable.fav_on else R.drawable.fav_off
            )
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_info -> showAboutAppDialog()
            R.id.menu_item_refresh -> viewModel.forceReloadTrams()
            R.id.menu_item_remove_ads -> removeAds()
            R.id.menu_item_rate -> rateApp()
            R.id.menu_item_settings -> {
                val handler = Handler()
                handler.post {
                    findNavController().navigate(R.id.action_nav_graph_to_settingsFragment)
                }
            }
            R.id.menu_item_favorite -> {
                val intent = Intent(
                    context!!.applicationContext,
                    FavoriteLinesActivity::class.java
                ) // TODO Change it to fragment
                startActivity(intent)
            }
            R.id.menu_item_favorite_switch -> viewModel.toggleFavorite()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
        adProviderInterface.resume()
    }

    override fun onPause() {
        viewModel.onPause()
        adProviderInterface.pause()
        super.onPause()
    }

    private fun showAboutAppDialog() {
        val view = createDialogView(R.string.disclaimer) ?: return
        AlertDialog.Builder(context!!)
            .setTitle(R.string.about_app)
            .setView(view)
            .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun removeAds() {
        val view = createDialogView(R.string.remove_info) ?: return
        AlertDialog.Builder(context!!)
            .setTitle(R.string.remove_title)
            .setView(view)
            .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    @SuppressLint("InflateParams")
    @Suppress("DEPRECATION")
    private fun createDialogView(@StringRes textId: Int): View? {
        val view = LayoutInflater.from(context)
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
        val uri = Uri.parse("market://details?id=${context!!.packageName}")
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=${context!!.packageName}")
                )
            )
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.apply {
            moveCamera(
                CameraUpdateFactory.newLatLngZoom(WARSAW_LATLNG, 15f)
            )
            setPadding(0, 0, 0, resources.getDimensionPixelOffset(R.dimen.map_zoom_offset))
            setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    activity!!.applicationContext,
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
            isMyLocationEnabled = checkLocationPermission(false)

            setOnMarkerClickListener {
                if (it.isInfoWindowShown) it.hideInfoWindow() else it.showInfoWindow()
                return@setOnMarkerClickListener true
            }
            setOnCameraMoveStartedListener { cameraMoveInProgress.set(true) }
            setOnCameraIdleListener {
                viewModel.visibleRegion = projection.visibleRegion.latLngBounds
                cameraMoveInProgress.set(false)
            }
        }

        viewModel.mapControls.observe(this, Observer {
            when (it) {
                is MapControls.ZoomIn -> map.animateCamera(CameraUpdateFactory.zoomIn())
                is MapControls.MoveTo -> map.animateCamera(CameraUpdateFactory.newLatLng(it.location))
            }
        })

        viewModel.forceReloadLastLocation()
    }

    private fun checkLocationPermission(doRequest: Boolean): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }

        if (ContextCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        viewModel.forceReloadLastLocation()
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @UiThread
    private fun updateExistingMarkers(
        tramMarkerList: List<TramMarker>,
        animate: Boolean
    ) {
        if (!::map.isInitialized) {
            return
        }
        if (cameraMoveInProgress.get()) {
            return
        }
        if (animate) {
            tramPathAnimator.removeAllAnimatedMarkers()
        }
        val diffCallback = TramDiffCallback(currentlyDisplayedTrams, tramMarkerList)
        val diffResult = DiffUtil.calculateDiff(diffCallback, false)

        for (i in (currentlyDisplayedTrams.size - 1) downTo 0) {
            if (diffResult.convertOldPositionToNew(i) == DiffUtil.DiffResult.NO_POSITION) {
                currentlyDisplayedTrams[i].apply {
                    tramPathAnimator.removeMarker(this)
                    remove()
                }
            }
        }

        for (i in 0..(tramMarkerList.size - 1)) {
            val tramMarker = tramMarkerList[i]
            if (diffResult.convertNewPositionToOld(i) == DiffUtil.DiffResult.NO_POSITION) {
                if (tramMarker.marker == null) {
                    val oldIconSet = viewModel.iconSettingsProvider.isOldIconSetEnabled()
                    tramMarker.marker = map.addMarker(
                        MarkerOptions().apply {
                            position(tramMarker.finalPosition) // if the markers blink - this is the reason - prevPosition should be here, but then new markers appear at the previous position instead of final
                            title(getString(R.string.brigade))
                            snippet(tramMarker.brigade)
                            icon(
                                TramMarker.getBitmap(
                                    tramMarker.tramLine,
                                    context!!,
                                    viewModel.iconSettingsProvider
                                )
                            )
                            if (!oldIconSet) {
                                anchor(0.5f, 0.8f)
                            }
                        }
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
}