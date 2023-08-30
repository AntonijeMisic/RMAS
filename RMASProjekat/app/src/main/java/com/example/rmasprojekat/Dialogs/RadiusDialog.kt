package com.example.rmasprojekat.Dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.*
import androidx.annotation.LayoutRes
import com.example.rmasprojekat.R
import com.example.rmasprojekat.databinding.RadiusDialogBinding

class RadiusDialog(context: Context): Dialog(context) {
    private  lateinit var radiusDialogView:View
    public lateinit var radiusBinding: RadiusDialogBinding

    public fun open(@LayoutRes layout: Int)
    {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        radiusDialogView = LayoutInflater.from(context).inflate(layout, null)
        radiusBinding = RadiusDialogBinding.inflate(LayoutInflater.from(context))
        this.setContentView(radiusBinding.root)
        this.show()
        this.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        this.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        this.window?.attributes?.windowAnimations = R.style.DialogAnimation
        this.window?.setGravity(Gravity.BOTTOM)
    }
}