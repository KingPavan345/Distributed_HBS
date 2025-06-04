package com.example.test_build_authentication

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/*
 * FilterDialogFragment.kt
 *
 * Dialog fragment for filtering vacation home listings by various criteria.
 * Handles user input and communicates selected filters back to the list activity.
 */

class FilterDialogFragment : BottomSheetDialogFragment() {
    private var filterListener: ((Map<String, String>) -> Unit)? = null
    private var initialFilters: Map<String, String> = emptyMap()

    companion object {
        fun newInstance(filters: Map<String, String>): FilterDialogFragment {
            val fragment = FilterDialogFragment()
            fragment.initialFilters = filters
            return fragment
        }
    }

    fun setFilterListener(listener: (Map<String, String>) -> Unit) {
        filterListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_filter, container, false)
        val countryAutoComplete = view.findViewById<android.widget.AutoCompleteTextView>(R.id.country_autocomplete)
        val minPriceEdit = view.findViewById<EditText>(R.id.min_price_edit)
        val maxPriceEdit = view.findViewById<EditText>(R.id.max_price_edit)
        val minBedroomsEdit = view.findViewById<EditText>(R.id.min_bedrooms_edit)
        val applyButton = view.findViewById<Button>(R.id.apply_button)
        val resetButton = view.findViewById<Button>(R.id.reset_button)

        val countries = resources.getStringArray(R.array.country_list)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, countries)
        countryAutoComplete.setAdapter(adapter)
        countryAutoComplete.threshold = 1

        // Restore initial value
        countryAutoComplete.setText(initialFilters["country"] ?: "", false)
        minPriceEdit.setText(initialFilters["min_price"] ?: "")
        maxPriceEdit.setText(initialFilters["max_price"] ?: "")
        minBedroomsEdit.setText(initialFilters["min_bedrooms"] ?: "")

        applyButton.setOnClickListener {
            val filters = mutableMapOf<String, String>()
            val countryInput = countryAutoComplete.text.toString().trim()
            // Try to match input to a country in the list (case-insensitive)
            val matchedCountry = countries.firstOrNull { it.equals(countryInput, ignoreCase = true) }
            val countryToSend = if (!matchedCountry.isNullOrBlank() && matchedCountry != "Other") matchedCountry else null
            if (!countryToSend.isNullOrBlank()) filters["country"] = countryToSend
            val minPrice = minPriceEdit.text.toString().trim()
            if (minPrice.isNotBlank()) filters["min_price"] = minPrice
            val maxPrice = maxPriceEdit.text.toString().trim()
            if (maxPrice.isNotBlank()) filters["max_price"] = maxPrice
            val minBedrooms = minBedroomsEdit.text.toString().trim()
            if (minBedrooms.isNotBlank()) filters["min_bedrooms"] = minBedrooms
            filterListener?.invoke(filters)
            dismiss()
        }
        resetButton.setOnClickListener {
            countryAutoComplete.setText("")
            minPriceEdit.text.clear()
            maxPriceEdit.text.clear()
            minBedroomsEdit.text.clear()
        }
        return view
    }
} 