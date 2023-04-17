package com.github.jing332.tts_server_android.utils


import androidx.lifecycle.*

/**
 * 零入侵零反射解决 [LiveData] 在 event 场景下的粘性数据问题
 *
 * 原理：既然不能入侵（魔改） [LiveData]，那就对 observer 做文章，以静态代理的方式包装 [Observer]，当 [Observer.onChanged] 被回调时判定其参数是否是粘性数据，如果是就忽略不处理
 *
 * 判定依据：liveData 订阅（即执行 [LiveData.observe]）前一刻 liveData 有了数据（即被执行过 setValue，[LiveData.mVersion] 不再是 [LiveData.START_VERSION]），且 observer 处于 active 状态（详见 [LiveData.ObserverWrapper.shouldBeActive]），
 * 那么订阅时会立即触发 [Observer.onChanged] 执行，此时认为第一次在 onChanged 收到的参数就是粘滞数据
 *
 * ‼️ 该方法中 [owner] 通过 [LifecycleOwner.activeWhenCreated] 扩宽了 [LiveData.LifecycleBoundObserver.shouldBeActive] 判定为 true 的范围；
 * 目的在于实时接收并处理 event. 即当 [androidx.lifecycle.Lifecycle.getCurrentState] 处于 [androidx.lifecycle.Lifecycle.State.CREATED] 就开始接收 LiveData 数据，
 * 不用等到 [androidx.lifecycle.Lifecycle.State.STARTED] 才开始处理；
 *
 * 实时接收并处理 Event 的原因：
 *
 * 1、此处防止数据粘滞的方案原理是识别粘滞数据并跳过对它的处理，如果在 inactive 时间区间内（即 observer observe ～ observer 状态达到 active 这个时间区间）[LiveData.setValue] 被触发，那么该行为不能够被 [NoStickyObserverWrapper] 有效感知
 * （因为处于 inactive 状态，[LiveData.dispatchingValue] -> [LiveData.considerNotify] -> [LiveData.ObserverWrapper.shouldBeActive] 为 false，所以不会回调 [Observer.onChanged]）
 * 进而会导致跳过了非粘性数据的 bug 发生。
 *
 * 2、event 的场景有别于 state 的场景，前者更加倾向于即时消费数据，不用延迟到 owner 处于 STARTED 状态再接收数据。虽然可以通过更加复杂的判定方案实现更加完美的粘滞数据判定，
 * 但 activeWhenCreated 方案原理简单直接，契合 event 场景，且避免了过度复杂的设计
 *
 * （复杂方案之一：NoStickyObserverWrapper 构造时持有 liveData 引用，observe 时读取一次 [LiveData.mVersion] 的值并记录（反射读取），[NoStickyObserverWrapper.onChanged] 第一次执行时读取 [LiveData.mVersion] 的值，
 * 两者进行比较，根据变化就能知道 inactive 期间是否有被 setValue）
 * （复杂方案之二：如果不使用反射，那么在 NoStickyObserverWrapper inactive 期间通过额外执行一次 observeForever 来辅助监视 liveData 的数据变化，当 NoStickyObserverWrapper 状态变更为 active 状态后移除 observeForever 相关配置。。。不推荐，复杂度急剧升高）
 *
 * ⚠️ 在试图通过 [LiveData.removeObservers] 方法移除 observer 时不能将 [NoStickyObserverWrapper] 真正移除。通常情况下 observer 会随着 owner 持有的 lifecycle 的 destroy 自动移除；
 * 非要手动移除 observer 合理的做法是通过 [observeNoSticky]（或 [observeForeverNoSticky]） 的返回值得到最终的 observer，再通过 [LiveData.removeObserver] 进行手动移除
 *
 * @return 返回实际传入 LiveData 的 Observer，用于进行后续的 [LiveData.removeObserver] 操作
 */
fun <T> LiveData<T>.observeNoSticky(
    owner: LifecycleOwner, observer: Observer<in T>
): Observer<in T> {
    // generate observer
    val noStickyObserverWrapper = NoStickyObserverWrapper(this.hasValue(), observer)
    // do observe
    this.observe(owner.activeWhenCreated(), noStickyObserverWrapper)
    return noStickyObserverWrapper
}

/**
 * 零入侵零反射解决 [LiveData] 在 event 场景下的粘性数据问题
 *
 * 通过 [LiveData.observeForever] 订阅数据的 observer 是一直处于 active 状态的，详见 [LiveData.AlwaysActiveObserver]
 *
 * @return 返回实际传入 LiveData 的 observer，用于进行后续的 [LiveData.removeObserver] 操作
 */
fun <T> LiveData<T>.observeForeverNoSticky(
    observer: Observer<in T>
): Observer<in T> {
    // generate observer
    val noStickyObserverWrapper = NoStickyObserverWrapper(this.hasValue(), observer)
    // do observe
    this.observeForever(noStickyObserverWrapper)
    return noStickyObserverWrapper
}

/**
 * 检测 LiveData 是否设置过数据（粘滞数据判定的关键方法）
 *
 * 如果 LiveData 已经有了有效数据（[LiveData.mData] 已经不是 [LiveData.NOT_SET], 或 [LiveData.mVersion] 已经不是 [LiveData.START_VERSION]），
 * 那么执行 [LiveData.observeForever] 方法时其内部会立即触发 [Observer.onChanged] 执行，
 * 利用这个特性得以实现在不魔改 LiveData 的前提下感知 liveData 内部数据状态
 *
 * （如果能接受使用反射，那么也可以直接反射读取 [LiveData.mVersion] 看它是不是 [LiveData.START_VERSION]）
 */
fun LiveData<*>.hasValue(): Boolean {
    var hasValue = false
    val observer = Observer<Any?> {
        hasValue = true
    }/* 如果 liveData 设置过数据，会在 observeForever 执行时同步执行 onChanged 回调。*/
    observeForever(observer)/* 用完 observer 后立即移除. */
    removeObserver(observer)
    return hasValue
}

///////////////////////////////////////////////////////////////////////////
// NoStickyObserverWrapper
///////////////////////////////////////////////////////////////////////////

/**
 * 对 [Observer] 进行静态代理包装，包装类 [NoStickyObserverWrapper] 拿到数据后识别是否是粘滞数据，如果是那么不将其传递给 [originObserver]
 *
 * @param hasValueBeforeObserve [LiveData] 在被本 Observer 观察前（即执行 observe）是否已经被设置了数据（没有被设置过数据那么 [LiveData.mData] 是 [LiveData.NOT_SET]）
 * @param originObserver 原始的 observer
 */
private class NoStickyObserverWrapper<T>(
    private val hasValueBeforeObserve: Boolean,
    val originObserver: Observer<in T>,
) : Observer<T> {
    private var firstTime = true

    override fun onChanged(value: T) {
        if (firstTime) {
            firstTime = false/* 第一次执行. */
            when {/* 如果 observe 前有了数据，那么跳过该（粘滞数据）*/
                hasValueBeforeObserve -> Unit/* 如果 observe 前还没有数据，那么传递该数据. */
                else -> originObserver.onChanged(value)
            }
        } else {/* 不是第一次执行则直接传递数据. */
            originObserver.onChanged(value)
        }
    }

}

/**
 * 将 owner 的 active 状态从 STARTED 状态扩宽到 CREATED 状态
 *
 * Fragment 或 Activity 处于 CREATED 状态时依然要接收 LiveData 的实时数据时，可以使用此方案解决
 */
fun LifecycleOwner.activeWhenCreated(): LifecycleOwner = ActiveWhenCreateLifecycleOwner(this)

/**
 * 根据当前 owner，构造一个新的 owner，源 owner 处于 CREATED 状态时，新 owner 处于 STARTED 状态（对应 LiveData 的 active 状态）
 */
private class ActiveWhenCreateLifecycleOwner(
    origin: LifecycleOwner,
) : LifecycleOwner {
    override val lifecycle = LifecycleRegistry(this)

    init {
        origin.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
                lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
            }

            override fun onResume(owner: LifecycleOwner) {
                lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            }

            override fun onPause(owner: LifecycleOwner) {
                lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
                lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            }
        })
    }
}
