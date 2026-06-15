// pages/staff/refund.js — 退款审核页
const { get, post } = require('../../utils/request')
const app = getApp()

const REFUND_STATUS_LABEL = {
  REQUEST_CANCEL: '待审核',
  REQUEST_REFUND: '待审核',
  COMPLETED: '已通过',
  REJECTED: '已拒绝'
}

Page({
  data: {
    storeId: 1,
    storeName: '',
    reservationId: null,   // 从订单列表传入，自动定位到该订单的退款记录
    refunds: [],
    filteredRefunds: [],
    refundFilter: 'pending',  // 默认显示待审核
    loading: true,
    // 拒绝弹层
    showRejectModal: false,
    rejectRefund: null,
    rejectReason: ''
  },

  onLoad(options) {
    if (!app.requireRole(['staff', 'manager', 'hq_ops'])) return
    const storeId = options.storeId ? parseInt(options.storeId) : (app.globalData.userInfo && app.globalData.userInfo.storeId || 1)
    const reservationId = options.reservationId ? parseInt(options.reservationId) : null
    this.setData({ storeId, reservationId })
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
    const { refundFilter, refunds, reservationId } = this.data
    let filtered = refunds

    // 若传入了 reservationId，优先展示该订单的退款记录
    if (reservationId) {
      filtered = refunds.filter(r => r.reservationId === reservationId)
    } else if (refundFilter !== 'all') {
      // 按状态筛选
      if (refundFilter === 'pending') {
        filtered = refunds.filter(r => {
          const s = (r.status || '').toUpperCase()
          return s === 'REQUEST_CANCEL' || s === 'REQUEST_REFUND'
        })
      } else if (refundFilter === 'approved') {
        filtered = refunds.filter(r => (r.status || '').toUpperCase() === 'COMPLETED')
      } else if (refundFilter === 'rejected') {
        filtered = refunds.filter(r => (r.status || '').toUpperCase() === 'REJECTED')
      }
    }
    this.setData({ filteredRefunds: filtered })
  },

  onFilterRefund(e) {
    this.setData({ refundFilter: e.currentTarget.dataset.filter, reservationId: null })
    this.applyFilter()
  },

  // 通过退款
  onApproveRefund(e) {
    const refund = e.currentTarget.dataset.refund
    if (!refund) return
    const refundTypeText = refund.status === 'REQUEST_CANCEL'
      ? '取消重下单（顾客继续用餐）'
      : '全单退款（释放桌位）'
    wx.showModal({
      title: '⚠️ 确认通过退款',
      content: `金额：¥${refund.refundAmount}\n类型：${refundTypeText}\n\n此操作不可撤销，确认通过？`,
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
