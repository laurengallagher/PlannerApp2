package org.wit.plannerapp2.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.card_planner.view.*
import org.wit.plannerapp2.R
import org.wit.plannerapp2.models.PlannerModel


interface PlannerListener {
    fun onPlannerClick(planner: PlannerModel)
}

class PlannerAdapter constructor(var planners: ArrayList<PlannerModel>,
                                  private val listener: PlannerListener)
    : RecyclerView.Adapter<PlannerAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        return MainHolder(
            LayoutInflater.from(parent?.context).inflate(
                R.layout.card_planner,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val planner = planners[holder.adapterPosition]
        holder.bind(planner,listener)
    }

    override fun getItemCount(): Int = planners.size

    fun removeAt(position: Int) {
        planners.removeAt(position)
        notifyItemRemoved(position)
    }

    class MainHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(planner: PlannerModel, listener: PlannerListener) {
            itemView.tag = planner
            itemView.paymentamount.text = planner.amount.toString()
            itemView.paymentmethod.text = planner.paymenttype
            itemView.imageIcon.setImageResource(R.mipmap.ic_launcher_planner_round)
            itemView.setOnClickListener { listener.onPlannerClick(planner) }
        }
    }
}