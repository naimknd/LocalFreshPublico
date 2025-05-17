package com.example.localfresh.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.localfresh.databinding.BottomSheetCategoryFilterBinding

class CategoryFilterBottomSheet : BaseFilterBottomSheet<BottomSheetCategoryFilterBinding>() {

    private var onCategoriesSelected: ((List<String>) -> Unit)? = null
    private var selectedCategories: List<String> = emptyList()
    private var title: String = "Selecciona Categorías"

    // Para compatibilidad con código vendedor que usa interface listener
    interface CategoryListener {
        fun onCategoriesSelected(categories: List<String>)
    }
    private var categoryListener: CategoryListener? = null

    companion object {
        fun newInstance(selectedCategories: List<String>): CategoryFilterBottomSheet {
            return CategoryFilterBottomSheet().apply {
                this.selectedCategories = selectedCategories
            }
        }
    }

    fun setTitle(title: String): CategoryFilterBottomSheet {
        this.title = title
        return this
    }

    // Para código comprador (usa lambda)
    fun setOnCategoriesSelectedListener(listener: (List<String>) -> Unit): CategoryFilterBottomSheet {
        onCategoriesSelected = listener
        return this
    }

    // Para código vendedor (usa interface)
    fun setCategoryListener(listener: CategoryListener): CategoryFilterBottomSheet {
        categoryListener = listener
        return this
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCategoryFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        with(binding) {
            // Configurar título
            tvTitulo.text = title

            // Restaurar chips seleccionados
            restoreSelectedChips(chipGroupCategorias, selectedCategories)

            // Configurar botones
            btnApply.setOnClickListener {
                val selectedCategories = getSelectedChips(chipGroupCategorias)
                // Notificar a ambos tipos de listeners
                onCategoriesSelected?.invoke(selectedCategories)
                categoryListener?.onCategoriesSelected(selectedCategories)
                dismiss()
            }

            btnReset.setOnClickListener {
                chipGroupCategorias.clearCheck()
                onCategoriesSelected?.invoke(emptyList())
                categoryListener?.onCategoriesSelected(emptyList())
                dismiss()
            }

            btnCancel.setOnClickListener { dismiss() }
        }
    }
}