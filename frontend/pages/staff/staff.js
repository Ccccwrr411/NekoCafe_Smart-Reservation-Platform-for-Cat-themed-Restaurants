// pages/staff/staff.js
const { get, post } = require('../../utils/request')
const app = getApp()

const ALERT_LEVEL_LABEL = {
  critical: '🔴 严重',
  warning:  '🟡 警告',
  info:     '🔵 提示'
}
const STATUS_CYCLE = ['available', 'booked', 'occupied', 'cleaning', 'maintenance']
const STATUS_LABEL = {
  occupied:    '使用中',
  booked:      '已预约',
  available:   '空闲',
  cleaning:    '打扫中',
  maintenance: '维护中'
}
const ORDER_STATUS_LABEL = {
  completed: '已完成',
  confirmed: '已确认',
  occupied:  '进行中',
  cancelled: '已取消',
  pending:   '待支付'
}

// 全部门店列表（用于 hq_ops 切换）
const ALL_STORES = [
  { id: 1, name: 'NekoCafé 朝阳店' },
  { id: 2, name: 'NekoCafé 海淀店' },
  { id: 3, name: 'NekoCafé 通州店' },
  { id: 4, name: 'NekoCafé 西城店' },
  { id: 5, name: 'NekoCafé 丰台店' }
]

Page({
  data: {
    currentTab: 0,   // 0=桌位管理, 1=接单中心, 2=告警中心, 3=全部订单
    tables: [],
    alerts: [],
    pendingOrders: [],
    orders: [],       // 全部订单
    loading: true,
    userRole: '',
    userName: '',
    userRoleId: '',   // 角色英文ID
    storeId: 1,
    storeName: 'NekoCafé 朝阳店',
    // 门店选择（仅总部运营可用）
    showStorePicker: false,
    allStores: ALL_STORES,
    storePickerIndex: 0,
    // 桌位统计
    countAvailable: 0, countOccupied: 0, countBooked: 0, countCleaning: 0,
    // 订单统计
    orderCount: 0, orderRevenue: 0,
    // 调度弹层
    showDispatchModal: false,
    dispatchTable: null,
    dispatchStatuses: [],
    dispatchTableStatusLabel: ''
  },

  onLoad(options) {
    if (!app.requireRole(['staff', 'manager', 'hq_ops'])) return
    const userInfo = app.globalData.userInfo || {}
    const userRole = app.globalData.userRole || ''
    const isHqOps = (userRole === 'hq_ops')

    // 优先使用 URL 参数传入的 storeId（总部运营从 Dashboard 跳转时带入）
    const paramStoreId = options.storeId ? parseInt(options.storeId) : null

    let storeId, storeName
    if (isHqOps) {
      storeId = paramStoreId || 1
      const store = ALL_STORES.find(s => s.id === storeId)
      storeName = '总部视角 · ' + (store ? store.name : ALL_STORES[0].name)
    } else {
      storeId = paramStoreId || (userInfo.storeId || 1)
      const store = ALL_STORES.find(s => s.id === storeId)
      storeName = store ? store.name : (userInfo.storeName || 'NekoCafé 朝阳店')
    }

    const pickerIndex = ALL_STORES.findIndex(s => s.id === storeId)
    this.setData({
      userRole: userInfo.roleLabel || '',
      userName: userInfo.nickName || '',
      userRoleId: userRole,
      storeId: storeId,
      storeName: storeName,
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

  // ── 门店切换（仅总部运营） ──────────────────────────────
  onStoreChange(e) {
    const idx = parseInt(e.detail.value)
    const store = ALL_STORES[idx]
    this.setData({ storeId: store.id, storeName: '总部视角 · ' + store.name, storePickerIndex: idx })
    this.loadData()
  },

  // ── 加载数据 ─────────────────────────────────────────────
  loadData() {
    this.setData({ loading: true })
    const sid = this.data.storeId
    Promise.all([
      get('/api/staff/tables?storeId=' + sid),
      get('/api/staff/alerts?storeId=' + sid),
      get('/api/staff/orders?storeId=' + sid)
    ]).then(([tableRes, alertRes, orderRes]) => {
      wx.stopPullDownRefresh()
      const tables = (tableRes.code === 0) ? tableRes.data : []
      const alerts = (alertRes.code === 0) ? alertRes.data : []
      const allOrders = (orderRes.code === 0) ? orderRes.data : []
      // 按 storeId 过滤订单（mock 返回全部，前端二次过滤）
      const orders = allOrders.filter(o => o.storeId === sid)
      // 给订单附加状态中文标签
      const ordersWithLabel = orders.map(o => ({ ...o, statusLabel: ORDER_STATUS_LABEL[o.status] || o.status }))
      // 待接单
      const pendingOrders = tables.filter(t => t.status === 'booked').map(t => ({
        tableId: t.id, tableName: t.name, tableType: t.type,
        customer: t.customer, arriveTime: t.arriveTime, catType: t.catType
      }))
      // 状态标签
      const tablesWithLabel = tables.map(t => ({ ...t, statusLabel: STATUS_LABEL[t.status] || t.status }))
      const alertsWithLabel = alerts.map(a => ({ ...a, levelLabel: ALERT_LEVEL_LABEL[a.level] || a.level }))
      // 统计
      const countAvailable = tables.filter(t => t.status === 'available').length
      const countOccupied  = tables.filter(t => t.status === 'occupied').length
      const countBooked    = tables.filter(t => t.status === 'booked').length
      const countCleaning  = tables.filter(t => t.status === 'cleaning').length
      const orderCount = orders.length
      const orderRevenue = orders.reduce((sum, o) => sum + (o.totalAmount || 0), 0)
      this.setData({
        tables: tablesWithLabel, alerts: alertsWithLabel, pendingOrders,
        orders: ordersWithLabel, loading: false,
        countAvailable, countOccupied, countBooked, countCleaning,
        orderCount, orderRevenue
      })
    }).catch(() => {
      wx.stopPullDownRefresh()
      this.setData({ loading: false })
    })
  },

  // ── Tab 切换 ─────────────────────────────────────────────
  switchTab(e) {
    const tab = parseInt(e.currentTarget.dataset.idx)
    this.setData({ currentTab: tab })
    // 切换到订单 Tab 时加载订单（如尚未加载）
    if (tab === 3 && this.data.orders.length === 0) {
      this.loadData()
    }
  },

  // ── 接单：确认到店 ───────────────────────────────────────
  onAcceptOrder(e) {
    const order = e.currentTarget.dataset.order
    wx.showModal({
      title: '确认顾客到店',
      content: `${order.customer} 预约 ${order.tableName}（${order.arriveTime}）\n确认后桌位状态变为"使用中"`,
      confirmText: '确认到店', cancelText: '取消',
      success: (res) => {
        if (!res.confirm) return
        const tables = this.data.tables.map(t =>
          t.id === order.tableId ? { ...t, status: 'occupied', statusLabel: '使用中' } : t
        )
        const pendingOrders = this.data.pendingOrders.filter(o => o.tableId !== order.tableId)
        const countOccupied = tables.filter(t => t.status === 'occupied').length
        const countBooked = tables.filter(t => t.status === 'booked').length
        this.setData({ tables, pendingOrders, countOccupied, countBooked })
        wx.showToast({ title: order.tableName + ' 已接单', icon: 'success' })
      }
    })
  },

  // ── 拒绝接单（未到取消） ──────────────────────────────────
  onRejectOrder(e) {
    const order = e.currentTarget.dataset.order
    wx.showModal({
      title: '确认未到店取消',
      content: `${order.customer} 超时未到，是否取消预约并释放 ${order.tableName}？`,
      confirmText: '确认取消', cancelText: '再等等', confirmColor: '#E74C3C',
      success: (res) => {
        if (!res.confirm) return
        const tables = this.data.tables.map(t =>
          t.id === order.tableId ? { ...t, status: 'available', statusLabel: '空闲', customer: '-', arriveTime: '-', estLeaveTime: '-' } : t
        )
        const pendingOrders = this.data.pendingOrders.filter(o => o.tableId !== order.tableId)
        const countAvailable = tables.filter(t => t.status === 'available').length
        const countBooked = tables.filter(t => t.status === 'booked').length
        this.setData({ tables, pendingOrders, countAvailable, countBooked })
        wx.showToast({ title: '已取消预约', icon: 'none' })
      }
    })
  },

  // ── 调度弹层 ─────────────────────────────────────────────
  onDispatch(e) {
    const table = e.currentTarget.dataset.table
    const dispatchStatuses = STATUS_CYCLE.filter(s => s !== table.status).map(s => ({ key: s, label: STATUS_LABEL[s] }))
    this.setData({ showDispatchModal: true, dispatchTable: table, dispatchStatuses, dispatchTableStatusLabel: STATUS_LABEL[table.status] || table.status })
  },
  onDispatchMaskTap() {
    this.setData({ showDispatchModal: false, dispatchTable: null })
  },
  onSetStatus(e) {
    const newStatus = e.currentTarget.dataset.status
    const table = this.data.dispatchTable
    const tables = this.data.tables.map(t => {
      const updated = t.id === table.id ? { ...t, status: newStatus } : t
      return { ...updated, statusLabel: STATUS_LABEL[updated.status] || updated.status }
    })
    const countAvailable = tables.filter(t => t.status === 'available').length
    const countOccupied  = tables.filter(t => t.status === 'occupied').length
    const countBooked    = tables.filter(t => t.status === 'booked').length
    const countCleaning  = tables.filter(t => t.status === 'cleaning').length
    this.setData({ tables, showDispatchModal: false, dispatchTable: null,
      countAvailable, countOccupied, countBooked, countCleaning })
    wx.showToast({ title: table.name + ' → ' + STATUS_LABEL[newStatus], icon: 'success' })
    if (newStatus === 'available') {
      const pendingOrders = tables.filter(t => t.status === 'booked').map(t => ({
        tableId: t.id, tableName: t.name, tableType: t.type,
        customer: t.customer, arriveTime: t.arriveTime, catType: t.catType
      }))
      this.setData({ pendingOrders })
    }
  },

  // ── 查看订单详情（跳转完整详情页）───────────────────────
  onViewOrderDetail(e) {
    const order = e.currentTarget.dataset.order
    if (order && order.id) {
      wx.navigateTo({ url: '/pages/orderDetail/orderDetail?orderId=' + order.id })
    }
  },

  // ── 处理告警 ─────────────────────────────────────────────
  onHandleAlert(e) {
    const alert = e.currentTarget.dataset.alert
    wx.showModal({
      title: alert.title,
      content: alert.desc + '\n\n请线下跟进处理',
      showCancel: false,
      confirmText: '已知晓'
    })
  },

  // ── 退出登录 ─────────────────────────────────────────────
  onLogout() {
    wx.showModal({
      title: '退出登录',
      content: '确认退出当前账号？',
      success: (res) => { if (res.confirm) app.logout() }
    })
  }
})
