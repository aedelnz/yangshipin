package top.jixiejidiguan.yangshipin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


@Suppress("DEPRECATION")
class ChannelAdapter(
    private val channelList: List<String>,
    private val onItemClick: (String) -> Unit // 点击回调，返回选中的频道名称
) : RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder>() {

    private var selectedPosition = 0 // 当前选中的位置

    // 视图持有者，绑定item布局的控件
    class ChannelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvChannelName: TextView = itemView.findViewById(R.id.tvChannelName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        // 加载item布局（自定义布局item_channel.xml，也可替换为android.R.layout.simple_list_item_1）
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_channel, parent, false)
        val holder = ChannelViewHolder(view)
        // 设置item点击事件，触发回调
        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION) { // 防止位置无效
                // 更新选中位置
                val previousPosition = selectedPosition
                selectedPosition = position
                // 刷新之前选中的item
                notifyItemChanged(previousPosition)
                // 刷新当前选中的item
                notifyItemChanged(selectedPosition)
                // 触发回调
                onItemClick(channelList[position])
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        // 绑定数据：设置频道名称
        holder.tvChannelName.text = channelList[position]
        // 设置选中状态
        holder.itemView.isSelected = (position == selectedPosition)
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
        }
    }
}