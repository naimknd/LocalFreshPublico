package com.example.localfresh.activitys.vendedor.paginaprincipal.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.localfresh.R
import com.example.localfresh.adapters.CategoryAdapter
import com.example.localfresh.databinding.EditarProductoVendedorLayoutBinding
import com.example.localfresh.model.vendedor.producto.EditProductRequest
import com.example.localfresh.utils.CategoryIconUtils
import com.example.localfresh.utils.ImageUtils
import com.example.localfresh.viewmodel.vendedor.paginaprincipal.EditarProductoViewModel
import com.yalantis.ucrop.UCrop
import java.io.File

class EditarProductoVendedorFragment : Fragment() {
    private var _binding: EditarProductoVendedorLayoutBinding? = null
    private val binding get() = _binding!!
    private val editarProductoViewModel: EditarProductoViewModel by viewModels()
    private var productId: Int = 0
    private var imageUri: Uri? = null
    private var imageFile: File? = null

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            val cropIntent = ImageUtils.getCropIntent(requireContext(), it, 1024, 1024)
            cropImageLauncher.launch(cropIntent)
        }
    }

    private val cropImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val resultUri = UCrop.getOutput(result.data!!)
            resultUri?.let { uri ->
                val compressedUri = ImageUtils.compressImage(requireContext(), uri, 1024, 1024, 100)
                compressedUri?.let { compressedUri ->
                    Glide.with(this).load(compressedUri).into(binding.productImageEdit)
                    imageFile = File(compressedUri.path!!)
                }
            }
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(result.data!!)
            cropError?.let {
                showToast(it.message ?: "Error desconocido al recortar la imagen", isError = true)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productId = arguments?.getInt("PRODUCT_ID") ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = EditarProductoVendedorLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
        setupObservers()
        setupListeners()
        setupCategorySelection()
        editarProductoViewModel.loadProductInfo(productId)
    }

    private fun setupObservers() {
        editarProductoViewModel.productInfo.observe(viewLifecycleOwner) { response ->
            response?.data?.let { product ->
                binding.productNameInputEdit.setText(product.name)
                binding.productDescriptionInputEdit.setText(product.description)
                binding.productPriceInputEdit.setText(product.price.toString())
                product.expiry_type?.let { setExpiryTypeChip(it) }
                binding.categoryCheckedTextView.setText(product.category, false)
                Glide.with(this).load(product.image_url).into(binding.productImageEdit)
            }
        }

        editarProductoViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            showToast(errorMessage, isError = true)
        }

        editarProductoViewModel.editProductResponse.observe(viewLifecycleOwner) { response ->
            response?.let {
                if (it.status == "success") {
                    showToast("Producto actualizado correctamente", isError = false)
                    parentFragmentManager.popBackStack()
                } else {
                    showToast(it.message, isError = true)
                }
            }
        }
    }

    private fun setupCategorySelection() {
        val categories = CategoryIconUtils.categoryNames
        val adapter = CategoryAdapter(requireContext(), categories)
        binding.categoryCheckedTextView.setAdapter(adapter)
        binding.categoryCheckedTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedCategory = categories[position]
            CategoryIconUtils.setCategoryIcon(requireContext(), binding.categoryCheckedTextView, selectedCategory, R.color.text_color)
        }
    }

    private fun setupListeners() {
        binding.changeImageButton.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.saveChangesButton.setOnClickListener {
            saveProduct()
        }
    }

    private fun saveProduct() {
        val name = binding.productNameInputEdit.text.toString()
        val description = binding.productDescriptionInputEdit.text.toString()
        val category = binding.categoryCheckedTextView.text.toString()
        val price = binding.productPriceInputEdit.text.toString().toDoubleOrNull()
        val expiryType = when (binding.expiryTypeChipGroup.checkedChipId) {
            R.id.chip_caducidad -> "caducidad"
            R.id.chip_consumo_preferente -> "consumo_preferente"
            else -> null
        }

        if (validateInputs(name, description, category, price, expiryType)) {
            val editProductRequest = EditProductRequest(
                product_id = productId,
                seller_id = 0,
                name = name,
                description = description,
                category = category,
                price = price!!,
                image_file = imageFile,
                expiry_type = expiryType!!
            )

            editarProductoViewModel.editProduct(editProductRequest)
        }
    }

    private fun validateInputs(name: String, description: String, category: String, price: Double?, expiryType: String?): Boolean {
        return when {
            name.isEmpty() || description.isEmpty() || category.isEmpty() || price == null || expiryType.isNullOrEmpty() -> {
                Toast.makeText(requireContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                false
            }
            expiryType != "caducidad" && expiryType != "consumo_preferente" -> {
                Toast.makeText(requireContext(), "Tipo de expiración inválido o no proporcionado.", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun setExpiryTypeChip(expiryType: String) {
        when (expiryType) {
            "caducidad" -> binding.expiryTypeChipGroup.check(R.id.chip_caducidad)
            "consumo_preferente" -> binding.expiryTypeChipGroup.check(R.id.chip_consumo_preferente)
        }
    }

    private fun showToast(message: String, isError: Boolean) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(productId: Int): EditarProductoVendedorFragment {
            val fragment = EditarProductoVendedorFragment()
            val args = Bundle()
            args.putInt("PRODUCT_ID", productId)
            fragment.arguments = args
            return fragment
        }
    }
}