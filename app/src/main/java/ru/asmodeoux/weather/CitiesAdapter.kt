package ru.asmodeoux.weather

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.city_item.view.*
import java.util.*
import kotlin.collections.ArrayList

internal class CitiesAdapter(private val arrayList: ArrayList<City>, private val context: Context) : RecyclerView.Adapter<CitiesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitiesAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.city_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: CitiesAdapter.ViewHolder, position: Int) {
        holder.bindItems(arrayList[position])
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    internal inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(item: City) {
            itemView.city_name.text = item.name
            itemView.city_phenomenon.text = getTranslatedPhenomenon(item.dayPhenomenon)

            if (getGradientStyle(item.dayPhenomenon) == 0) {
                itemView.city_card_gradient.background = context.getDrawable(R.drawable.gradient_light)
            } else if (getGradientStyle(item.dayPhenomenon) == 1) {
                itemView.city_card_gradient.background = context.getDrawable(R.drawable.gradient_medium)
            } else {
                itemView.city_card_gradient.background = context.getDrawable(R.drawable.gradient_dark)
            }


            var minText = ""
            var maxText = ""

            // adding "+" sign if more than 0 degrees

            val minimal = item.min
            val maximum = item.max

            if (minimal != null) {
                if (minimal > 0) {
                    minText = "+" + item.min
                } else {
                    minText = item.min.toString()
                }
            }

            if (maximum != null) {
                if (maximum > 0) {
                    maxText = "+" + item.max
                } else {
                    maxText = item.max.toString()
                }
            }

            itemView.city_degrees.text = String.format(context.getString(R.string.degrees_all), minText, maxText)

            if (context is MainActivity) {
                if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 20) {
                    itemView.city_weather_icon.setImageResource(
                        context.weatherIcon(
                            item.dayPhenomenon,
                            DayTime.DAY
                        )
                    )
                } else {
                    itemView.city_weather_icon.setImageResource(
                        context.weatherIcon(
                            item.nightPhenomenon,
                            DayTime.NIGHT
                        )
                    )
                }
            }

            itemView.city_card_parent.setOnClickListener {
                if (context is MainActivity) {
                    context.setCityInfo(item.name, item.dayPhenomenon, item.nightPhenomenon, item.min!!, item.max!!)
                    context.openCityInfo()
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

            else -> return context.getString(R.string.unknown)
        }
    }

    fun getGradientStyle(phenomenon: String): Int { // 0 is light, 1 is darker, 2 is the darkest gradient
        when (phenomenon) {
            "Clear", "Few clouds", "Cloudy with clear spells" -> return 0
            "Variable clouds", "Cloudy", "Light shower", "Light rain", "Moderate shower", "Moderate rain", "Risk of glaze", "Light sleet", "Light snowfall", "Moderate snowfall", "Drifting snow", "Mist", "Fog" -> return 1
            "Light snow shower", "Moderate snow shower", "Heavy snow shower", "Heavy shower", "Heavy rain", "Moderate sleet", "Heavy snowfall", "Snowstorm", "Hail", "Thunder", "Thunderstorm" -> return 2

            else -> return 1
        }
    }

}