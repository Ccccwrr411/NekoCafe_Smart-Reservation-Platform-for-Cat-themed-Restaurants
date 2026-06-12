// pages/orderList/orderList.js
const { get } = require('../../utils/request')
const { ORDER_STATUS_MAP } = require('../../utils/util')

// 筛选标签配置
// 业务语义：
//   已预约 = booked（预约未确认）
//   用餐中 = confirmed（已确认到店/用餐中）
//   已取消 = cancelled（CANCEL_BOOKING 取消预约 + CANCEL_ORDER 取消订单，前端展示由 mapStatusToFrontend 细分）
//   售后   = afterSales（售后中/拒绝售后/售后完成，同时看预约表和退单表状态）
//   已完成 = completed（正常完成）
const FILTER_TABS = [
  { key: 'all',        label: '全部' },
  { key: 'booked',     label: '已预约' },
  { key: 'confirmed',  label: '用餐中' },
  { key: 'cancelled',  label: '已取消' },
  { key: 'afterSales', label: '售后' },
  { key: 'completed',  label: '已完成' }
]

Page({
  data: {
    orders: [],
    loading: true,
    ORDER_STATUS_MAP,
    // 筛选
    filterTabs: FILTER_TABS,
    activeFilter: 'all',
    // 搜索
    searchKeyword: '',
    showSearch: false
  },

  onLoad() {
    this.loadOrders()
  },

  onPullDownRefresh() {
    this.loadOrders()
  },

  // 加载订单列表
  loadOrders() {
    this.setData({ loading: true })
    const { activeFilter, searchKeyword } = this.data

    // 从本地存储获取 userId（优先 userInfo.id，其次直接存储的 userId）
    const userInfo = wx.getStorageSync('userInfo') || {}
    const userId = userInfo.id || userInfo.userId || wx.getStorageSync('userId') || ''

    let url = `/api/orders?userId=${userId}`
    if (activeFilter && activeFilter !== 'all') {
      url += `&status=${activeFilter}`
    }
    if (searchKeyword.trim()) {
      url += `&keyword=${encodeURIComponent(searchKeyword.trim())}`
    }

    get(url).then(res => {
      wx.stopPullDownRefresh()
      if (res.code === 0) {
        this.setData({ orders: res.data || [], loading: false })
      } else {
        this.setData({ orders: [], loading: false })
        wx.showToast({ title: res.message || '加载失败', icon: 'none' })
      }
    }).catch(() => {
      wx.stopPullDownRefresh()
      this.setData({ loading: false })
      wx.showToast({ title: '网络异常，请稍后重试', icon: 'none' })
    })
  },

  // 切换筛选标签
  onFilterTap(e) {
    const key = e.currentTarget.dataset.key
    if (key === this.data.activeFilter) return
    this.setData({ activeFilter: key, orders: [] })
    this.loadOrders()
  },

  // 切换搜索栏显示
  onToggleSearch() {
    this.setData({ showSearch: !this.data.showSearch })
    if (this.data.showSearch) {
      // 收起搜索时清空关键词并重新加载
      if (this.data.searchKeyword.trim()) {
        this.setData({ searchKeyword: '' })
        this.loadOrders()
      }
    }
  },

  // 搜索输入
  onSearchInput(e) {
    this.setData({ searchKeyword: e.detail.value })
  },

  // 执行搜索
  onSearchConfirm() {
    this.loadOrders()
  },

  // 清除搜索
  onClearSearch() {
    this.setData({ searchKeyword: '' })
    this.loadOrders()
  },

  // 查看订单详情
  onOrderClick(e) {
    const orderId = e.currentTarget.dataset.orderid
    wx.navigateTo({ url: `/pages/orderDetail/orderDetail?orderId=${orderId}` })
  }
})
