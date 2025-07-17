package com.weather.clearsky.feature.settings.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.weather.clearsky.feature.settings.databinding.ItemSettingActionBinding
import com.weather.clearsky.feature.settings.databinding.ItemSettingHeaderBinding
import com.weather.clearsky.feature.settings.databinding.ItemSettingInfoBinding
import com.weather.clearsky.feature.settings.databinding.ItemSettingListBinding
import com.weather.clearsky.feature.settings.databinding.ItemSettingSwitchBinding
import com.weather.clearsky.feature.settings.presentation.model.SettingItem

class SettingsAdapter(
    private val onSwitchChanged: (SettingItem.SwitchSetting, Boolean) -> Unit,
    private val onListItemClicked: (SettingItem.ListSetting, Int) -> Unit,
    private val onActionClicked: (SettingItem.ActionSetting) -> Unit
) : ListAdapter<SettingItem, RecyclerView.ViewHolder>(SettingsDiffCallback()) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_SWITCH = 1
        private const val TYPE_LIST = 2
        private const val TYPE_ACTION = 3
        private const val TYPE_INFO = 4
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is SettingItem.HeaderSetting -> TYPE_HEADER
            is SettingItem.SwitchSetting -> TYPE_SWITCH
            is SettingItem.ListSetting -> TYPE_LIST
            is SettingItem.ActionSetting -> TYPE_ACTION
            is SettingItem.InfoSetting -> TYPE_INFO
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(
                ItemSettingHeaderBinding.inflate(inflater, parent, false)
            )
            TYPE_SWITCH -> SwitchViewHolder(
                ItemSettingSwitchBinding.inflate(inflater, parent, false)
            )
            TYPE_LIST -> ListViewHolder(
                ItemSettingListBinding.inflate(inflater, parent, false)
            )
            TYPE_ACTION -> ActionViewHolder(
                ItemSettingActionBinding.inflate(inflater, parent, false)
            )
            TYPE_INFO -> InfoViewHolder(
                ItemSettingInfoBinding.inflate(inflater, parent, false)
            )
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.bind(getItem(position) as SettingItem.HeaderSetting)
            is SwitchViewHolder -> holder.bind(getItem(position) as SettingItem.SwitchSetting)
            is ListViewHolder -> holder.bind(getItem(position) as SettingItem.ListSetting)
            is ActionViewHolder -> holder.bind(getItem(position) as SettingItem.ActionSetting)
            is InfoViewHolder -> holder.bind(getItem(position) as SettingItem.InfoSetting)
        }
    }

    inner class HeaderViewHolder(
        private val binding: ItemSettingHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: SettingItem.HeaderSetting) {
            binding.tvHeader.setText(item.titleRes)
        }
    }

    inner class SwitchViewHolder(
        private val binding: ItemSettingSwitchBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: SettingItem.SwitchSetting) {
            binding.apply {
                tvTitle.setText(item.titleRes)
                
                item.descriptionRes?.let {
                    tvDescription.setText(it)
                    tvDescription.visibility = View.VISIBLE
                } ?: run {
                    tvDescription.visibility = View.GONE
                }
                
                item.iconRes?.let {
                    ivIcon.setImageResource(it)
                    ivIcon.visibility = View.VISIBLE
                } ?: run {
                    ivIcon.visibility = View.GONE
                }
                
                switchSetting.isChecked = item.value
                switchSetting.setOnCheckedChangeListener { _, isChecked ->
                    onSwitchChanged(item, isChecked)
                }
                
                root.setOnClickListener {
                    switchSetting.toggle()
                }
            }
        }
    }

    inner class ListViewHolder(
        private val binding: ItemSettingListBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: SettingItem.ListSetting) {
            binding.apply {
                tvTitle.setText(item.titleRes)
                
                item.descriptionRes?.let {
                    tvDescription.setText(it)
                    tvDescription.visibility = View.VISIBLE
                } ?: run {
                    tvDescription.visibility = View.GONE
                }
                
                item.iconRes?.let {
                    ivIcon.setImageResource(it)
                    ivIcon.visibility = View.VISIBLE
                } ?: run {
                    ivIcon.visibility = View.GONE
                }
                
                val selectedValue = if (item.selectedIndex < item.entries.size) {
                    item.entries[item.selectedIndex]
                } else {
                    "Unknown"
                }
                tvValue.text = selectedValue
                
                root.setOnClickListener {
                    showListDialog(item)
                }
            }
        }
        
        private fun showListDialog(item: SettingItem.ListSetting) {
            androidx.appcompat.app.AlertDialog.Builder(binding.root.context)
                .setTitle(item.titleRes)
                .setSingleChoiceItems(
                    item.entries.toTypedArray(),
                    item.selectedIndex
                ) { dialog, which ->
                    onListItemClicked(item, which)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    inner class ActionViewHolder(
        private val binding: ItemSettingActionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: SettingItem.ActionSetting) {
            binding.apply {
                tvTitle.setText(item.titleRes)
                
                item.descriptionRes?.let {
                    tvDescription.setText(it)
                    tvDescription.visibility = View.VISIBLE
                } ?: run {
                    tvDescription.visibility = View.GONE
                }
                
                item.iconRes?.let {
                    ivIcon.setImageResource(it)
                    ivIcon.visibility = View.VISIBLE
                } ?: run {
                    ivIcon.visibility = View.GONE
                }
                
                root.setOnClickListener {
                    onActionClicked(item)
                }
            }
        }
    }

    inner class InfoViewHolder(
        private val binding: ItemSettingInfoBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: SettingItem.InfoSetting) {
            binding.apply {
                tvTitle.setText(item.titleRes)
                tvValue.text = item.value
                
                item.iconRes?.let {
                    ivIcon.setImageResource(it)
                    ivIcon.visibility = View.VISIBLE
                } ?: run {
                    ivIcon.visibility = View.GONE
                }
            }
        }
    }

    private class SettingsDiffCallback : DiffUtil.ItemCallback<SettingItem>() {
        override fun areItemsTheSame(oldItem: SettingItem, newItem: SettingItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SettingItem, newItem: SettingItem): Boolean {
            return oldItem == newItem
        }
    }
} 