package org.wit.plannerapp2.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_report.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.wit.plannerapp2.R
import org.wit.plannerapp2.adapters.PlannerAdapter
import org.wit.plannerapp2.adapters.PlannerListener
import org.wit.plannerapp2.api.PlannerWrapper
import org.wit.plannerapp2.main.PlannerApp
import org.wit.plannerapp2.models.PlannerModel
import org.wit.plannerapp2.utils.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ReportFragment : Fragment(), AnkoLogger,
    Callback<List<PlannerModel>>,
    PlannerListener {

    lateinit var app: PlannerApp
    lateinit var loader : AlertDialog
    lateinit var root: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as PlannerApp
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_report, container, false)
        activity?.title = getString(R.string.action_report)

        root.recyclerView.setLayoutManager(LinearLayoutManager(activity))
        root.recyclerView.adapter = PlannerAdapter(app.planners,this)
        loader = createLoader(activity!!)
        setSwipeRefresh()

        val swipeDeleteHandler = object : SwipeToDeleteCallback(activity!!) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = root.recyclerView.adapter as PlannerAdapter
                adapter.removeAt(viewHolder.adapterPosition)
                deletePlanner((viewHolder.itemView.tag as PlannerModel)._id)
            }
        }
        val itemTouchDeleteHelper = ItemTouchHelper(swipeDeleteHandler)
        itemTouchDeleteHelper.attachToRecyclerView(root.recyclerView)

        val swipeEditHandler = object : SwipeToEditCallback(activity!!) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                onPlannerClick(viewHolder.itemView.tag as PlannerModel)
            }
        }
        val itemTouchEditHelper = ItemTouchHelper(swipeEditHandler)
        itemTouchEditHelper.attachToRecyclerView(root.recyclerView)

        return root
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ReportFragment().apply {
                arguments = Bundle().apply { }
            }
    }

    fun setSwipeRefresh() {
        root.swiperefresh.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                root.swiperefresh.isRefreshing = true
                getAllPlanners()
            }
        })
    }

    fun checkSwipeRefresh() {
        if (root.swiperefresh.isRefreshing) root.swiperefresh.isRefreshing = false
        getAllPlanners(app.auth.currentUser!!.uid)
    }

    override fun onFailure(call: Call<List<PlannerModel>>, t: Throwable) {
        info("Retrofit Error : $t.message")
        serviceUnavailableMessage(activity!!)
        checkSwipeRefresh()
        hideLoader(loader)
    }

    override fun onResponse(call: Call<List<PlannerModel>>, response: Response<List<PlannerModel>>) {
        serviceAvailableMessage(activity!!)
        info("Retrofit JSON = ${response.body()}")
        app.planners = response.body() as ArrayList<PlannerModel>
        root.recyclerView.adapter = PlannerAdapter(app.planners,this)
        root.recyclerView.adapter?.notifyDataSetChanged()
        checkSwipeRefresh()
        hideLoader(loader)
    }

    fun getAllPlanners(userId: String?) {
        showLoader(loader, "Downloading Planners from Firebase")
        var plannersList = ArrayList<PlannerModel>()
        app.database.child("user-planners").child(userId!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    info("Firebase Planner error : ${error.message}")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val children = snapshot!!.children
                    children.forEach {
                        val planner = it.getValue<PlannerModel>(PlannerModel::class.java!!)

                        plannersList.add(planner!!)
                        app.planners = plannersList
                        root.recyclerView.adapter =
                            PlannerAdapter(app.planners, this@ReportFragment)
                        root.recyclerView.adapter?.notifyDataSetChanged()
                        checkSwipeRefresh()
                        hideLoader(loader)
                        app.database.child("user-planners").child(userId!!).removeEventListener(this)
                    }
                }
            })
    }


    fun updateUserPlanner(userId: String, uid: String?, planner: PlannerModel) {
        app.database.child("user-planners").child(userId).child(uid!!)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.ref.setValue(planner)
                        activity!!.supportFragmentManager.beginTransaction()
                            .replace(R.id.homeFrame, ReportFragment.newInstance())
                            .addToBackStack(null)
                            .commit()
                        hideLoader(loader)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        info("Firebase Planner error : ${error.message}")
                    }
                })
    }

    fun updatePlanner(uid: String?, planner: PlannerModel) {
        app.database.child("planners").child(uid!!)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.ref.setValue(planner)
                        hideLoader(loader)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        info("Firebase Planner error : ${error.message}")
                    }
                })
    }


    fun deleteUserPlanner(userId: String, uid: String?) {
        app.database.child("user-planners").child(userId).child(uid!!)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.ref.removeValue()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        info("Firebase Planner error : ${error.message}")
                    }
                })
    }

    fun deletePlanner(uid: String?) {
        app.database.child("planners").child(uid!!)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.ref.removeValue()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        info("Firebase Planner error : ${error.message}")
                    }
                })
    }

    override fun onPlannerClick(planner: PlannerModel) {
        activity!!.supportFragmentManager.beginTransaction()
            .replace(R.id.homeFrame, EditFragment.newInstance(planner))
            .addToBackStack(null)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        getAllPlanners(app.auth.currentUser!!.uid)
    }
}
