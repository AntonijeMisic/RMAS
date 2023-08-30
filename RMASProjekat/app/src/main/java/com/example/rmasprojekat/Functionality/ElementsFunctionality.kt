package com.example.rmasprojekat.Functionality

import android.view.View
import android.widget.EditText
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class ElementsFunctionality() {

    public fun convertTextViewtoEditView(textView: TextView, editText: EditText, sadrzaj: String)
    {
        textView.visibility= View.GONE
        editText.visibility= View.VISIBLE
        editText.setText(sadrzaj)
    }
    public fun convertEditTextToTextView(textView: TextView, editText: EditText, sadrzaj: String)
    {
        editText.visibility= View.GONE
        textView.visibility= View.VISIBLE
        textView.text=sadrzaj
    }

    public fun stringToDate(dateString: String?): Date {
        val format = SimpleDateFormat("dd-MM-yyyy")
        val datum= format.parse(dateString!!)!!
        return datum
    }
    public fun dateToString(date: Date?): String{
        val format = SimpleDateFormat("dd-MM-yyyy")
        val datString = format.format(date!!)
        return datString
    }
    public fun isDateValidFormat(dateString: String?): Boolean
    {
        if (dateString == null) {
            return false
        }

        val format = SimpleDateFormat("dd-MM-yyyy")
        format.isLenient = false

        try {
            format.parse(dateString)
            return true
        } catch (e: Exception) {
            return false
        }
    }
    public fun isTimeValidFormat(timeString: String): Boolean
    {
        if (timeString == null) {
            return false
        }

        val format = SimpleDateFormat("HH:mm:ss")
        format.isLenient = false

        try {
            format.parse(timeString)
            return true
        } catch (e: Exception) {
            return false
        }
    }



    public fun getTipColor(tip: String): String
    {
        when{
            (tip=="Radna akcija") -> return "ljubicasta"
            (tip == "Prikupljanje novca") -> return "plava"
            (tip == "Volontiranje na festivalu") -> return "roze"
            (tip =="Humanitarna akcija") -> return "zelena"
            (tip =="Drugo")-> return "crvena"
            else -> return "crvena"
        }
    }
    public fun getTipVrednost(tip: String): Int
    {
        when{
            (tip=="Radna akcija") -> return 10
            (tip == "Prikupljanje novca") -> return 5
            (tip == "Volontiranje na festivalu") -> return 8
            (tip =="Humanitarna akcija") -> return 5
            (tip =="Drugo")-> return 2
            else -> return 0
        }
    }
}