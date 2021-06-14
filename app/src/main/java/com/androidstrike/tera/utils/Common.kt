package com.androidstrike.tera.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

object Common {
    //Day of the week (Today)
    @RequiresApi(Build.VERSION_CODES.O)
    var date: LocalDate = LocalDate.now()
    @RequiresApi(Build.VERSION_CODES.O)
    var dow = date.dayOfWeek.toString().toLowerCase(Locale.ROOT)
    var dowGood = dow[0].toUpperCase()+dow.substring(1)


    var c: Date = Calendar.getInstance().time
    var df: SimpleDateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    @RequiresApi(Build.VERSION_CODES.O)
    val formattedToday = date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))

    @RequiresApi(Build.VERSION_CODES.O)
    val tomorrow = LocalDate.now().plusDays(1)
    @RequiresApi(Build.VERSION_CODES.O)
    val dowTom = tomorrow.dayOfWeek.toString().toLowerCase(Locale.ROOT)
    var dowTomGood = dowTom[0].toUpperCase()+ dowTom.substring(1)

    @RequiresApi(Build.VERSION_CODES.O)
    val formattedTomorrow = tomorrow.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))

    @RequiresApi(Build.VERSION_CODES.O)
    val yesterday = LocalDate.now().minusDays(1)
    @RequiresApi(Build.VERSION_CODES.O)
    val dowYes = yesterday.dayOfWeek.toString().toLowerCase(Locale.ROOT)
    var dowYesGood = dowYes[0].toUpperCase()+ dowYes.substring(1)
    @RequiresApi(Build.VERSION_CODES.O)
    val formattedYesterday = yesterday.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))

}