package com.androidstrike.tera.ui

import android.icu.text.DateFormat.getDateInstance
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.androidstrike.tera.R
import com.androidstrike.tera.data.CourseDetail
import com.androidstrike.tera.utils.Common
import com.androidstrike.tera.utils.toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_today.*
import java.lang.StringBuilder
import java.text.DateFormat
import java.text.DateFormat.getDateInstance
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import kotlin.time.hours

class Today : Fragment() {

    private val todayCourses = Firebase.firestore.collection(Common.dowGood)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_today, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        bn_toast.setOnClickListener {
            activity?.toast(Common.dowGood)
        }

        bn_toast_date.setOnClickListener {
            activity?.toast(Common.formattedToday)
        }

        getRealTimeCourses()
        Log.d("EQUA", "onActivityCreated: Reached Here")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getRealTimeCourses() {
        if (Common.dowGood == "Sunday") {
            txt.text = "Lecture Free Day, Weekend\nPrepare for Tomorrow"
        } else {
            todayCourses
                .whereEqualTo("course_time", "${Common.formattedToday}")
                .addSnapshotListener { value, error ->
                    error?.let {
                        activity?.toast(it.message.toString())
                        return@addSnapshotListener
                    }
                    value?.let {
                        val sb = StringBuilder()
                        Log.d("EQUA", "getRealTimeCourses: Reached Here")
                        for (document in it) {
                            Log.d("EQUA", "getRealTimeCourses: $it")
                            val courses = document.toObject<CourseDetail>()
                            var fmt = courses.time
                            val bla = LocalTime.of(
                                fmt?.hours!!,
                                fmt.minutes
                            ) //fmt?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) //DateTimeFormatter.ofPattern("H:m") //.getDateInstance(DateFormat.MEDIUM).format(fmt)
                            sb.append("${courses.course}\n$bla\n")
                        }
                        txt.text = sb.toString()
                    }
                }
        }
    }
}