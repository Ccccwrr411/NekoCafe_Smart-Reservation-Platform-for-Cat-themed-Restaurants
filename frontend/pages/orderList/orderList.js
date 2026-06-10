// pages/orderList/orderList.js
const { get } = require('../../utils/request')
const { ORDER_STATUS_MAP } = require('../../utils/util')

Page({
  data: {
    orders: [],
    loading: true,
    ORDER_STATUS_MAP
  },

  onLoad() {
    this.loadOrders()
  },

  onPullDownRefresh() {
    this.loadOrders()
  },

  loadOrders() {
    this.setData({ loading: true })
    get('/api/orders?userId=1001').then(res => {
      wx.stopPullDownRefresh()
      if (res.code === 0) {
        this.setData({ orders: res.data, loading: false })
      }
    }).catch(() => {
      wx.stopPullDownRefresh()
      this.setData({ loading: false })
    })
  },

  // 查看订单详情
  onOrderClick(e) {
    const orderId = e.currentTarget.dataset.orderid
    wx.navigateTo({ url: `/pages/orderDetail/orderDetail?orderId=${orderId}` })
  }
})
