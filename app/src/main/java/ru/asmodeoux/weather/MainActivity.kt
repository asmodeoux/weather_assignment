package ru.asmodeoux.weather

import android.animation.ObjectAnimator
import android.content.Context
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList
import android.content.Intent
import android.net.Uri
import android.support.v4.app.SupportActivity
import android.support.v4.app.SupportActivity.ExtraData
import android.support.v4.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatDelegate


class MainActivity : AppCompatActivity() {

    var context: Context? = null
    private var webResponse: String? = ""

    var cities: ArrayList<City> = ArrayList()
    var days: ArrayList<Day> = ArrayList()

    var chosenDayIndex: Int = -1

    var loading: Boolean = false
    var barOpened: Boolean = false // to check if any floating bar is opened and override back-button

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        setContentView(R.layout.activity_main)

        context = applicationContext

        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = 0 // for horizontal one

        citiesRV.layoutManager = linearLayoutManager
        daysRV.layoutManager = LinearLayoutManager(context)

        swiperefreshlayout.setOnRefreshListener {
            if (!loading) {
                loading = true
                updateData()
            }
        }

        if (savedInstanceState == null) {
            swiperefreshlayout.post({ swiperefreshlayout.isRefreshing = true })
            updateData()
        } else {
            webResponse = savedInstanceState.getString("webresponse")

            val cache = webResponse
            if (cache != null && cache != "") {
                parseJSON(cache)
            } else {
                swiperefreshlayout.post({ swiperefreshlayout.isRefreshing = true })
                updateData()
            }
        }

        main_text.setOnClickListener { openFloatingBar() }
        view_more.setOnClickListener { openFloatingBar() }

        darker.setOnClickListener { closeFloatingBar(); closeCityInfo() }
        view_more_parent.setOnClickListener { closeFloatingBar() }
        city_info_parent.setOnClickListener { closeCityInfo() }
        view_more_constraint.setOnClickListener { closeFloatingBar() }

    }

    private fun updateData() {

        days.clear()
        cities.clear()

        Log.w("NO CONNECTION", "TEST-1")

        val request = Runnable {
            val response = try {

                URL("https://weather.aw.ee/api/estonia/forecast").openStream().bufferedReader().use { it.readText() }

            } catch (e: IOException) {
                e.printStackTrace()
                Log.w("NO CONNECTION", "TEST0")
                "Error with ${e.message}."
            }

            Log.w("NO CONNECTION", "TEST1")
            Log.w("NO CONNECTION", response)


            try {
                val forecasts: JSONArray = JSONObject(response).getJSONArray("forecasts")

                Log.w("API", response)
                webResponse = response

                parseJSON(response)

            } catch (e: Exception) {
                e.printStackTrace()
                restoreData()

                val cachedResponse = webResponse
                if (cachedResponse != null) {
                    runOnUiThread {
                        Toast.makeText(context, getString(R.string.cached_data), Toast.LENGTH_LONG).show()
                    }
                    parseJSON(cachedResponse)
                } else {
                    runOnUiThread {
                        Toast.makeText(context, getString(R.string.no_connection), Toast.LENGTH_LONG).show()
                        loading = false
                        swiperefreshlayout.isRefreshing = false
                    }
                }
            }

        }

        AsyncTask.execute(request)

    }

    fun updateUI(day: Int) { // number of a day in days array

        cities.clear()
        cities.addAll(days.get(day).cities)

        val minimumDay: Int? = days.get(day).minDay
        val maximumDay: Int? = days.get(day).maxDay

        var minimumStringDay: String? = minimumDay.toString()
        var maximumStringDay: String? = maximumDay.toString()

        if (minimumDay != null) {
            if (minimumDay > 0) {
                minimumStringDay = "+" + minimumStringDay
            }
        }

        if (maximumDay != null) {
            if (maximumDay > 0) {
                maximumStringDay = "+" + maximumStringDay
            }
        }

        val minimumNight: Int? = days.get(day).minNight
        val maximumNight: Int? = days.get(day).maxNight

        var minimumStringNight: String? = minimumNight.toString()
        var maximumStringNight: String? = maximumNight.toString()

        if (minimumNight != null) {
            if (minimumNight > 0) {
                minimumStringNight = "+" + minimumStringNight
            }
        }

        if (maximumNight != null) {
            if (maximumNight > 0) {
                maximumStringNight = "+" + maximumStringNight
            }
        }
        
        runOnUiThread {

            if (minimumDay != null) {
                if (maximumDay != null) {
                    todayDayDegrees.text = String.format(getString(R.string.degrees_all), minimumStringDay, maximumStringDay)
                } else {
                    todayDayDegrees.text = String.format(getString(R.string.degrees_more_than), minimumStringDay)
                }
            } else {
                if (maximumDay != null) {
                    todayDayDegrees.text = String.format(getString(R.string.degrees_less_than), maximumStringDay)
                } else {
                    todayDayDegrees.text = getString(R.string.unknown)
                }
            }

            if (minimumNight != null) {
                if (maximumNight != null) {
                    todayNightDegrees.text = String.format(getString(R.string.degrees_all), minimumStringNight, maximumStringNight)
                } else {
                    todayNightDegrees.text = String.format(getString(R.string.degrees_more_than), minimumStringNight)
                }
            } else {
                if (maximumNight != null) {
                    todayNightDegrees.text = String.format(getString(R.string.degrees_less_than), maximumStringNight)
                } else {
                    todayNightDegrees.text = getString(R.string.unknown)
                }
            }

            date_info.text = days.get(day).dayNumber.toString() + " " + days.get(day).month

            val dayTime: Int

            if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) > 19) dayTime = DayTime.NIGHT else dayTime = DayTime.DAY

            if (dayTime == DayTime.DAY) {

                if (getGradientStyle(days.get(day).dayPhenomenon) == 0) {
                    header.background = getDrawable(R.drawable.gradient_light)
                } else if (getGradientStyle(days.get(day).dayPhenomenon) == 1) {
                    header.background = getDrawable(R.drawable.gradient_medium)
                } else {
                    header.background = getDrawable(R.drawable.gradient_dark)
                }

                weather_icon.setImageResource(weatherIcon(days.get(day).dayPhenomenon, DayTime.DAY))

                if (days.get(day).textDay != null && days.get(day).textDay != "null") {
                    main_text.text = days.get(day).textDay
                } else if (days.get(day).daySea != null && days.get(day).daySea != "null") {
                    main_text.text = days.get(day).daySea
                } else if (days.get(day).dayPeipsi != null && days.get(day).dayPeipsi != "null") {
                    main_text.text = days.get(day).dayPeipsi
                } else {
                    main_text.text = getString(R.string.no_data_main_text)
                }
            } else {

                if (getGradientStyle(days.get(day).nightPhenomenon) == 0) {
                    header.background = getDrawable(R.drawable.gradient_light)
                } else if (getGradientStyle(days.get(day).nightPhenomenon) == 1) {
                    header.background = getDrawable(R.drawable.gradient_medium)
                } else {
                    header.background = getDrawable(R.drawable.gradient_dark)
                }

                weather_icon.setImageResource(weatherIcon(days.get(day).nightPhenomenon, DayTime.NIGHT))

                if (days.get(day).textNight != null && days.get(day).textNight != "null") {
                    main_text.text = days.get(day).textNight
                } else if (days.get(day).nightSea != null && days.get(day).nightSea != "null") {
                    main_text.text = days.get(day).nightSea
                } else if (days.get(day).nightPeipsi != null && days.get(day).nightPeipsi != "null") {
                    main_text.text = days.get(day).nightPeipsi
                } else {
                    main_text.text = getString(R.string.no_data_main_text)
                }
            }

            todayDayPhenomenon.text = getTranslatedPhenomenon(days.get(day).dayPhenomenon)
            todayNightPhenomenon.text = getTranslatedPhenomenon(days.get(day).nightPhenomenon)

            if (days.get(day).minWindDay != null) {
                todayDayWinds.text = String.format(
                    getString(R.string.winds),
                    days.get(day).minWindDay,
                    days.get(day).maxWindDay
                )
                todayNightWinds.text = String.format(
                    getString(R.string.winds),
                    days.get(day).minWindNight,
                    days.get(day).maxWindNight
                )
            } else {
                todayDayWinds.text = getString(R.string.no_data)
                todayNightWinds.text = getString(R.string.no_data)
            }

            todayDayImage.setImageResource(weatherIcon(days.get(day).dayPhenomenon, DayTime.DAY))
            todayNightImage.setImageResource(weatherIcon(days.get(day).nightPhenomenon, DayTime.NIGHT))

            view_more.visibility = View.VISIBLE

            if (cities.size == 0) {
                citiesRV.visibility = View.INVISIBLE   // not gone but invisible to avoid layout jump
                no_cities_data.visibility = View.VISIBLE
            } else {
                no_cities_data.visibility = View.INVISIBLE
                citiesRV.visibility = View.VISIBLE
                citiesRV.adapter = CitiesAdapter(cities, this)
                citiesRV.setHasFixedSize(true)
            }

            if (days.get(day).textDay != null && days.get(day).textDay != "" && days.get(day).textDay != "null") {
                view_more_main_day.setTextColor(ContextCompat.getColor(this, R.color.defaultText))
                view_more_main_day.text = days.get(day).textDay
            } else {
                view_more_main_day.visibility = View.GONE
            }

            if (days.get(day).textNight != null && days.get(day).textNight != "" && days.get(day).textNight != "null") {
                view_more_main_night.setTextColor(ContextCompat.getColor(this, R.color.defaultText))
                view_more_main_night.text = days.get(day).textNight
            } else {
                view_more_main_night.visibility = View.GONE
            }

            if (days.get(day).daySea != null && days.get(day).daySea != "" && days.get(day).daySea != "null") {
                Log.w("SEA DATA NON NULL", days.get(day).daySea)
                view_more_sea_day.setTextColor(ContextCompat.getColor(this, R.color.defaultText))
                view_more_sea_day.text = days.get(day).daySea
            } else {
                view_more_sea_day.setTextColor(ContextCompat.getColor(this, R.color.separatorColor))
                view_more_sea_day.text = getString(R.string.no_sea_data_day)
            }

            if (days.get(day).nightSea != null && days.get(day).nightSea != "" && days.get(day).nightSea != "null") {
                view_more_sea_night.setTextColor(ContextCompat.getColor(this, R.color.defaultText))
                view_more_sea_night.text = days.get(day).nightSea
            } else {
                view_more_sea_night.setTextColor(ContextCompat.getColor(this, R.color.separatorColor))
                view_more_sea_night.text = getString(R.string.no_sea_data_night)
            }

            if (days.get(day).dayPeipsi != null && days.get(day).dayPeipsi != "" && days.get(day).dayPeipsi != "null") {
                view_more_peipsi_day.setTextColor(ContextCompat.getColor(this, R.color.defaultText))
                view_more_peipsi_day.text = days.get(day).dayPeipsi
            } else {
                view_more_peipsi_day.setTextColor(ContextCompat.getColor(this, R.color.separatorColor))
                view_more_peipsi_day.text = getString(R.string.no_peipsi_day)
            }

            if (days.get(day).nightPeipsi != null && days.get(day).nightPeipsi != "" && days.get(day).nightPeipsi != "null") {
                view_more_peipsi_night.setTextColor(ContextCompat.getColor(this, R.color.defaultText))
                view_more_peipsi_night.text = days.get(day).nightPeipsi
            } else {
                view_more_peipsi_night.setTextColor(ContextCompat.getColor(this, R.color.separatorColor))
                view_more_peipsi_night.text = getString(R.string.no_peipsi_night)
            }

            daysRV.adapter = DaysAdapter(days, this)
            daysRV.setHasFixedSize(true)

            loading = false
            swiperefreshlayout.isRefreshing = false

        }

    }

    fun weatherIcon(phenomenon: String, time: Int): Int {

        // Selecting an icon depending on a phenomenon and time of a day

        when (phenomenon) {

            "Clear" -> if (time == DayTime.NIGHT) return R.drawable.ic_clear_night else return R.drawable.ic_clear_day
            "Few clouds", "Cloudy with clear spells" -> if (time == DayTime.NIGHT) return R.drawable.ic_few_clouds_night else return R.drawable.ic_few_clouds_day
            "Variable clouds" -> return R.drawable.ic_variable_clouds
            "Cloudy" -> return R.drawable.ic_cloudy
            "Light snow shower" -> return R.drawable.ic_light_snow_shower
            "Moderate snow shower" -> return R.drawable.ic_moderate_snow_shower
            "Heavy snow shower" -> return R.drawable.ic_heavy_snow_shower
            "Light shower", "Light rain" -> if (time == DayTime.NIGHT) return R.drawable.ic_light_rain_night else return R.drawable.ic_light_rain_day
            "Moderate shower" -> return R.drawable.ic_moderate_shower
            "Heavy shower", "Heavy rain" -> return R.drawable.ic_heavy_shower
            "Moderate rain" -> return R.drawable.ic_moderate_rain
            "Risk of glaze" -> return R.drawable.ic_risk_of_glaze
            "Light sleet" -> return R.drawable.ic_light_sleet
            "Moderate sleet" -> return R.drawable.ic_moderate_sleet
            "Light snowfall" -> return R.drawable.ic_light_snowfall
            "Moderate snowfall" -> return R.drawable.ic_moderate_snowfall
            "Heavy snowfall" -> return R.drawable.ic_heavy_snowfall
            "Snowstorm" -> return R.drawable.ic_snowstorm
            "Drifting snow" -> return R.drawable.ic_drifting_snow
            "Hail" -> return R.drawable.ic_hail
            "Mist" -> return R.drawable.ic_mist
            "Fog" -> return R.drawable.ic_fog
            "Thunder" -> return R.drawable.ic_thunder
            "Thunderstorm" -> return R.drawable.ic_thunderstorm

            else -> {
                Log.e("WEATHERICON", "No match")
                return R.drawable.ic_unknown
            }
        }
    }

    fun parseJSON(response: String) {
        try {
            val forecasts: JSONArray = JSONObject(response).getJSONArray("forecasts")

            cacheData() // if json exists then we cache it for offline usage

            for (i in 0 until forecasts.length()) {

                val dayData: JSONObject = forecasts.getJSONObject(i) // the whole day object

                val dayInfo: JSONObject = dayData.getJSONObject("day")
                val nightInfo: JSONObject = dayData.getJSONObject("night")

                val thisDayCities = ArrayList<City>()

                var minWindDay: Int? = null
                var maxWindDay: Int? = null

                var minWindNight: Int? = null
                var maxWindNight: Int? = null

                val monthNumber: Int = dayData.getString("date").split("-")[1].toInt()
                var monthName: String = ""

                when (monthNumber) {
                    1 -> monthName = getString(R.string.january)
                    2 -> monthName = getString(R.string.february)
                    3 -> monthName = getString(R.string.march)
                    4 -> monthName = getString(R.string.april)
                    5 -> monthName = getString(R.string.may)
                    6 -> monthName = getString(R.string.june)
                    7 -> monthName = getString(R.string.july)
                    8 -> monthName = getString(R.string.august)
                    9 -> monthName = getString(R.string.september)
                    10 -> monthName = getString(R.string.october)
                    11 -> monthName = getString(R.string.november)
                    12 -> monthName = getString(R.string.december)

                    else -> {
                        Log.e("MONTHNAME", "ERROR")
                        monthName = getString(R.string.unknown)
                    }
                }

                try {
                    val dayCities: JSONArray = dayInfo.getJSONArray("places")
                    val nightCities: JSONArray = nightInfo.getJSONArray("places")

                    val dayWinds: JSONArray = dayInfo.getJSONArray("winds")
                    val nightWinds: JSONArray = nightInfo.getJSONArray("winds")

                    Log.w("API", "Parsing today's weather")

                    for (c in 0 until dayCities.length()) {

                        val cityName = dayCities.getJSONObject(c).getString("name")
                        var nightAnalog = -1 // to check if there's weather info for this city at night
                        var cityMinTemp: Int? = null
                        var cityMaxTemp: Int? = null
                        var dayPhenomenon: String
                        var nightPhenomenon: String

                        for (v in 0 until nightCities.length()) {
                            if (nightCities.getJSONObject(v).getString("name") == cityName) {
                                nightAnalog = v
                                break
                            }
                        }

                        try {
                            cityMinTemp = dayCities.getJSONObject(c).getInt("tempmin")
                        } catch (e: Exception) {
                            //e.printStackTrace()
                            if (nightAnalog != -1) {
                                try {
                                    cityMinTemp =
                                        nightCities.getJSONObject(nightAnalog).getInt("tempmin")
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    cityMinTemp = null
                                }
                            }

                        }

                        try {
                            cityMaxTemp = dayCities.getJSONObject(c).getInt("tempmax")
                        } catch (e: Exception) {
                            //e.printStackTrace()
                            if (nightAnalog != -1) {
                                try {
                                    cityMaxTemp =
                                        nightCities.getJSONObject(nightAnalog).getInt("tempmax")
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    cityMaxTemp = null
                                }
                            }
                        }

                        try {
                            dayPhenomenon = dayCities.getJSONObject(c).getString("phenomenon")
                        } catch (e: Exception) {
                            e.printStackTrace()
                            dayPhenomenon = getString(R.string.unknown)
                        }

                        try {
                            nightPhenomenon =
                                nightCities.getJSONObject(nightAnalog).getString("phenomenon")
                        } catch (e: Exception) {
                            e.printStackTrace()
                            nightPhenomenon = getString(R.string.unknown)
                        }

                        thisDayCities.add(
                            City(
                                cityName,
                                cityMinTemp,
                                cityMaxTemp,
                                dayPhenomenon,
                                nightPhenomenon
                            )
                        )

                    }

                    for (c in 0 until dayWinds.length()) {

                        try {
                            if (minWindDay == null) {
                                minWindDay = dayWinds.getJSONObject(c).getInt("speedmin")
                            } else {
                                if (minWindDay > dayWinds.getJSONObject(c).getInt("speedmin")) {
                                    minWindDay = dayWinds.getJSONObject(c).getInt("speedmin")
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        try {
                            if (maxWindDay == null) {
                                maxWindDay = dayWinds.getJSONObject(c).getInt("speedmax")
                            } else {
                                if (maxWindDay < dayWinds.getJSONObject(c).getInt("speedmax")) {
                                    maxWindDay = dayWinds.getJSONObject(c).getInt("speedmax")
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }

                    for (c in 0 until nightWinds.length()) {

                        try {
                            if (minWindNight == null) {
                                minWindNight = nightWinds.getJSONObject(c).getInt("speedmin")
                            } else {
                                if (minWindNight > nightWinds.getJSONObject(c).getInt("speedmin")) {
                                    minWindNight = nightWinds.getJSONObject(c).getInt("speedmin")
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        try {
                            if (maxWindNight == null) {
                                maxWindNight = nightWinds.getJSONObject(c).getInt("speedmax")
                            } else {
                                if (maxWindNight < nightWinds.getJSONObject(c).getInt("speedmax")) {
                                    maxWindNight = nightWinds.getJSONObject(c).getInt("speedmax")
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }

                } catch (e: Exception) {
                    // places are null, only today has more infos
                    Log.e("API", "Not today, no city or wind info")
                }

                days.add(Day(dayData.getString("date"), dayInfo.getInt("tempmin"), dayInfo.getInt("tempmax"), dayInfo.getString("phenomenon"), nightInfo.getInt("tempmin"),
                    nightInfo.getInt("tempmax"), nightInfo.getString("phenomenon"), dayData.getString("date").split("-")[2].toInt(), monthName, dayInfo.getString("peipsi"),
                    nightInfo.getString("peipsi"), dayInfo.getString("sea"), nightInfo.getString("sea"), minWindDay, maxWindDay, minWindNight,
                    maxWindNight, dayInfo.getString("text"), nightInfo.getString("text"), thisDayCities))

            }

            chosenDayIndex = 0
            updateUI(0)

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun openFloatingBar() {
        barOpened = true
        darker.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(view_more_parent, "translationY", 800f, 0f).apply {
            duration = 125
            start()
        }
        view_more_parent.visibility = View.VISIBLE
    }

    fun closeFloatingBar() {
            ObjectAnimator.ofFloat(view_more_parent, "translationY", 0f, 1500f).apply {
                duration = 125
                start()
            }
            Handler().postDelayed({ darker.visibility = View.GONE }, 50)
            Handler().postDelayed({ view_more_parent.visibility = View.GONE }, 125)
            barOpened = false
    }

    fun openCityInfo() {
        barOpened = true
        darker.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(city_info_parent, "translationY", 300f, 0f).apply {
            duration = 100
            start()
        }
        city_info_parent.visibility = View.VISIBLE
    }

    fun closeCityInfo() {
            ObjectAnimator.ofFloat(city_info_parent, "translationY", 0f, 700f).apply {
                duration = 100
                start()
            }
            Handler().postDelayed({ darker.visibility = View.GONE }, 50)
            Handler().postDelayed({ city_info_parent.visibility = View.GONE }, 100)
            barOpened = false
    }

    fun setCityInfo(cityName: String, dayPhenomenon: String, nightPhenomenon: String, minDegree: Int?, maxDegree: Int?) {

        city_info_name.text = cityName

        city_day_phenomenon.text = getTranslatedPhenomenon(dayPhenomenon)
        city_night_phenomenon.text = getTranslatedPhenomenon(nightPhenomenon)

        if (minDegree != null) {
            if (minDegree > 0) {
                city_night_degrees.text =
                    String.format(getString(R.string.degrees_more_than), "+$minDegree")
            } else {
                city_night_degrees.text =
                    String.format(getString(R.string.degrees_more_than), minDegree.toString())
            }
        }

        if (maxDegree != null) {
            if (maxDegree > 0) {
                city_day_degrees.text =
                    String.format(getString(R.string.degrees_less_than), "+$maxDegree")
            } else {
                city_day_degrees.text =
                    String.format(getString(R.string.degrees_less_than), maxDegree.toString())
            }
        }

        city_image_day.setImageResource(weatherIcon(dayPhenomenon, DayTime.DAY))
        city_image_night.setImageResource(weatherIcon(nightPhenomenon, DayTime.NIGHT))

        val map = "http://maps.google.co.in/maps?q=$cityName"

        find_on_a_map.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(map))
            startActivity(intent)
        }

    }

    fun getTranslatedPhenomenon(phenomenon: String): String {

        // translates the phenomenon to show

        when (phenomenon) {

            "Clear" -> return getString(R.string.clear)
            "Few clouds" -> return getString(R.string.few_clouds)
            "Cloudy with clear spells" -> return getString(R.string.cloudy_spells)
            "Variable clouds" -> return getString(R.string.var_clouds)
            "Cloudy" -> return getString(R.string.cloudy)
            "Light snow shower" -> return getString(R.string.light_snow_shower)
            "Moderate snow shower" -> return getString(R.string.moderate_snow_shower)
            "Heavy snow shower" -> return getString(R.string.heavy_snow_shower)
            "Light shower" -> return getString(R.string.light_shower)
            "Light rain" -> return getString(R.string.light_rain)
            "Moderate shower" -> return getString(R.string.moderate_shower)
            "Heavy shower" -> return getString(R.string.heavy_shower)
            "Heavy rain" -> return getString(R.string.heavy_rain)
            "Moderate rain" -> return getString(R.string.moderate_rain)
            "Risk of glaze" -> return getString(R.string.risk_of_glaze)
            "Light sleet" -> return getString(R.string.light_sleet)
            "Moderate sleet" -> return getString(R.string.moderate_sleet)
            "Light snowfall" -> return getString(R.string.light_snowfall)
            "Moderate snowfall" -> return getString(R.string.moderate_snowfall)
            "Heavy snowfall" -> return getString(R.string.heavy_snowfall)
            "Snowstorm" -> return getString(R.string.snowstorm)
            "Drifting snow" -> return getString(R.string.drifting_snow)
            "Hail" -> return getString(R.string.hail)
            "Mist" -> return getString(R.string.mist)
            "Fog" -> return getString(R.string.fog)
            "Thunder" -> return getString(R.string.thunder)
            "Thunderstorm" -> return getString(R.string.thunderstorm)

            else -> return getString(R.string.unknown)

        }
    }

    fun getGradientStyle(phenomenon: String): Int {

        // 0 is light, 1 is darker, 2 is the darkest gradient

        when (phenomenon) {

            "Clear", "Few clouds", "Cloudy with clear spells" -> return 0
            "Variable clouds", "Cloudy", "Light shower", "Light rain", "Moderate shower", "Moderate rain", "Risk of glaze", "Light sleet", "Light snowfall", "Moderate snowfall", "Drifting snow", "Mist", "Fog" -> return 1
            "Light snow shower", "Moderate snow shower", "Heavy snow shower", "Heavy shower", "Heavy rain", "Moderate sleet", "Heavy snowfall", "Snowstorm", "Hail", "Thunder", "Thunderstorm" -> return 2

            else -> return 1
        }

    }

    fun cacheData() {
        val sp = getSharedPreferences("cache", Context.MODE_PRIVATE)
        sp.edit().putString("response", webResponse).apply()
    }

    fun restoreData() {
        val sp = getSharedPreferences("cache", Context.MODE_PRIVATE)
        webResponse = sp.getString("response", null)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        if (webResponse != null && webResponse != "") {
            outState?.putString("webresponse", webResponse);
        }
    }

    override fun onBackPressed() {
        if (barOpened) {
            closeCityInfo()
            closeFloatingBar()
        } else {
            finish()
        }
        return
    }

}
