package com.example.teknikinterview

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val calendarView: CalendarView = findViewById(R.id.calendarView)
        val eventArray = ArrayList<CalendarData>()
        val arrayTemp = ArrayList<CalendarData>()
        //API Post Request prerequisites
        val requestQueue = Volley.newRequestQueue(this)
        val url = "https://api.myeducare.ro/parent_api/parent_get_school_events.php"
        val params = HashMap<String, String>()
        params["school_group"] = "educare"
        params["db"] = "educare_2023_2024"
        params["student_idx"] = "8443"

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener { response ->
                try {
                    val jsonObject = JSONObject(response)
                    val jsonArray : JSONArray = jsonObject.getJSONArray("data")
                    val eventDatesCalendarArray = ArrayList<Calendar>()
                    var eventDatesArray = ArrayList<EventDay>()

                    for (i in 0 until jsonArray.length()) {
                        val jsonObject1 = jsonArray.getJSONObject(i)
                        val name = jsonObject1.getString("name")
                        val desc1 = jsonObject1.getString("desc1")
                        val text = jsonObject1.getString("text")
                        val start_date = jsonObject1.getString("start_date")
                        val end_date = jsonObject1.getString("end_date")
                        val eventDatesCalendar = getDatesBetween(start_date.toInt(),end_date.toInt())
                        for (event in eventDatesCalendar){
                            eventDatesCalendarArray.add(event)
                        }

                        val start_dateD = toDate(start_date)
                        val end_dateD = toDate(end_date)

                        val eventItem = CalendarData(name, desc1, text, eventDatesCalendar, start_dateD, end_dateD)
                        eventArray.add(eventItem)
                    }
                    eventDatesArray = numberOfEventsInDays(eventDatesCalendarArray)
                    calendarView.setEvents(eventDatesArray)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                Log.e("ERROR", error.toString())
            }) {
            override fun getParams(): Map<String, String> {
                return params
            }
        }
        requestQueue.add(stringRequest)


        calendarView.setOnDayClickListener(object : OnDayClickListener {
            //this is a method from Material Calendar View library allows setting an onclick listener
            override fun onDayClick(eventDay: EventDay) {
                val eventAdapter = EventAdapter(arrayTemp)
                val rvEventList = findViewById<RecyclerView>(R.id.rvEventList)
                //arrayTemp is cleared so that when another day is clicked the array will clear
                // and be filled with the corresponding data
                arrayTemp.clear()
                val clickedDayCalendar = eventDay.calendar
                for (event in eventArray){
                    for (date in event.calendarEventDatesCalendarData) {
                        if (compareDates(clickedDayCalendar.time,date.time)) {
                            //clicked day is checked against all the dates in the event array
                            //when the dates match the event is added to the array
                            arrayTemp.add(event)
                            //Recycler View is filled with items in the array
                            if (rvEventList != null) {
                                rvEventList.adapter = eventAdapter
                                rvEventList.layoutManager = LinearLayoutManager(this@MainActivity)
                            }
                        }
                    }
                }
                //Recycler View is filled with items in the array
                if (rvEventList != null) {
                    rvEventList.adapter = eventAdapter
                    rvEventList.layoutManager = LinearLayoutManager(this@MainActivity)
                }
            }
        })

    }

    private fun toDate(date:String): LocalDate{
        //By utilizing DateTimeFormatter this is used to convert the string date to LocalDate type
        val dateString = date
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        val localDate: LocalDate = LocalDate.parse(dateString, formatter)
        //Log.d("Burasi", localDate.toString())
        return localDate
    }
    private fun getDatesBetween(stDate: Int, endDate:Int):ArrayList<Calendar>{
        //This function is used to determine if we have a date range or a singular date
        val calendarArray = ArrayList<Calendar>()
        val deltaDate = endDate - stDate
        //By checking the delta of the dates we determine if there is a difference between them
        //if not we simply add the startDate to our calendar array and return it
        if(deltaDate == 0)
        {
            val calendar = Calendar.getInstance()
            val setDate = toDate(stDate.toString())
            calendar.set(setDate.year,setDate.monthValue-1,setDate.dayOfMonth)
            calendarArray.add(calendar)
            return calendarArray
        }else{
            //if the delta is not zero then that means we have a date range
            val startDateLD = toDate(stDate.toString())
            val endDateLD = toDate(endDate.toString())
            //below here we use chronounit to calculate how many days there are in between the dates
            // and using generateDates function to return the dates in between in a List
            val numOfDays = ChronoUnit.DAYS.between(startDateLD, endDateLD)
            val listOfDates = generateDates(startDateLD,numOfDays.toInt())
            for (date in listOfDates) {
                val calendar = Calendar.getInstance()
                calendar.set(date.year,date.monthValue-1,date.dayOfMonth)
                calendarArray.add(calendar)
            }
            return calendarArray
        }
    }
    private fun generateDates(startDate: LocalDate, numOfDays: Int): List<LocalDate> {
        // the dates in between is put in a LocalDate type List and returned
        return generateSequence(startDate) { it.plusDays(1) }
            .take(numOfDays + 1)
            .toList()
    }
    private fun numberOfEventsInDays(eventDates: ArrayList<Calendar>): ArrayList<EventDay> {
        //this function is used to add the drawables depending on the amount of events happening in that day
        //the amount is determined by using dupedays function
        val eventArray = ArrayList<EventDay>()
        val dupeVal = dupeDays(eventDates)
        var idDrawable = 0
        for (event in dupeVal){
            idDrawable = amountOfEvents(event.value)
            eventArray.add(EventDay(event.key,
                ResourcesCompat.getDrawable(resources,idDrawable,null)!!
            ))
        }
        return eventArray


    }
    private fun dupeDays(eventDates:ArrayList<Calendar>): HashMap<Calendar, Int>{
        // this function is used to count duplicate event dates and assigns a number depending on the
        //count utilizing hashmap
        val hash = HashMap<Calendar,Int>()
        val hashDate = HashMap<Date,Int>()
        var i = 0
        for (event in eventDates){
            var detected = false
            for (temp in hashDate.keys){
                if (compareDates(temp,event.time)){
                    hashDate[temp] = hashDate[temp]!!+1
                    detected = true
                    break
                }
            }
            if (!detected){
                hashDate[event.time] = 1
            }
        }
        for (hashI in hashDate){
            hash.put(calendarDate(hashI.key),hashI.value)
        }
        return hash
    }
    private fun amountOfEvents(i:Int):Int{
        //depending on the number i a drawable will be returned
        when(i){
            1 -> return R.drawable.event_a_1
            2 -> return R.drawable.event_a_2
            3 -> return R.drawable.event_a_3
            4 -> return R.drawable.event_a_4
        }
        return  R.drawable.event_more
    }
    private fun calendarDate(date:Date):Calendar{
        //this is used to return a calendar using date
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = date
        return calendar
    }
    private fun compareDates(date1: Date, date2: Date):Boolean{
        return date1.date == date2.date && date1.month == date2.month && date1.year == date2.year
    }

}