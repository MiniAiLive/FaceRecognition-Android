package com.miniai.facerecognition

import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        findViewById<TextView>(R.id.txtMail).setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "plain/text"
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("info@miniai.live"))
            intent.putExtra(Intent.EXTRA_SUBJECT, "License Request")
            intent.putExtra(Intent.EXTRA_TEXT, "")
            startActivity(Intent.createChooser(intent, ""))
        }

        findViewById<TextView>(R.id.txtWhatsapp).setOnClickListener {
            val general = Intent(Intent.ACTION_VIEW, Uri.parse("https://com.whatsapp/miniailive"))
            val generalResolvers: HashSet<String> = HashSet()
            val generalResolveInfo: List<ResolveInfo> = packageManager.queryIntentActivities(general, 0)
            for (info in generalResolveInfo) {
                if (info.activityInfo.packageName != null) {
                    generalResolvers.add(info.activityInfo.packageName)
                }
            }

            val telegram = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/19167142374"))
            var goodResolver = 0

            val resInfo: List<ResolveInfo> = packageManager.queryIntentActivities(telegram, 0)
            if (!resInfo.isEmpty()) {
                for (info in resInfo) {
                    if (info.activityInfo.packageName != null && !generalResolvers.contains(info.activityInfo.packageName)) {
                        goodResolver++
                        telegram.setPackage(info.activityInfo.packageName)
                    }
                }
            }

            if (goodResolver != 1) {
                telegram.setPackage(null)
            }
            if (telegram.resolveActivity(packageManager) != null) {
                startActivity(telegram)
            }
        }

        findViewById<TextView>(R.id.txtTelegram).setOnClickListener {
            val general = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.com/miniailive"))
            val generalResolvers: HashSet<String> = HashSet()
            val generalResolveInfo: List<ResolveInfo> = packageManager.queryIntentActivities(general, 0)
            for (info in generalResolveInfo) {
                if (info.activityInfo.packageName != null) {
                    generalResolvers.add(info.activityInfo.packageName)
                }
            }

            val telegram = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/miniailive"))
            var goodResolver = 0

            val resInfo: List<ResolveInfo> = packageManager.queryIntentActivities(telegram, 0)
            if (!resInfo.isEmpty()) {
                for (info in resInfo) {
                    if (info.activityInfo.packageName != null && !generalResolvers.contains(info.activityInfo.packageName)) {
                        goodResolver++
                        telegram.setPackage(info.activityInfo.packageName)
                    }
                }
            }

            if (goodResolver != 1) {
                telegram.setPackage(null)
            }
            if (telegram.resolveActivity(packageManager) != null) {
                startActivity(telegram)
            }
        }
    }
}
