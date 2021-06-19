package com.androidstrike.tera.ui.adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.androidstrike.tera.R
import com.androidstrike.tera.utils.IRecyclerItemClickListener
import kotlinx.android.synthetic.main.custom_history_item.view.*
import org.w3c.dom.Text

class HistoryAdapter(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener {

    var lecturerName: TextView
    var totalCounts: TextView
    var lecturerRating: TextView

    lateinit var iRecyclerItemClickListener: IRecyclerItemClickListener

    fun setClick(iRecyclerItemClickListener: IRecyclerItemClickListener){
        this.iRecyclerItemClickListener = iRecyclerItemClickListener
    }

    init {
        lecturerName = itemView.findViewById(R.id.tv_lecturer_name_history) as TextView
        totalCounts = itemView.findViewById(R.id.tv_raters_count) as TextView
        lecturerRating = itemView.findViewById(R.id.tv_history_rating) as TextView

        itemView.setOnClickListener(this)
    }


    override fun onClick(v: View?) {
        iRecyclerItemClickListener.onItemClickListener(v!!,adapterPosition)
    }
}