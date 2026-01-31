package top.jixiejidiguan.yangshipin

import android.content.Context
import androidx.core.content.edit

/**
 * 本地配置管理类
 * 用于简化 SharedPreferences 的读写操作
 *
 * @property context 上下文对象，用于获取 SharedPreferences 实例
 */
@Suppress("unused")
class PreferencesManager(private val context: Context) {

    // 使用 lazy 懒加载模式初始化 SharedPreferences
    // 只有在第一次访问 sharedPreferences 时才会创建实例，文件名定为 "app_settings"
    private val sharedPreferences by lazy {
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    }

    /**
     * 保存布尔值 (Boolean)
     * @param key 键名
     * @param value 要保存的布尔值
     */
    fun saveBoolean(key: String, value: Boolean) {
        // 使用 KTX 扩展函数 edit {} 简化操作
        // 默认会自动调用 apply() 进行异步提交，性能较好
        sharedPreferences.edit { putBoolean(key, value) }
    }

    /**
     * 保存整型值 (Int)
     * @param key 键名
     * @param value 要保存的整数
     */
    fun saveInt(key: String, value: Int) {
        sharedPreferences.edit { putInt(key, value) }
    }

    /**
     * 获取布尔值 (Boolean)
     * @param key 键名
     * @param defaultValue 如果找不到对应的键，则返回此默认值
     * @return 存储的值或默认值
     */
    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    /**
     * 获取整型值 (Int)
     * @param key 键名
     * @param defaultValue 如果找不到对应的键，则返回此默认值
     * @return 存储的值或默认值
     */
    fun getInt(key: String, defaultValue: Int): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }
}