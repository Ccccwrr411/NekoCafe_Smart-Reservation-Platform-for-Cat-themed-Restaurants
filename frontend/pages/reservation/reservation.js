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

    // 门店选择器地图
    pickerMapLat: 39.9042,
    pickerMapLng: 116.4074,
    pickerMarkers: [],
    activePickerStoreId: null,
    userLocation: null,

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
    // 先检查 token
    if (!wx.getStorageSync('token')) return

    // 生成未来7天日期
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

    // 优先从 URL 参数获取（从首页卡片跳转过来）
    if (options.storeId) {
      this.setData({
        storeId: options.storeId,
        storeName: decodeURIComponent(options.storeName || 'NekoCafé')
      })
      const app = getApp()
      app.globalData.currentStore = { id: options.storeId, name: decodeURIComponent(options.storeName || 'NekoCafé') }
    }
    // 否则用全局缓存的当前门店
    else {
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

    // 每次显示时同步全局门店（可能在其他页面切换了）
    const app = getApp()
    if (app.globalData.currentStore && (!this.data.storeId || app.globalData.currentStore.id !== this.data.storeId)) {
      this.setData({
        storeId: app.globalData.currentStore.id,
        storeName: app.globalData.currentStore.name
      })
      this.loadTables()
    }
    // 如果仍然没有门店，自动弹出选择器
    if (!this.data.storeId && this.data.stores.length > 0) {
      this.setData({ showStorePicker: true })
    }
  },

  // 加载所有门店 → 自动定位 → 按距离排序
  loadStores() {
    get('/api/stores').then(res => {
      if (res.code === 0) {
        const rawStores = res.data
        // 自动获取定位 → 计算距离 → 排序
        this.autoSortByLocation(rawStores)
      }
    })
  },

  // 自动定位 + 距离排序
  autoSortByLocation(rawStores) {
    getUserLocation().then(userLoc => {
      this.setData({ userLocation: userLoc })
      const sorted = applyDistanceAndSort(rawStores, userLoc)
      this.applySortedStores(sorted)
    }).catch(() => {
      const fallback = applyDistanceAndSort(rawStores, null)
      this.applySortedStores(fallback)
      wx.showToast({ title: '无法获取位置，显示未知距离', icon: 'none' })
    })
  },

  // 应用排序后的门店列表
  applySortedStores(sorted) {
    this.setData({ stores: sorted })

    // 如果还没有选中门店，用第一个（最近）作为默认
    if (!this.data.storeId && sorted.length > 0) {
      const first = sorted[0]
      this.setData({
        storeId: first.id,
        storeName: first.name
      })
      const app = getApp()
      app.globalData.currentStore = { id: first.id, name: first.name }
      this.loadTables()
    } else if (this.data.storeId) {
      this.loadTables()
    }
  },

  // 点击门店头部 → 构建标记并弹出切换面板
  onStoreHeaderTap() {
    this.buildPickerMarkers()
    this.setData({ showStorePicker: true, activePickerStoreId: null })
  },

  // 隐藏门店选择器
  hideStorePicker() {
    this.setData({ showStorePicker: false })
  },

  // 构建门店选择器地图标记
  buildPickerMarkers() {
    const { stores, userLocation } = this.data
    if (!stores || stores.length === 0) return

    const markers = stores.map((s, index) => ({
      id: s.id,
      latitude: s.lat,
      longitude: s.lng,
      title: s.name,
      // 不设 callout（门店选择器地图不需要导航；避免 qqmap:// scheme 报错）
      label: {
        content: String(index + 1),
        color: '#ffffff',
        fontSize: 12,
        x: 12,
        y: -24,
        bgColor: '#C97E5A',
        borderRadius: 16,
        padding: 4
      },
      width: 26,
      height: 26
    }))

    // 地图居中：用户定位优先 → 所有门店中心
    const loc = userLocation
    let centerLat, centerLng
    if (loc && loc.lat) {
      centerLat = loc.lat
      centerLng = loc.lng
    } else {
      const lats = stores.map(s => s.lat)
      const lngs = stores.map(s => s.lng)
      centerLat = (Math.min(...lats) + Math.max(...lats)) / 2
      centerLng = (Math.min(...lngs) + Math.max(...lngs)) / 2
    }

    this.setData({
      pickerMarkers: markers,
      pickerMapLat: centerLat,
      pickerMapLng: centerLng
    })
  },

  // 地图标记点击 → 滚动列表到对应门店
  onPickerMarkerTap(e) {
    const id = e.detail && e.detail.markerId
    if (!id) return
    this.setData({ activePickerStoreId: id })
  },

  // 切换门店
  onStoreSelect(e) {
    const store = e.currentTarget.dataset.store
    if (store.id === this.data.storeId) {
      this.hideStorePicker()
      return
    }
    this.setData({
      storeId: store.id,
      storeName: store.name,
      selectedTable: null,  // 清空已选桌位
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

  // 筛选桌型
  onFilterChange(e) {
    this.setData({ filterType: e.currentTarget.dataset.type, selectedTable: null })
  },

  // 选择桌位
  onTableSelect(e) {
    const table = e.currentTarget.dataset.table
    if (table.status !== 'available') {
      wx.showToast({ title: table.status === 'booked' ? '该桌已被预约' : '该桌暂停使用', icon: 'none' })
      return
    }
    this.setData({ selectedTable: table })
  },

  // 日期选择
  onDateSelect(e) {
    this.setData({ reserveDate: e.currentTarget.dataset.date })
  },

  // 时间选择
  onTimeChange(e) {
    const timeSlots = this.data.timeSlots
    this.setData({ reserveTime: timeSlots[e.detail.value] })
  },

  // 人数调整
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

  // 时长调整
  onDurationMinus() {
    if (this.data.duration <= 1) return
    this.setData({ duration: this.data.duration - 1 })
  },
  onDurationPlus() {
    if (this.data.duration >= 4) { wx.showToast({ title: '最长预约4小时', icon: 'none' }); return }
    this.setData({ duration: this.data.duration + 1 })
  },

  // 提交预约
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
