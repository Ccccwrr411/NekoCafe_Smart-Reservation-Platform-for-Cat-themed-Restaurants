// pages/reservation/reservation.js
const { get, post } = require('../../utils/request')
const { TABLE_STATUS_MAP, genTimeSlots, formatDistance } = require('../../utils/util')
const { getUserLocation, applyDistanceAndSort } = require('../../utils/lbs')

Page({
  data: {
    storeId: null,
    storeName: '',
    stores: [],           // 所有门店列表（已按距离排序）
    showStorePicker: false, // 门店选择弹层

    tables: [],
    loading: true,
    selectedTable: null,

    // 预约表单
    reserveDate: '',
    reserveTime: '',
    persons: 2,
    duration: 2,

    // 时间选择
    timeSlots: [],
    dateList: [],

    // 筛选
    filterType: 'all',
    tableTypes: ['all', '双人桌', '四人桌', '包间', '吧台位'],
    tableTypeLabels: { all: '全部', '双人桌': '双人', '四人桌': '四人', '包间': '包间', '吧台位': '吧台' },

    TABLE_STATUS_MAP
  },

  onLoad(options) {
    if (!wx.getStorageSync('token')) return

    const dateList = []
    for (let i = 0; i < 7; i++) {
      const d = new Date()
      d.setDate(d.getDate() + i)
      const month = d.getMonth() + 1
      const day = d.getDate()
      dateList.push({
        full: `${d.getFullYear()}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`,
        label: i === 0 ? '今天' : i === 1 ? '明天' : `${month}/${day}`
      })
    }

    this.setData({
      timeSlots: genTimeSlots(),
      dateList,
      reserveDate: dateList[0].full,
      reserveTime: '14:00'
    })

    if (options.storeId) {
      this.setData({
        storeId: options.storeId,
        storeName: decodeURIComponent(options.storeName || 'NekoCafé')
      })
      const app = getApp()
      app.globalData.currentStore = { id: options.storeId, name: decodeURIComponent(options.storeName || 'NekoCafé') }
    } else {
      const app = getApp()
      if (app.globalData.currentStore) {
        this.setData({
          storeId: app.globalData.currentStore.id,
          storeName: app.globalData.currentStore.name
        })
      }
    }
    this.loadStores()
  },

  onShow() {
    if (!wx.getStorageSync('token')) return

    const app = getApp()
    if (app.globalData.currentStore && (!this.data.storeId || app.globalData.currentStore.id !== this.data.storeId)) {
      this.setData({
        storeId: app.globalData.currentStore.id,
        storeName: app.globalData.currentStore.name
      })
      this.loadTables()
    }
    if (!this.data.storeId && this.data.stores.length > 0) {
      this.setData({ showStorePicker: true })
    }
  },

  loadStores() {
    get('/api/stores').then(res => {
      if (res.code === 0) {
        this.autoSortByLocation(res.data)
      }
    })
  },

  autoSortByLocation(rawStores) {
    getUserLocation().then(userLoc => {
      const sorted = applyDistanceAndSort(rawStores, userLoc)
      this.applySortedStores(sorted)
    }).catch(() => {
      const fallback = applyDistanceAndSort(rawStores, null)
      this.applySortedStores(fallback)
      wx.showToast({ title: '无法获取位置，显示未知距离', icon: 'none' })
    })
  },

  applySortedStores(sorted) {
    this.setData({ stores: sorted })
    if (!this.data.storeId && sorted.length > 0) {
      const first = sorted[0]
      this.setData({ storeId: first.id, storeName: first.name })
      const app = getApp()
      app.globalData.currentStore = { id: first.id, name: first.name }
      this.loadTables()
    } else if (this.data.storeId) {
      this.loadTables()
    }
  },

  onStoreHeaderTap() { this.setData({ showStorePicker: true }) },
  hideStorePicker() { this.setData({ showStorePicker: false }) },

  onStoreSelect(e) {
    const store = e.currentTarget.dataset.store
    if (store.id === this.data.storeId) {
      this.hideStorePicker()
      return
    }
    this.setData({
      storeId: store.id,
      storeName: store.name,
      selectedTable: null, 
      loading: true,
      showStorePicker: false
    })
    const app = getApp()
    app.globalData.currentStore = { id: store.id, name: store.name }
    this.loadTables()
  },

  loadTables() {
    if (!this.data.storeId) return
    this.setData({ loading: true })
    get(`/api/tables?storeId=${this.data.storeId}`).then(res => {
      if (res.code === 0) {
        this.setData({ tables: res.data, loading: false })
      }
    })
  },

  onFilterChange(e) {
    this.setData({ filterType: e.currentTarget.dataset.type, selectedTable: null })
  },

  onTableSelect(e) {
    const table = e.currentTarget.dataset.table
    // 添加物理震动反馈，提升不可点击状态的体验
    if (table.status !== 'available') {
      wx.vibrateShort({ type: 'medium' }) 
      wx.showToast({ title: table.status === 'booked' ? '该桌已被预约' : '该桌维护中', icon: 'none' })
      return
    }
    this.setData({ selectedTable: table })
  },

  onDateSelect(e) { this.setData({ reserveDate: e.currentTarget.dataset.date }) },
  onTimeChange(e) { this.setData({ reserveTime: this.data.timeSlots[e.detail.value] }) },

  onPersonsMinus() {
    if (this.data.persons <= 1) return
    this.setData({ persons: this.data.persons - 1 })
  },
  onPersonsPlus() {
    const max = this.data.selectedTable ? this.data.selectedTable.capacity : 8
    if (this.data.persons >= max) {
      wx.showToast({ title: `该桌最多坐${max}人`, icon: 'none' }); return
    }
    this.setData({ persons: this.data.persons + 1 })
  },

  onDurationMinus() {
    if (this.data.duration <= 1) return
    this.setData({ duration: this.data.duration - 1 })
  },
  onDurationPlus() {
    if (this.data.duration >= 4) { wx.showToast({ title: '最长预约4小时', icon: 'none' }); return }
    this.setData({ duration: this.data.duration + 1 })
  },

  onSubmit() {
    const { selectedTable, reserveDate, reserveTime, persons, storeId } = this.data
    if (!selectedTable) { wx.showToast({ title: '请先选择桌位', icon: 'none' }); return }

    wx.showModal({
      title: '确认预约',
      content: `${reserveDate} ${reserveTime}\n${selectedTable.name}（${selectedTable.type}）\n${persons}人`,
      success: (res) => {
        if (!res.confirm) return
        wx.showLoading({ title: '提交中...' })
        post('/api/reservation/create', {
          storeId,
          tableId: selectedTable.id,
          reserveDate,
          reserveTime,
          persons,
          duration: this.data.duration
        }).then(result => {
          wx.hideLoading()
          if (result.code === 0) {
            const app = getApp()
            app.globalData.selectedTable = selectedTable
            wx.showToast({ title: '预约成功！', icon: 'success' })
            setTimeout(() => {
              wx.navigateTo({ url: `/pages/menu/menu?storeId=${storeId}&tableId=${selectedTable.id}` })
            }, 800)
          }
        }).catch(() => wx.hideLoading())
      }
    })
  }
})