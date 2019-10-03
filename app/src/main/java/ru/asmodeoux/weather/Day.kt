package ru.asmodeoux.weather

open class Day(date: String, minDay: Int?, maxDay: Int?, dayPhenomenon: String, minNight: Int?, maxNight: Int?, nightPhenomenon: String, dayNumber: Int,
               month: String, dayPeipsi: String?, nightPeipsi: String?, daySea: String?, nightSea: String?, minWindDay: Int?, maxWindDay: Int?,
               minWindNight: Int?, maxWindNight: Int?, textDay: String?, textNight: String?, cities: ArrayList<City>) {

    var date = date // date string from JSON: "2019-10-01"
    var dayNumber = dayNumber
    var month = month // month name, set in JSON parser

    var minDay = minDay
    var maxDay = maxDay
    var dayPhenomenon = dayPhenomenon
    var dayPeipsi = dayPeipsi
    var daySea = daySea

    var minNight = minNight
    var maxNight = maxNight
    var nightPhenomenon = nightPhenomenon
    var nightPeipsi = nightPeipsi
    var nightSea = nightSea

    var minWindDay = minWindDay
    var maxWindDay = maxWindDay

    var minWindNight = minWindNight
    var maxWindNight = maxWindNight

    var textDay = textDay
    var textNight = textNight

    var cities: ArrayList<City> = cities

}