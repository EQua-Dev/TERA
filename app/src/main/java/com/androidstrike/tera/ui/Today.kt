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
import kotlinx.android.synthetic.main.fragment_today.*
import kotlinx.android.synthetic.main.fragment_tommorrow.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

class Today : Fragment() {

    var courseAdapter: FirestoreRecyclerAdapter<CourseDetail, CourseAdapter>? = null
    lateinit var lectureTime: String
//    val connectionCheck = History().isNetworkAvailable(context)
//
//    fun isThereConnection(){
//        if (!connectionCheck)
//            Log.d("EQUABEFORE", "isThereConnection: omoda")
//        else
//            return
//
//    }

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

//        isThereConnection()

        val layoutManager = LinearLayoutManager(requireContext())
        rv_today.layoutManager = layoutManager
        rv_today.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                layoutManager.orientation
            )
        )


        Log.d("EQUA", "onActivityCreated: ${LocalTime.now()}")

//        Log.d("EQUA11223344", "onCreateView: $connectionCheck")
//        Log.d("EQUA11223344", "onCreateView: ${!connectionCheck}")


//        if (connectionCheck)
        getRealTimeCourses()
//        else
//            activity?.toast("Check Network Connection")

        Log.d("EQUA", "onActivityCreated: Reached Here")

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getRealTimeCourses() {
        if (Common.dowGood == "Friday" || Common.dowGood == "Saturday") {
            txt.visibility = View.VISIBLE
            txt.text = "Lecture Free Day\nEnjoy the Weekend!"
        } else if (Common.dowGood == "Sunday") {
            txt_tom.visibility = View.VISIBLE
            txt_tom.text = "Lecture Free Day \n Prepare for Tomorrow!"
        } else {

            val firestore = FirebaseFirestore.getInstance()
            val todayCourses = firestore.collection(Common.dowGood)
            val query = todayCourses.whereEqualTo("course_time", "${Common.formattedToday}")
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

                        val nameOfLecturer = model.lecturer.toString()

                        val omo = nameOfLecturer.split(" ".toRegex())
                        val pureLecturerName = omo[1]

                        val nameOfCourse = model.course.toString()

                        holder?.txtCourseTime?.text = bla.toString()
                        lectureTime = model.time.toString()


                        holder?.setClick(object : IRecyclerItemClickListener {
                            override fun onItemClickListener(view: View, position: Int) {
                                val timeNow = LocalTime.now()
                                val fmtTimeNow =
                                    timeNow.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))

                                isRated()

                                val c = Calendar.getInstance()
                                val format = SimpleDateFormat("HH:mm")
                                val currentTime = format.format(c.time)
                                val sixPm = "18:00"
//                                first check if the time is up to 6pm
//                                if (currentTime.compareTo(sixPm) < 0) {
//                                    activity?.toast("Class Rating Available From 6PM")
//                                } else {
                                if (isRated()) {//(sharedPrefs.getBoolean("${model.time}", true)) {
                                    //check if the class has already been rated
                                    activity?.toast("Class Already Rated")
                                    Log.d(
                                        "EQUA",
                                        "onItemClickListener: Does not know what it is doing"
                                    )
                                } else {
                                    showRatingDialog(pureLecturerName)
                                    Log.d("EQUA", "onItemClickListener: meant to show dialog")
                                    //save in shared pref that the course for that particular timestamp has been rated
                                    val sharedPref = requireActivity().getSharedPreferences(
                                        "Is_Rated",
                                        Context.MODE_PRIVATE
                                    )
                                    val editor = sharedPref.edit()
                                    editor.putBoolean(lectureTime, false)
                                    editor.apply()

//                                    }
                                }

                            }
                        })
                    }

                }

//            courseAdapter!!.startListening()
            rv_today.adapter = courseAdapter

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
//        punctualitySlider.beginTrackingListener = btnClickFalse

        fluencySlider.positionListener = { pos ->
            fluencySlider.bubbleText =
                "${ratingMin + (ratingTotal * pos).toInt()}"
        }
        fluencySlider.position = 0.3f
        fluencySlider.startText = "$ratingMin"
        fluencySlider.endText = "$ratingMax"
        //fluencySlider.beginTrackingListener = btnClick

        engagementSlider.positionListener = { pos ->
            engagementSlider.bubbleText =
                "${ratingMin + (ratingTotal * pos).toInt()}"
        }
        engagementSlider.position = 0.3f
        engagementSlider.startText = "$ratingMin"
        engagementSlider.endText = "$ratingMax"
        //engagementSlider.beginTrackingListener = btnClick

        techTipsSlider.positionListener = { pos ->
            techTipsSlider.bubbleText =
                "${ratingMin + (ratingTotal * pos).toInt()}"
        }
        techTipsSlider.position = 0.3f
        techTipsSlider.startText = "$ratingMin"
        techTipsSlider.endText = "$ratingMax"
//        techTipsSlider.endTrackingListener = btnClickTrue

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

            Log.d("EQUA", "showRatingDialog: $punctualityRating")

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
//                Firebase.firestore.collection("Lecturers").document()
                val lecturerRef = Common.lecturersRatingRef.document("$pureLecturerName")
                Log.d("EQUA", "updateRatingTransaction: $pureLecturerName")
                val rating = transaction.get(lecturerRef)
                val oldCount = rating["counts"] as Long //gets the count in the db
                val oldAvrRating = rating["avr_rating"] as Long //gets the average rating in the db
                val newCount = rating["counts"] as Long + 1 //increases the count in the db by 1
                Log.d("EQUA1122", "updateRatingTransaction: $newCount")
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
                Log.d("EQUA", "updateRatingTransaction: ${e.message.toString()}")
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
                Log.d("EQUA", "updateRating: Rating Updating")
            }.await()
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                activity?.toast("transaction${e.message.toString()}")
                Log.d("EQUA", "updateRating: error here")
            }
        }
    }


    override fun onStart() {
        super.onStart()
        if (courseAdapter != null)
            courseAdapter!!.startListening()
        else
            activity?.toast("lalala")
    }

    override fun onStop() {
        super.onStop()
        if (courseAdapter != null)
            courseAdapter!!.stopListening()
    }

}