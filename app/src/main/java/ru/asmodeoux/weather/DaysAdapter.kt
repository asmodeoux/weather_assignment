package ru.asmodeoux.weather

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.day_item.view.*
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

internal class DaysAdapter(private val arrayList: ArrayList<Day>, private val context: Context) : RecyclerView.Adapter<DaysAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DaysAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.day_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: DaysAdapter.ViewHolder, position: Int) {
        holder.bindItems(arrayList[position])
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    internal inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(item: Day) {

            itemView.day_number.text = item.dayNumber.toString()
            itemView.month_name.text = item.month

            var minDayString = item.minDay.toString()
            var maxDayString = item.maxDay.toString()
            var minNightString = item.minNight.toString()
            var maxNightString = item.maxNight.toString()

            try {
                if (item.minDay!! > 0) {
                    minDayString = "+" + item.minDay
                }
                if (item.maxDay!! > 0) {
                    maxDayString = "+" + item.maxDay
                }
                if (item.minNight!! > 0) {
                    minNightString = "+" + item.minNight
                }
                if (item.maxNight!! > 0) {
                    maxNightString = "+" + item.maxNight
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            itemView.day_degrees.text = String.format(context.getString(R.string.degrees_all), minDayString, maxDayString)
            itemView.night_degrees.text = String.format(context.getString(R.string.degrees_all), minNightString, maxNightString)

            itemView.day_phenomenon.text = getTranslatedPhenomenon(item.dayPhenomenon)
            itemView.night_phenomenon.text = getTranslatedPhenomenon(item.nightPhenomenon)

            if (context is MainActivity) {
                itemView.day_weather_icon.setImageResource(context.weatherIcon(item.dayPhenomenon, DayTime.DAY))
                itemView.night_weather_icon.setImageResource(context.weatherIcon(item.nightPhenomenon, DayTime.NIGHT))

                itemView.day_parent.setOnClickListener {
                    context.chosenDayIndex = adapterPosition
                    notifyDataSetChanged()
                    context.updateUI(context.chosenDayIndex)
                }

                if (adapterPosition == context.chosenDayIndex) {
                    itemView.month_name.setBackgroundColor(context.resources.getColor(R.color.colorAccent))
                    itemView.day_parent.elevation = 3.0f
                } else {
                    itemView.month_name.setBackgroundColor(context.resources.getColor(R.color.separatorColor))
                    itemView.day_parent.elevation = 1.0f
                }

            }
        }
    }

    fun getTranslatedPhenomenon(phenomenon: String): String {

        when (phenomenon) {

            "Clear" -> return context.getString(R.string.clear)
            "Few clouds" -> return context.getString(R.string.few_clouds)
            "Cloudy with clear spells" -> return context.getString(R.string.cloudy_spells)
            "Variable clouds" -> return context.getString(R.string.var_clouds)
            "Cloudy" -> return context.getString(R.string.cloudy)
            "Light snow shower" -> return context.getString(R.string.light_snow_shower)
            "Moderate snow shower" -> return context.getString(R.string.moderate_snow_shower)
            "Heavy snow shower" -> return context.getString(R.string.heavy_snow_shower)
            "Light shower" -> return context.getString(R.string.light_shower)
            "Light rain" -> return context.getString(R.string.light_rain)
            "Moderate shower" -> return context.getString(R.string.moderate_shower)
            "Heavy shower" -> return context.getString(R.string.heavy_shower)
            "Heavy rain" -> return context.getString(R.string.heavy_rain)
            "Moderate rain" -> return context.getString(R.string.moderate_rain)
            "Risk of glaze" -> return context.getString(R.string.risk_of_glaze)
            "Light sleet" -> return context.getString(R.string.light_sleet)
            "Moderate sleet" -> return context.getString(R.string.moderate_sleet)
            "Light snowfall" -> return context.getString(R.string.light_snowfall)
            "Moderate snowfall" -> return context.getString(R.string.moderate_snowfall)
            "Heavy snowfall" -> return context.getString(R.string.heavy_snowfall)
            "Snowstorm" -> return context.getString(R.string.snowstorm)
            "Drifting snow" -> return context.getString(R.string.drifting_snow)
            "Hail" -> return context.getString(R.string.hail)
            "Mist" -> return context.getString(R.string.mist)
            "Fog" -> return context.getString(R.string.fog)
            "Thunder" -> return context.getString(R.string.thunder)
            "Thunderstorm" -> return context.getString(R.string.thunderstorm)

            else -> {
                return context.getString(R.string.unknown)
            }

        }
    }

}