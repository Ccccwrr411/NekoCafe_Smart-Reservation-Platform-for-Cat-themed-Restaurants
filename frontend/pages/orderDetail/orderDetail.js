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

  onPullDownRefresh() {
    this.loadDetail()
  },

  loadDetail() {
    this.setData({ loading: true })
    get(`/api/order/detail?orderId=${this.data.orderId}`).then(res => {
      wx.stopPullDownRefresh()
      if (res.code === 0) {
        this.setData({ order: res.data, loading: false })
      }
    }).catch(() => {
      wx.stopPullDownRefresh()
      this.setData({ loading: false })
    })
  },

  // 取消订单
  onCancel() {
    wx.showModal({
      title: '取消预约',
      content: '取消后款项将原路退回，确定取消吗？',
      confirmColor: '#F44336',
      success: (res) => {
        if (!res.confirm) return
        wx.showLoading({ title: '处理中...' })
        post('/api/order/cancel', { orderId: this.data.orderId }).then(r => {
          wx.hideLoading()
          if (r.code === 0) {
            wx.showToast({ title: '已取消', icon: 'success' })
            this.loadDetail()
          }
        }).catch(() => wx.hideLoading())
      }
    })
  },

  // 改约
  onReschedule() {
    wx.showModal({
      title: '修改预约时间',
      content: '改约需要门店重新确认，是否继续？',
      success: (res) => {
        if (!res.confirm) return
        wx.showLoading({ title: '提交中...' })
        post('/api/order/reschedule', { orderId: this.data.orderId }).then(r => {
          wx.hideLoading()
          if (r.code === 0) {
            wx.showToast({ title: '改约成功', icon: 'success' })
            this.loadDetail()
          }
        }).catch(() => wx.hideLoading())
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
          }
        }).catch(() => wx.hideLoading())
      }
    })
  },

  // 去评价
  goReview() {
    wx.navigateTo({ url: `/pages/review/review?orderId=${this.data.orderId}` })
  }
})
