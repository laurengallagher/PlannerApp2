package org.wit.plannerapp2.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_edit.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.wit.plannerapp2.R
import org.wit.plannerapp2.api.PlannerWrapper
import org.wit.plannerapp2.main.PlannerApp
import org.wit.plannerapp2.models.PlannerModel
import org.wit.plannerapp2.utils.createLoader
import org.wit.plannerapp2.utils.hideLoader
import org.wit.plannerapp2.utils.serviceUnavailableMessage
import org.wit.plannerapp2.utils.showLoader
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditFragment : Fragment(), Callback<PlannerWrapper>, AnkoLogger {

    lateinit var app: PlannerApp
    lateinit var loader : AlertDialog
    lateinit var root: View
    var editPlanner: PlannerModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as PlannerApp

        arguments?.let {
            editPlanner = it.getParcelable("editplanner")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_edit, container, false)
        activity?.title = getString(R.string.action_edit)
        loader = createLoader(activity!!)

        root.editAmount.setText(editPlanner!!.amount.toString())
        root.editPaymenttype.setText(editPlanner!!.paymenttype)
        root.editMessage.setText(editPlanner!!.message)
        root.editUpvotes.setText(editPlanner!!.upvotes.toString())

        root.editUpdateButton.setOnClickListener {
            showLoader(loader, "Updating Planner on Server...")
            updatePlannerData()
           // var callUpdate = app.plannerService.put(app.auth.currentUser?.email,
             //   (editPlanner as PlannerModel)._id ,editPlanner as PlannerModel)
           // callUpdate.enqueue(this)
        }

        return root
    }


    companion object {
        @JvmStatic
        fun newInstance(planner: PlannerModel) =
            EditFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("editplanner",planner)
                }
            }
    }

    override fun onFailure(call: Call<PlannerWrapper>, t: Throwable) {
        info("Retrofit Error : $t.message")
        serviceUnavailableMessage(activity!!)
        hideLoader(loader)
    }

    override fun onResponse(call: Call<PlannerWrapper>, response: Response<PlannerWrapper>) {
        hideLoader(loader)
        activity!!.supportFragmentManager.beginTransaction()
            .replace(R.id.homeFrame, ReportFragment.newInstance())
            .addToBackStack(null)
            .commit()
    }

    fun updatePlannerData() {
        editPlanner!!.amount = root.editAmount.text.toString().toInt()
        editPlanner!!.message = root.editMessage.text.toString()
        editPlanner!!.upvotes = root.editUpvotes.text.toString().toInt()
    }
}
