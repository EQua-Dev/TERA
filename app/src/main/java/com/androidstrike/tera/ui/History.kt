package com.androidstrike.tera.ui

import android.app.Dialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidstrike.tera.R
import com.androidstrike.tera.data.Rating
import com.androidstrike.tera.ui.adapter.HistoryAdapter
import com.androidstrike.tera.utils.IRecyclerItemClickListener
import com.androidstrike.tera.utils.toast
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_history.*
import kotlinx.android.synthetic.main.fragment_today.*
import java.lang.StringBuilder

class History : AppCompatActivity() {

    var historyAdapter: FirestoreRecyclerAdapter<Rating, HistoryAdapter>? = null


    lateinit var punctualityRating: String
    lateinit var fluencyRating: String
    lateinit var engagementRating: String
    lateinit var techTipsRating: String
    lateinit var lecturerName: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_TERA)
        setContentView(R.layout.activity_history)

        supportActionBar?.title = "Ratings"
        val layoutManager = LinearLayoutManager(this)
        rv_history.layoutManager = layoutManager
        rv_history.addItemDecoration(
            DividerItemDecoration(
                this,
                layoutManager.orientation
            )
        )

        getRealTimeRating()
    }

    private fun getRealTimeRating() {
        val firestore = FirebaseFirestore.getInstance()
        val allRatings =
            firestore.collection("Lecturers").orderBy("avr_rating", Query.Direction.DESCENDING)
        val query = allRatings.whereGreaterThanOrEqualTo(
            "avr_rating",
            0
        )//GreaterThanOrEqualTo("avr_rating", 0)//.orderBy("${allRatings.document().id}", Query.Direction.ASCENDING)
        val options = FirestoreRecyclerOptions.Builder<Rating>()
            .setQuery(query, Rating::class.java).build()

        try {
            historyAdapter =
                object : FirestoreRecyclerAdapter<Rating, HistoryAdapter>(options) {
                    override fun onCreateViewHolder(
                        parent: ViewGroup,
                        viewType: Int
                    ): HistoryAdapter {
                        val itemView = LayoutInflater.from(parent.context)
                            .inflate(R.layout.custom_history_item, parent, false)
                        return HistoryAdapter(itemView)
                    }

                    override fun onBindViewHolder(
                        holder: HistoryAdapter,
                        position: Int,
                        model: Rating
                    ) {
                        holder.lecturerName.text = StringBuilder(model.lecturer)
                        holder.totalCounts.text = "${model.counts} Total Raters"
                        holder.lecturerRating.text = StringBuilder(model.avr_rating.toString())

                        punctualityRating = "${model.punctuality}"
                        fluencyRating = model.fluency.toString()
                        engagementRating = model.engagement.toString()
                        techTipsRating = model.tech_tips.toString()
                        lecturerName = model.lecturer

                        holder.setClick(object : IRecyclerItemClickListener {
                            override fun onItemClickListener(view: View, position: Int) {
                                showRatedDialog(
                                    "${model.punctuality}",
                                    "${model.fluency}",
                                    "${model.engagement}",
                                    "${model.tech_tips}",
                                    model.lecturer
                                )
                            }

                        })
                    }

                }
        } catch (e: Exception) {
            toast(e.message.toString())
        }


        rv_history.adapter = historyAdapter
    }

    private fun showRatedDialog(
        punctualityRating: String,
        fluencyRating: String,
        engagementRating: String,
        techTipsRating: String,
        lecturerName: String
    ) {
        val dialog = Dialog(this)
        dialog.setTitle("$lecturerName's Rating")
        dialog.window?.setBackgroundDrawableResource(R.drawable.custom_dialog_design)
        dialog.setContentView(R.layout.custom_history_rating)

        val punctualityBar = dialog.findViewById<TextView>(R.id.tv_lecturer_punctuality_rating)
        val fluencyBar = dialog.findViewById<TextView>(R.id.tv_lecturer_fluency_rating)
        val engagementBar = dialog.findViewById<TextView>(R.id.tv_lecturer_engagement_rating)
        val techTipsBar = dialog.findViewById<TextView>(R.id.tv_lecturer_tech_tips_rating)

        punctualityBar.text = punctualityRating
        fluencyBar.text = fluencyRating
        engagementBar.text = engagementRating
        techTipsBar.text = techTipsRating

        dialog.show()
    }

    fun isNetworkAvailable(context: Context?): Boolean {
        if (context == null) return false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        return true
                    }
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return true
            }
        }
        return false

    }

    override fun onStart() {
        super.onStart()
        historyAdapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        if (historyAdapter != null)
            historyAdapter!!.stopListening()
    }

}