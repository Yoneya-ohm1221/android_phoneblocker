package com.example.phoneblockerproject.Fragment

import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.CallLog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.phoneblockerproject.MainActivity
import com.example.phoneblockerproject.R
import com.example.phoneblockerproject.databass.DBHelper
import java.lang.Long
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class HistoryFragment : Fragment() {
    private var data = ArrayList<Data>()
    var phoneblock = ArrayList<String>()
    private var dataServer = ArrayList<Data>()
    var recyclerView: RecyclerView? = null
    private var btnhisphone:Button?=null
    private var btnhismessage:Button?=null
    //popupmenu
    var conDelete:ConstraintLayout?=null
    var conBlock:ConstraintLayout?=null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val root =  inflater.inflate(R.layout.fragment_history, container, false)
        recyclerView = root.findViewById(R.id.recyclerView)
        btnhisphone = root.findViewById(R.id.btnhisphone)
        btnhisphone?.setOnClickListener{
            val fragmentTransaction = requireActivity().
            supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.nav_host_fragment, HistoryPhoneFragment())
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

        btnhismessage = root.findViewById(R.id.btnhismessage)
        btnhismessage?.setOnClickListener{
            val fragmentTransaction = requireActivity().
            supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.nav_host_fragment, HistoryMessageFragment())
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

        var a =MainActivity.data



        val db = DBHelper(requireContext())
        phoneblock = db.getBlocknumber()
        getdata()
        return root
    }

    private fun getdata(){
        data.clear()
        data = getCalllog()
        recyclerView!!.adapter = DataAdapter(data)
    }


    class Data(var id:String,var number: String, var type: String, var date: String, var duration: String, var name: String)

    internal inner class DataAdapter(private val list: List<Data>) :
            RecyclerView.Adapter<DataAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view: View = LayoutInflater.from(parent.context).inflate(
                R.layout.item_history,
                    parent, false
            )
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            Log.d("txt", "x3")
            val data = list[position]
            holder.data = data
            holder.txtname2.text = data.name
            holder.txtphone2.text = data.number
            holder.txtdate.text = data.date
            holder.conmenu.setOnLongClickListener {
                showCustomDialog()
                conDelete?.setOnClickListener {
                    DeleteCallById(data.id,data.number)
                    alertDialog.dismiss()
                }
                conBlock?.setOnClickListener {
                    blockcall(data.name,data.number)
                    alertDialog.dismiss()
                }

                return@setOnLongClickListener true
            }
            if(data.type=="MISSED"){
                holder.txttype.text="สายที่ไม่ได้รับ"
            }else{
                holder.txttype.text="ระยะเวลาโทร "+data.duration
            }
            holder.imgicon.visibility = View.GONE
           if (data.number in phoneblock){
              holder.imgpro.setImageResource(R.drawable.user_block);
           }else{
               holder.imgpro.setImageResource(R.drawable.user);
           }


        }


        override fun getItemCount(): Int {
            return list.size
        }

        internal inner class ViewHolder(itemView: View) :
                RecyclerView.ViewHolder(itemView) {

            var data: Data? = null
            var txtname2: TextView = itemView.findViewById(R.id.txtname2)
            var txtphone2: TextView = itemView.findViewById(R.id.txtphone2)
            var txtdate: TextView = itemView.findViewById(R.id.txtdate)
            var conmenu: ConstraintLayout = itemView.findViewById(R.id.conmenu)
            var imgicon: ImageView = itemView.findViewById(R.id.imgicon)
            var imgicon2: ImageView = itemView.findViewById(R.id.imgicon2)
            var txttype :TextView=itemView.findViewById(R.id.txtyype)
            var imgpro :ImageView=itemView.findViewById(R.id.imageView2)
        }
    }

    private lateinit var alertDialog: AlertDialog
    fun showCustomDialog() {
        val inflater: LayoutInflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.popup_menu_history, null)
        conDelete=dialogView.findViewById(R.id.constraintdelete)
        var conReport:ConstraintLayout=dialogView.findViewById(R.id.constraintreport)
        conBlock=dialogView.findViewById(R.id.constraintgolist)


        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        dialogBuilder.setOnDismissListener { }
        dialogBuilder.setView(dialogView)


        alertDialog = dialogBuilder.create();
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
    }

    private fun getCalllog():ArrayList<Data> {
        var call= ArrayList<Data>()
        val cursor: Cursor? = requireContext().contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                null, null, null, CallLog.Calls.DATE + " DESC"
        )
        val number: Int = cursor!!.getColumnIndex(CallLog.Calls.NUMBER)
        val type: Int = cursor.getColumnIndex(CallLog.Calls.TYPE)
        val date: Int = cursor.getColumnIndex(CallLog.Calls.DATE)
        val duration: Int = cursor.getColumnIndex(CallLog.Calls.DURATION)
        val name: Int = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
        val id: Int = cursor.getColumnIndex(CallLog.Calls._ID)
        var i=0

        while (cursor.moveToNext()) {
            val phNumber: String = cursor.getString(number)
            val callType: String = cursor.getString(type)
            val callDate: String = cursor.getString(date)
            val callid: String = cursor.getString(id)
            val callDayTime = Date(Long.valueOf(callDate))
            val dateFormated = SimpleDateFormat("dd/MM/yyyy HH:mm").format(callDayTime)
            var name1 :String="ไม่มีชื่อ"
            if (cursor.getString(name)!=null){
                name1  = cursor.getString(name)
            }
            val callDuration: String = cursor.getString(duration)
            var second = callDuration.toLong()
            var  minutes = (second % 3600) / 60;
            var seconds = second % 60;
            var fomat:String=""
            if(second<60){
                fomat = seconds.toString()+ "วินาที "
            }else if(second>60){
                fomat = minutes.toString()+ "นาที "+seconds.toString()+" วินาที"
            }
            var dir: String? = null
            when (callType.toInt()) {
                CallLog.Calls.OUTGOING_TYPE -> dir = "OUTGOING"
                CallLog.Calls.INCOMING_TYPE -> dir = "INCOMING"
                CallLog.Calls.MISSED_TYPE -> dir = "MISSED"
            }
            if (i==100){break}

            call.add(Data(callid,phNumber, dir.toString(), dateFormated.toString(), fomat, name1))
            i++

        }


        cursor.close()
        return call
    }

    fun blockcall(name:String,phone:String){

        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("ต้องการที่จะบล็อกเบอร์ $phone หรือไม่?")
            .setCancelable(false)
            .setPositiveButton("ใช่") { _, _ ->
                val db = DBHelper(requireContext())
                db.addPhone(name,phone)
                Toast.makeText(context, "สำเร็จ", Toast.LENGTH_LONG).show()
                phoneblock.clear()
                phoneblock = db.getBlocknumber()
                getdata()
            }
            .setNegativeButton("ยกเลิก") { dialog, _ ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }

    fun DeleteCallById(idd: String,phone:String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("ต้องการจะลบบันทึกเบอร์ $phone หรือไม่?")
                .setCancelable(false)
                .setPositiveButton("ใช่") { _, _ ->
                    // Delete selected note from database
                    requireContext().contentResolver.delete(CallLog.Calls.CONTENT_URI, CallLog.Calls._ID + " = ? ", arrayOf(idd))
                    Toast.makeText(context, "สำเร็จ", Toast.LENGTH_LONG).show()
                    getdata()
                }
                .setNegativeButton("ยกเลิก") { dialog, id ->

                    dialog.dismiss()
                }
        val alert = builder.create()
        alert.show()

    }


}