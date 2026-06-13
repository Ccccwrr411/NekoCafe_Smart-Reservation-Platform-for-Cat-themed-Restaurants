// pages/orderDetail/orderDetail.js
const { get, post } = require('../../utils/request')
const { ORDER_STATUS_MAP, formatPrice } = require('../../utils/util')

Page({
  data: {
    orderId: '',
    order: null,
    loading: true,
    ORDER_STATUS_MAP,
    formatPrice
  },

  onLoad(options) {
    const orderId = options.orderId || ''
    this.setData({ orderId })
    if (orderId) this.loadDetail()
  },

  onShow() {
    // 每次显示时刷新订单详情（从点单、评价等页面返回时能看到最新状态）
    if (this.data.orderId) {
      this.loadDetail()
    }
  },

  onPullDownRefresh() {
    this.loadDetail()
  },

  loadDetail() {
    this.setData({ loading: true })
    get(`/api/order/detail?orderId=${this.data.orderId}`).then(res => {
      wx.stopPullDownRefresh()
      if (res.code === 0) {
        const data = res.data
        // 取消订单后 order_items 已删除，用保存的菜品列表补回来
        if (this._cancelItems && (!data.items || data.items.length === 0)) {
          data.items = this._cancelItems
          data.refundAmount = this._cancelRefundAmount
          this._cancelItems = null
          this._cancelRefundAmount = null
        }
        this.setData({ order: data, loading: false })
      }
    }).catch(() => {
      wx.stopPullDownRefresh()
      this.setData({ loading: false })
    })
  },

  // 取消预约 / 取消订单
  onCancel() {
    const order = this.data.order
    const status = order && order.status
    const isBooked = status === 'booked'
    const title = isBooked ? '取消预约' : '取消订单'
    const content = isBooked
      ? '确定要取消该预约吗？桌位将释放。'
      : '款项将原路退回，桌位保留不释放，确定取消订单吗？'
    wx.showModal({
      title: title,
      content: content,
      confirmColor: '#F44336',
      success: (res) => {
        if (!res.confirm) return
        wx.showLoading({ title: '处理中...' })
        post('/api/order/cancel', { orderId: this.data.orderId }).then(r => {
          wx.hideLoading()
          if (r.code === 0) {
            wx.showToast({ title: isBooked ? '取消预约成功' : '取消订单成功', icon: 'success', duration: 1500 })
            // 保存后端返回的菜品列表（CONFIRMED 取消后 order_items 会被删除，需要先存下来）
            if (!isBooked && r.data && r.data.items) {
              this._cancelItems = r.data.items
              this._cancelRefundAmount = r.data.refundAmount
            }
            // 统一调用 loadDetail 从后端获取完整的最新数据（含时间线）
            setTimeout(() => {
              this.loadDetail()
            }, 800)
          } else {
            wx.showToast({ title: r.message || '取消失败', icon: 'none' })
          }
        }).catch(() => {
          wx.hideLoading()
          wx.showToast({ title: '网络异常，请稍后重试', icon: 'none' })
        })
      }
    })
  },

  // 申请退款
  onRefund() {
    wx.showModal({
      title: '申请退款',
      content: '退款将在1-3个工作日原路退回，确定申请吗？',
      confirmColor: '#F44336',
      success: (res) => {
        if (!res.confirm) return
        wx.showLoading({ title: '处理中...' })
        post('/api/order/refund', { orderId: this.data.orderId }).then(r => {
          wx.hideLoading()
          if (r.code === 0) {
            wx.showToast({ title: '退款申请已提交', icon: 'success' })
            this.loadDetail()
          } else {
            wx.showToast({ title: r.message || '申请失败', icon: 'none' })
          }
        }).catch(() => {
          wx.hideLoading()
          wx.showToast({ title: '网络异常，请稍后重试', icon: 'none' })
        })
      }
    })
  },

  // 去评价
  goReview() {
    wx.navigateTo({ url: `/pages/review/review?orderId=${this.data.orderId}` })
  },

  // 去点单（预约成功后进入点单流程）
  goOrder() {
    const order = this.data.order
    if (!order) return
    const app = getApp()
    // 将预约信息写入 globalData，供 order 页面使用
    app.globalData.selectedTable = {
      id: order.tableId,
      name: order.tableName,
      type: order.tableType,
      capacity: order.tableCapacity
    }
    app.globalData.currentStore = {
      id: order.storeId,
      name: order.storeName
    }
    // 跳转到 menu 页面点菜
    wx.switchTab({ url: '/pages/menu/menu' })
  },

  // 重新点单（取消订单后，复用原预约记录重新激活为 BOOKED，跳到菜单页重新选菜）
  goReorder() {
    const order = this.data.order
    if (!order) return

    // 激活已取消的预约（CANCEL_ORDER → BOOKED），复用同一预约记录
    wx.showLoading({ title: '激活预约中...' })
    post('/api/order/reactivate', { orderId: this.data.orderId }).then(r => {
      wx.hideLoading()
      if (r.code === 0 && r.data && r.data.orderId) {
        const app = getApp()
        const data = r.data

        // 将预约信息写入 globalData（使用后端返回的完整信息）
        app.globalData.selectedTable = {
          id: data.tableId || order.tableId,
          name: data.tableName || order.tableName,
          type: data.tableType || order.tableType,
          capacity: data.tableCapacity || order.tableCapacity
        }
        app.globalData.currentStore = {
          id: data.storeId || order.storeId,
          name: data.storeName || order.storeName
        }

        // 将原订单菜品预填入购物车，用户可在菜单页调整后重新下单
        if (order.items && order.items.length > 0) {
          app.globalData.cartItems = order.items.map(item => ({
            id: item.dishId,
            name: item.name,
            price: item.price,
            imageUrl: item.imageUrl || '',
            qty: item.quantity
          }))
        } else {
          app.globalData.cartItems = []
        }

        // 跳转到菜单页选菜（和"去点单"流程一致：switchTab 到 menu）
        wx.switchTab({ url: '/pages/menu/menu' })
      } else {
        wx.showToast({ title: r.message || '激活预约失败', icon: 'none' })
      }
    }).catch(() => {
      wx.hideLoading()
      wx.showToast({ title: '网络异常，请稍后重试', icon: 'none' })
    })
  }
})
