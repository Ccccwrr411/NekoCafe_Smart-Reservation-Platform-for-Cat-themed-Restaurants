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
    this.setData({ loading: true })
    // 各接口独立请求，一个失败不影响其他
    var metricsData = null
    var schedules = []
    var shifts = []
    var exceptions = []

    function tryFinish() {
      self.setData({
        metrics: metricsData,
        schedules: schedules,
        shifts: shifts,
        exceptions: exceptions,
        scheduleStat: {
          todayOnDuty: self.calcTodayOnDuty(schedules),
          pendingReview: self.calcPendingReview(exceptions),
          onLeave: self.calcOnLeave(exceptions),
          shiftDefCount: shifts.length
        },
        mineStat: {
          managedStores: self.data.mineStat.managedStores,
          teamSize: self.calcTeamSize(schedules),
          pendingReview: self.calcPendingReview(exceptions)
        },
        loading: false
      })
    }

    var pending = 4

    function done() {
      pending--
      if (pending <= 0) tryFinish()
    }

    var storeId = this.data.storeId

    get('/api/dashboard/metrics?storeId=' + storeId + '&range=7d').then(function(res) {
      if (res.code === 0 && res.data) {
        var raw = res.data
        var cd = raw.chartData || {}
        var labels = cd.labels || []
        // 将后端 chartData 字段映射为模板期望的 spaceEfficiency/turnoverRate/repurchaseRate 结构
        metricsData = Object.assign({}, raw, {
          spaceEfficiency: { labels: labels, values: cd.revenuePerSeat || [] },
          turnoverRate:    { labels: labels, values: cd.tableTurnoverRate || [] },
          repurchaseRate:  { labels: labels, values: cd.repurchaseRate || [] }
        })
      }
      done()
    }).catch(function(err) {
      console.warn('[staffDashboard] metrics failed:', err)
      done()
    })

    get('/api/manager/schedules?storeId=' + storeId).then(function(res) {
      if (res.code === 0) schedules = res.data || []
      done()
    }).catch(function(err) {
      console.warn('[staffDashboard] schedules failed:', err)
      done()
    })

    get('/api/manager/shifts').then(function(res) {
      if (res.code === 0) shifts = res.data || []
      done()
    }).catch(function(err) {
      console.warn('[staffDashboard] shifts failed:', err)
      done()
    })

    get('/api/manager/exceptions?storeId=' + storeId).then(function(res) {
      if (res.code === 0) exceptions = res.data || []
      done()
    }).catch(function(err) {
      console.warn('[staffDashboard] exceptions failed:', err)
      done()
    })
  },

  formatToday() {
    var d = new Date()
    var y = d.getFullYear()
    var m = String(d.getMonth() + 1).padStart(2, '0')
    var day = String(d.getDate()).padStart(2, '0')
    return y + '-' + m + '-' + day
  },

  calcTodayOnDuty(schedules) {
    var todayStr = this.formatToday()
    var count = 0
    for (var i = 0; i < schedules.length; i++) {
      if (schedules[i].workDate === todayStr && schedules[i].startTime) count++
    }
    return count
  },

  calcPendingReview(exceptions) {
    var count = 0
    for (var i = 0; i < exceptions.length; i++) {
      if (exceptions[i].status === 'PENDING') count++
    }
    return count
  },

  calcOnLeave(exceptions) {
    var todayStr = this.formatToday()
    var count = 0
    for (var i = 0; i < exceptions.length; i++) {
      var e = exceptions[i]
      if (e.status === 'APPROVED' && e.type === 'LEAVE' && e.exceptionDate === todayStr) count++
    }
    return count
  },

  calcTeamSize(schedules) {
    var ids = new Set()
    for (var i = 0; i < schedules.length; i++) {
      ids.add(schedules[i].staffId)
    }
    return ids.size
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
