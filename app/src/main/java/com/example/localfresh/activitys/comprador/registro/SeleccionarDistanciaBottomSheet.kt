package com.example.localfresh.activitys.comprador.registro

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.localfresh.databinding.BottomSheetSeleccionarDistanciaBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SeleccionarDistanciaBottomSheet(
    private val onDistanciaSeleccionada: (Int) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSeleccionarDistanciaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetSeleccionarDistanciaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.seekBarDistancia.max = 5
        binding.seekBarDistancia.progress = 3
        binding.tvDistancia.text = "3 km"
        binding.etDistanciaManual.setText("3")

        binding.seekBarDistancia.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvDistancia.text = "$progress km"
                if (fromUser) {
                    binding.etDistanciaManual.setText(progress.toString())
                }
            }
            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })

        binding.etDistanciaManual.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val distancia = s.toString().toIntOrNull()
                if (distancia != null && distancia in 0..binding.seekBarDistancia.max) {
                    binding.seekBarDistancia.progress = distancia
                }
            }
        })

        binding.btnConfirmar.setOnClickListener {
            val distancia = binding.etDistanciaManual.text.toString().toIntOrNull()
                ?.coerceIn(0, binding.seekBarDistancia.max)
                ?: binding.seekBarDistancia.progress
            onDistanciaSeleccionada(distancia)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}