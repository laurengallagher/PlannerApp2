package org.wit.plannerapp2.main

import android.app.Application
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.StorageReference
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info


import org.wit.plannerapp2.api.PlannerService
import org.wit.plannerapp2.models.PlannerModel


class PlannerApp : Application(), AnkoLogger {


    lateinit var plannerService: PlannerService
    var planners = ArrayList<PlannerModel>()

    // [START declare_auth]
    lateinit var auth: FirebaseAuth
    // [END declare_auth]

    lateinit var database: DatabaseReference

    lateinit var storage: StorageReference

    lateinit var userImage: Uri

    override fun onCreate() {
        super.onCreate()
        info("Planner App started")
        plannerService = PlannerService.create()
        info("Planner Service Created")
    }
}