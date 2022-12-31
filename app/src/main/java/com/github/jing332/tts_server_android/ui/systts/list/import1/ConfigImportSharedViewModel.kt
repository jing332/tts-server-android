package com.github.jing332.tts_server_android.ui.systts.list.import1

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.jing332.tts_server_android.data.entities.systts.GroupWithTtsItem

class ConfigImportSharedViewModel : ViewModel() {
    val previewLiveData: MutableLiveData<List<GroupWithTtsItem>> by lazy { MutableLiveData() }

    fun setPreview(list: List<GroupWithTtsItem>) {
        previewLiveData.postValue(list)
    }

}
