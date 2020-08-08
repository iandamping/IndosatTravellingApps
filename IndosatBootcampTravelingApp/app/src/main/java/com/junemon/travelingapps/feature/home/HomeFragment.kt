package com.junemon.travelingapps.feature.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.junemon.core.presentation.PresentationConstant.placeRvCallback
import com.junemon.core.presentation.di.factory.viewModelProvider
import com.junemon.core.presentation.util.interfaces.LoadImageHelper
import com.junemon.core.presentation.util.interfaces.RecyclerHelper
import com.junemon.core.presentation.util.interfaces.ViewHelper
import com.junemon.model.domain.PlaceCacheData
import com.junemon.model.domain.Results
import com.junemon.model.presentation.dto.mapCacheToPresentation
import com.junemon.travelingapps.R
import com.junemon.travelingapps.base.BasePlaceFragment
import com.junemon.travelingapps.databinding.FragmentHomeBinding
import com.junemon.travelingapps.feature.home.slideradapter.HomeSliderAdapter
import com.junemon.travelingapps.vm.PlaceViewModel
import kotlinx.android.synthetic.main.item_recyclerview.view.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Ian Damping on 04,December,2019
 * Github https://github.com/iandamping
 * Indonesia.
 */
class HomeFragment : BasePlaceFragment() {
    @Inject
    lateinit var viewAdapter: HomeSliderAdapter

    @Inject
    lateinit var viewHelper: ViewHelper

    @Inject
    lateinit var loadingImageHelper: LoadImageHelper

    @Inject
    lateinit var recyclerViewHelper: RecyclerHelper

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var placeVm: PlaceViewModel

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var pageSize: Int = 0
    private var currentPage = 0

    override fun createView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        placeVm = viewModelProvider(viewModelFactory)
        return binding.root
    }

    override fun viewCreated(view: View, savedInstanceState: Bundle?) {
        placeVm.startRunningViewPager()
        binding.run {
            initData()
            initView()
        }
    }

    override fun destroyView() {
        fireSignOut()
        placeVm.stopRunningViewPager()
        _binding = null
    }



    override fun activityCreated() {
        placeVm.setRunningForever.observe(viewLifecycleOwner, Observer {
            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                while (it) {
                    if (currentPage == pageSize) {
                        currentPage = 0
                    }
                    delay(4000L)
                    if (_binding != null) {
                        binding.vpPlaceRandom.setCurrentItem(currentPage++, true)
                    }
                }
            }
        })

        // placeVm.getUserProfile().observe(viewLifecycleOwner, Observer {userResult ->
        //     when(userResult){
        //         is Results.Success ->{
        //             if (userResult.data.getDisplayName() ==null){
        //                 fireSignIn()
        //             }
        //            Timber.e("name ${ userResult.data.getDisplayName()}")
        //         }
        //         is Results.Error ->{
        //             Timber.e("name ${ userResult.exception}")
        //         }
        //     }
        // })
    }

    private fun FragmentHomeBinding.initView() {
        loadingImageHelper.run {
            tbImageLogo.loadWithGlide(
                requireContext().resources.getDrawable(R.drawable.samarinda_logo, null)
            )
        }
        btnCreate.setOnClickListener {
            findNavController()
                .navigate(HomeFragmentDirections.actionHomeFragmentToUploadFragment(null))
        }
        lnSeeAllPlaceCultureType.setOnClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToPaginationFragment(getString(R.string.place_culture))
            )
        }
        lnSeeAllPlaceNatureType.setOnClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToPaginationFragment(getString(R.string.place_nature))
            )
        }
        lnSeeAllPlaceReligiusType.setOnClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToPaginationFragment(getString(R.string.place_religi))
            )
        }
        btnSearchMain.setOnClickListener {

            findNavController()
                .navigate(HomeFragmentDirections.actionHomeFragmentToSearchFragment())
        }
    }

    private fun FragmentHomeBinding.initData() {
        placeVm.getCache().observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Results.Success -> {
                    stopAllShimmer()
                    initViewPager(result.data)
                    initRecyclerView(result.data)
                }
                is Results.Error -> {
                    stopAllShimmer()
                    initViewPager(result.cache)
                    initRecyclerView(result.cache)
                }
                is Results.Loading -> {
                    startAllShimmer()
                }
            }
        })
    }

    private fun FragmentHomeBinding.initViewPager(result: List<PlaceCacheData>?) {
        ilegallStateCatching {
            checkNotNull(result)
            check(result.isNotEmpty())

            pageSize = if (result.size > 10) {
                10
            } else result.size

            viewAdapter.addData(result.mapCacheToPresentation().shuffled().take(10))
            vpPlaceRandom.adapter = viewAdapter
            indicator.setViewPager(vpPlaceRandom)

        }
    }

    private fun FragmentHomeBinding.initRecyclerView(result: List<PlaceCacheData>?) {
        universalCatching {
            checkNotNull(result)
            check(result.isNotEmpty())
            val religiData = result.mapCacheToPresentation()
                .filter { it.placeType == "Wisata Religi" }
            val natureData =
                result.mapCacheToPresentation().filter { it.placeType == "Wisata Alam" }
            val cultureData = result.mapCacheToPresentation()
                .filter { it.placeType == "Wisata Budaya" }

            recyclerViewHelper.run {

                rvPlaceNatureType.setUpHorizontalListAdapter(
                    items = natureData, diffUtil = placeRvCallback,
                    bindHolder = {
                        loadingImageHelper.run { ivItemPlaceImage.loadWithGlide(it?.placePicture) }
                        tvItemPlaceName.text = it?.placeName
                        tvItemPlaceDistrict.text = it?.placeDistrict
                    },
                    layoutResId = R.layout.item_recyclerview, itemClick = {

                        findNavController().navigate(
                            HomeFragmentDirections.actionHomeFragmentToDetailFragment(
                                gson.toJson(
                                    this
                                )
                            )
                        )
                    }
                )
                rvPlaceCultureType.setUpHorizontalListAdapter(
                    items = cultureData, diffUtil = placeRvCallback,
                    bindHolder = {
                        loadingImageHelper.run { ivItemPlaceImage.loadWithGlide(it?.placePicture) }
                        tvItemPlaceName.text = it?.placeName
                        tvItemPlaceDistrict.text = it?.placeDistrict
                    },
                    layoutResId = R.layout.item_recyclerview, itemClick = {

                        findNavController().navigate(
                            HomeFragmentDirections.actionHomeFragmentToDetailFragment(
                                gson.toJson(
                                    this
                                )
                            )
                        )
                    }
                )
                rvPlaceReligiusType.setUpHorizontalListAdapter(
                    items = religiData, diffUtil = placeRvCallback,
                    bindHolder = {
                        loadingImageHelper.run { ivItemPlaceImage.loadWithGlide(it?.placePicture) }
                        tvItemPlaceName.text = it?.placeName
                        tvItemPlaceDistrict.text = it?.placeDistrict
                    },
                    layoutResId = R.layout.item_recyclerview, itemClick = {

                        findNavController().navigate(
                            HomeFragmentDirections.actionHomeFragmentToDetailFragment(
                                gson.toJson(
                                    this
                                )
                            )
                        )
                    }
                )
            }
        }
    }

    private fun FragmentHomeBinding.stopAllShimmer() {
        viewHelper.run {
            shimmerSlider.stopShimmer()
            shimmerSlider.hideShimmer()
            shimmerSlider.gone()
            vpPlaceRandom.visible()

            shimmerCultureType.stopShimmer()
            shimmerCultureType.hideShimmer()
            shimmerCultureType.gone()
            rvPlaceCultureType.visible()

            shimmerNatureType.stopShimmer()
            shimmerNatureType.hideShimmer()
            shimmerNatureType.gone()
            rvPlaceNatureType.visible()

            shimmerReligiusType.stopShimmer()
            shimmerReligiusType.hideShimmer()
            shimmerReligiusType.gone()
            rvPlaceReligiusType.visible()
        }
    }

    private fun FragmentHomeBinding.startAllShimmer() {
        viewHelper.run {
            shimmerSlider.visible()
            shimmerSlider.startShimmer()

            shimmerCultureType.visible()
            shimmerCultureType.startShimmer()

            shimmerNatureType.visible()
            shimmerNatureType.startShimmer()

            shimmerReligiusType.visible()
            shimmerReligiusType.startShimmer()
        }
    }

    private fun fireSignIn() {
        lifecycleScope.launch {
            val signInIntent = placeVm.initSignIn()
            startActivityForResult(
                signInIntent,
                15
            )
        }
    }

    private fun fireSignOut() {
        lifecycleScope.launch {
            placeVm.initLogout {
                Timber.e("success logout")
            }
        }
    }
}