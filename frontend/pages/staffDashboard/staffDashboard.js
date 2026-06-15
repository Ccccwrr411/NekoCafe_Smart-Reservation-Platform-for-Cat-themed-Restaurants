// pages/staffDashboard/staffDashboard.js
// 店长工作台 - 自定义 TabBar 容器
// 包含三个 Tab：数据看板 / 人员调动 / 我的
// 注意：app.json 的 tabBar 是顾客端全局配置，店长端必须自绘底部 TabBar
// 所有数据走真实后端接口（useMock=false），从接口动态加载
const { get, post } = require('../../utils/request')
const app = getApp()

const TYPE_LABEL = { LEAVE: '请假', SWAP: '调班', OVERTIME: '加班', NO_SHOW: '客人未到' }
const STATUS_LABEL = {
  PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回',
  ACKNOWLEDGED: '已确认', RESOLVED: '已解决'
}
const STATUS_COLOR = {
  PENDING:      { bg: '#FFF7E6', fg: '#D48806' },
  APPROVED:     { bg: '#E8F8EE', fg: '#27AE60' },
  REJECTED:     { bg: '#FDECEC', fg: '#E74C3C' },
  ACKNOWLEDGED: { bg: '#EAF4FE', fg: '#3498DB' },
  RESOLVED:     { bg: '#F0F0F0', fg: '#7F8C8D' }
}

Page({
  data: {
    activeTab: 'dashboard',
    tabs: [
      { key: 'dashboard', icon: '📊', label: '数据看板' },
      { key: 'schedule',  icon: '👥', label: '人员调动' },
      { key: 'mine',      icon: '👤', label: '我的' }
    ],
    userInfo: null,
    userRoleLabel: '',
    storeName: '',
    storeId: 1,
    showLogoutModal: false,
    loading: false,

    // 数据看板
    metrics: null,
    // 人员调动快速统计
    scheduleStat: {
      todayOnDuty: 0,
      pendingReview: 0,
      onLeave: 0,
      shiftDefCount: 0
    },
    // 我的页面统计
    mineStat: {
      managedStores: 1,
      teamSize: 0,
      pendingReview: 0
    },

    // 人员调动子 Tab
    scheduleSubTab: 'schedule',
    scheduleSubTabs: [
      { key: 'schedule',  label: '排班表' },
      { key: 'shift',     label: '班次' },
      { key: 'exception', label: '异常申请' }
    ],
    schedules: [],
    shifts: [],
    exceptions: [],
    TYPE_LABEL: TYPE_LABEL,
    STATUS_LABEL: STATUS_LABEL,
    STATUS_COLOR: STATUS_COLOR
  },

  onLoad() {
    if (!app.requireRole(['manager', 'hq_ops'])) return
    this.refreshUserInfo()
    this.loadAll()
  },

  onShow() {
    if (app.globalData.userInfo) {
      this.refreshUserInfo()
    }
  },

  refreshUserInfo() {
    const userInfo = app.globalData.userInfo || {}
    const userRole = app.globalData.userRole || ''
    this.setData({
      userInfo: userInfo,
      userRoleLabel: userInfo.roleLabel || (userRole === 'hq_ops' ? '总部运营' : '店长'),
      storeId: userInfo.storeId,
      storeName: userInfo.storeName || '',
      mineStat: {
        ...this.data.mineStat,
        managedStores: userRole === 'hq_ops' ? 5 : 1
      }
    })
  },

  loadAll() {
    var self = this
    var storeId = this.data.storeId
    if (!storeId) {
      this.setData({ loading: false })
      return
    }
    this.setData({ loading: true })
    Promise.all([
      get('/api/dashboard/metrics?storeId=' + storeId + '&range=7d'),
      get('/api/manager/schedules?storeId=' + storeId),
      get('/api/manager/shifts'),
      get('/api/manager/exceptions?storeId=' + storeId)
    ]).then(function(res) {
      var metricsRes = res[0]
      var schedRes = res[1]
      var shiftRes = res[2]
      var exRes = res[3]
      var metrics = metricsRes.code === 0 ? metricsRes.data : null
      var schedules = schedRes.code === 0 ? schedRes.data : []
      var shifts = shiftRes.code === 0 ? shiftRes.data : []
      var exceptions = exRes.code === 0 ? exRes.data : []

      var todayStr = self.formatToday()
      var todayOnDuty = 0
      var i
      for (i = 0; i < schedules.length; i++) {
        if (schedules[i].workDate === todayStr && schedules[i].startTime) todayOnDuty++
      }
      var pendingReview = 0
      for (i = 0; i < exceptions.length; i++) {
        if (exceptions[i].status === 'PENDING') pendingReview++
      }
      var onLeave = 0
      for (i = 0; i < exceptions.length; i++) {
        var e = exceptions[i]
        if (e.status === 'APPROVED' && e.type === 'LEAVE' && e.exceptionDate === todayStr) onLeave++
      }
      var shiftDefCount = shifts.length

      var teamIds = new Set()
      for (i = 0; i < schedules.length; i++) {
        teamIds.add(schedules[i].staffId)
      }

      self.setData({
        metrics: metrics,
        schedules: schedules,
        shifts: shifts,
        exceptions: exceptions,
        scheduleStat: { todayOnDuty: todayOnDuty, pendingReview: pendingReview, onLeave: onLeave, shiftDefCount: shiftDefCount },
        mineStat: {
          managedStores: self.data.mineStat.managedStores,
          teamSize: teamIds.size,
          pendingReview: pendingReview
        },
        loading: false
      })
    }).catch(function() {
      self.setData({ loading: false })
    })
  },

  formatToday() {
    var d = new Date()
    var y = d.getFullYear()
    var m = String(d.getMonth() + 1).padStart(2, '0')
    var day = String(d.getDate()).padStart(2, '0')
    return y + '-' + m + '-' + day
  },

  onSwitchTab(e) {
    var key = e.currentTarget.dataset.key
    if (key === this.data.activeTab) return
    this.setData({ activeTab: key })
  },

  onViewStoreOrders() {
    var storeId = this.data.storeId
    if (!storeId) {
      wx.showToast({ title: '暂无门店信息', icon: 'none' })
      return
    }
    wx.navigateTo({ url: '/pages/staff/staff?storeId=' + storeId })
  },

  onEditProfile() {
    wx.navigateTo({ url: '/pages/settings/settings' })
  },

  onGoSettings() {
    wx.navigateTo({ url: '/pages/settings/settings' })
  },

  onTapLogout() {
    this.setData({ showLogoutModal: true })
  },

  onCancelLogout() {
    this.setData({ showLogoutModal: false })
  },

  onConfirmLogout() {
    this.setData({ showLogoutModal: false })
    app.logout()
  },

  // 人员调动子 Tab 切换
  onSwitchScheduleTab(e) {
    this.setData({ scheduleSubTab: e.currentTarget.dataset.key })
  },

  // 刷新人员调动统计（审批后调用）
  refreshScheduleStat() {
    var schedules = this.data.schedules
    var exceptions = this.data.exceptions
    var todayStr = this.formatToday()
    var todayOnDuty = schedules.filter(function(s) { return s.workDate === todayStr && s.startTime }).length
    var pendingReview = exceptions.filter(function(e) { return e.status === 'PENDING' }).length
    var onLeave = exceptions.filter(function(e) {
      return e.status === 'APPROVED' && e.type === 'LEAVE' && e.exceptionDate === todayStr
    }).length
    var teamIds = new Set(schedules.map(function(s) { return s.staffId }))
    this.setData({
      scheduleStat: {
        todayOnDuty: todayOnDuty,
        pendingReview: pendingReview,
        onLeave: onLeave,
        shiftDefCount: this.data.scheduleStat.shiftDefCount
      },
      mineStat: {
        managedStores: this.data.mineStat.managedStores,
        teamSize: teamIds.size,
        pendingReview: pendingReview
      }
    })
  },

  // 审批异常申请
  onReviewException(e) {
    var that = this
    var id = e.currentTarget.dataset.id
    var action = e.currentTarget.dataset.action
    var actionText = action === 'approve' ? '通过' : '驳回'
    wx.showModal({
      title: '审批确认',
      content: '确认' + actionText + '此异常申请？',
      success: function(res) {
        if (!res.confirm) return
        post('/api/manager/exception/review', { exceptionId: id, action: action }).then(function(r) {
          if (r.code === 0) {
            wx.showToast({ title: r.data.message, icon: 'success' })
            var newList = that.data.exceptions.map(function(item) {
              if (item.exceptionId === id) {
                return Object.assign({}, item, { status: r.data.status, statusLabel: STATUS_LABEL[r.data.status] })
              }
              return item
            })
            that.setData({ exceptions: newList })
            that.refreshScheduleStat()
          }
        })
      }
    })
  }
})
