package com.kksionek.gdzietentramwaj.map.view

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import androidx.annotation.DimenRes
import androidx.annotation.UiThread
import androidx.appcompat.widget.ShareActionProvider
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.kksionek.gdzietentramwaj.BuildConfig
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.TramApplication
import com.kksionek.gdzietentramwaj.base.observeNonNull
import com.kksionek.gdzietentramwaj.base.view.ImageLoader
import com.kksionek.gdzietentramwaj.base.viewModel.ViewModelFactory
import com.kksionek.gdzietentramwaj.main.viewModel.MainViewModel
import com.kksionek.gdzietentramwaj.makeExhaustive
import com.kksionek.gdzietentramwaj.map.model.DifficultiesState
import com.kksionek.gdzietentramwaj.map.viewModel.FollowedTramData
import com.kksionek.gdzietentramwaj.map.viewModel.MapsViewModel
import com.kksionek.gdzietentramwaj.showToast
import kotlinx.android.synthetic.main.bottom_sheet_difficulties.*
import kotlinx.android.synthetic.main.fragment_map.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private val mainViewModel: MainViewModel by viewModels(
            factoryProducer = { viewModelFactory },
            ownerProducer = { requireActivity() }
    )
    private val mapsViewModel: MapsViewModel by viewModels(factoryProducer = { viewModelFactory })

    private lateinit var menuItemFavoriteSwitch: MenuItem
    private var menuItemRefresh: MenuItemRefreshCtrl? = null

    private var displaysOldIcons = false

    private val polylineGenerator = PolylineGenerator()
    private val tramPathAnimator = TramPathAnimator(polylineGenerator)
    private lateinit var difficultiesBottomSheet: DifficultiesBottomSheet

    private lateinit var vehicleInfoWindowAdapter: VehicleInfoWindowAdapter

    private val cameraMoveInProgress = AtomicBoolean(false)

    private var currentlyDisplayedTrams = emptyList<TramMarker>()

    @Inject
    internal lateinit var viewModelFactory: ViewModelFactory

    @Inject
    internal lateinit var imageLoader: ImageLoader

    private val shareIntent: Intent by lazy {
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
            )
        }
    }

    private val tramDataObserver: (UiState<BusTramLoading>) -> Unit = { uiState ->
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
                if (mapsViewModel.favoriteView.value == true && uiState.data.data.isEmpty()) {
                    showToast(R.string.map_error_no_favorites_visible)
                } else if (uiState.data.newData) {
                    showToast(R.string.map_position_update_sucessful)
                }
                updateExistingMarkers(tramMarkerList, uiState.data.animate)
            }
        }.makeExhaustive
    }

    private val locationPermissionObserver: (Boolean) -> Unit = { permissionGranted ->
        mapsViewModel.reloadLastLocation()
        if (this::map.isInitialized) {
            @Suppress("MissingPermission")
            map.isMyLocationEnabled = permissionGranted
            setSwitchButtonMargin()
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

        displaysOldIcons = mapsViewModel.isOldIconSetEnabled
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFollowedTramView()

        loadMap()

        mapsViewModel.apply {
            tramData.observeNonNull(viewLifecycleOwner, tramDataObserver)
            favoriteView.observeNonNull(viewLifecycleOwner, favoriteModeObserver)
            difficulties.observeNonNull(viewLifecycleOwner, difficultiesObserver)
        }
        if (mainViewModel.locationPermissionGrantedStatus.value != true) {
            mainViewModel.locationPermissionGrantedStatus.observeNonNull(
                viewLifecycleOwner,
                locationPermissionObserver
            )
        }

        setSwitchButtonMargin()
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

    private fun setupFollowedTramView() {
        val followedTramData = mapsViewModel.followedVehicle
        if (followedTramData == null) {
            hideFollowedView(animate = false)
        } else {
            showFollowedView(followedTramData)
        }

        map_followed_cancel_button.setOnClickListener {
            mapsViewModel.followedVehicle = null
            hideFollowedView()
        }
    }

    private fun loadMap() {
        if (!this::map.isInitialized) {
            (childFragmentManager.findFragmentById(R.id.map_fragment) as? SupportMapFragment)
                ?.getMapAsync(this)
        }
    }

    private fun setSwitchButtonMargin() {
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
            context?.let { context ->
                menuItemRefresh = MenuItemRefreshCtrl(context, it)
                if (mapsViewModel.tramData.value is UiState.InProgress) {
                    menuItemRefresh?.startAnimation()
                }
            }
        }

        val menuShare = menu.findItem(R.id.menu_item_share)
        val shareActionProvider =
            MenuItemCompat.getActionProvider(menuShare) as ShareActionProvider

        shareActionProvider.setShareIntent(shareIntent)

        menuItemFavoriteSwitch = menu.findItem(R.id.menu_item_favorite_switch)
        setFavoriteButtonIcon(mapsViewModel.favoriteView.value ?: false)
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
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed(
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
            activity?.let {
                setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        it.applicationContext,
                        R.raw.map_style
                    )
                )
            }
            uiSettings?.apply {
                isTiltGesturesEnabled = false
                isZoomControlsEnabled = true
            }
            isBuildingsEnabled = false
            isIndoorEnabled = false
            isTrafficEnabled = mapsViewModel.isTrafficShowingEnabled
            @SuppressLint("MissingPermission")
            isMyLocationEnabled = mainViewModel.locationPermissionGrantedStatus.value ?: false
            setSwitchButtonMargin()
            mapType = mapsViewModel.getMapType().googleCode

            setInfoWindowAdapter(vehicleInfoWindowAdapter)

            setOnMarkerClickListener {
                if (mapsViewModel.isBrigadeShowingEnabled) {
                    it.showInfoWindow()
                }
                return@setOnMarkerClickListener true
            }
            setOnInfoWindowClickListener {
                val followedTramData = it.tag as FollowedTramData
                mapsViewModel.followedVehicle = followedTramData
                showFollowedView(followedTramData)
                it.hideInfoWindow()
            }
            setOnCameraMoveStartedListener { cameraMoveInProgress.set(true) }
            setOnCameraIdleListener {
                mapsViewModel.visibleRegion = projection.visibleRegion.latLngBounds
                cameraMoveInProgress.set(false)
            }
        }

        mapsViewModel.mapControls.observeNonNull(this) {
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

        mapsViewModel.reloadLastLocation()
    }

    private fun showFollowedView(marker: FollowedTramData) {
        map_followed_constraintlayout.animate()
            .y(0f)
            .setDuration(1000L)
            .setInterpolator(BounceInterpolator())
            .start()
        map_followed_textview.text =
            getString(R.string.map_followed_text, marker.title, marker.snippet)
    }

    private fun hideFollowedView(animate: Boolean = true) {
        if (animate) {
            map_followed_constraintlayout.animate()
                .y(-map_followed_constraintlayout.height.toFloat())
                .setInterpolator(BounceInterpolator())
                .setDuration(1000L)
                .start()
        } else {
            map_followed_constraintlayout.apply {
                post {
                    y = -height.toFloat()
                    visibility = View.VISIBLE
                }
            }
        }
    }

    @UiThread
    private fun updateExistingMarkers(
        tramMarkerList: List<TramMarker>,
        animate: Boolean
    ) {
        val context = context ?: return
        if (!::map.isInitialized || cameraMoveInProgress.get()) {
            return
        }
        if (animate) {
            tramPathAnimator.removeAllAnimatedMarkers()
        }

        val currentOldIconEnabledSetting = mapsViewModel.isOldIconSetEnabled
        if (displaysOldIcons != currentOldIconEnabledSetting) {
            TramMarker.clearCache()
            displaysOldIcons = currentOldIconEnabledSetting
            currentlyDisplayedTrams.forEach {
                tramPathAnimator.removeMarker(it)
                it.remove()
            }
            currentlyDisplayedTrams = emptyList()
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

        for (i in tramMarkerList.indices) {
            val tramMarker = tramMarkerList[i]
            if (diffResult.convertNewPositionToOld(i) == DiffUtil.DiffResult.NO_POSITION) {
                if (tramMarker.marker == null) {
                    val title = getString(R.string.marker_info_line, tramMarker.tramLine)
                    val snippet = getString(R.string.marker_info_brigade, tramMarker.brigade)

                    tramMarker.marker = map.addMarker(
                        MarkerOptions().apply {
                            position(tramMarker.finalPosition) // if the markers blink - this is the reason - prevPosition should be here, but then new markers appear at the previous position instead of final
                            title(title)
                            snippet(snippet)
                            icon(
                                TramMarker.getBitmap(
                                    tramMarker.tramLine,
                                    tramMarker.isTram,
                                    context,
                                    mapsViewModel.isOldIconSetEnabled
                                )
                            )
                            if (!currentOldIconEnabledSetting) {
                                anchor(0.5f, 0.8f)
                            }
                        }
                    ).apply {
                        tag = FollowedTramData(
                            tramMarker.id,
                            title,
                            snippet,
                            tramMarker.finalPosition
                        )
                    }
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