package org.wit.plannerapp2.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_planner.*
import kotlinx.android.synthetic.main.fragment_planner.view.*
import kotlinx.android.synthetic.main.fragment_planner.view.progressBar
import kotlinx.android.synthetic.main.fragment_planner.view.totalSoFar
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.toast
import org.wit.plannerapp2.R
import org.wit.plannerapp2.api.PlannerWrapper
import org.wit.plannerapp2.main.PlannerApp
import org.wit.plannerapp2.models.PlannerModel
import org.wit.plannerapp2.utils.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.String.format


class PlannerFragment : Fragment(), AnkoLogger, Callback<List<PlannerModel>> {

    lateinit var app: PlannerApp
    var totalAdded = 0
    lateinit var loader : AlertDialog
    lateinit var eventListener : ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as PlannerApp
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_planner, container, false)
        loader = createLoader(activity!!)
        activity?.title = getString(R.string.action_planner)

        root.progressBar.max = 10000
        root.amountPicker.minValue = 1
        root.amountPicker.maxValue = 1000

        root.amountPicker.setOnValueChangedListener { _, _, newVal ->
            //Display the newly selected number to paymentAmount
            root.paymentAmount.setText("$newVal")
        }
        setButtonListener(root)
        return root;
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            PlannerFragment().apply {
                arguments = Bundle().apply {}
            }
    }

    fun setButtonListener( layout: View) {
        layout.plannerButton.setOnClickListener {
            val amount = if (layout.paymentAmount.text.isNotEmpty())
                layout.paymentAmount.text.toString().toInt() else layout.amountPicker.value
            if(totalAdded >= layout.progressBar.max)
                activity?.toast("Added Amount Exceeded!")
            else {
                val paymentmethod = if(layout.paymentMethod.checkedRadioButtonId == R.id.Direct) "Direct" else "Paypal"
                //addPlanner(PlannerModel(paymenttype = paymentmethod,amount = amount))
                writeNewPlanner(PlannerModel(paymenttype = paymentmethod, amount = amount,
                    email = app.auth.currentUser?.email))
            }
        }
    }


    override fun onResume() {
        super.onResume()
        getTotalAdded(app.auth.currentUser?.uid)
    }

    override fun onPause() {
        super.onPause()
        if(app.auth.uid != null)
            app.database.child("user-planners")
                .child(app.auth.currentUser!!.uid)
                .removeEventListener(eventListener)
    }

    fun writeNewPlanner(planner: PlannerModel) {
        // Create new planner at /planners & /planners/$uid
        showLoader(loader, "Adding Planner to Firebase")
        info("Firebase DB Reference : $app.database")
        val uid = app.auth.currentUser!!.uid
        val key = app.database.child("planners").push().key
        if (key == null) {
            info("Firebase Error : Key Empty")
            return
        }
        planner.uid = key
        val plannerValues = planner.toMap()

        val childUpdates = HashMap<String, Any>()
        childUpdates["/planners/$key"] = plannerValues
        childUpdates["/user-planners/$uid/$key"] = plannerValues

        app.database.updateChildren(childUpdates)
        hideLoader(loader)
    }




    fun getTotalAdded(userId: String?) {

        eventListener = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                info("Firebase Planner error : ${error.message}")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                totalAdded = 0
                val children = snapshot!!.children
                children.forEach {
                    val planner = it.getValue<PlannerModel>(PlannerModel::class.java!!)
                    totalAdded += planner!!.amount
                }
                progressBar.progress = totalAdded
                totalSoFar.text = format("$ $totalAdded")
            }
        }

        app.database.child("user-donations").child(userId!!)
            .addValueEventListener(eventListener)
    }


}
