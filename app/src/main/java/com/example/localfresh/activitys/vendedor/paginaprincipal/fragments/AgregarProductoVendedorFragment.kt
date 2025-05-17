package com.example.localfresh.activitys.vendedor.paginaprincipal.fragments

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.localfresh.R
import com.example.localfresh.adapters.CategoryAdapter
import com.example.localfresh.databinding.AgregarProductoVendedorLayoutBinding
import com.example.localfresh.model.vendedor.producto.AddProductRequest
import com.example.localfresh.utils.CategoryIconUtils
import com.example.localfresh.viewmodel.vendedor.paginaprincipal.AgregarProductoViewModel
import com.yalantis.ucrop.UCrop
import java.io.File

class AgregarProductoVendedorFragment : Fragment() {
    private var _binding: AgregarProductoVendedorLayoutBinding? = null
    private val binding get() = _binding!!
    private var selectedImageUri: Uri? = null
    private val viewModel: AgregarProductoViewModel by viewModels()
    private var sellerId: Int = -1

    // Launcher for image selection and cropping
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { startCrop(it) }
    }

    private val cropImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        when (result.resultCode) {
            android.app.Activity.RESULT_OK -> UCrop.getOutput(result.data!!)?.let { uri ->
                binding.productImage.setImageURI(uri)
                selectedImageUri = uri
            }
            UCrop.RESULT_ERROR -> Toast.makeText(requireContext(),
                UCrop.getError(result.data!!)?.message ?: "Error al recortar la imagen",
                Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sellerId = arguments?.getInt("SELLER_ID", -1) ?: -1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = AgregarProductoVendedorLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }
            productImage.setOnClickListener { getContent.launch("image/*") }
            changeImageButton.setOnClickListener { getContent.launch("image/*") }
            submitProductButton.setOnClickListener { saveProduct() }
        }

        setupCategorySelection()

        // Observer setup
        viewModel.addProductSuccess.observe(viewLifecycleOwner) { if (it) handleSuccess() }
        viewModel.errorMessage.observe(viewLifecycleOwner) { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
    }

    private fun setupCategorySelection() {
        CategoryIconUtils.categoryNames.also { categories ->
            binding.categoryCheckedTextView.apply {
                setAdapter(CategoryAdapter(requireContext(), categories))
                setOnItemClickListener { _, _, position, _ ->
                    CategoryIconUtils.setCategoryIcon(requireContext(), this, categories[position], R.color.text_color)
                }
            }
        }
    }

    private fun startCrop(uri: Uri) {
        val destinationUri = Uri.fromFile(File(requireContext().cacheDir, "cropped_image_${System.currentTimeMillis()}.jpg"))
        val options = UCrop.Options().apply {
            setCompressionFormat(Bitmap.CompressFormat.JPEG)
            setCompressionQuality(100)
            withMaxResultSize(800, 800)
            setToolbarTitle("Recorta la imagen")

            val green = resources.getColor(R.color.green, null)
            setToolbarColor(green)
            setStatusBarColor(green)
            setActiveControlsWidgetColor(green)
        }

        cropImageLauncher.launch(UCrop.of(uri, destinationUri).withOptions(options).getIntent(requireContext()))
    }

    private fun saveProduct() {
        val name = binding.productNameInput.text.toString()
        val description = binding.productDescriptionInput.text.toString()
        val category = binding.categoryCheckedTextView.text.toString()
        val price = binding.productPriceInput.text.toString().toDoubleOrNull()
        val expiryType = when (binding.expiryTypeChipGroup.checkedChipId) {
            R.id.chip_caducidad -> "caducidad"
            R.id.chip_consumo_preferente -> "consumo_preferente"
            else -> null
        }

        if (!validateInputs(name, description, category, price, expiryType)) return

        requireContext().getSharedPreferences("LocalFreshPrefs", android.content.Context.MODE_PRIVATE)
            .getString("TOKEN", null)?.let { token ->
                viewModel.addProduct(
                    AddProductRequest(
                        seller_id = sellerId,
                        name = name,
                        description = description,
                        category = category,
                        price = price!!,
                        image_file = selectedImageUri?.path?.let { File(it) },
                        expiry_type = expiryType!!
                    ),
                    token
                )
            } ?: Toast.makeText(requireContext(), "No se encontró el token de autenticación", Toast.LENGTH_SHORT).show()
    }

    private fun validateInputs(name: String, description: String, category: String, price: Double?, expiryType: String?): Boolean {
        val errorMsg = when {
            name.isEmpty() || description.isEmpty() || category.isEmpty() || price == null || expiryType.isNullOrEmpty() ->
                "Por favor, completa todos los campos"
            expiryType != "caducidad" && expiryType != "consumo_preferente" ->
                "Tipo de expiración inválido o no proporcionado"
            selectedImageUri == null ->
                "Por favor, selecciona una imagen para el producto"
            else -> return true
        }

        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
        return false
    }

    private fun handleSuccess() {
        Toast.makeText(requireContext(), "Producto agregado exitosamente", Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(sellerId: Int) = AgregarProductoVendedorFragment().apply {
            arguments = Bundle().apply { putInt("SELLER_ID", sellerId) }
        }
    }
}
