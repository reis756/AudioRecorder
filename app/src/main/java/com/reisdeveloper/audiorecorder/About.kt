package com.reisdeveloper.audiorecorder

import android.R.color
import android.content.res.Configuration
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import mehdi.sakout.aboutpage.AboutPage
import mehdi.sakout.aboutpage.Element
import java.lang.String
import java.util.*


class About : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        simulateDayNight( /* DAY */0)
        val adsElement = Element()
        adsElement.title = "Advertise with us"

        val aboutPage = AboutPage(this)
            .isRTL(false)
            .setImage(R.mipmap.ic_launcher)
            .addItem(Element().setTitle("Version 6.2"))
            .addGroup(getString(R.string.connect_us))
            .addEmail("reis756004@gmail.com")
            .addYoutube("UCdPQtdWIsg7_pi4mrRu46vA")
            .addPlayStore("com.ideashower.readitlater.pro")
            .addGitHub("reis756")
            .addItem(getCopyRightsElement())
            .create()

        setContentView(aboutPage)

    }

    private fun getCopyRightsElement(): Element? {
        val copyRightsElement = Element()
        val copyrights = String.format(
            getString(R.string.copy_right),
            Calendar.getInstance().get(Calendar.YEAR)
        )
        copyRightsElement.setTitle(copyrights)
        //copyRightsElement.setIconDrawable(R.drawable.about_icon_copy_right)
        copyRightsElement.setIconTint(mehdi.sakout.aboutpage.R.color.about_item_icon_color)
        copyRightsElement.iconNightTint = color.white
        copyRightsElement.gravity = Gravity.CENTER
        copyRightsElement.onClickListener =
            View.OnClickListener { Toast.makeText(this@About, copyrights, Toast.LENGTH_SHORT).show() }
        return copyRightsElement
    }

    private fun simulateDayNight(currentSetting: Int) {
        val DAY = 0
        val NIGHT = 1
        val FOLLOW_SYSTEM = 3
        val currentNightMode = (resources.configuration.uiMode
                and Configuration.UI_MODE_NIGHT_MASK)
        if (currentSetting == DAY && currentNightMode != Configuration.UI_MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )
        } else if (currentSetting == NIGHT && currentNightMode != Configuration.UI_MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
            )
        } else if (currentSetting == FOLLOW_SYSTEM) {
            AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            )
        }
    }

}