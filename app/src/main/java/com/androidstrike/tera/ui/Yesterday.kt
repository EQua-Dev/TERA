package com.androidstrike.tera.ui

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.androidstrike.tera.R
import com.androidstrike.tera.data.CourseDetail
import com.androidstrike.tera.utils.Common
import com.androidstrike.tera.utils.toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_today.*
import kotlinx.android.synthetic.main.fragment_yesterday.*
import java.lang.StringBuilder
import java.time.LocalTime

class Yesterday : Fragment() {

    private val yesterdayCourses = Firebase.firestore.collection(Common.dowYesGood)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_yesterday, container, false)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        bn_toast_yes.setOnClickListener {
            activity?.toast(Common.dowYesGood.toString())
        }

        bn_toast_date_yes.setOnClickListener {
            activity?.toast(Common.formattedYesterday)
        }

        getRealTimeCourses()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getRealTimeCourses() {
        if (Common.dowYesGood == "Friday" || Common.dowYesGood == "Saturday"){
            txt_yes.text = "Lecture Free Day\n Enjoy the Weekend!"
        }else {
            yesterdayCourses
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
                        txt_yes.text = sb.toString()
                    }
                }
        }
    }

}