import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import top.jixiejidiguan.yangshipin.R

// 频道列表适配器，参数：频道数据列表 + 点击回调
class ChannelAdapter(
    private val channelList: List<String>,
    private val onItemClick: (String) -> Unit // 点击回调，返回选中的频道名称
) : RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder>() {

    // 视图持有者，绑定item布局的控件
    inner class ChannelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
                onItemClick(channelList[position])
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        // 绑定数据：设置频道名称
        holder.tvChannelName.text = channelList[position]
    }

    // 返回数据总数
    override fun getItemCount() = channelList.size
}