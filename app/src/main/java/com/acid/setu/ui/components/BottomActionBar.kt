package com.acid.setu.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BottomActionBar(
    isLoading: Boolean,
    hasImage: Boolean,
    r18Enabled: Boolean,
    onFetch: () -> Unit,
    onSave: () -> Unit,
    onToggleR18: () -> Unit
) {
    NavigationBar(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 获取图片按钮
            Button(
                onClick = onFetch,
                enabled = !isLoading,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isLoading) "加载中..." else "获取图片")
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 保存图片按钮
            Button(
                onClick = onSave,
                enabled = hasImage && !isLoading,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("保存")
            }

            Spacer(modifier = Modifier.width(8.dp))

                // R18切换按钮（图标 + 下方文字）
                IconButton(
                    onClick = onToggleR18,
                    enabled = !isLoading
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "切换R18",
                            tint = if (r18Enabled) MaterialTheme.colorScheme.error 
                                   else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (r18Enabled) "R18开" else "R18关",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (r18Enabled) MaterialTheme.colorScheme.error 
                                    else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
        }
    }
}