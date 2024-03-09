package com.example.teknikinterview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventAdapter(val eventList:ArrayList<CalendarData>, ):
    RecyclerView.Adapter<EventAdapter.EventHolder>() {
    class EventHolder(view: View):RecyclerView.ViewHolder(view) {
        fun bind(event:CalendarData){
            itemView.findViewById<TextView>(R.id.tvEventname).text = event.calendarDesc1
            itemView.findViewById<TextView>(R.id.tvDates).text =
                "${event.calendarStDate.toString()} - ${event.calendarEndDate.toString()}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.upcoming_event_row_layout,parent,false)
        return EventHolder(view)
    }

    override fun getItemCount(): Int {
    return eventList.count()
    }

    override fun onBindViewHolder(holder: EventHolder, position: Int) {
        holder.bind(eventList[position])
    }
}