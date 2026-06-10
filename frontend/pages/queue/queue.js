// pages/queue/queue.js
const { get, post } = require('../../utils/request')

Page({
  data: {
    storeId: 1,
    queue: null,
    loading: true,
    taking: false
  },

  onLoad(options) {
    const storeId = options.storeId || 1
    this.setData({ storeId })
    this.loadQueue()
  },

  onPullDownRefresh() {
    this.loadQueue()
  },

  loadQueue() {
    this.setData({ loading: true })
    get(`/api/queue/status?storeId=${this.data.storeId}`).then(res => {
      wx.stopPullDownRefresh()
      if (res.code === 0) {
        this.setData({ queue: res.data, loading: false })
      }
    }).catch(() => {
      wx.stopPullDownRefresh()
      this.setData({ loading: false })
    })
  },

  onTakeNumber() {
    this.setData({ taking: true })
    post('/api/queue/take', { storeId: this.data.storeId, persons: 2, type: '双人桌' }).then(res => {
      this.setData({ taking: false })
      if (res.code === 0) {
        wx.showToast({ title: `取号成功：${res.data.number}号`, icon: 'success' })
        this.loadQueue()
      }
    }).catch(() => this.setData({ taking: false }))
  }
})
