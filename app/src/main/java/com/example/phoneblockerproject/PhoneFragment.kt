package com.example.phoneblockerproject

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CallLog
import android.provider.ContactsContract
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.phoneblockerproject.databass.DBHelper
import java.io.IOException
import java.io.InputStream
import java.lang.Long
import java.util.*
import kotlin.collections.ArrayList


class PhoneFragment : Fragment() {
    var recyclerView: RecyclerView? = null
    var imgaddphone:ImageView?=null
    var imgback:ImageView?=null
    val data = ArrayList<Data>()
    var searchView:SearchView?=null
    var txtempty:TextView?=null
    var phonenumber:String?=null
    var phonename:String?=null

    //box
    var editTextPhone :EditText?=null
    var editTextName:EditText?=null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root =inflater.inflate(R.layout.fragment_phone, container, false)
        imgaddphone = root.findViewById(R.id.imgaddphone)
        recyclerView = root.findViewById(R.id.recyclerView)
        imgback = root.findViewById(R.id.imgback)
        searchView =root.findViewById(R.id.SearchView)
        txtempty = root.findViewById(R.id.txtemtry)
       var a = getCalllog()
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                var title = newText
                allPhone(title)
                return false
            }
        })


        imgaddphone?.setOnClickListener(){
            showCustomDialog()
        }
        allPhone("")

        imgback?.setOnClickListener(){
            val fragmentTransaction = requireActivity().
            supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.nav_host_fragment, HomeFragment())
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

        return root
    }

    private lateinit var alertDialog: AlertDialog
    fun showCustomDialog() {
        val inflater: LayoutInflater = this.getLayoutInflater()
        val dialogView: View = inflater.inflate(R.layout.popup_add_phone, null)
         editTextPhone = dialogView.findViewById(R.id.editTextPhone)
         editTextName = dialogView.findViewById(R.id.editTextName)
        val btnConfirm: Button = dialogView.findViewById(R.id.btnConfirm)
        var btncancel :Button=dialogView.findViewById(R.id.btnConfirm2)
        var imgcontext:ImageButton=dialogView.findViewById(R.id.imgcontext)
        var imgphonehis:ImageButton=dialogView.findViewById(R.id.imgcontext2)

        imgcontext.setOnClickListener {
            requestPermission()
        }
        imgphonehis.setOnClickListener {

            showCustomDialogCon()
        }

        btnConfirm.setOnClickListener {
            if(!editTextPhone?.text.toString().isNullOrEmpty()){
                if (editTextName?.text.toString().isNullOrEmpty()){editTextName?.setText("ไม่มีชื่อ")}
                val db = DBHelper(requireContext())
                db.addPhone(editTextName?.text.toString(), editTextPhone?.text.toString())
                allPhone("")
                alertDialog.dismiss()
            }else{
                Toast.makeText(context, "ระบุเบอร์!!", Toast.LENGTH_LONG).show()
            }
        }
        btncancel.setOnClickListener {
            alertDialog.dismiss()
        }
        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        dialogBuilder.setOnDismissListener { }
        dialogBuilder.setView(dialogView)

        alertDialog = dialogBuilder.create();
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
    }


    class Data(
        var id: String, var name: String, var phoneNumber: String
    )


    internal inner class DataAdapter(private val list: List<Data>) :
            RecyclerView.Adapter<DataAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view: View = LayoutInflater.from(parent.context).inflate(
                R.layout.item_phone,
                parent, false
            )
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val data = list[position]
            holder.data = data
            holder.txtname.text = data.name
            holder.txtphone.text = data.phoneNumber
            holder.delete.setOnClickListener {
                deletePhone(data.phoneNumber, data.id)
            }

        }

        override fun getItemCount(): Int {
            return list.size
        }

        internal inner class ViewHolder(itemView: View) :
                RecyclerView.ViewHolder(itemView) {

            var data: Data? = null
            var txtname: TextView = itemView.findViewById(R.id.txtname)
            var txtphone: TextView = itemView.findViewById(R.id.txtphone)
            var delete:ImageButton = itemView.findViewById(R.id.deleteimgbuttom)

        }
    }

    @SuppressLint("Range")
    fun allPhone(text: String){
        data.clear()
        val db = DBHelper(requireContext())
        val cur =db.getPhone(text)
        while (cur!!.moveToNext()) {
            data.add(Data(cur.getString(0), cur.getString(1), cur.getString(2)))
        }
        if(!data.isNullOrEmpty()){
            txtempty?.visibility = View.GONE
        }else{
            txtempty?.visibility = View.VISIBLE
        }
        recyclerView!!.adapter = DataAdapter(data)
    }

    fun deletePhone(phone: String, idp: String){
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("ต้องการจะลบ เบอร์ $phone หรือไม่?")
            .setCancelable(false)
            .setPositiveButton("ใช่") { dialog, id ->
                // Delete selected note from database
                val db = DBHelper(requireContext())
                db.deletePhone(idp)
                allPhone("")
                Toast.makeText(context, "สำเร็จ", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("ยกเลิก") { dialog, id ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }

    fun requestPermission(){
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ){
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.READ_CONTACTS),
                1
            )
        }else{
            val intent = Intent(
                Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            )
            startActivityForResult(intent, 125)

        }
    }
    @SuppressLint("Range")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (android.R.attr.data != null) {
                val result: Uri = data!!.data!!
                val c = requireContext().contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone._ID + "=?",
                    arrayOf(result.lastPathSegment),
                    null
                )
                if (c!!.count >= 1 && c!!.moveToFirst()) {
                    phonenumber = c!!.getString(c!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    phonename = c!!.getString(c!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                    if ((phonenumber != "" || phonename != "")) {

                        editTextName?.setText(phonename)
                        editTextPhone?.setText(phonenumber)
                    }
                }
            }
        }else{

        }

    }
    class Call(number: String, type: String, date: String, duration: String,name:String)
    private fun getCalllog():ArrayList<Call> {
        var call= ArrayList<Call>()
        val cursor: Cursor? = requireContext().contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null, null, null, CallLog.Calls.DATE + " DESC"
        )
        val number: Int = cursor!!.getColumnIndex(CallLog.Calls.NUMBER)
        val type: Int = cursor.getColumnIndex(CallLog.Calls.TYPE)
        val date: Int = cursor.getColumnIndex(CallLog.Calls.DATE)
        val duration: Int = cursor.getColumnIndex(CallLog.Calls.DURATION)
        val Name: Int = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
        var i=0

            while (cursor.moveToNext()) {
                val phNumber: String = cursor.getString(number)
                val callType: String = cursor.getString(type)
                val callDate: String = cursor.getString(date)
                val callDayTime = Date(Long.valueOf(callDate))
                var name :String="ไม่มีชื่อ"
                if (cursor.getString(Name)!=null){
                     name  = cursor.getString(Name)
                }
                val callDuration: String = cursor.getString(duration)
                var dir: String? = null
                val dircode = callType.toInt()
                when (dircode) {
                    CallLog.Calls.OUTGOING_TYPE -> dir = "OUTGOING"
                    CallLog.Calls.INCOMING_TYPE -> dir = "INCOMING"
                    CallLog.Calls.MISSED_TYPE -> dir = "MISSED"
                }
                if (i==100){break}

                 call.add(Call(phNumber, dir.toString(), callDate, callDuration,name))
                i++

            }


        cursor.close()
        return call
    }

    @SuppressLint("ResourceAsColor")
    fun showCustomDialogCon() {
        var view: View = layoutInflater.inflate(R.layout.fragment_read_contact,null)
        var back:ImageView = view.findViewById(R.id.imgbackc)

        var dialog = Dialog(requireContext(),android.R.style.ThemeOverlay_DeviceDefault_Accent_DayNight)
        dialog.setContentView(view)
        back?.setOnClickListener {
            Log.d("testt","testt")
            dialog.dismiss()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            dialog.window?.statusBarColor = R.color.purple_500;
        }
        dialog.show()

    }

}