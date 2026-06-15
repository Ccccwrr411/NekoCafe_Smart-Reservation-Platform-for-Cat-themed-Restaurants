// pages/staffDashboard/staffDashboard.js
// 店长工作台 - 自定义 TabBar 容器
// 包含三个 Tab：数据看板 / 人员调动 / 我的
// 注意：app.json 的 tabBar 是顾客端全局配置，店长端必须自绘底部 TabBar
// 所有数据走真实后端接口（useMock=false），从接口动态加载
const { get, post, put } = require('../../utils/request')
const chart = require('../../utils/chart')
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
    STATUS_COLOR: STATUS_COLOR,

    // ── 排班弹窗（创建 / 编辑） ──────────────────────────────
    showScheduleModal: false,     // 是否显示弹窗
    scheduleModalMode: 'create',  // 'create' | 'edit'
    scheduleModalTitle: '新建排班',
    scheduleForm: {
      scheduleId: null,
      staffId: '',           // 内部保留，选中员工后自动填入
      staffDisplayName: '',  // 显示在输入框中的员工姓名
      workDate: '',
      shiftId: '',
      position: '',
      notes: ''
    },
    scheduleFormSubmitting: false,
    // picker 用：班次选项列表（从 shifts 数据生成）
    shiftPickerRange: [],
    shiftPickerIndex: 0,

    // ── 员工搜索 ────────────────────────────────────────────
    staffKeyword: '',          // 搜索关键词
    staffSearchResults: [],   // 搜索结果列表
    staffSearchLoading: false,
    staffSearchTimer: null    // 防抖定时器
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

  /**
   * 绘制三个 Canvas 图表：坪效折线图 / 翻台率柱状图 / 复购率饼图
   */
  drawCharts() {
    var metrics = this.data.metrics
    if (!metrics) return

    var that = this

    // ── 坪效 折线图 ──
    chart.initCanvas('#chart-space', this).then(function(res) {
      chart.drawLine(res.ctx, {
        labels: metrics.spaceEfficiency.labels || [],
        values: metrics.spaceEfficiency.values || [],
        width: res.width,
        height: res.height,
        lineColor: '#C97E5A'
      })
    }).catch(function(e) {
      console.warn('[chart] spaceEfficiency line chart failed:', e)
    })

    // ── 翻台率 柱状图 ──
    chart.initCanvas('#chart-turnover', this).then(function(res) {
      chart.drawBar(res.ctx, {
        labels: metrics.turnoverRate.labels || [],
        values: metrics.turnoverRate.values || [],
        width: res.width,
        height: res.height,
        barColor: '#8B5A3C'
      })
    }).catch(function(e) {
      console.warn('[chart] turnoverRate bar chart failed:', e)
    })

    // ── 会员复购率 饼图 ──
    chart.initCanvas('#chart-repurchase', this).then(function(res) {
      chart.drawPie(res.ctx, {
        labels: metrics.repurchaseRate.labels || [],
        values: metrics.repurchaseRate.values || [],
        width: res.width,
        height: res.height
      })
    }).catch(function(e) {
      console.warn('[chart] repurchaseRate pie chart failed:', e)
    })
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
    var metricsData = null
    var schedules = []
    var shifts = []
    var exceptions = []

    function tryFinish() {
      // 生成班次 Picker 范围（shiftId 对应显示名）
      var shiftPickerRange = shifts.map(function(s) {
        return s.shiftName + '（' + (s.startTime || '') + '-' + (s.endTime || '') + '）'
      })
      self.setData({
        metrics: metricsData,
        schedules: schedules,
        shifts: shifts,
        exceptions: exceptions,
        shiftPickerRange: shiftPickerRange,
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
      // 延迟画图表，等 Canvas 渲染完成
      setTimeout(function() { self.drawCharts() }, 300)
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

  // 刷新人员调动统计
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
  },

  // ════════════════════════════════════════════════════════════════
  //  排班 创建 / 编辑 弹窗
  // ════════════════════════════════════════════════════════════════

  /** 打开"新建排班"弹窗 */
  onOpenCreateSchedule() {
    var defaultDate = this.formatToday()
    var shiftPickerIndex = 0
    var defaultShiftId = this.data.shifts.length > 0 ? this.data.shifts[0].shiftId : ''
    this.setData({
      showScheduleModal: true,
      scheduleModalMode: 'create',
      scheduleModalTitle: '新建排班',
      shiftPickerIndex: shiftPickerIndex,
      scheduleForm: {
        scheduleId: null,
        staffId: '',
        staffDisplayName: '',
        workDate: defaultDate,
        shiftId: String(defaultShiftId),
        position: '',
        notes: ''
      },
      staffKeyword: '',
      staffSearchResults: [],
      staffSearchLoading: false
    })
  },

  /** 打开"编辑排班"弹窗，传入当前排班数据 */
  onOpenEditSchedule(e) {
    var item = e.currentTarget.dataset.item
    // 找到 shiftId 在 shifts 列表中的下标
    var shiftPickerIndex = 0
    var shifts = this.data.shifts
    for (var i = 0; i < shifts.length; i++) {
      if (shifts[i].shiftId === item.shiftId) {
        shiftPickerIndex = i
        break
      }
    }
    this.setData({
      showScheduleModal: true,
      scheduleModalMode: 'edit',
      scheduleModalTitle: '编辑排班',
      shiftPickerIndex: shiftPickerIndex,
      scheduleForm: {
        scheduleId: item.scheduleId,
        staffId: String(item.staffId || ''),
        staffDisplayName: item.staffName || '',
        workDate: item.workDate || '',
        shiftId: String(item.shiftId || ''),
        position: item.position || '',
        notes: item.notes || ''
      },
      staffKeyword: item.staffName || '',
      staffSearchResults: [],
      staffSearchLoading: false
    })
  },

  /** 关闭弹窗 */
  onCloseScheduleModal() {
    this.setData({ showScheduleModal: false, staffSearchResults: [] })
  },

  /** 阻止弹窗内容区域点击冒泡（避免点内容区关掉弹窗） */
  onScheduleModalContentTap() {},

  /** 表单字段同步（非员工姓名字段） */
  onScheduleFormInput(e) {
    var field = e.currentTarget.dataset.field
    var value = e.detail.value
    var form = Object.assign({}, this.data.scheduleForm)
    form[field] = value
    this.setData({ scheduleForm: form })
  },

  /** 员工姓名搜索输入 —— 防抖 300ms 后调接口 */
  onStaffNameInput(e) {
    var keyword = (e.detail.value || '').trim()
    var that = this
    var form = Object.assign({}, this.data.scheduleForm)
    form.staffDisplayName = keyword
    // 输入时清空已选 staffId（防止用户改了名字但 id 还是旧的）
    form.staffId = ''
    this.setData({ staffKeyword: keyword, scheduleForm: form })

    // 清掉旧定时器
    if (this.data.staffSearchTimer) {
      clearTimeout(this.data.staffSearchTimer)
    }

    // 空关键词 → 清掉搜索结果
    if (!keyword) {
      this.setData({ staffSearchResults: [], staffSearchLoading: false })
      return
    }

    // 防抖 300ms
    var timer = setTimeout(function() {
      that.doStaffSearch(keyword)
    }, 300)
    this.setData({ staffSearchTimer: timer })
  },

  /** 执行员工搜索请求 */
  doStaffSearch(keyword) {
    var that = this
    var storeId = this.data.storeId
    this.setData({ staffSearchLoading: true })
    get('/api/manager/staff?storeId=' + storeId + '&keyword=' + encodeURIComponent(keyword)).then(function(res) {
      if (res.code === 0) {
        that.setData({ staffSearchResults: res.data || [], staffSearchLoading: false })
      } else {
        that.setData({ staffSearchResults: [], staffSearchLoading: false })
      }
    }).catch(function(err) {
      console.warn('[staffSearch] failed:', err)
      that.setData({ staffSearchResults: [], staffSearchLoading: false })
    })
  },

  /** 从搜索结果中选中一名员工 */
  onSelectStaff(e) {
    var userId = e.currentTarget.dataset.userid
    var displayName = e.currentTarget.dataset.name
    var form = Object.assign({}, this.data.scheduleForm)
    form.staffId = String(userId)
    form.staffDisplayName = displayName
    this.setData({
      scheduleForm: form,
      staffKeyword: displayName,
      staffSearchResults: []  // 选中后关闭下拉列表
    })
  },

  /** 班次 Picker 选择 */
  onShiftPickerChange(e) {
    var index = Number(e.detail.value)
    var shifts = this.data.shifts
    if (index < 0 || index >= shifts.length) return
    var form = Object.assign({}, this.data.scheduleForm)
    form.shiftId = String(shifts[index].shiftId)
    this.setData({ shiftPickerIndex: index, scheduleForm: form })
  },

  /** 提交表单（创建或更新） */
  onSubmitScheduleForm() {
    var form = this.data.scheduleForm
    var mode = this.data.scheduleModalMode

    // 基本校验
    if (!form.staffId || String(form.staffId).trim() === '') {
      wx.showToast({ title: '请选择员工', icon: 'none' }); return
    }
    if (!form.workDate) {
      wx.showToast({ title: '请选择工作日期', icon: 'none' }); return
    }
    if (!form.shiftId) {
      wx.showToast({ title: '请选择班次', icon: 'none' }); return
    }

    this.setData({ scheduleFormSubmitting: true })

    var that = this
    var storeId = this.data.storeId

    if (mode === 'create') {
      var body = {
        storeId: storeId,
        staffId: Number(form.staffId),
        workDate: form.workDate,
        shiftId: Number(form.shiftId),
        position: form.position || '',
        notes: form.notes || ''
      }
      post('/api/manager/schedule', body).then(function(res) {
        that.setData({ scheduleFormSubmitting: false })
        if (res.code === 0) {
          wx.showToast({ title: '排班创建成功', icon: 'success' })
          that.setData({ showScheduleModal: false })
          // 将新记录追加到列表
          var newSchedule = res.data.schedule
          var newList = that.data.schedules.concat(newSchedule ? [newSchedule] : [])
          that.setData({ schedules: newList })
          that.refreshScheduleStat()
        } else {
          wx.showToast({ title: res.message || '创建失败', icon: 'none' })
        }
      }).catch(function(err) {
        that.setData({ scheduleFormSubmitting: false })
        console.error('[createSchedule] error:', err)
        wx.showToast({ title: '网络异常', icon: 'none' })
      })
    } else {
      // edit
      var scheduleId = form.scheduleId
      var updateBody = {
        staffId: Number(form.staffId),
        workDate: form.workDate,
        shiftId: Number(form.shiftId),
        position: form.position || '',
        notes: form.notes || ''
      }
      put('/api/manager/schedule/' + scheduleId, updateBody).then(function(res) {
        that.setData({ scheduleFormSubmitting: false })
        if (res.code === 0) {
          wx.showToast({ title: '排班更新成功', icon: 'success' })
          that.setData({ showScheduleModal: false })
          // 更新列表中对应条目
          var updatedSchedule = res.data.schedule
          var newList = that.data.schedules.map(function(item) {
            if (item.scheduleId === scheduleId) {
              return updatedSchedule || item
            }
            return item
          })
          that.setData({ schedules: newList })
          that.refreshScheduleStat()
        } else {
          wx.showToast({ title: res.message || '更新失败', icon: 'none' })
        }
      }).catch(function(err) {
        that.setData({ scheduleFormSubmitting: false })
        console.error('[updateSchedule] error:', err)
        wx.showToast({ title: '网络异常', icon: 'none' })
      })
    }
  }
})
