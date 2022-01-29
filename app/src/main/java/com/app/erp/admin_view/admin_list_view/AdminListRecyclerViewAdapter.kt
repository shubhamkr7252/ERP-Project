package com.app.erp.admin_view.admin_list_view

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.erp.R

class AdminListRecyclerViewAdapter(private val adminList: ArrayList<AdminListRecyclerViewDataClass>,
                                   val context: Context) :

RecyclerView.Adapter<AdminListRecyclerViewAdapter.ViewHolderAdmin>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderAdmin {
        return (ViewHolderAdmin(
            LayoutInflater.from(context).inflate(R.layout.recycler_view_admin_list_item, parent, false))
                )
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolderAdmin, position: Int) {

        holder.tvName.text = (position+1).toString().plus(". ").plus(adminList[position].Name)
        holder.tvCode.text = adminList[position].Code
        holder.tvGender.text = adminList[position].Gender.plus(", ").plus(adminList[position].Age)
        holder.tvEmail.text = adminList[position].Email
        holder.tvPhone.text = adminList[position].Phone.toString()
    }

    override fun getItemCount(): Int {
        return adminList.size
    }

    public class ViewHolderAdmin(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tv_name)
        val tvCode: TextView = view.findViewById(R.id.tv_code)
        val tvGender: TextView = view.findViewById(R.id.tv_gender)
        val tvEmail: TextView = view.findViewById(R.id.tv_email)
        val tvPhone: TextView = view.findViewById(R.id.tv_phone)
    }
}

