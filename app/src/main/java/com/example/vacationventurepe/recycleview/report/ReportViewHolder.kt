package com.example.vacationventurepe.recycleview.report

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.vacationventurepe.R
import com.example.vacationventurepe.entity.Venture
import com.example.vacationventurepe.entity.VentureRecord
import com.example.vacationventurepe.interfaces.OnItemClickListener

class ReportViewHolder(
    itemView: View,
    onItemClickListener: OnItemClickListener,
    private val resourceContext: Context
) :
    ViewHolder(itemView) {

    private val vacationTypeTextView: TextView =
        itemView.findViewById(R.id.vacation_item_vacation_type_text_view)
    private val vacationNameTextView: TextView =
        itemView.findViewById(R.id.vacation_item_vacation_name_text_view)
    private val vacationBelongTextView: TextView =
        itemView.findViewById(R.id.vacation_item_vacation_belong)
    private val vacationDuringTextView: TextView =
        itemView.findViewById(R.id.vacation_item_vacation_during_text_view)
    private val vacationStateTextView: TextView =
        itemView.findViewById(R.id.vacation_item_state_text_view)
    private val vacationTipsTextView: TextView =
        itemView.findViewById(R.id.vacation_item_tips_text_view)

    init {
        itemView.setOnClickListener {
            if (adapterPosition != RecyclerView.NO_POSITION) {
                onItemClickListener.onItemClick(itemView, adapterPosition)

            }
        }
    }

    fun setVacationText(vacationEntity: Venture) {
        vacationEntity.apply {
            vacationTypeTextView.text = ventureType
            vacationNameTextView.text = ventureName
            vacationBelongTextView.text =
                "${ventureBelongCollege}-${ventureBelongSpec}-${ventureBelongClass}"
            vacationDuringTextView.text = "${ventureStartDate}到${ventureEndDate}"
            vacationStateTextView.text = ventureState
            resourceContext.apply {
                if (ventureState == getString(R.string.example_vacation_overdue)) {
                    vacationStateTextView.setTextColor(
                        getColor(R.color.overdue_maroon)
                    )
                    vacationTipsTextView.setTextColor(
                        getColor(R.color.overdue_maroon)
                    )
                    vacationTipsTextView.text = getString(R.string.click_to_check)
                }
            }

        }
    }

}