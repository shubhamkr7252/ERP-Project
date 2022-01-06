package com.example.erp

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class TakeAttendanceListRecyclerViewAdapter(private val studentListRecyclerViewTake: ArrayList<TakeAttendanceListRecyclerViewDataClass>, val context: Context, private val listener: ViewHolderAttendance.Listener) :

    RecyclerView.Adapter<ViewHolderAttendance>() {
    private var selectedList: ArrayList<String> = ArrayList()
    private var unselectedList: ArrayList<String> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderAttendance {
        return (ViewHolderAttendance(
            LayoutInflater.from(context).inflate(R.layout.recycler_view_take_attendance_list_item, parent, false))
        )
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolderAttendance, position: Int) {
        holder.tvName.text = studentListRecyclerViewTake[position].Name
        holder.tvSrn.text = "SRN: ${studentListRecyclerViewTake[position].SRN}"
        if(!holder.checkbox.isChecked){
            unselectedList.add(studentListRecyclerViewTake[position].SRN.toString())
        }
        else{
            unselectedList.remove(studentListRecyclerViewTake[position].SRN.toString())
        }
        listener.getUnselectedStudentList(unselectedList)
        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                holder.checkboxCardLayout.setCardBackgroundColor(context.getColorFromAttr(R.attr.colorPrimaryContainer))
                holder.checkbox.buttonTintList = ColorStateList.valueOf(context.getColorFromAttr(R.attr.colorOnPrimary))
                holder.checkbox.setTextColor(context.getColorFromAttr(R.attr.colorOnPrimary))
                selectedList.add(studentListRecyclerViewTake[position].SRN.toString())
                unselectedList.remove(studentListRecyclerViewTake[position].SRN.toString())
                notifyDataSetChanged()
            } else if (!isChecked) {
                holder.checkboxCardLayout.setCardBackgroundColor(context.getColorFromAttr(R.attr.colorSurface))
                holder.checkbox.buttonTintList = ColorStateList.valueOf(context.getColorFromAttr(R.attr.textFillColor))
                holder.checkbox.setTextColor(context.getColorFromAttr(R.attr.textFillColor))
                selectedList.remove(studentListRecyclerViewTake[position].SRN.toString())
                unselectedList.add(studentListRecyclerViewTake[position].SRN.toString())
                notifyDataSetChanged()
            }
            listener.getSelectedStudentList(selectedList)
            listener.getUnselectedStudentList(unselectedList)
            notifyDataSetChanged()
            unselectedList.clear()
        }
    }

    fun getSelected(): ArrayList<String> {
        return selectedList
    }
    fun getUnselected(): ArrayList<String> {
        return unselectedList
    }

    override fun getItemCount(): Int {
        return studentListRecyclerViewTake.size
    }
    @ColorInt
    fun Context.getColorFromAttr(@AttrRes attrColor: Int, typedValue: TypedValue = TypedValue(), resolveRefs: Boolean = true
    ): Int {
        theme.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }
}


class ViewHolderAttendance(view: View) : RecyclerView.ViewHolder(view) {
    val tvName: TextView = view.findViewById(R.id.tvName)
    val tvSrn: TextView = view.findViewById(R.id.tvSrn)
    val checkbox: CheckBox = view.findViewById(R.id.checkbox)
    val linearLayout: LinearLayout = view.findViewById(R.id.linearLayout)
    val checkboxCardLayout: MaterialCardView = view.findViewById(R.id.checkboxCardLayout)

    interface Listener {
        fun getSelectedStudentList(list : ArrayList<String>)
        fun getUnselectedStudentList(list : ArrayList<String>)
    }
}