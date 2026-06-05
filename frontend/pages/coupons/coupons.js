// pages/coupons/coupons.js
const { get } = require('../../utils/request')

Page({
  data: {
    coupons: [],
    loading: true
  },

  onLoad() {
    this.loadCoupons()
  },

  onPullDownRefresh() {
    this.loadCoupons()
  },

  loadCoupons() {
    this.setData({ loading: true })
    get('/api/coupons?userId=1001').then(res => {
      wx.stopPullDownRefresh()
      if (res.code === 0) {
        this.setData({ coupons: res.data, loading: false })
      }
    }).catch(() => {
      wx.stopPullDownRefresh()
      this.setData({ loading: false })
    })
  },

  onUseCoupon(e) {
    const coupon = e.currentTarget.dataset.coupon
    if (coupon.status === 'used') {
      wx.showToast({ title: '已使用', icon: 'none' })
    } else if (coupon.status === 'expired') {
      wx.showToast({ title: '已过期', icon: 'none' })
    } else {
      wx.showToast({ title: '请在订单页选择使用', icon: 'none' })
    }
  }
})
