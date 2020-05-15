package com.kksionek.gdzietentramwaj.map.view

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DimenRes
import androidx.annotation.UiThread
import androidx.appcompat.widget.ShareActionProvider
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MapStyleOptions
import com.kksionek.gdzietentramwaj.BuildConfig
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.TramApplication
import com.kksionek.gdzietentramwaj.base.observeNonNull
import com.kksionek.gdzietentramwaj.base.view.ImageLoader
import com.kksionek.gdzietentramwaj.base.viewModel.ViewModelFactory
import com.kksionek.gdzietentramwaj.main.viewModel.MainViewModel
import com.kksionek.gdzietentramwaj.makeExhaustive
import com.kksionek.gdzietentramwaj.map.model.DifficultiesState
import com.kksionek.gdzietentramwaj.map.model.FollowedVehicleData
import com.kksionek.gdzietentramwaj.map.model.MapControls
import com.kksionek.gdzietentramwaj.map.model.UiState
import com.kksionek.gdzietentramwaj.map.model.VehicleLoadingResult
import com.kksionek.gdzietentramwaj.map.model.VehicleToDrawData
import com.kksionek.gdzietentramwaj.map.viewModel.MapsViewModel
import com.kksionek.gdzietentramwaj.showToast
import kotlinx.android.synthetic.main.bottom_sheet_difficulties.*
import kotlinx.android.synthetic.main.fragment_map.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private val mainViewModel: MainViewModel by viewModels(factoryProducer = { viewModelFactory })
    private val mapsViewModel: MapsViewModel by viewModels(factoryProducer = { viewModelFactory })

    private lateinit var menuItemFavoriteSwitch: MenuItem
    private var menuItemRefresh: MenuItemRefreshCtrl? = null

    private var displaysOldIcons = false

    private val polylineGenerator = PolylineGenerator()
    private val tramPathAnimator = TramPathAnimator(polylineGenerator)

    private lateinit var difficultiesBottomSheet: DifficultiesBottomSheet
    private lateinit var vehicleInfoWindowAdapter: VehicleInfoWindowAdapter
    private lateinit var followedView: FollowedView

    private val cameraMoveInProgress = AtomicBoolean(false)

    private var currentlyDisplayedVehicles = emptyList<VehicleMarker>()

    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory

    @Inject
    internal lateinit var imageLoader: ImageLoader

    @Inject
    internal lateinit var bitmapCache: BitmapCache

    private val shareIntent: Intent by lazy {
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
            )
        }
    }

    private val tramDataObserver: (UiState<VehicleLoadingResult>) -> Unit = { uiState ->
        when (uiState) {
            is UiState.InProgress -> {
                menuItemRefresh?.startAnimation()
            }
            is UiState.Error -> {
                menuItemRefresh?.endAnimation()
                showToast(getString(uiState.message, *uiState.args.toTypedArray()), true)
            }
            is UiState.Success -> {
                menuItemRefresh?.endAnimation()
                val tramMarkerList = uiState.data.data
                if (mapsViewModel.favoriteView.value == true && tramMarkerList.isEmpty()) {
                    showToast(R.string.map_error_no_favorites_visible)
                } else if (uiState.data.newData) {
                    showToast(R.string.map_position_update_sucessful)
                }
                updateMarkers(tramMarkerList, uiState.data.animate)
            }
        }.makeExhaustive
    }

    private val locationPermissionObserver: (Boolean) -> Unit = { permissionGranted ->
        mapsViewModel.reloadLastLocation()
        if (this::map.isInitialized) {
            @Suppress("MissingPermission")
            map.isMyLocationEnabled = permissionGranted
            setMapTypeSwitchTopMargin()
        }
    }

    private val favoriteModeObserver: (Boolean) -> Unit = { setFavoriteButtonIcon(it) }

    private val difficultiesObserver: (UiState<DifficultiesState>) -> Unit = {
        if (this::map.isInitialized) {
            val offset = when (it) {
                is UiState.Success -> {
                    if (it.data.isSupported) {
                        resources.getDimensionPixelOffset(R.dimen.map_zoom_offset)
                    } else {
                        0
                    }
                }
                else -> 0
            }
            map.setPadding(0, 0, 0, offset)
        }
    }

    private val mapControlsObserver: (MapControls) -> Unit = {
        when (it) {
            is MapControls.ZoomIn ->
                map.animateCamera(CameraUpdateFactory.zoomIn())
            is MapControls.IgnoredZoomIn ->
                showToast(it.data)
            is MapControls.MoveTo -> {
                if (it.customAnimationDuration) {
                    map.animateCamera(
                        CameraUpdateFactory.newLatLng(it.location),
                        ANIMATION_DURATION.toInt(),
                        null
                    )
                } else {
                    map.animateCamera(CameraUpdateFactory.newLatLng(it.location))
                }
            }
            is MapControls.ChangeType ->
                map.mapType = it.mapType.googleCode
        }.makeExhaustive
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
        (context.applicationContext as TramApplication).appComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadMap()

        followedView = FollowedView(
            requireContext(),
            map_followed_constraintlayout,
            mapsViewModel
        )

        mapsViewModel.apply {
            vehicleData.observeNonNull(viewLifecycleOwner, tramDataObserver)
            favoriteView.observeNonNull(viewLifecycleOwner, favoriteModeObserver)
            difficulties.observeNonNull(viewLifecycleOwner, difficultiesObserver)
        }
        if (mainViewModel.locationPermissionGrantedStatus.value != true) {
            mainViewModel.locationPermissionGrantedStatus.observeNonNull(
                viewLifecycleOwner,
                locationPermissionObserver
            )
        }

        setMapTypeSwitchTopMargin()
        map_switch_type_imagebutton.setOnClickListener {
            mapsViewModel.onSwitchMapTypeButtonClicked()
        }

        difficultiesBottomSheet = DifficultiesBottomSheet(
            constraintlayout_bottomsheet_rootview,
            view.context,
            this,
            mapsViewModel,
            imageLoader
        )

        vehicleInfoWindowAdapter = VehicleInfoWindowAdapter(view.context)

        mainViewModel.requestLocationPermission()
    }

    private fun loadMap() {
        if (!this::map.isInitialized) {
            (childFragmentManager.findFragmentById(R.id.map_fragment) as? SupportMapFragment)
                ?.getMapAsync(this)
        }
    }

    private fun setMapTypeSwitchTopMargin() {
        @DimenRes
        val marginForMapSwitchButton: Int =
            if (this::map.isInitialized && map.isMyLocationEnabled) {
                R.dimen.map_layer_margin_big
            } else {
                R.dimen.map_layer_margin_small
            }
        map_switch_type_imagebutton.layoutParams =
            (map_switch_type_imagebutton.layoutParams as ViewGroup.MarginLayoutParams).apply {
                topMargin = resources.getDimensionPixelOffset(marginForMapSwitchButton)
            }
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.main_menu, menu)

        menu.findItem(R.id.menu_item_refresh)?.also {
            menuItemRefresh = MenuItemRefreshCtrl(requireContext(), it)
            if (mapsViewModel.vehicleData.value is UiState.InProgress) {
                menuItemRefresh?.startAnimation()
            }
        }

        val menuShare = menu.findItem(R.id.menu_item_share)
        (MenuItemCompat.getActionProvider(menuShare) as ShareActionProvider).apply {
            setShareIntent(shareIntent)
        }

        menuItemFavoriteSwitch = menu.findItem(R.id.menu_item_favorite_switch)
        setFavoriteButtonIcon(favoriteEnabled = mapsViewModel.favoriteView.value == true)
        return super.onCreateOptionsMenu(menu, menuInflater)
    }

    override fun onDestroyOptionsMenu() {
        menuItemRefresh?.endAnimation()
        menuItemRefresh = null
        super.onDestroyOptionsMenu()
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
            R.id.menu_item_refresh -> mapsViewModel.forceReloadTrams()
            R.id.menu_item_rate -> rateApp()
            R.id.menu_item_settings -> {
                Handler().postDelayed(
                    {
                        findNavController().apply {
                            if (currentDestination?.id == R.id.destination_map) {
                                navigate(R.id.action_destinationMap_to_settingsFragment)
                            }
                        }
                    },
                    100
                )
            }
            R.id.menu_item_favorite_switch -> mapsViewModel.onToggleFavorite()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        mapsViewModel.onResume()
        if (this::map.isInitialized) {
            map.isTrafficEnabled = mapsViewModel.isTrafficShowingEnabled
        }
    }

    override fun onPause() {
        mapsViewModel.onPause()
        super.onPause()
    }

    private fun rateApp() {
        val uri = Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}")
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
                )
            )
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val cameraStartPosition = mapsViewModel.mapInitialPosition
        val cameraStartZoom = mapsViewModel.mapInitialZoom
        map.apply {
            moveCamera(
                CameraUpdateFactory.newLatLngZoom(cameraStartPosition, cameraStartZoom)
            )
            if (mapsViewModel.difficulties.value != null) {
                setPadding(0, 0, 0, resources.getDimensionPixelOffset(R.dimen.map_zoom_offset))
            }
            setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireActivity().applicationContext,
                    R.raw.map_style
                )
            )
            uiSettings?.apply {
                isTiltGesturesEnabled = false
                isZoomControlsEnabled = true
            }
            isBuildingsEnabled = false
            isIndoorEnabled = false
            isTrafficEnabled = mapsViewModel.isTrafficShowingEnabled
            @SuppressLint("MissingPermission")
            isMyLocationEnabled = mainViewModel.locationPermissionGrantedStatus.value ?: false
            setMapTypeSwitchTopMargin()
            mapType = mapsViewModel.getMapType().googleCode

            setInfoWindowAdapter(vehicleInfoWindowAdapter)

            setOnMarkerClickListener {
                if (mapsViewModel.isBrigadeShowingEnabled) {
                    it.showInfoWindow()
                }
                return@setOnMarkerClickListener true
            }
            setOnInfoWindowClickListener {
                val followedTramData = it.tag as FollowedVehicleData
                mapsViewModel.setFollowedVehicle(followedTramData)
                followedView.showFollowedView(followedTramData)
                it.hideInfoWindow()
            }
            setOnCameraMoveStartedListener { cameraMoveInProgress.set(true) }
            setOnCameraIdleListener {
                mapsViewModel.visibleRegion = projection.visibleRegion.latLngBounds
                cameraMoveInProgress.set(false)
            }
        }

        with(mapsViewModel) {
            mapControls.observeNonNull(viewLifecycleOwner, mapControlsObserver)
            reloadLastLocation()
        }
    }

    @UiThread
    private fun updateMarkers(
        vehiclesToDrawList: List<VehicleToDrawData>,
        animate: Boolean
    ) {
        if (!::map.isInitialized || cameraMoveInProgress.get()) {
            return
        }

        handleChangeOfIconsIfNeeded()
        handleStaleMarkers(vehiclesToDrawList)
        val vehicleMarkersToDraw = handleVisibleMarkers(vehiclesToDrawList)

        currentlyDisplayedVehicles = vehicleMarkersToDraw
        if (animate) {
            with(tramPathAnimator) {
                removeAllMarkers()
                addAllMarkers(vehicleMarkersToDraw)
                startAnimation()
            }
        }
    }

    private fun handleChangeOfIconsIfNeeded() {
        val currentOldIconEnabledSetting = mapsViewModel.isOldIconSetEnabled
        if (displaysOldIcons != currentOldIconEnabledSetting) {
            displaysOldIcons = currentOldIconEnabledSetting
            currentlyDisplayedVehicles.forEach {
                tramPathAnimator.removeMarker(it)
                it.remove()
            }
            currentlyDisplayedVehicles = emptyList()
            bitmapCache.clearCache()
        }
    }

    private fun handleStaleMarkers(vehiclesToDrawList: List<VehicleToDrawData>) {
        for (vehicleMarker in currentlyDisplayedVehicles) {
            if (vehiclesToDrawList.none { it.id == vehicleMarker.id }) {
                tramPathAnimator.removeMarker(vehicleMarker)
                vehicleMarker.remove()
            }
        }
    }

    private fun handleVisibleMarkers(
        vehiclesToDrawList: List<VehicleToDrawData>
    ): List<VehicleMarker> {
        val vehicleMarkersToDraw = mutableListOf<VehicleMarker>()
        for (vehicleToDraw in vehiclesToDrawList) {
            val indexOfExisting =
                currentlyDisplayedVehicles.indexOfFirst { it.id == vehicleToDraw.id }
            if (indexOfExisting != -1) {
                // update marker with new data
                val markerToUpdate = currentlyDisplayedVehicles[indexOfExisting].apply {
                    update(vehicleToDraw)
                }
                vehicleMarkersToDraw.add(markerToUpdate)
            } else {
                // create new marker
                val newMarker = VehicleMarker.Factory.createMarker(
                    requireContext(),
                    map,
                    vehicleToDraw,
                    mapsViewModel.isOldIconSetEnabled,
                    polylineGenerator,
                    bitmapCache
                )
                vehicleMarkersToDraw.add(newMarker)
            }
        }
        return vehicleMarkersToDraw
    }
}