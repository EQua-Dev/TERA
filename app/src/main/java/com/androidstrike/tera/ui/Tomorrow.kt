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
import kotlinx.android.synthetic.main.fragment_today.bn_toast_date
import kotlinx.android.synthetic.main.fragment_tommorrow.*
import java.lang.StringBuilder
import java.time.LocalTime

class Tomorrow : Fragment() {

    private val tomorrowCourses = Firebase.firestore.collection(Common.dowTomGood)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tommorrow, container, false)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        bn_toast_tom.setOnClickListener {
            activity?.toast(Common.dowTomGood)
        }

        bn_toast_date_tom.setOnClickListener {
            activity?.toast(Common.formattedTomorrow)
        }

        getRealTimeCourses()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getRealTimeCourses() {
        if(Common.dowTomGood == "Saturday" || Common.dowTomGood == "Sunday"){
            txt_tom.text = "Lecture Free Day \n Enjoy the Weekend!"
        }else {
            tomorrowCourses
                .whereEqualTo("course_time", "${Common.formattedTomorrow}")
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
                        txt_tom.text = sb.toString()
                    }
                }
        }
    }

}