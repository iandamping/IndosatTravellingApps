package com.junemon.travelingapps.feature.upload

import android.app.AlertDialog
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.junemon.core.di.module.CameraXFileDirectory
import com.junemon.core.presentation.PresentationConstant.RequestSelectGalleryImage
import com.junemon.core.presentation.di.factory.viewModelProvider
import com.junemon.core.presentation.util.interfaces.CommonHelper
import com.junemon.core.presentation.util.interfaces.ImageHelper
import com.junemon.core.presentation.util.interfaces.PermissionHelper
import com.junemon.core.presentation.util.interfaces.ViewHelper
import com.junemon.model.domain.PlaceRemoteData
import com.junemon.travelingapps.R
import com.junemon.travelingapps.base.BasePlaceFragment
import com.junemon.travelingapps.databinding.FragmentUploadBinding
import com.junemon.travelingapps.vm.PlaceViewModel
import java.io.File
import javax.inject.Inject
import kotlin.properties.Delegates

/**
 * Created by Ian Damping on 04,December,2019
 * Github https://github.com/iandamping
 * Indonesia.
 */
class UploadFragment : BasePlaceFragment() {
    @Inject
    lateinit var viewHelper: ViewHelper

    @Inject
    lateinit var imageHelper: ImageHelper

    @Inject
    lateinit var commonHelper: CommonHelper

    @Inject
    lateinit var permissionHelper: PermissionHelper

    private var _binding: FragmentUploadBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var placeVm: PlaceViewModel

    /** AndroidX navigation arguments */
    private val args: UploadFragmentArgs by navArgs()

    private val passedUri:String? by lazy {
        args.passedUri
    }

    private var isPermisisonGranted by Delegates.notNull<Boolean>()
    private var selectedUriForFirebase by Delegates.notNull<Uri>()
    private var placeType by Delegates.notNull<String>()
    private var placeCity by Delegates.notNull<String>()

    override fun createView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUploadBinding.inflate(inflater, container, false)
        placeVm = viewModelProvider(viewModelFactory)
        return binding.root
    }

    override fun viewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            initView()
        }
    }

    override fun destroyView() {
        _binding = null
    }

    override fun activityCreated() {
        permissionHelper.getAllPermission(requireActivity()) {
            isPermisisonGranted = it
        }

        if (passedUri!=null){
            val savedUri = Uri.parse(passedUri)
            val bitmap = when {
                Build.VERSION.SDK_INT < 28 -> MediaStore.Images.Media.getBitmap(
                    requireContext().contentResolver,
                    savedUri
                )
                else -> {
                    val source = ImageDecoder.createSource(requireContext().contentResolver, savedUri)
                    ImageDecoder.decodeBitmap(source)
                }
            }
            viewHelper.run {
                binding.btnUnggahFoto.gone()
                binding.tvInfoUpload.gone()
                binding.ivPickPhoto.visible()
            }
            binding.ivPickPhoto.setImageBitmap(bitmap)
        }
    }

    private fun FragmentUploadBinding.initView() {
        val allTypeCategory: Array<String> =
            requireContext().resources?.getStringArray(R.array.place_type_items)!!
        val allDistrictCategory: Array<String> =
            requireContext().resources?.getStringArray(R.array.place_districts_items)!!
        val arrayTypeSpinnerAdapter: ArrayAdapter<String>? =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, allTypeCategory)
        val arrayDistrictSpinnerAdapter: ArrayAdapter<String>? =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                allDistrictCategory
            )
        arrayTypeSpinnerAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        arrayDistrictSpinnerAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        btnUnggahFoto.setOnClickListener {
            openGalleryAndCamera(isPermisisonGranted)
        }
        btnUnggah.setOnClickListener {
            uploadItem()
        }
        spPlaceDistrict.adapter = arrayDistrictSpinnerAdapter
        spPlaceType.adapter = arrayTypeSpinnerAdapter
    }

    private fun FragmentUploadBinding.uploadItem() {
        val placeName = etPlaceName.text.toString()
        val placeDetail = etPlaceDetail.text.toString()
        placeCity = spPlaceDistrict.selectedItem.toString()
        placeType = spPlaceType.selectedItem.toString()
        val placeAddress = etPlaceAddress.text.toString()
        when {
            placeName.isBlank() -> commonHelper.run { etPlaceName.requestError(getString(R.string.place_name_checker)) }
            placeDetail.isBlank() -> commonHelper.run { etPlaceDetail.requestError(getString(R.string.place_description_checker)) }
            placeType.isBlank() -> commonHelper.run { context?.myToast(getString(R.string.place_type_checker)) }
            placeCity.isBlank() -> commonHelper.run { context?.myToast(getString(R.string.place_district_checker)) }
            placeAddress.isBlank() -> commonHelper.run { etPlaceAddress.requestError(getString(R.string.place_address_checker)) }
            else -> {
                ilegallStateCatching {
                    setDialogShow(false)
                    placeVm.uploadFirebaseData(
                        data = PlaceRemoteData(
                            placeType = placeType,
                            placeName = placeName,
                            placeAddres = placeAddress,
                            placeDistrict = placeCity,
                            placeDetail = placeDetail,
                            placePicture = null
                        ), imageUri = selectedUriForFirebase, success = {
                            setDialogShow(it)
                            moveUp()
                        }, failed = { status, _ ->
                            setDialogShow(status)
                            moveUp()
                        })
                }
            }
        }
    }

    private fun moveUp() {
        findNavController().navigateUp()
    }

    private fun openGalleryAndCamera(status: Boolean) {
        universalCatching {
            require(status)
            val options = arrayOf("Buka Galeri", "Gunakan Kamera")
            AlertDialog.Builder(requireContext())
                .setItems(options) { dialog, which ->
                    when (which) {
                        0 -> imageHelper.openImageFromGallery(this)
                        1 -> {
                            findNavController().navigate(UploadFragmentDirections.actionUploadFragmentToOpenCameraFragment())
                        }
                    }
                    dialog.dismiss()
                }
                .show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == FragmentActivity.RESULT_OK) {
            if (requestCode == RequestSelectGalleryImage) {
                universalCatching {
                    requireNotNull(data)
                    requireNotNull(data.data)
                    selectedUriForFirebase = data.data!!
                    val bitmap = imageHelper.getBitmapFromGallery(requireContext(), data.data!!)
                    viewHelper.run {
                        binding.btnUnggahFoto.gone()
                        binding.tvInfoUpload.gone()
                        binding.ivPickPhoto.visible()
                    }
                    binding.ivPickPhoto.setImageBitmap(bitmap)
                }
            }
        }
    }
}