package com.junemon.travelingapps.feature.home.slideradapter

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.junemon.model.presentation.PlaceCachePresentation
import com.junemon.travelingapps.R
import com.junemon.core.presentation.util.interfaces.LoadImageHelper
import com.junemon.travelingapps.presentation.util.interfaces.ViewHelper
import kotlinx.android.synthetic.main.item_slider.view.*

/**
 * Created by Ian Damping on 05,December,2019
 * Github https://github.com/iandamping
 * Indonesia.
 */
class HomeSliderAdapter(
    private val data: List<PlaceCachePresentation>,
    private val viewHelper: ViewHelper,
    private val loadImageHelper: LoadImageHelper
) : PagerAdapter() {
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val views = viewHelper.run { container.inflates(R.layout.item_slider) }
        loadImageHelper.run { views.ivSliderImage.loadWithGlide(data[position].placePicture) }
        views.tvPlaceName.text = data[position].placeName
        views.tvPlaceAddress.text = data[position].placeAddres
        views.ivSliderImage?.setOnClickListener {
            // it.findNavController().navigate(MovieFragmentDirections.actionHomeFragmentToDetailMovieFragment(data[position].id!!))
        }
        container.addView(views)
        return views
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount() = data.size
}