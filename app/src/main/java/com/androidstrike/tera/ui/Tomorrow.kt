package com.androidstrike.tera.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidstrike.tera.R
import com.androidstrike.tera.data.CourseDetail
import com.androidstrike.tera.ui.adapter.CourseAdapter
import com.androidstrike.tera.utils.Common
import com.androidstrike.tera.utils.IRecyclerItemClickListener
import com.androidstrike.tera.utils.toast
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_tommorrow.*
import java.time.LocalTime

class Tomorrow : Fragment() {

    var courseAdapter: FirestoreRecyclerAdapter<CourseDetail, CourseAdapter>? = null
//    private val connectionCheck = History().isNetworkAvailable(context) //invoke network connection check from history class



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

        val layoutManager = LinearLayoutManager(requireContext())
        rv_tomorrow.layoutManager = layoutManager
        rv_tomorrow.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                layoutManager.orientation
            )
        )

        //check the network status
//        if (!connectionCheck)
//            activity?.toast("Check Network Connection")
//        else
        getRealTimeCourses()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getRealTimeCourses() {
        // check if it is weekend
        if (Common.dowGood == "Friday" || Common.dowGood == "Saturday" || Common.dowTomGood == "Friday") {
            txt_tom.visibility = View.VISIBLE
            txt_tom.text = "Lecture Free Day \n Enjoy the Weekend!"
        }else {
            val firestore = FirebaseFirestore.getInstance()
            val tomorrowCourses = firestore.collection(Common.dowTomGood)
            val query = tomorrowCourses.whereEqualTo("course_time", "${Common.formattedTomorrow}")
            val options = FirestoreRecyclerOptions.Builder<CourseDetail>()
                .setQuery(query, CourseDetail::class.java).build()

            courseAdapter =
                object : FirestoreRecyclerAdapter<CourseDetail, CourseAdapter>(options) {
                    override fun onCreateViewHolder(
                        parent: ViewGroup,
                        viewType: Int
                    ): CourseAdapter {
                        val itemView = LayoutInflater.from(parent.context)
                            .inflate(R.layout.custom_day_course, parent, false)
                        return CourseAdapter(itemView)
                    }

                    override fun onBindViewHolder(
                        holder: CourseAdapter,
                        position: Int,
                        model: CourseDetail
                    ) {
                        holder?.txtCourseCode?.text = StringBuilder(model?.course_code!!)
                        holder?.txtCourseTitle?.text = StringBuilder(model?.course!!)
                        holder?.txtCourseLecturer?.text = StringBuilder(model?.lecturer!!)

                        val fmt = model.time
                        val bla = LocalTime.of(
                            fmt?.hours!!,
                            fmt.minutes
                        )

                        val nameOfCourse = model.course.toString()

                        holder?.txtCourseTime?.text = bla.toString()

                        holder?.setClick(object : IRecyclerItemClickListener {
                            override fun onItemClickListener(view: View, position: Int) {
                                activity?.toast("Check Back Tomorrow After The Class")
                            }
                        })
                    }

                }

            rv_tomorrow.adapter = courseAdapter


        }
    }

    override fun onStart() {
        super.onStart()
//        if (!connectionCheck)
//            activity?.toast("Check Network Connection")
//        else
        if (courseAdapter!= null)
            courseAdapter!!.startListening()
        else
            activity?.toast("lalala")    }

    override fun onStop() {
        super.onStop()
        if (courseAdapter != null)
            courseAdapter!!.stopListening()
    }

}