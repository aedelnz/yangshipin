package top.jixiejidiguan.yangshipin

/**
 * 应用配置类 - 管理频道URL等配置信息
 */
object AppConfig {
    /**
     * 获取频道配置数据
     * @return 频道名称到URL的映射
     */
    fun getData(): Map<String, String> {
        return mapOf(

            "CCTV1" to "https://www.yangshipin.cn/tv/home?pid=600001859",
            "CCTV2" to "https://www.yangshipin.cn/tv/home?pid=600001800",
            "CCTV3" to "https://www.yangshipin.cn/tv/home?pid=600001801",
            "CCTV4" to "https://www.yangshipin.cn/tv/home?pid=600001814",
            "CCTV5" to "https://www.yangshipin.cn/tv/home?pid=600001818",
            "CCTV5+" to "https://www.yangshipin.cn/tv/home?pid=600001817",
            "CCTV6" to "https://www.yangshipin.cn/tv/home?pid=600108442",
            "CCTV7" to "https://www.yangshipin.cn/tv/home?pid=600004092",
            "CCTV8" to "https://www.yangshipin.cn/tv/home?pid=600001803",
            "CCTV9" to "https://www.yangshipin.cn/tv/home?pid=600004078",
            "CCTV10" to "https://www.yangshipin.cn/tv/home?pid=600001805",
            "CCTV11" to "https://www.yangshipin.cn/tv/home?pid=600001806",
            "CCTV12" to "https://www.yangshipin.cn/tv/home?pid=600001807",
            "CCTV13" to "https://www.yangshipin.cn/tv/home?pid=600001811",
            "CCTV14" to "https://www.yangshipin.cn/tv/home?pid=600001809",
            "CCTV15" to "https://www.yangshipin.cn/tv/home?pid=600001815",
            "CCTV16-HD" to "https://www.yangshipin.cn/tv/home?pid=600098637",
            "CCTV16(4K）" to "https://www.yangshipin.cn/tv/home?pid=600099502",
            "CCTV17" to "https://www.yangshipin.cn/tv/home?pid=600001810",
            "北京卫视" to "https://www.yangshipin.cn/tv/home?pid=600002309",
            "江苏卫视" to "https://www.yangshipin.cn/tv/home?pid=600002521",
            "东方卫视" to "https://www.yangshipin.cn/tv/home?pid=600002483",
            "浙江卫视" to "https://www.yangshipin.cn/tv/home?pid=600002520",
            "湖南卫视" to "https://www.yangshipin.cn/tv/home?pid=600002475",
            "湖北卫视" to "https://www.yangshipin.cn/tv/home?pid=600002508",
            "广东卫视" to "https://www.yangshipin.cn/tv/home?pid=600002485",
            "广西卫视" to "https://www.yangshipin.cn/tv/home?pid=600002509",
            "黑龙江卫视" to "https://www.yangshipin.cn/tv/home?pid=600002498",
            "海南卫视" to "https://www.yangshipin.cn/tv/home?pid=600002506",
            "重庆卫视" to "https://www.yangshipin.cn/tv/home?pid=600002531",
            "深圳卫视" to "https://www.yangshipin.cn/tv/home?pid=600002481",
            "四川卫视" to "https://www.yangshipin.cn/tv/home?pid=600002516",
            "河南卫视" to "https://www.yangshipin.cn/tv/home?pid=600002525",
            "福建东南卫视" to "https://www.yangshipin.cn/tv/home?pid=600002484",
            "贵州卫视" to "https://www.yangshipin.cn/tv/home?pid=600002490",
            "江西卫视" to "https://www.yangshipin.cn/tv/home?pid=600002503",
            "辽宁卫视" to "https://www.yangshipin.cn/tv/home?pid=600002505",
            "安徽卫视" to "https://www.yangshipin.cn/tv/home?pid=600002532",
            "河北卫视" to "https://www.yangshipin.cn/tv/home?pid=600002493",
            "山东卫视" to "https://www.yangshipin.cn/tv/home?pid=600002513",
            "天津卫视" to "https://www.yangshipin.cn/tv/home?pid=600152137",
            "吉林卫视" to "https://www.yangshipin.cn/tv/home?pid=600190405",
            "陕西卫视" to "https://www.yangshipin.cn/tv/home?pid=600190400",
            "宁夏卫视" to "https://www.yangshipin.cn/tv/home?pid=600190737",
            "内蒙古卫视" to "https://www.yangshipin.cn/tv/home?pid=600190401",
            "云南卫视" to "https://www.yangshipin.cn/tv/home?pid=600190402",
            "山西卫视" to "https://www.yangshipin.cn/tv/home?pid=600190407",
            "青海卫视" to "https://www.yangshipin.cn/tv/home?pid=600190406",
            "西藏卫视" to "https://www.yangshipin.cn/tv/home?pid=600190403",
            "中国教育电视台1频道" to "https://www.yangshipin.cn/tv/home?pid=600171827",
            "新疆卫视" to "https://www.yangshipin.cn/tv/home?pid=600152138"
        )
    }
    
    /**
     * 根据索引获取频道URL
     * @param index 频道索引
     * @return 对应频道的URL，如果索引无效则返回默认URL
     */
    fun getUrlByIndex(index: Int): String {
        val data = getData()
        val keys = data.keys.toList()
        return if (index in keys.indices) {
            data[keys[index]] ?: getData()["CCTV1"]!!
        } else {
            getData()["CCTV1"]!!
        }
    }
    
    /**
     * 根据索引获取频道标题
     * @param index 频道索引
     * @return 对应频道的标题，如果索引无效则返回默认标题
     */
    fun getChannelTitleByIndex(index: Int): String {
        val data = getData()
        val keys = data.keys.toList()
        return if (index in keys.indices) {
            keys[index]
        } else {
            "CCTV1"
        }
    }

}
