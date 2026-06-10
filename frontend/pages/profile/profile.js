// pages/profile/profile.js
const { get } = require('../../utils/request')
const { ORDER_STATUS_MAP } = require('../../utils/util')
const app = getApp()

Page({
  data: {
    userInfo: null,
    orders: [],
    loading: true,
    ORDER_STATUS_MAP,
    menuItems: []
  },

  onShow() {
    this.buildMenuItems()
    this.loadUserInfo()
    this.loadOrders()
  },

  // 根据角色动态构建菜单
  buildMenuItems() {
    const role = app.globalData.userRole || 'customer'
    const baseMenu = [
      { icon: '🎫', label: '我的优惠券', path: '/pages/coupons/coupons' },
      { icon: '⭐', label: '我的收藏', path: '' },
      { icon: '🐾', label: '猫咪档案', path: '/pages/cats/cats' },
      { icon: '🪪', label: '实名认证', path: '/pages/realname/realname' },
      { icon: '📞', label: '联系客服', path: '' },
      { icon: '⚙️', label: '设置', path: '' }
    ]
    // 店员/店长/总部运营：在菜单顶部插入后台入口
    if (role === 'staff' || role === 'manager') {
      baseMenu.unshift(
        { icon: '🏪', label: '店员工作台', path: '/pages/staff/staff' }
      )
    }
    if (role === 'manager') {
      baseMenu.unshift(
        { icon: '📊', label: '数据看板', path: '/pages/dashboard/dashboard' }
      )
    }
    if (role === 'hq_ops') {
      baseMenu.unshift(
        { icon: '🏪', label: '店员工作台', path: '/pages/staff/staff' },
        { icon: '📊', label: '数据看板', path: '/pages/dashboard/dashboard' }
      )
    }
    this.setData({ menuItems: baseMenu })
  },

  loadUserInfo() {
    get('/api/user/profile').then(res => {
      if (res.code === 0) {
        this.setData({ userInfo: res.data })
        wx.setStorageSync('userInfo', res.data)
      }
    })
  },

  loadOrders() {
    get('/api/orders?userId=1001').then(res => {
      if (res.code === 0) {
        this.setData({ orders: res.data.slice(0, 3), loading: false })
      }
    })
  },

  goAllOrders() {
    wx.navigateTo({ url: '/pages/orderList/orderList' })
  },

  goOrderDetail(e) {
    const orderId = e.currentTarget.dataset.orderid
    wx.navigateTo({ url: `/pages/orderDetail/orderDetail?orderId=${orderId}` })
  },

  onMenuItemClick(e) {
    const path = e.currentTarget.dataset.path
    const label = e.currentTarget.dataset.label
    if (path) {
      wx.navigateTo({ url: path })
    } else {
      wx.showToast({ title: `${label} 页面开发中`, icon: 'none' })
    }
  },

  onLogout() {
    wx.showModal({
      title: '退出登录',
      content: '确认退出当前账号？',
      success: (res) => { if (res.confirm) app.logout() }
    })
  },

  // 积分进度
  get pointsPercent() {
    const u = this.data.userInfo
    if (!u) return 0
    return Math.round(u.points / (u.points + u.pointsToNext) * 100)
  }
})
