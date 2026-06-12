// pages/staff/refund.js — 退款审核页
const { get, post } = require('../../utils/request')
const app = getApp()

const REFUND_STATUS_LABEL = {
  PENDING: '待审核',
  APPROVED: '已通过',
  REJECTED: '已拒绝',
  pending: '待审核',
  approved: '已通过',
  rejected: '已拒绝'
}

Page({
  data: {
    storeId: 1,
    storeName: '',
    refunds: [],
    filteredRefunds: [],
    refundFilter: 'pending',
    loading: true,
    // 拒绝弹层
    showRejectModal: false,
    rejectRefund: null,
    rejectReason: ''
  },

  onLoad(options) {
    if (!app.requireRole(['staff', 'manager', 'hq_ops'])) return
    const storeId = options.storeId ? parseInt(options.storeId) : (app.globalData.userInfo && app.globalData.userInfo.storeId || 1)
    this.setData({ storeId })
    this.loadRefunds()
  },

  onShow() {
    if (!app.checkRole(['staff', 'manager', 'hq_ops'])) return
    this.loadRefunds()
  },

  onPullDownRefresh() {
    this.loadRefunds()
  },

  loadRefunds() {
    this.setData({ loading: true })
    get('/api/staff/refunds?storeId=' + this.data.storeId).then(res => {
      wx.stopPullDownRefresh()
      if (res.code === 0) {
        const refunds = (res.data || []).map(r => ({
          ...r,
          statusLabel: REFUND_STATUS_LABEL[r.status] || r.status
        }))
        this.setData({ refunds, loading: false })
        this.applyFilter()
      } else {
        this.setData({ loading: false })
      }
    }).catch(() => {
      wx.stopPullDownRefresh()
      this.setData({ loading: false })
    })
  },

  applyFilter() {
    const { refundFilter, refunds } = this.data
    const filtered = refundFilter === 'all'
      ? refunds
      : refunds.filter(r => r.status && r.status.toUpperCase() === refundFilter.toUpperCase())
    this.setData({ filteredRefunds: filtered })
  },

  onFilterRefund(e) {
    this.setData({ refundFilter: e.currentTarget.dataset.filter })
    this.applyFilter()
  },

  // 通过退款
  onApproveRefund(e) {
    const refund = e.currentTarget.dataset.refund
    if (!refund) return
    wx.showModal({
      title: '确认通过退款',
      content: `退款金额 ¥${refund.refundAmount}，确认通过？`,
      confirmText: '确认通过',
      confirmColor: '#C97E5A',
      success: (res) => {
        if (!res.confirm) return
        wx.showLoading({ title: '处理中...' })
        post('/api/staff/refund/review', {
          refundId: refund.refundId,
          action: 'approve',
          operatorId: app.globalData.userInfo && app.globalData.userInfo.id
        }).then(apiRes => {
          wx.hideLoading()
          if (apiRes.code === 0 && apiRes.data && apiRes.data.success) {
            wx.showToast({ title: '退款已通过', icon: 'success' })
            this.loadRefunds()
          } else {
            wx.showToast({ title: (apiRes.data && apiRes.data.message) || '操作失败', icon: 'none' })
          }
        }).catch(() => {
          wx.hideLoading()
          wx.showToast({ title: '网络异常', icon: 'none' })
        })
      }
    })
  },

  // 拒绝退款
  onRejectRefund(e) {
    const refund = e.currentTarget.dataset.refund
    this.setData({ showRejectModal: true, rejectRefund: refund, rejectReason: '' })
  },
  onCloseRejectModal() {
    this.setData({ showRejectModal: false, rejectRefund: null, rejectReason: '' })
  },
  onInputReason(e) {
    this.setData({ rejectReason: e.detail.value })
  },
  onConfirmRejectRefund() {
    const refund = this.data.rejectRefund
    if (!refund) return
    if (!this.data.rejectReason.trim()) {
      wx.showToast({ title: '请填写拒绝原因', icon: 'none' })
      return
    }
    wx.showLoading({ title: '处理中...' })
    post('/api/staff/refund/review', {
      refundId: refund.refundId,
      action: 'reject',
      operatorId: app.globalData.userInfo && app.globalData.userInfo.id
    }).then(apiRes => {
      wx.hideLoading()
      if (apiRes.code === 0 && apiRes.data && apiRes.data.success) {
        wx.showToast({ title: '退款已拒绝', icon: 'success' })
        this.setData({ showRejectModal: false, rejectRefund: null, rejectReason: '' })
        this.loadRefunds()
      } else {
        wx.showToast({ title: (apiRes.data && apiRes.data.message) || '操作失败', icon: 'none' })
      }
    }).catch(() => {
      wx.hideLoading()
      wx.showToast({ title: '网络异常', icon: 'none' })
    })
  }
})
