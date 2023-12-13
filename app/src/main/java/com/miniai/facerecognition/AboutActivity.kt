package com.miniai.facerecognition

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import mehdi.sakout.aboutpage.AboutPage
import mehdi.sakout.aboutpage.Element
import java.util.Calendar

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val aboutPage = AboutPage(this)
//            .isRTL(false)
            .setImage(R.drawable.logo_name)
            .setDescription("MiniAiLive is a provider of Touchless Biometrics Authentication, ID verification solutions. We offer strong security solutions with cutting-edge technologies for facial recognition, liveness detection, and ID document recognition. We also ensure that these solutions seamlessly integrate with our clients’ existing systems.\n")
            .addWebsite("https://www.miniai.live/")
            .addYoutube("UCU3D895D0XiF4TGy02GhN_Q")
            .addGitHub("MiniAiLive")
            .addEmail("info@miniai.live")
            .addItem(getCopyRightsElement())
//            .addFacebook("")
//            .addTwitter("")
//            .addPlayStore("com.x.x")
//            .addInstagram("")
            .create()
        setContentView(aboutPage)
    }

    fun getCopyRightsElement(): Element? {
        val copyRightsElement = Element()
        val copyrights = String.format(
            getString(R.string.copy_right),
            Calendar.getInstance()[Calendar.YEAR]
        )
        copyRightsElement.setTitle(copyrights)
        copyRightsElement.setIconDrawable(R.drawable.about_icon_copy_right)
        copyRightsElement.autoApplyIconTint = true
        copyRightsElement.setIconTint(mehdi.sakout.aboutpage.R.color.about_item_icon_color)
        copyRightsElement.iconNightTint = android.R.color.white
        copyRightsElement.gravity = Gravity.CENTER
        copyRightsElement.onClickListener = View.OnClickListener {
            Toast.makeText(this, copyrights, Toast.LENGTH_SHORT).show()
        }
        return copyRightsElement
    }
}
