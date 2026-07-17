package com.acid.setu.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acid.setu.data.repository.SetuRepository
import com.acid.setu.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import coil.imageLoader

data class SetuUiState(
    val currentImageUrl: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val r18Enabled: Boolean = false,
    val saveSuccessMessage: String? = null
)

class SetuViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SetuUiState())
    val uiState: StateFlow<SetuUiState> = _uiState.asStateFlow()

    private var repository: SetuRepository? = null

    fun initRepository() {
        if (repository == null) {
            repository = SetuRepository()
        }
    }

    fun toggleR18() {
        _uiState.value = _uiState.value.copy(r18Enabled = !_uiState.value.r18Enabled)
    }

    fun fetchNewImage(context: Context) {
        if (_uiState.value.isLoading) return

        initRepository()
        val repo = repository ?: return

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null,
            currentImageUrl = null
        )

        viewModelScope.launch {
            try {
                val url = withContext(Dispatchers.IO) {
                    val r18 = if (_uiState.value.r18Enabled) 1 else 0
                    repo.fetchImageUrl(r18)
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentImageUrl = url,
                    errorMessage = if (url == null) "获取图片失败，请重试" else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "发生错误: ${e.message}"
                )
            }
        }
    }

    fun saveCurrentImage(context: Context) {
        val url = _uiState.value.currentImageUrl
        if (url == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "没有图片可以保存")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val success = withContext(Dispatchers.IO) {
                    val cacheFile = FileUtils.downloadImageToCache(context, url)
                        ?: return@withContext false
                    FileUtils.saveToDownload(context, cacheFile)
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = if (!success) "保存失败" else null,
                    saveSuccessMessage = if (success) "图片已保存到 Pictures 文件夹" else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "保存出错: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearSaveSuccessMessage() {
        _uiState.value = _uiState.value.copy(saveSuccessMessage = null)
    }

    fun clearCache(context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // 清理 FileUtils 下载的缓存文件（setu_*）
                FileUtils.clearAllCache(context)
                // 清理 Coil 的磁盘缓存
                context.imageLoader.diskCache?.clear()
                // 清理 Coil 的内存缓存
                context.imageLoader.memoryCache?.clear()
            }
        }
    }
}