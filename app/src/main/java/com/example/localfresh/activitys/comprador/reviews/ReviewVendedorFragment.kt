package com.example.localfresh.activitys.comprador.reviews

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.localfresh.databinding.FragmentReviewVendedorBinding
import com.example.localfresh.viewmodel.comprador.reviews.ReviewViewModel

class ReviewVendedorFragment : Fragment() {
    private var _binding: FragmentReviewVendedorBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReviewViewModel by viewModels()

    private var userId: Int = -1
    private var sellerId: Int = -1
    private var reservationId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getInt("USER_ID", -1)
            sellerId = it.getInt("SELLER_ID", -1)
            reservationId = it.getInt("RESERVATION_ID", -1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewVendedorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupObservers()
    }

    private fun setupUI() {
        binding.submitReviewButton.setOnClickListener {
            val rating = binding.ratingBar.rating.toInt()
            val comment = binding.commentEditText.text.toString()

            if (rating < 1) {
                Toast.makeText(requireContext(), "Por favor, selecciona una calificación", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.submitReview(userId, sellerId, reservationId, rating, comment)
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.submitReviewButton.isEnabled = !isLoading
        }

        viewModel.submitReviewResult.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.fold(
                onSuccess = { response ->
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                    if (response.status == "success") {
                        requireActivity().supportFragmentManager.popBackStack()
                    }
                },
                onFailure = { error ->
                    Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(userId: Int, sellerId: Int, reservationId: Int) =
            ReviewVendedorFragment().apply {
                arguments = Bundle().apply {
                    putInt("USER_ID", userId)
                    putInt("SELLER_ID", sellerId)
                    putInt("RESERVATION_ID", reservationId)
                }
            }
    }
}