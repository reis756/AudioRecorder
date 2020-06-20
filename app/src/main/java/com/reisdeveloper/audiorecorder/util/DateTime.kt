package com.reisdeveloper.audiorecorder.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import androidx.appcompat.widget.AppCompatDrawableManager
import androidx.core.graphics.drawable.DrawableCompat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Created by amene on 14/12/2017.
 */
class DateTime() {
    private var calendar: Calendar? = null

    constructor(data: Date?) : this() {
        if (data != null) {
            calendar = Calendar.getInstance()
            this.calendar?.time = data
        }
    }

    constructor(milliseconds: Long) : this() {
        calendar = Calendar.getInstance()
        this.calendar?.timeInMillis = milliseconds
    }

    constructor(data: String?) : this() {
        if (!data.isNullOrEmpty()) {
            this.calendar = Calendar.getInstance()
            val dateFormat = determineDateFormat(data!!)
                ?: throw ParseException("Formato da data não reconhecido.", 0)
            this.calendar?.time = parse(data, dateFormat)

            if (!dateFormat.contains("yyyy")) {
                val dt: Calendar? = Calendar.getInstance()
                this[Calendar.DAY_OF_MONTH] = dt!!.get(Calendar.DAY_OF_MONTH)
                this[Calendar.MONTH] = dt.get(Calendar.MONTH)
                this[Calendar.YEAR] = dt.get(Calendar.YEAR)
            }
        }
    }

    fun get(calendarField: Int): Any? = calendar?.get(calendarField)

    //region Dia
    fun getDia(): Int = Coalesce(0, calendar?.get(Calendar.DAY_OF_MONTH))

    fun getDiaDaSemana(): String =
        if (this.calendar != null) SimpleDateFormat("EEEE", Locale.getDefault()).format(this.calendar!!.time) else ""
    //endregion

    //region Mês
    fun getMes(): Mes? = if (this.calendar != null) Mes(
        this.calendar!!
    ) else null

    data class Mes(private val cal: Calendar) {
        val enum: Int = cal.get(Calendar.MONTH)
        val mes: Int = cal.get(Calendar.MONTH) + 1
        val nome: String = SimpleDateFormat("MMMM", Locale.getDefault()).format(this.cal.time)
    }
    //endregion

    //region Ano
    fun getAno(): Int = Coalesce(0, calendar?.get(Calendar.YEAR))
    //endregion

    //region Hora
    fun getHora(): Int = Coalesce(0, calendar?.get(Calendar.HOUR_OF_DAY))
    //endregion

    //region Minuto
    fun getMinuto(): Int = Coalesce(0, calendar?.get(Calendar.MINUTE))
    //endregion

    //region Segundo
    fun getSegundo(): Int = Coalesce(0, calendar?.get(Calendar.SECOND))
    //endregion

    //region Data
    fun getData(): Date? = calendar?.time

    fun getData(format: String): String {
        val dateFormat = SimpleDateFormat(format)
        return dateFormat.format(calendar?.time)
    }

    fun getTryDate(out: String?): String? = this.getTryDate(out, "dd/MM/yyyy HH:mm:ss")
    fun getTryDate(out: String?, format: String): String? {
        return try {
            val hora = SimpleDateFormat(format)
            hora.isLenient = false
            hora.format(this.calendar?.time)
        } catch (e: Exception) {
            out
        }
    }

    //endregion

    fun getTimeInMillis(): Long = Coalesce(0, calendar?.timeInMillis)

    fun isNull(): Boolean = calendar == null

    override fun toString(): String = this.getTryDate("", "dd/MM/yyyy HH:mm:ss")!!
    fun toShortDate(): String = this.getTryDate("", "dd/MM/yyyy")!!
    fun toShortTime(): String = this.getTryDate("", "HH:mm:ss")!!

    fun alter(dia: Int, mes: Int, ano: Int): DateTime = alter(dia, mes, ano, getHora(), getMinuto())
    fun alter(hora: Int, minuto: Int): DateTime =
        alter(getDia(), Coalesce(0, calendar?.get(Calendar.MONTH)), getAno(), hora, minuto)

    fun alter(dia: Int, mes: Int, ano: Int, hora: Int, minuto: Int): DateTime {

        if (calendar == null) calendar = Calendar.getInstance()

        if (dia > 0) calendar!!.set(Calendar.DAY_OF_MONTH, dia)
        if (mes > -1) calendar!!.set(Calendar.MONTH, mes)
        if (dia > 0) calendar!!.set(Calendar.YEAR, ano)

        calendar!!.set(Calendar.HOUR, hora)
        calendar!!.set(Calendar.MINUTE, minuto)
        return this
    }

    fun alter(date: Date, vararg calendars: Int): DateTime {
        val cal = Calendar.getInstance()
        cal.time = date

        calendar?.set(Calendar.SECOND, 0)
        calendars.forEach { calendar?.set(it, cal.get(it)) }

        return this
    }

    fun alter(valor: String): DateTime {
        try {
            if (calendar == null) calendar = Calendar.getInstance()
            calendar!!.clear()
            calendar!!.time = SimpleDateFormat("dd/MM/yyyy").parse(valor)

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return this
    }

    fun Now(): DateTime {
        calendar = Calendar.getInstance()
        return this
    }

    operator fun set(field: Int, value: Int): DateTime {
        if (this.calendar == null) calendar = Calendar.getInstance()
        calendar!!.set(field, value)
        return this
    }


    fun add(field: Int, value: Int): DateTime {
        calendar?.add(field, value)
        return this
    }

    fun setAcrescentaMes(quantidade: Int): DateTime {
        if (quantidade > 0)
            calendar?.add(Calendar.MONTH, quantidade - 1)
        return this
    }

    fun getUltimoDia(): DateTime {
        calendar?.set(Calendar.DAY_OF_MONTH, calendar!!.getActualMaximum(Calendar.DAY_OF_MONTH))
        return this
    }

    @Throws(ParseException::class)
    private fun parse(dateString: String, dateFormat: String): Date {
        return try {
            val simpleDateFormat = SimpleDateFormat(dateFormat)
            simpleDateFormat.isLenient = false
            simpleDateFormat.parse(dateString)
        } catch (_: Exception) {
            Date(dateString)
        }
    }

    private fun determineDateFormat(dateString: String): String? {
        return DATE_FORMAT_REGEXPS.keys.firstOrNull { dateString.toLowerCase().matches(it.toRegex()) }
            ?.let { DATE_FORMAT_REGEXPS[it] }
        /*
        for (regexp in DATE_FORMAT_REGEXPS.keys) {
            if (dateString.toLowerCase().matches(regexp.toRegex())) {
                return DATE_FORMAT_REGEXPS[regexp]
            }
        }
        */
    }

    fun getDiffInMinuts(valor: DateTime): Long {
        return this.getDiffInMinuts(valor.getData()!!)
    }

    fun getDiffInMinuts(ate: Date): Long {
        val diff = ate.time - this.getData()!!.time
        return diff / (60 * 1000)
    }


    fun getDiff(ate: DateTime, calendarField: Int): Long {
        val diff = ate.getData()!!.time - this.getData()!!.time
        return when (calendarField) {
            Calendar.YEAR -> TimeUnit.MILLISECONDS.toDays(diff) / 365
            Calendar.DAY_OF_MONTH -> TimeUnit.MILLISECONDS.toDays(diff)
            Calendar.HOUR -> TimeUnit.MILLISECONDS.toHours(diff)
            Calendar.MINUTE -> TimeUnit.MILLISECONDS.toMinutes(diff)
            Calendar.SECOND -> TimeUnit.MILLISECONDS.toSeconds(diff)
            else -> diff
        }
    }


    fun getTimeDifference(valor: DateTime): LongArray? {

        if (this.calendar == null) return null

        val result = LongArray(5)
        val t1 = this.calendar!!.timeInMillis

        var diff = Math.abs(valor.getTimeInMillis() - t1)
        val ONE_DAY = 1000 * 60 * 60 * 24
        val ONE_HOUR = ONE_DAY / 24
        val ONE_MINUTE = ONE_HOUR / 60
        val ONE_SECOND = ONE_MINUTE / 60

        val d = diff / ONE_DAY
        diff %= ONE_DAY.toLong()

        val h = diff / ONE_HOUR
        diff %= ONE_HOUR.toLong()

        val m = diff / ONE_MINUTE
        diff %= ONE_MINUTE.toLong()

        val s = diff / ONE_SECOND
        val ms = diff % ONE_SECOND
        result[0] = d
        result[1] = h
        result[2] = m
        result[3] = s
        result[4] = ms

        return result
    }

    private val DATE_FORMAT_REGEXPS = object : HashMap<String, String>() {
        init {
            put("^\\d{8}$", "yyyyMMdd")
            put("^\\d{1,2}-\\d{1,2}-\\d{4}$", "dd-MM-yyyy")
            put("^\\d{4}-\\d{1,2}-\\d{1,2}$", "yyyy-MM-dd")
            put("^\\d{1,2}/\\d{1,2}/\\d{4}$", "MM/dd/yyyy")
            put("^\\d{4}/\\d{1,2}/\\d{1,2}$", "yyyy/MM/dd")
            put("^\\d{1,2}/\\d{1,2}/\\d{4}$", "dd/MM/yyyy")
            put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}$", "dd MMM yyyy")
            put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}$", "dd MMMM yyyy")
            put("^\\d{12}$", "yyyyMMddHHmm")
            put("^\\d{8}\\s\\d{4}$", "yyyyMMdd HHmm")
            put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}$", "dd-MM-yyyy HH:mm")
            put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy-MM-dd HH:mm")
            put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}$", "MM/dd/yyyy HH:mm")
            put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy/MM/dd HH:mm")
            put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}$", "dd/MM/yyyy HH:mm")
            put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMM yyyy HH:mm")
            put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMMM yyyy HH:mm")
            put("^\\d{14}$", "yyyyMMddHHmmss")
            put("^\\d{8}\\s\\d{6}$", "yyyyMMdd HHmmss")
            put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd-MM-yyyy HH:mm:ss")
            put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}.\\d{3}$", "dd-MM-yyyy HH:mm:ss.SSS")
            put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy-MM-dd HH:mm:ss")
            put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}.\\d{3}$", "yyyy-MM-dd HH:mm:ss.SSS")
            put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "MM/dd/yyyy HH:mm:ss")
            put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}.\\d{3}$", "MM/dd/yyyy HH:mm:ss.SSS")
            put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy/MM/dd HH:mm:ss")
            put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}.\\d{3}$", "yyyy/MM/dd HH:mm:ss.SSS")
            put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd/MM/yyyy HH:mm:ss")
            put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}.\\d{3}$", "dd/MM/yyyy HH:mm:ss.SSS")
            put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMM yyyy HH:mm:ss")
            put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}.\\d{3}$", "dd MMM yyyy HH:mm:ss.SSS")
            put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMMM yyyy HH:mm:ss")
            put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}.\\d{3}$", "dd MMMM yyyy HH:mm:ss.SSS")

            put("^[a-z]{3}\\s\\d{1,2}[,]\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}\$", "MMM dd, yyyy HH:mm:ss")
            put("^[a-z]{3}\\s\\d{1,2}[,]\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}.\\d{3}\$", "MMM dd, yyyy HH:mm:ss.SSS")
            put("^[a-z]{4,}\\s\\d{1,2}[,]\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}\$", "MMMM dd, yyyy HH:mm:ss")
            put("^[a-z]{4,}\\s\\d{1,2}[,]\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}.\\d{3}\$", "MMMM dd, yyyy HH:mm:ss.SSS")

            put("^[a-z]{3}\\s\\d{1,2}[,]\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}\$", "MMM dd, yyyy hh:mm:ss")
            put("^[a-z]{3}\\s\\d{1,2}[,]\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}.\\d{3}\$", "MMM dd, yyyy hh:mm:ss.SSS")
            put("^[a-z]{4,}\\s\\d{1,2}[,]\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}\$", "MMMM dd, yyyy hh:mm:ss")
            put("^[a-z]{4,}\\s\\d{1,2}[,]\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}.\\d{3}\$", "MMMM dd, yyyy hh:mm:ss.SSS")

            put("^[a-z]{3}\\s\\d{1,2}[,]\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}\\s[AMPamp]{2}\$", "MMM dd, yyyy hh:mm:ss aa")
            put(
                "^[a-z]{4,}\\s\\d{1,2}[,]\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}\\s[AMPamp]{2}\$",
                "MMMM dd, yyyy hh:mm:ss aa"
            )

            put("^\\d{1,2}:\\d{2}$", "HH:mm")
            put("^\\d{1,2}:\\d{2}:\\d{2}$", "HH:mm:ss")
            put("^\\d{1,2}:\\d{2}:\\d{2}.\\d{3}$", "HH:mm:ss.SSS")
        }
    }


    public fun <T> Coalesce(default: T, vararg value: T?): T {

        var retorno: T = default
        for (i in value) if (i != null) {
            retorno = i
            break
        }

        return retorno
    }

    @SuppressLint("RestrictedApi")
    fun getVectorDrawableToBitmap(context: Context, drawableId: Int): Bitmap {
        var drawable = AppCompatDrawableManager.get().getDrawable(context, drawableId)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = DrawableCompat.wrap(drawable).mutate()
        }

        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }


    companion object {
        fun isDataInicioMenorQueDataFim(dataInicio: DateTime?, dataFim: DateTime?): Boolean {
            return dataInicio?.getData()?.after(dataFim?.getData()) == true
        }
    }
}