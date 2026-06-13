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
    showTablePopup: false,  // 桌位详情弹窗

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
    // 检查登录态，未登录跳转到登录页
    if (!wx.getStorageSync('token')) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }

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
    // 检查登录态，未登录跳转到登录页
    if (!wx.getStorageSync('token')) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }

    // 每次显示都重新加载门店列表和桌位，确保数据最新
    this.loadStores()
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

  // 点击门店头部 → 弹出切换面板
  onStoreHeaderTap() {
    this.setData({ showStorePicker: true })
  },

  // 隐藏门店选择器
  hideStorePicker() {
    this.setData({ showStorePicker: false })
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
    const { storeId, reserveDate, reserveTime, duration } = this.data
    let url = `/api/tables?storeId=${storeId}`
    if (reserveDate) url += `&reserveDate=${reserveDate}`
    if (reserveTime) url += `&reserveTime=${encodeURIComponent(reserveTime)}`
    if (duration) url += `&duration=${duration}`
    get(url).then(res => {
      if (res.code === 0) {
        this.setData({ tables: res.data, loading: false })
      }
    })
  },

  // 筛选桌型
  onFilterChange(e) {
    this.setData({ filterType: e.currentTarget.dataset.type, selectedTable: null })
  },

  // 选择桌位 - 弹出详情弹窗
  onTableSelect(e) {
    const table = e.currentTarget.dataset.table
    // 添加物理震动反馈，提升不可点击状态的体验
    if (table.status !== 'available') {
      wx.vibrateShort({ type: 'medium' })
      wx.showToast({ title: table.status === 'booked' ? '该桌已被预约' : '该桌维护中', icon: 'none' })
      return
    }
    this.setData({ 
      selectedTable: table,
      showTablePopup: true 
    })
  },

  // 隐藏桌位详情弹窗
  hideTablePopup() {
    this.setData({ showTablePopup: false })
  },

  // 日期选择
  onDateSelect(e) {
    const reserveDate = e.currentTarget.dataset.date
    this.setData({ reserveDate, selectedTable: null }, () => { this.loadTables() })
  },

  // 时间选择
  onTimeChange(e) {
    const reserveTime = this.data.timeSlots[e.detail.value]
    this.setData({ reserveTime, selectedTable: null }, () => { this.loadTables() })
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
    const duration = this.data.duration - 1
    this.setData({ duration, selectedTable: null }, () => { this.loadTables() })
  },
  onDurationPlus() {
    if (this.data.duration >= 4) { wx.showToast({ title: '最长预约4小时', icon: 'none' }); return }
    const duration = this.data.duration + 1
    this.setData({ duration, selectedTable: null }, () => { this.loadTables() })
  },

  // 提交预约（从弹窗内点击）
  onSubmit() {
    const { selectedTable, reserveDate, reserveTime, persons, storeId } = this.data
    if (!selectedTable) return

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
        // 预约成功，关闭弹窗，刷新桌位
        this.setData({ showTablePopup: false, selectedTable: null }, () => {
          this.loadTables()
        })

        const app = getApp()
        app.globalData.selectedTable = selectedTable
        wx.showToast({ title: '预约成功！', icon: 'success' })

        const orderId = result.data.orderId
        setTimeout(() => {
          wx.navigateTo({ url: `/pages/orderDetail/orderDetail?orderId=${orderId}` })
        }, 800)
      } else {
        wx.showToast({ title: result.msg || '预约失败', icon: 'none' })
      }
    }).catch(() => {
      wx.hideLoading()
      wx.showToast({ title: '网络开小差啦', icon: 'none' })
    })
  }
})
