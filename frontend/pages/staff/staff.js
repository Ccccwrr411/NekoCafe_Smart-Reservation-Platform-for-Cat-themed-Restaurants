// pages/staff/staff.js — 店员工作台（4Tab底部导航）
const { get, post } = require('../../utils/request')
const app = getApp()

// ── 常量 ──────────────────────────────────────
const ORDER_STATUS_LABEL = {
  booked:    '待下单',
  confirmed: '已确认',
  making:    '制作中',
  serving:   '已上菜',
  completed: '已完成',
  cancelled: '已取消',
  refunding: '退款中',
  refunded:  '已退款'
}
const TABLE_STATUS_LABEL = {
  available:   '空闲',
  booked:      '已预约',
  occupied:    '用餐中',
  cleaning:    '待清洁',
}
const ALERT_LEVEL_LABEL = {
  high:   '🔴 严重',
  medium: '🟡 警告',
  low:    '🔵 提示'
}
const DISPATCH_OPTIONS = [
  { key: 'available', label: '🟢 设为空闲' },
  { key: 'booked', label: '⚪ 设为预定' },
  { key: 'occupied', label: '🔴 设为用餐中' },
  { key: 'cleaning', label: '🟡 设为待清洁' },
]
const STATUS_TO_DB = {
  available: 'IDLE',
  booked: 'RESERVED',
  occupied: 'OCCUPIED',
  cleaning: 'CLEANING',
  maintenance: 'IDLE'  // 数据库无 maintenance，映射为 IDLE
}
const ALL_STORES = [
  { id: 1, name: 'NekoCafé 朝阳店' },
  { id: 2, name: 'NekoCafé 海淀店' },
  { id: 3, name: 'NekoCafé 通州店' },
  { id: 4, name: 'NekoCafé 西城店' },
  { id: 5, name: 'NekoCafé 丰台店' }
]

Page({
  data: {
    // 导航
    activeTab: 'orders',   // orders | tables | alerts | profile
    // 订单
    orderFilter: 'all', // booked | confirmed | making | serving | completed | refunding | cancelled | all | pending
    orders: [],
    filteredOrders: [],
    countByStatus: {},
    // 全部下拉
    showAllDropdown: false,
    // 桌位
    tables: [],
    tableViewMode: 'map',   // map | list
    countAvailable: 0,
    countOccupied: 0,
    countBooked: 0,
    countCleaning: 0,
    // 告警
    alerts: [],
    alertFilter: 'pending',  // pending | acknowledged | resolved | all
    filteredAlerts: [],
    pendingAlertCount: 0,
    // 通知
    notifications: [],
    unreadCount: 0,
    // 概览
    todayOrderCount: 0,
    pendingCount: 0,
    refundCount: 0,
    occupancyRate: '0%',
    todayRevenue: 0,
    // 用户
    userRole: '',
    userName: '',
    userRoleId: '',
    storeId: 1,
    storeName: 'NekoCafe 朝阳店',
    showStorePicker: false,
    allStores: ALL_STORES,
    storePickerIndex: 0,
    // 加载
    loading: true,
    // 弹层
    showDispatchModal: false,
    dispatchTable: null,
    dispatchOptions: DISPATCH_OPTIONS,
    showRejectModal: false,
    rejectOrder: null,
    rejectReason: '',
    // 退款审核弹层
    showRefundModal: false,
    refundModalData: null,   // 当前审核的退款记录
    refundModalOrder: null,  // 当前审核的订单
    // 常量（供 wxml 引用）
    ORDER_STATUS_LABEL
  },

  onLoad(options) {
    if (!app.requireRole(['staff', 'manager', 'hq_ops'])) return
    const userInfo = app.globalData.userInfo || {}
    const userRole = app.globalData.userRole || ''
    const isHqOps = (userRole === 'hq_ops')
    const paramStoreId = options.storeId ? parseInt(options.storeId) : null
    let storeId, storeName
    if (isHqOps) {
      storeId = paramStoreId || 1
      const store = ALL_STORES.find(s => s.id === storeId)
      storeName = '总部视角 · ' + (store ? store.name : ALL_STORES[0].name)
    } else {
      storeId = paramStoreId || (userInfo.storeId || 1)
      const store = ALL_STORES.find(s => s.id === storeId)
      storeName = store ? store.name : (userInfo.storeName || 'NekoCafe 朝阳店')
    }
    const pickerIndex = ALL_STORES.findIndex(s => s.id === storeId)
    this.setData({
      userRole: userInfo.roleLabel || '',
      userName: userInfo.nickName || '',
      userRoleId: userRole,
      storeId, storeName,
      showStorePicker: isHqOps,
      storePickerIndex: pickerIndex >= 0 ? pickerIndex : 0
    })
    this.loadData()
  },

  onShow() {
    if (!app.checkRole(['staff', 'manager', 'hq_ops'])) return
    this.loadData()
  },

  onPullDownRefresh() {
    this.loadData()
  },

  // ── 门店切换 ─────────────────────────────────
  onStoreChange(e) {
    const idx = parseInt(e.detail.value)
    const store = ALL_STORES[idx]
    this.setData({ storeId: store.id, storeName: '总部视角 · ' + store.name, storePickerIndex: idx })
    this.loadData()
  },

  // ── 数据加载 ─────────────────────────────────
  loadData() {
    this.setData({ loading: true })
    const sid = this.data.storeId
    Promise.all([
      get('/api/staff/tables?storeId=' + sid),
      get('/api/staff/alerts?storeId=' + sid),
      get('/api/staff/orders?storeId=' + sid),
      get('/api/staff/refunds?storeId=' + sid),
      get('/api/notifications/store?storeId=' + sid + '&page=1&size=50'),
      get('/api/notifications/unread/store?storeId=' + sid)
    ]).then(([tableRes, alertRes, orderRes, refundRes, notifRes, unreadRes]) => {
      wx.stopPullDownRefresh()
      const tables = (tableRes.code === 0) ? tableRes.data : []
      const alerts = (alertRes.code === 0) ? alertRes.data : []
      const allOrders = (orderRes.code === 0) ? orderRes.data : []
      const allRefunds = (refundRes.code === 0) ? refundRes.data : []
      const notifications = (notifRes.code === 0) ? notifRes.data : []
      const unreadCount = (unreadRes.code === 0) ? unreadRes.data : 0

      // 待审核退款数：退款记录中状态为 REQUEST_CANCEL / REQUEST_REFUND
      const refundCount = allRefunds.filter(r => {
        const s = (r.status || '').toUpperCase()
        return s === 'REQUEST_CANCEL' || s === 'REQUEST_REFUND'
      }).length

      // 订单处理：后端返回 refundStatus 字段区分"退款中(refunding)"和"已退款(refunded)"
      const orders = allOrders.map(o => {
        // 若 refundStatus=refunded，前端显示为"已退款"而不是"退款中"
        const effectiveStatus = (o.refundStatus === 'refunded') ? 'refunded' : o.status
        return {
          ...o,
          status: effectiveStatus,
          statusLabel: ORDER_STATUS_LABEL[effectiveStatus] || o.status
        }
      })
      // 按时间升序排序（最早的排最前）
      orders.sort((a, b) => {
        const ta = a.reservationTime ? new Date(a.reservationTime).getTime() : 0
        const tb = b.reservationTime ? new Date(b.reservationTime).getTime() : 0
        return ta - tb
      })
      const countByStatus = {}
      orders.forEach(o => {
        countByStatus[o.status] = (countByStatus[o.status] || 0) + 1
      })

      // 桌位处理
      const tablesWithLabel = tables.map(t => ({
        ...t,
        id: t.tableId,
        name: t.tableNo || ('桌 ' + t.tableId),
        statusLabel: TABLE_STATUS_LABEL[t.status] || t.status
      }))
      const countAvailable = tables.filter(t => t.status === 'available').length
      const countOccupied = tables.filter(t => t.status === 'occupied').length
      const countBooked = tables.filter(t => t.status === 'booked').length
      const countCleaning = tables.filter(t => t.status === 'cleaning').length

      // 告警处理
      const alertsWithLabel = alerts.map(a => ({
        ...a,
        levelLabel: ALERT_LEVEL_LABEL[a.level] || a.level,
        level: a.level || 'low'
      }))
      const pendingAlertCount = alertsWithLabel.filter(a => {
        const s = (a.status || '').toUpperCase()
        return s === 'PENDING' || s === 'ACKNOWLEDGED'
      }).length

      // 概览统计
      const todayOrderCount = allOrders.length
      // 待处理 = 非已完成、非已取消的所有订单
      const pendingCount = orders.filter(o => o.status !== 'completed' && o.status !== 'cancelled').length
      const totalTables = tables.length || 1
      const occupancyRate = Math.round((countOccupied + countBooked) / totalTables * 100) + '%'
      const todayRevenue = allOrders.reduce((sum, o) => sum + (o.totalAmount || 0), 0)

      this.setData({
        tables: tablesWithLabel, alerts: alertsWithLabel, orders, notifications,
        allRefunds, countByStatus, countAvailable, countOccupied, countBooked, countCleaning,
        todayOrderCount, pendingCount, refundCount, occupancyRate, todayRevenue,
        unreadCount, pendingAlertCount, loading: false
      })
      this.applyFilters()
    }).catch(() => {
      wx.stopPullDownRefresh()
      this.setData({ loading: false })
    })
  },

  // ── 筛选 ─────────────────────────────────────
  applyFilters() {
    // 订单筛选
    const { orderFilter, orders } = this.data
    let filteredOrders
    if (orderFilter === 'all') {
      filteredOrders = orders
    } else if (orderFilter === 'pending') {
      // 待处理 = 非已完成、非已取消
      filteredOrders = orders.filter(o => o.status !== 'completed' && o.status !== 'cancelled')
    } else {
      filteredOrders = orders.filter(o => o.status === orderFilter)
    }
    // 告警筛选（三态：待处理/已知晓/已处理）
    const { alertFilter, alerts } = this.data
    let filteredAlerts
    if (alertFilter === 'all') {
      filteredAlerts = alerts
    } else if (alertFilter === 'pending') {
      filteredAlerts = alerts.filter(a => (a.status || '').toUpperCase() === 'PENDING')
    } else if (alertFilter === 'acknowledged') {
      filteredAlerts = alerts.filter(a => (a.status || '').toUpperCase() === 'ACKNOWLEDGED')
    } else {
      // resolved：已处理（RESOLVED / APPROVED / REJECTED）
      filteredAlerts = alerts.filter(a => {
        const s = (a.status || '').toUpperCase()
        return s !== 'PENDING' && s !== 'ACKNOWLEDGED'
      })
    }
    this.setData({ filteredOrders, filteredAlerts })
  },

  onFilterOrder(e) {
    const filter = e.currentTarget.dataset.filter
    this.setData({ orderFilter: filter, showAllDropdown: false })
    this.applyFilters()
  },

  // ── 待处理点击 ────────────────────────────────
  onFilterPending() {
    this.setData({ orderFilter: 'pending', showAllDropdown: false })
    this.applyFilters()
  },

  // ── 全部下拉切换 ──────────────────────────────
  onToggleAllDropdown() {
    this.setData({ showAllDropdown: !this.data.showAllDropdown })
  },
  onCloseAllDropdown() {
    this.setData({ showAllDropdown: false })
  },

  onFilterAlert(e) {
    const filter = e.currentTarget.dataset.filter
    this.setData({ alertFilter: filter })
    this.applyFilters()
  },

  // ── Tab 切换 ─────────────────────────────────
  onSwitchTab(e) {
    this.setData({ activeTab: e.currentTarget.dataset.tab })
  },

  onSwitchTableView(e) {
    this.setData({ tableViewMode: e.currentTarget.dataset.mode })
  },

  // ── 接单 ─────────────────────────────────────
  onAcceptOrder(e) {
    const order = e.currentTarget.dataset.order
    if (!order || !order.reservationId) {
      wx.showToast({ title: '缺少预约信息', icon: 'none' })
      return
    }
    wx.showModal({
      title: '确认接单',
      content: `${order.customerName || '顾客'} 预约 ${order.tableNo || ''}，确认到店接单？`,
      confirmText: '确认接单',
      confirmColor: '#C97E5A',
      success: (res) => {
        if (!res.confirm) return
        wx.showLoading({ title: '处理中...' })
        post('/api/staff/order/accept', { reservationId: order.reservationId }).then(apiRes => {
          wx.hideLoading()
          if (apiRes.code === 0 && apiRes.data && apiRes.data.success) {
            wx.showToast({ title: '接单成功', icon: 'success' })
            this.loadData()
          } else {
            wx.showToast({ title: (apiRes.data && apiRes.data.message) || '接单失败', icon: 'none' })
          }
        }).catch(() => {
          wx.hideLoading()
          wx.showToast({ title: '网络异常', icon: 'none' })
        })
      }
    })
  },

  // ── 拒单弹层 ─────────────────────────────────
  onRejectOrder(e) {
    const order = e.currentTarget.dataset.order
    this.setData({ showRejectModal: true, rejectOrder: order, rejectReason: '' })
  },
  onCloseReject() {
    this.setData({ showRejectModal: false, rejectOrder: null, rejectReason: '' })
  },
  onInputRejectReason(e) {
    this.setData({ rejectReason: e.detail.value })
  },
  onConfirmReject() {
    const order = this.data.rejectOrder
    if (!order) return
    if (!this.data.rejectReason.trim()) {
      wx.showToast({ title: '请填写拒单原因', icon: 'none' })
      return
    }
    wx.showLoading({ title: '处理中...' })
    // 释放桌位
    post('/api/staff/table/dispatch', {
      tableId: order.tableId,
      status: 'IDLE'
    }).then(apiRes => {
      wx.hideLoading()
      if (apiRes.code === 0 && apiRes.data && apiRes.data.success) {
        wx.showToast({ title: '已拒单并释放桌位', icon: 'success' })
        this.setData({ showRejectModal: false, rejectOrder: null, rejectReason: '' })
        this.loadData()
      } else {
        wx.showToast({ title: '操作失败', icon: 'none' })
      }
    }).catch(() => {
      wx.hideLoading()
      wx.showToast({ title: '网络异常', icon: 'none' })
    })
  },

  // ── 订单进度推进 ─────────────────────────────
  onProgressOrder(e) {
    const order = e.currentTarget.dataset.order
    const target = e.currentTarget.dataset.target
    if (!order || !target) return

    const actionLabels = {
      MAKING: '开始制作',
      SERVING: '确认上菜',
      COMPLETED: '完成用餐'
    }
    const confirmLabels = {
      MAKING: `确认将订单 #${order.id} 开始制作？`,
      SERVING: `确认订单 #${order.id} 已上菜？`,
      COMPLETED: `确认订单 #${order.id} 用餐完成？桌位将变为待清洁状态`
    }

    wx.showModal({
      title: actionLabels[target],
      content: confirmLabels[target],
      confirmText: '确认',
      confirmColor: '#C97E5A',
      success: (res) => {
        if (!res.confirm) return
        wx.showLoading({ title: '处理中...' })
        post('/api/staff/order/progress', {
          reservationId: order.reservationId || order.id,
          targetStatus: target
        }).then(apiRes => {
          wx.hideLoading()
          if (apiRes.code === 0 && apiRes.data && apiRes.data.success) {
            wx.showToast({ title: '操作成功', icon: 'success' })
            this.loadData()
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

  // ── 退款审核跳转（概览栏用）──────────────────
  onViewRefund() {
    wx.navigateTo({ url: '/pages/staff/refund?storeId=' + this.data.storeId })
  },

  // ── 审核退款弹窗（订单页用）──────────────────
  onViewRefundDetail(e) {
    const order = e.currentTarget.dataset.order
    const allRefunds = this.data.allRefunds || []
    const refund = allRefunds.find(r =>
      r.reservationId === (order.reservationId || order.id)
      && (r.status === 'REQUEST_CANCEL' || r.status === 'REQUEST_REFUND')
    )
    if (!refund) {
      wx.showToast({ title: '未找到待审核退款记录', icon: 'none' })
      return
    }
    this.setData({ showRefundModal: true, refundModalData: refund, refundModalOrder: order })
  },
  onCloseRefundModal() {
    this.setData({ showRefundModal: false, refundModalData: null, refundModalOrder: null })
  },
  onApproveRefund() {
    const refund = this.data.refundModalData
    if (!refund) return
    const typeText = refund.status === 'REQUEST_CANCEL' ? '取消重下单（顾客继续用餐）' : '全单退款（释放桌位）'
    wx.showModal({
      title: '⚠️ 确认通过退款',
      content: `金额：¥${refund.refundAmount}\n类型：${typeText}\n\n此操作不可撤销，确认通过？`,
      confirmText: '确认通过',
      confirmColor: '#C97E5A',
      success: (res) => {
        if (!res.confirm) return
        wx.showLoading({ title: '处理中...' })
        post('/api/staff/refund/review', {
          refundId: refund.refundId, action: 'approve',
          operatorId: app.globalData.userInfo?.id || null
        }).then(apiRes => {
          wx.hideLoading()
          if (apiRes.code === 0 && apiRes.data?.success) {
            wx.showToast({ title: '退款已通过', icon: 'success' })
            this.setData({ showRefundModal: false })
            this.loadData()
          } else {
            wx.showToast({ title: (apiRes.data && apiRes.data.message) || '操作失败', icon: 'none' })
          }
        }).catch(() => { wx.hideLoading(); wx.showToast({ title: '网络异常', icon: 'none' }) })
      }
    })
  },
  onRejectRefund() {
    const refund = this.data.refundModalData
    if (!refund) return
    wx.showModal({
      title: '⚠️ 拒绝退款 ¥' + (refund.refundAmount || ''),
      content: '请输入拒绝原因',
      editable: true,
      placeholderText: '请填写拒绝原因（必填）',
      confirmText: '确认拒绝',
      confirmColor: '#E74C3C',
      success: (res) => {
        if (!res.confirm) return
        if (!res.content || !res.content.trim()) {
          wx.showToast({ title: '请填写拒绝原因', icon: 'none' })
          return
        }
        wx.showLoading({ title: '处理中...' })
        post('/api/staff/refund/review', {
          refundId: refund.refundId, action: 'reject',
          operatorId: app.globalData.userInfo?.id || null
        }).then(apiRes => {
          wx.hideLoading()
          if (apiRes.code === 0 && apiRes.data?.success) {
            wx.showToast({ title: '已拒绝', icon: 'success' })
            this.setData({ showRefundModal: false })
            this.loadData()
          } else {
            wx.showToast({ title: (apiRes.data && apiRes.data.message) || '操作失败', icon: 'none' })
          }
        }).catch(() => { wx.hideLoading(); wx.showToast({ title: '网络异常', icon: 'none' }) })
      }
    })
  },

  // ── 数据看板跳转 ─────────────────────────────
  onViewDashboard() {
    wx.navigateTo({ url: '/pages/dashboard/dashboard' })
  },

  // ── 桌位调度弹层 ─────────────────────────────
  onDispatch(e) {
    const table = e.currentTarget.dataset.table
    const opts = DISPATCH_OPTIONS.filter(s => s.key !== table.status)
    this.setData({
      showDispatchModal: true,
      dispatchTable: table,
      dispatchOptions: opts
    })
  },
  onCloseDispatch() {
    this.setData({ showDispatchModal: false, dispatchTable: null })
  },
  onSetStatus(e) {
    const newStatus = e.currentTarget.dataset.status
    const table = this.data.dispatchTable
    const dbStatus = STATUS_TO_DB[newStatus] || newStatus.toUpperCase()

    wx.showModal({
      title: '确认切换状态',
      content: `将 ${table.name || table.tableNo} 设为「${TABLE_STATUS_LABEL[newStatus]}」？`,
      confirmColor: '#C97E5A',
      success: (res) => {
        if (!res.confirm) return
        wx.showLoading({ title: '处理中...' })
        post('/api/staff/table/dispatch', {
          tableId: table.id || table.tableId,
          status: dbStatus
        }).then(apiRes => {
          wx.hideLoading()
          if (apiRes.code === 0 && apiRes.data && apiRes.data.success) {
            wx.showToast({ title: '状态已更新', icon: 'success' })
            this.setData({ showDispatchModal: false, dispatchTable: null })
            this.loadData()
          } else {
            wx.showToast({ title: (apiRes.data && apiRes.data.message) || '调度失败', icon: 'none' })
          }
        }).catch(() => {
          wx.hideLoading()
          wx.showToast({ title: '网络异常', icon: 'none' })
        })
      }
    })
  },

  // ── 告警处理 ─────────────────────────────────
  onAcknowledgeAlert(e) {
    const alert = e.currentTarget.dataset.alert
    wx.showModal({
      title: '确认已知晓',
      content: `告警：${alert.reason || alert.type}，确认已知晓？`,
      confirmText: '已知晓',
      confirmColor: '#C97E5A',
      success: (res) => {
        if (!res.confirm) return
        wx.showLoading({ title: '处理中...' })
        post('/api/staff/alert/acknowledge', {
          exceptionId: alert.alertId,
          operatorId: app.globalData.userInfo?.id || null
        }).then(apiRes => {
          wx.hideLoading()
          if (apiRes.code === 0 && apiRes.data && apiRes.data.success) {
            wx.showToast({ title: '已标记', icon: 'success' })
            this.loadData()
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

  // ── 告警解决 ─────────────────────────────────
  onResolveAlert(e) {
    const alert = e.currentTarget.dataset.alert
    wx.showModal({
      title: '解决告警',
      content: `确认已解决「${alert.reason || alert.type}」？`,
      editable: true,
      placeholderText: '可选：输入解决说明',
      confirmText: '确认解决',
      confirmColor: '#4CAF50',
      success: (res) => {
        if (!res.confirm) return
        wx.showLoading({ title: '处理中...' })
        post('/api/staff/alert/resolve', {
          exceptionId: alert.alertId,
          resolution: res.content || '已处理',
          operatorId: app.globalData.userInfo?.id || null
        }).then(apiRes => {
          wx.hideLoading()
          if (apiRes.code === 0 && apiRes.data && apiRes.data.success) {
            wx.showToast({ title: '已解决', icon: 'success' })
            this.loadData()
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

  // ── 通知标记已读 ──────────────────────────────
  onMarkNotificationRead(e) {
    const notif = e.currentTarget.dataset.notif
    wx.showLoading({ title: '处理中...' })
    post('/api/notifications/read', {
      notificationId: notif.notificationId
    }).then(apiRes => {
      wx.hideLoading()
      if (apiRes.code === 0 && apiRes.data && apiRes.data.success) {
        wx.showToast({ title: '已标记', icon: 'success' })
        this.loadData()
      } else {
        wx.showToast({ title: '操作失败', icon: 'none' })
      }
    }).catch(() => {
      wx.hideLoading()
      wx.showToast({ title: '网络异常', icon: 'none' })
    })
  },

  // ── 通知全部已读 ──────────────────────────────
  onMarkAllRead() {
    wx.showModal({
      title: '全部已读',
      content: '确认将所有消息标记为已读？',
      confirmText: '全部已读',
      confirmColor: '#3498DB',
      success: (res) => {
        if (!res.confirm) return
        wx.showLoading({ title: '处理中...' })
        post('/api/notifications/read-all/store', {
          storeId: this.data.storeId
        }).then(apiRes => {
          wx.hideLoading()
          if (apiRes.code === 0) {
            wx.showToast({ title: '已全部标记', icon: 'success' })
            this.loadData()
          } else {
            wx.showToast({ title: '操作失败', icon: 'none' })
          }
        }).catch(() => {
          wx.hideLoading()
          wx.showToast({ title: '网络异常', icon: 'none' })
        })
      }
    })
  },

  // ── 退出登录 ─────────────────────────────────
  onLogout() {
    wx.showModal({
      title: '退出登录',
      content: '确认退出当前账号？',
      confirmColor: '#E74C3C',
      success: (res) => { if (res.confirm) app.logout() }
    })
  }
})
