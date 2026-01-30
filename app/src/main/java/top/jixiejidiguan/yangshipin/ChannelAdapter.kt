package top.jixiejidiguan.yangshipin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

@Suppress("DEPRECATION")
class ChannelAdapter(private val channelList: List<String>, private val onItemClick: (String) -> Unit) : RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder>() {

    private var selectedPosition = 0 // 当前选中的位置

    class ChannelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) { val tvChannelName: TextView = itemView.findViewById(R.id.tvChannelName) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_channel, parent, false)
        val holder = ChannelViewHolder(view)
        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION) { // 防止位置无效
                val previousPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onItemClick(channelList[position])
            }
        }
        return holder
    }
// 更新选择高亮
    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        holder.tvChannelName.text = channelList[position]
        val isSelected = (position == selectedPosition)
        holder.itemView.isSelected = isSelected
        if (isSelected) {
            holder.tvChannelName.textSize = 32f
        } else {
            holder.tvChannelName.textSize = 24f
        }
    }

    // 返回数据总数
    override fun getItemCount() = channelList.size

    // 设置选中位置
    fun setSelectedPosition(position: Int) {
        if (position in 0 until channelList.size) {
            val previousPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
            if (previousPosition > 0) notifyItemChanged(previousPosition - 1)
            if (previousPosition < channelList.size - 1) notifyItemChanged(previousPosition + 1)
            if (selectedPosition > 0) notifyItemChanged(selectedPosition - 1)
            if (selectedPosition < channelList.size - 1) notifyItemChanged(selectedPosition + 1)
        }
    }

    // 获取当前选中位置
    fun getSelectedPosition(): Int {
        return selectedPosition
    }
}