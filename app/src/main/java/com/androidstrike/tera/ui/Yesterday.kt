package com.androidstrike.tera.ui

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ramotion.fluidslider.FluidSlider
import kotlinx.android.synthetic.main.fragment_yesterday.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class Yesterday : Fragment() {

    var courseAdapter: FirestoreRecyclerAdapter<CourseDetail, CourseAdapter>? = null
    lateinit var lectureTime: String
//    val connectionCheck = History().isNetworkAvailable(context)


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

        val layoutManager = LinearLayoutManager(requireContext())
        rv_yesterday.layoutManager = layoutManager
        rv_yesterday.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                layoutManager.orientation
            )
        )
        getRealTimeCourses()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getRealTimeCourses() {
        if (Common.dowYesGood == "Friday" || Common.dowYesGood == "Saturday") {
            txt_yes.visibility = View.VISIBLE
            txt_yes.text = "Lecture Free Day!"
        } else {
            val firestore = FirebaseFirestore.getInstance()
            val yesterdayCourses = firestore.collection(Common.dowYesGood)
            val query = yesterdayCourses.whereEqualTo("course_time", "${Common.formattedYesterday}")
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
                        holder.txtCourseCode.text = StringBuilder(model.course_code!!)
                        holder.txtCourseTitle.text = StringBuilder(model.course!!)
                        holder.txtCourseLecturer.text = StringBuilder(model.lecturer!!)

                        val fmt = model.time
                        val bla = LocalTime.of(
                            fmt?.hours!!,
                            fmt.minutes
                        )

                        val nameOfCourse = model.course.toString()
                        val nameOfLecturer = model.lecturer.toString()

                        val omo = nameOfLecturer.split(" ".toRegex())
                        val pureLecturerName = omo[1]


                        holder.txtCourseTime.text = bla.toString()
                        lectureTime = model.time.toString()
                        Log.d("EQua1", "onBindViewHolder: $lectureTime")

                        holder.setClick(object : IRecyclerItemClickListener {
                            override fun onItemClickListener(view: View, position: Int) {
                                Log.d("EQua", "onItemClickListener: $nameOfCourse")
                                Log.d("EQua", "onItemClickListener: ${model.time}")
                                lectureTime = "${model.time}"
                                Log.d("EQua", "onItemClickListenerLT: $lectureTime")

                                val timeNow = LocalDateTime.now()
                                val fmtTimeNow =
                                    timeNow.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
//                                isRated()

                                if (!isRated()) {//(sharedPrefs.getBoolean("${model.time}", true)) {
                                    showRatingDialog(pureLecturerName)
                                    //save in shared pref that the course for that particular timestamp has been rated
                                    val sharedPref = requireActivity().getSharedPreferences(
                                        "Is_Rated",
                                        Context.MODE_PRIVATE
                                    )
                                    val editor = sharedPref.edit()
                                    editor.putBoolean("${model.time}", true)
                                    editor.apply()

                                } else {
                                    //check if the class has already been rated
                                    activity?.toast("Class Already Rated")
                                }


                            }
                        })
                    }

                }

            rv_yesterday.adapter = courseAdapter

        }
    }


    private fun isRated(): Boolean {
        val sharedPrefs = requireActivity().getSharedPreferences(
            "Is_Rated",
            Context.MODE_PRIVATE
        )
        return sharedPrefs.getBoolean(lectureTime, false)
    }

    private fun showRatingDialog(pureLecturerName: String) {
        //rating dialog
        val ratingMax = 5
        val ratingMin = 0
        val ratingTotal = ratingMax - ratingMin
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(R.drawable.custom_dialog_design)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_rating_dialog)

        val punctualitySlider =
            dialog.findViewById<FluidSlider>(R.id.punctuality_bar)
        val fluencySlider =
            dialog.findViewById<FluidSlider>(R.id.fluency_bar)
        val engagementSlider =
            dialog.findViewById<FluidSlider>(R.id.engagement_bar)
        val techTipsSlider =
            dialog.findViewById<FluidSlider>(R.id.tech_tips_bar)
        val submitReviewButton =
            dialog.findViewById<Button>(R.id.btn_submit_review)

        val btnClickFalse = {
            submitReviewButton.isClickable = false
        }

        val btnClickTrue = {
            submitReviewButton.isClickable = true
        }

        punctualitySlider.positionListener = { pos ->
            punctualitySlider.bubbleText =
                "${ratingMin + (ratingTotal * pos).toInt()}"
        }
        punctualitySlider.position = 0.3f
        punctualitySlider.startText = "$ratingMin"
        punctualitySlider.endText = "$ratingMax"

        fluencySlider.positionListener = { pos ->
            fluencySlider.bubbleText =
                "${ratingMin + (ratingTotal * pos).toInt()}"
        }
        fluencySlider.position = 0.3f
        fluencySlider.startText = "$ratingMin"
        fluencySlider.endText = "$ratingMax"

        engagementSlider.positionListener = { pos ->
            engagementSlider.bubbleText =
                "${ratingMin + (ratingTotal * pos).toInt()}"
        }
        engagementSlider.position = 0.3f
        engagementSlider.startText = "$ratingMin"
        engagementSlider.endText = "$ratingMax"

        techTipsSlider.positionListener = { pos ->
            techTipsSlider.bubbleText =
                "${ratingMin + (ratingTotal * pos).toInt()}"
        }
        techTipsSlider.position = 0.3f
        techTipsSlider.startText = "$ratingMin"
        techTipsSlider.endText = "$ratingMax"

        submitReviewButton.setOnClickListener {
            //get the rating from each bar and assign to a variable
            val punctualityRating =
                punctualitySlider.bubbleText.toString().toInt()
            val fluencyRating =
                fluencySlider.bubbleText.toString().toInt()
            val engagementRating =
                engagementSlider.bubbleText.toString().toInt()
            val techTipsRating =
                techTipsSlider.bubbleText.toString().toInt()

            updateRatingTransaction(
                pureLecturerName,
                punctualityRating,
                fluencyRating,
                engagementRating,
                techTipsRating
            )
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun updateRatingTransaction(
        pureLecturerName: String,
        punctualityRating: Int,
        fluencyRating: Int,
        engagementRating: Int,
        techTipsRating: Int
    ) = CoroutineScope(Dispatchers.IO).launch {
        try {
            Firebase.firestore.runTransaction { transaction ->
                val lecturerRef = Common.lecturersRatingRef.document("$pureLecturerName")
                Log.d("EQUA", "updateRatingTransaction: $pureLecturerName")
                val rating = transaction.get(lecturerRef)
                val oldCount = rating["counts"] as Long //gets the count in the db
                val oldAvrRating = rating["avr_rating"] as Long //gets the average rating in the db
                val newCount = rating["counts"] as Long + 1 //increases the count in the db by 1
                val newPunctuality =
                    ((rating["punctuality"] as Long * oldCount) + punctualityRating) / newCount //adds to the punctuality rating in the db
                val newFluency =
                    ((rating["fluency"] as Long * oldCount) + fluencyRating) / newCount //adds to the fluency rating in the db
                val newEngagement =
                    ((rating["engagement"] as Long * oldCount) + engagementRating) / newCount //adds to the engagement rating in the db
                val newTechTips =
                    ((rating["tech_tips"] as Long * oldCount) + techTipsRating) / newCount //adds to the tech tips rating in the db

                //in order to successfully add the new ratings to the average, some math logic
                val avrRatDissolved = oldCount * oldAvrRating
                Log.d("EQUA", "updateRatingTransaction: $avrRatDissolved")
                val newTotalRating =
                    (newPunctuality + newFluency + newEngagement + newTechTips) / 4 //total of the new ratings
                val newAvrRating =
                    (avrRatDissolved + newTotalRating) / newCount //new average rating


                updateRating(
                    pureLecturerName,
                    punctualityRating,
                    fluencyRating,
                    engagementRating,
                    techTipsRating,
                    newCount,
                    newAvrRating
                )
                null
            }.await()
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                activity?.toast("transaction${e.message.toString()}")
            }
        }
    }

    private fun updateRating(
        nameOfLecturer: String,
        punctualityRating: Int,
        fluencyRating: Int,
        engagementRating: Int,
        techTipsRating: Int,
        count: Long,
        avrRating: Long
    ) = CoroutineScope(Dispatchers.IO).launch {
        try {
            Firebase.firestore.runBatch { batch ->
                val lecturerRef = Common.lecturersRatingRef.document(nameOfLecturer)
                batch.update(lecturerRef, "counts", count)
                batch.update(lecturerRef, "punctuality", punctualityRating)
                batch.update(lecturerRef, "fluency", fluencyRating)
                batch.update(lecturerRef, "engagement", engagementRating)
                batch.update(lecturerRef, "tech_tips", techTipsRating)
                batch.update(lecturerRef, "avr_rating", avrRating)
            }.await()
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                activity?.toast("transaction${e.message.toString()}")
            }
        }
    }


    override fun onStart() {
        super.onStart()
//        if (!connectionCheck)
//            activity?.toast("Check Network Connection")
//        else
        if (courseAdapter != null)
            courseAdapter!!.startListening()
    }

    override fun onStop() {
        super.onStop()
        if (courseAdapter != null)
            courseAdapter!!.stopListening()
    }

}