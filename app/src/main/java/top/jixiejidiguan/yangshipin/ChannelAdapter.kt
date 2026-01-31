package top.jixiejidiguan.yangshipin

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION

class ChannelAdapter(
    private val context: android.content.Context,
    private val channelList: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder>() {

    private var selectedPosition = 0

    class ChannelViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_channel, parent, false)
    ) {
        val tvChannelName: TextView = itemView.findViewById(R.id.tvChannelName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ChannelViewHolder(parent).apply {
        itemView.setOnClickListener {
            val currentPos = bindingAdapterPosition
            if (currentPos != NO_POSITION && currentPos != selectedPosition) {
                notifyItemChanged(selectedPosition)
                selectedPosition = currentPos
                notifyItemChanged(selectedPosition)
                onItemClick(channelList[currentPos])
            }
        }
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        holder.tvChannelName.text = channelList[position]
        holder.itemView.isSelected = position == selectedPosition
    }

    override fun getItemCount() = channelList.size

    fun setSelectedPosition(position: Int) {
        if (channelList.isEmpty()) return
        
        val validPosition = if (position in channelList.indices) position else 0
        
        if (validPosition != selectedPosition) {
            notifyItemChanged(selectedPosition)
            selectedPosition = validPosition
            notifyItemChanged(selectedPosition)
        }
    }

    fun getSelectedPosition() = selectedPosition
}