package com.example.vacationventurepe.recycleview.report

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.vacationventurepe.R
import com.example.vacationventurepe.entity.Venture
import com.example.vacationventurepe.interfaces.OnItemClickListener

class ReportAdapter(
    private val onItemClickListener: OnItemClickListener,
    private val context: Context,
    private val vacationList:List<Venture>
) : RecyclerView.Adapter<ReportViewHolder>() {

    private val TAG = this.javaClass.name

    //DS: DEBUG 测试用例
    private val unloadText: List<String> = mutableListOf<String>().apply {
        repeat(50) {
            this.add("Unload")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(parent.context)
        val itemView: View =
            inflater.inflate(R.layout.activity_main_item_layout, parent, false)

        return ReportViewHolder(itemView, onItemClickListener, context)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.setVacationText(vacationList[position])
    }

//        holder.textView.text = unloadText[position]

//        val minAlpha = 0.3f
//        val maxAlpha = 1f
//
//        val itemsCount = itemCount
//
//        val nowAlpha = maxAlpha-(position/itemsCount.toFloat())*(maxAlpha-minAlpha)
//
//        holder.itemView.alpha = nowAlpha


    override fun getItemCount(): Int {
        return vacationList.size
    }


}