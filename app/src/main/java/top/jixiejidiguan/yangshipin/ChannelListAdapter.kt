package top.jixiejidiguan.yangshipin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.pow

/**
 * 频道列表适配器
 * 支持高亮选中项和自动滚动定位
 */
class ChannelListAdapter(
    private val channels: List<Pair<String, String>>,
    private val onChannelClick: (Int) -> Unit,
    private val onSelectionChanged: ((Int) -> Unit)? = null
) : RecyclerView.Adapter<ChannelListAdapter.ChannelViewHolder>() {

    // 当前选中的频道位置
    private var selectedPosition = 0
    // 保存上一次选中的位置，用于优化刷新
    private var lastSelectedPosition = 0

    /**
     * 频道视图持有者
     */
    inner class ChannelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.card_view)
        val channelTitle: TextView = itemView.findViewById(R.id.channel_title)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    // 更新选中位置并通知数据变化
                    setSelectedPosition(position)
                    onChannelClick(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_channel_card, parent, false)
        return ChannelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        val (title) = channels[position]
        holder.channelTitle.text = title
        
        // 设置选中状态
        holder.cardView.isSelected = (position == selectedPosition)
        
        // 添加平滑过渡动画
        holder.cardView.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300)
            .setInterpolator { input -> 
                // 自定义插值器，实现平滑过渡
                (1 - (1 - input).toDouble().pow(3.0)).toFloat()
            }
            .start()
        
        // 设置选中项高亮
        if (position == selectedPosition) {
            // 选中状态：蓝色背景，黑色文字，放大效果
            holder.cardView.scaleX = 1.05f
            holder.cardView.scaleY = 1.05f
            holder.channelTitle.setTextColor(holder.itemView.context.getColor(android.R.color.black))
        } else {
            // 普通状态：深色背景，白色文字，正常大小
            holder.cardView.scaleX = 1f
            holder.cardView.scaleY = 1f
            holder.channelTitle.setTextColor(holder.itemView.context.getColor(R.color.white))
        }
    }

    override fun getItemCount(): Int = channels.size

    /**
     * 设置选中的位置
     * 优化：只刷新变化的项，而不是整个列表
     */
    fun setSelectedPosition(position: Int) {
        lastSelectedPosition = selectedPosition
        selectedPosition = position
        
        // 只刷新变化的项，优化性能
        notifyItemChanged(lastSelectedPosition)
        notifyItemChanged(selectedPosition)
        
        // 通知外部选中位置变化，以便滚动到可见区域
        onSelectionChanged?.invoke(selectedPosition)
    }
    
    /**
     * 获取当前选中的位置
     */
    fun getSelectedPosition(): Int = selectedPosition

    /**
     * 获取当前选中的频道数据
     */
    fun getSelectedChannel(): Pair<String, String>? =
        channels.getOrNull(selectedPosition)

    /**
     * 获取指定位置的频道数据
     */
    fun getChannel(position: Int): Pair<String, String>? =
        channels.getOrNull(position)

    /**
     * 判断指定位置是否被选中
     */
    fun isSelected(position: Int): Boolean = position == selectedPosition

    /**
     * 清除选中状态（重置为 -1，表示无选中）
     */
    fun clearSelection() {
        val old = selectedPosition
        selectedPosition = -1
        notifyItemChanged(old)
        // 仅在位置有效时调用滚动回调，避免无效位置导致闪退
        // -1 表示无选中，不需要滚动
    }
}