// pages/hq/workspace.js
// 总部运营工作台：看板 / 运营 / 通知 / 我的 四 Tab
const { get, post, put, del, patch } = require('../../utils/request')
const chart = require('../../utils/chart')
const app = getApp()

const ALL_STORES = [
  { id: 1, name: '朝阳店' },
  { id: 2, name: '海淀店' },
  { id: 3, name: '通州店' },
  { id: 4, name: '西城店' },
  { id: 5, name: '丰台店' }
]

Page({
  data: {
    activeTab: 'dashboard',
    loading: false,
    userName: '',

    // ── Tab1 看板 ──
    hqOverview: null,
    storeId: 1,
    range: '7d',
    metrics: null,
    allStores: ALL_STORES,
    storePickerIndex: 0,

    // ── Tab2 运营 - 活动管理 ──
    promoSubTab: 'promotions',  // promotions | coupons
    promoFilter: 'all',
    promotions: [],

    // ── Tab2 运营 - 发券 ──
    activePromos: [],
    activePromoLabels: [],
    // 单人
    singlePromoIdx: -1,
    singlePromoLabel: '',
    singleUserId: '',
    singleResult: '',
    // 批量
    batchPromoIdx: -1,
    batchPromoLabel: '',
    batchTargetType: '',
    batchTargetLabel: '',
    batchTargetTypes: ['全部用户', '按角色', '按会员等级'],
    batchTargetValues: ['all', 'role', 'member_level'],
    batchRoleIdx: -1,
    batchRoleLabel: '',
    batchMemberIdx: -1,
    batchMemberLabel: '',
    batchResult: '',

    // ── Tab4 我的 ──
    userInfo: null
  },

  onLoad() {
    const userInfo = app.globalData.userInfo || {}
    this.setData({
      userName: userInfo.nickName || userInfo.userName || '运营'
    })
  },

  onShow() {
    this.loadTabData()
  },

  // ════════════════════════════════════════════════════════
  // Tab 切换
  // ════════════════════════════════════════════════════════
  switchTab(e) {
    const tab = e.currentTarget.dataset.tab
    this.setData({ activeTab: tab }, () => this.loadTabData())
  },

  loadTabData() {
    const tab = this.data.activeTab
    if (tab === 'dashboard') this.loadAll()
    else if (tab === 'marketing') { this.loadPromotions(); this.loadActivePromos() }
    else if (tab === 'profile') this.loadUserInfo()
  },

  // 运营子 Tab 切换
  switchPromoSubTab(e) {
    const tab = e.currentTarget.dataset.tab
    this.setData({ promoSubTab: tab })
    if (tab === 'promotions') this.loadPromotions()
    else if (tab === 'coupons') this.loadActivePromos()
  },

  // ════════════════════════════════════════════════════════
  // Tab1 看板
  // ════════════════════════════════════════════════════════
  loadHqOverview() {
    this.setData({ loading: true })
    get('/api/hq/stores-overview')
      .then(res => {
        this.setData({ loading: false })
        if (res.code === 0) {
          this.setData({ hqOverview: res.data })
        } else {
          wx.showToast({ title: res.message || '加载失败', icon: 'none' })
        }
      })
      .catch(() => {
        this.setData({ loading: false })
        wx.showToast({ title: '网络异常', icon: 'none' })
      })
  },

  loadAll() {
    this.loadMetrics()
  },

  loadMetrics() {
    this.setData({ loading: true })
    get('/api/dashboard/metrics', { storeId: this.data.storeId, range: this.data.range })
      .then(res => {
        this.setData({ loading: false })
        if (res.code === 0 && res.data) {
          // 与 staffDashboard 完全一致：将 chartData 转换为三个图表子对象
          var raw = res.data
          var cd = raw.chartData || {}
          var labels = cd.labels || []
          var metricsData = Object.assign({}, raw, {
            spaceEfficiency: { labels: labels, values: cd.revenuePerSeat || [] },
            turnoverRate:    { labels: labels, values: cd.tableTurnoverRate || [] },
            repurchaseRate:  { labels: labels, values: cd.repurchaseRate || [] }
          })
          this.setData({ metrics: metricsData })
          // 等待 setData 渲染完成后再绘图
          setTimeout(() => { this.drawCharts() }, 300)
        }
      })
      .catch(() => { this.setData({ loading: false }) })
  },

  /**
   * 绘制三个 Canvas 图表（与 staffDashboard 完全对应）
   * 坪效折线图 / 翻台率柱状图 / 会员复购率饼图
   * 内置一次重试：canvas 初始化可能因渲染时序失败
   */
  drawCharts() {
    var metrics = this.data.metrics
    if (!metrics || !metrics.spaceEfficiency) return

    var that = this

    /**
     * 尝试绘制单个图表，失败后延迟重试一次
     * @param {string} selector  canvas 选择器
     * @param {function} drawFn  绘制函数 (ctx, options)
     * @param {object} options   绘制参数
     * @param {string} name      图表名称（日志用）
     */
    function tryDraw(selector, drawFn, options, name) {
      chart.initCanvas(selector, that).then(function(res) {
        var fullOpts = Object.assign({}, options, { width: res.width, height: res.height })
        drawFn(res.ctx, fullOpts)
      }).catch(function(e) {
        console.warn('[hq chart] ' + name + ' first attempt failed:', e)
        // 500ms 后重试一次
        setTimeout(function() {
          chart.initCanvas(selector, that).then(function(res2) {
            var fullOpts2 = Object.assign({}, options, { width: res2.width, height: res2.height })
            drawFn(res2.ctx, fullOpts2)
          }).catch(function(e2) {
            console.warn('[hq chart] ' + name + ' retry also failed:', e2)
          })
        }, 500)
      })
    }

    // ── 坪效 折线图 ──
    tryDraw('#chart-space', chart.drawLine, {
      labels: (metrics.spaceEfficiency && metrics.spaceEfficiency.labels) || [],
      values: (metrics.spaceEfficiency && metrics.spaceEfficiency.values) || [],
      lineColor: '#C97E5A'
    }, 'spaceEfficiency')

    // ── 翻台率 柱状图 ──
    tryDraw('#chart-turnover', chart.drawBar, {
      labels: (metrics.turnoverRate && metrics.turnoverRate.labels) || [],
      values: (metrics.turnoverRate && metrics.turnoverRate.values) || [],
      barColor: '#8B5A3C'
    }, 'turnoverRate')

    // ── 会员复购率 饼图 ──
    tryDraw('#chart-repurchase', chart.drawPie, {
      labels: (metrics.repurchaseRate && metrics.repurchaseRate.labels) || [],
      values: (metrics.repurchaseRate && metrics.repurchaseRate.values) || []
    }, 'repurchaseRate')
  },

  onStoreChange(e) {
    const idx = parseInt(e.detail.value)
    const store = this.data.allStores[idx]
    this.setData({
      storePickerIndex: idx,
      storeId: store.id
    }, () => {
      // 如果在看板 Tab，切换门店后重新加载数据
      if (this.data.activeTab === 'dashboard') {
        this.loadAll()
      }
    })
  },

  fmtDate(dateStr) {
    if (!dateStr) return ''
    const s = String(dateStr)
    return s.length >= 10 ? s.substring(0, 10) : s
  },

  // ════════════════════════════════════════════════════════
  // Tab2 运营 - 活动管理
  // ════════════════════════════════════════════════════════
  loadPromotions() {
    this.setData({ loading: true })
    const params = { page: 1, pageSize: 50 }
    if (this.data.promoFilter === 'active') params.isActive = true
    if (this.data.promoFilter === 'inactive') params.isActive = false

    get('/api/hq/promotions', params)
      .then(res => {
        this.setData({ loading: false })
        if (res.code === 0) {
          const list = (res.data && res.data.list) ? res.data.list : []
          const formatted = list.map(p => ({
            ...p,
            startTimeStr: this.fmtDate(p.startTime),
            endTimeStr: this.fmtDate(p.endTime)
          }))
          this.setData({ promotions: formatted })
        }
      })
      .catch(() => { this.setData({ loading: false }); wx.showToast({ title: '网络异常', icon: 'none' }) })
  },

  setPromoFilter(e) {
    this.setData({ promoFilter: e.currentTarget.dataset.filter }, () => this.loadPromotions())
  },

  // ── 新建 / 编辑（跳转独立页面）──
  showCreatePromoModal() {
    wx.navigateTo({ url: '/pages/hq/promo-form/promo-form?mode=create' })
  },

  editPromo(e) {
    const item = e.currentTarget.dataset.item
    wx.setStorageSync('__promo_edit_data__', item)
    wx.navigateTo({ url: '/pages/hq/promo-form/promo-form?mode=edit' })
  },

  togglePromo(e) {
    const id = e.currentTarget.dataset.id
    const active = e.currentTarget.dataset.active
    const action = active ? '停用' : '启用'
    wx.showModal({
      title: `确认${action}`, content: `确定要${action}该活动吗？`,
      success: (res) => {
        if (!res.confirm) return
        this.setData({ loading: true })
        patch(`/api/hq/promotions/${id}/toggle`)
          .then(res => {
            this.setData({ loading: false })
            if (res.code === 0) { wx.showToast({ title: `${action}成功`, icon: 'success' }); this.loadPromotions(); this.loadActivePromos() }
            else wx.showToast({ title: res.message || '操作失败', icon: 'none' })
          })
          .catch(() => { this.setData({ loading: false }); wx.showToast({ title: '网络异常', icon: 'none' }) })
      }
    })
  },

  deletePromo(e) {
    const id = e.currentTarget.dataset.id
    const name = e.currentTarget.dataset.name || '该活动'
    wx.showModal({
      title: '确认删除', content: `确定要删除"${name}"吗？`,
      success: (res) => {
        if (!res.confirm) return
        this.setData({ loading: true })
        del(`/api/hq/promotions/${id}`)
          .then(res => {
            this.setData({ loading: false })
            if (res.code === 0) { wx.showToast({ title: '删除成功', icon: 'success' }); this.loadPromotions(); this.loadActivePromos() }
            else wx.showToast({ title: res.message || '删除失败', icon: 'none' })
          })
          .catch(() => { this.setData({ loading: false }); wx.showToast({ title: '网络异常', icon: 'none' }) })
      }
    })
  },

  // ════════════════════════════════════════════════════════
  // Tab2 运营 - 发券
  // ════════════════════════════════════════════════════════
  loadActivePromos() {
    get('/api/hq/promotions', { isActive: true, page: 1, pageSize: 100 })
      .then(res => {
        if (res.code === 0) {
          const list = (res.data && res.data.list) ? res.data.list : []
          this.setData({ activePromos: list, activePromoLabels: list.map(p => p.name) })
        }
      })
  },

  // 单人发券
  onSinglePromoPick(e) {
    const idx = parseInt(e.detail.value)
    this.setData({ singlePromoIdx: idx, singlePromoLabel: this.data.activePromoLabels[idx] })
  },
  onSingleUserId(e) { this.setData({ singleUserId: e.detail.value }) },
  sendSingleCoupon() {
    const { activePromos, singlePromoIdx, singleUserId } = this.data
    const promo = activePromos[singlePromoIdx]
    const userId = parseInt(singleUserId)
    if (!promo || !userId) { wx.showToast({ title: '请选择活动并输入用户ID', icon: 'none' }); return }
    this.setData({ loading: true, singleResult: '' })
    post('/api/hq/coupons/send', { promoId: promo.promoId, userId })
      .then(res => {
        this.setData({ loading: false })
        this.setData({ singleResult: res.code === 0 ? '✅ 发券成功' : `❌ ${res.message || '发券失败'}` })
      })
      .catch(() => { this.setData({ loading: false, singleResult: '❌ 网络异常' }) })
  },

  // 批量发券
  onBatchPromoPick(e) {
    const idx = parseInt(e.detail.value)
    this.setData({ batchPromoIdx: idx, batchPromoLabel: this.data.activePromoLabels[idx], batchResult: '' })
  },
  onBatchTargetPick(e) {
    const idx = parseInt(e.detail.value)
    this.setData({ batchTargetType: this.data.batchTargetValues[idx], batchTargetLabel: this.data.batchTargetTypes[idx], batchResult: '' })
  },
  onBatchRolePick(e) {
    const idx = parseInt(e.detail.value)
    const roles = ['顾客', '店员', '店长', '总部运营', '猫咪管家']
    this.setData({ batchRoleIdx: idx, batchRoleLabel: roles[idx] })
  },
  onBatchMemberPick(e) {
    const idx = parseInt(e.detail.value)
    const levels = ['普通会员', '银卡会员', '金卡会员', '钻石会员']
    this.setData({ batchMemberIdx: idx, batchMemberLabel: levels[idx] })
  },
  sendBatchCoupon() {
    const { activePromos, batchPromoIdx, batchTargetType } = this.data
    const promo = activePromos[batchPromoIdx]
    if (!promo || !batchTargetType) { wx.showToast({ title: '请选择活动和目标类型', icon: 'none' }); return }

    let targetValue = null
    if (batchTargetType === 'role') targetValue = String(this.data.batchRoleIdx + 1)
    else if (batchTargetType === 'member_level') targetValue = this.data.batchMemberLabel

    this.setData({ loading: true, batchResult: '' })
    post('/api/hq/coupons/batch-send', {
      promoId: promo.promoId,
      targetType: batchTargetType,
      targetValue,
      userIds: null
    }).then(res => {
      this.setData({ loading: false })
      if (res.code === 0) {
        const d = res.data || {}
        const sc = d.success || 0
        const sk = d.skipped || 0
        this.setData({ batchResult: `完成：发放 ${sc} 人，跳过(已领) ${sk} 人` })
        if (sc === 0 && sk === 0) {
          wx.showToast({ title: '未找到目标用户，请确认数据库有对应等级会员', icon: 'none', duration: 3000 })
        }
      } else {
        this.setData({ batchResult: `❌ ${res.message || '发券失败'}` })
      }
    }).catch(() => { this.setData({ loading: false, batchResult: '❌ 网络异常' }) })
  },

  // ════════════════════════════════════════════════════════
  // Tab3 我的
  // ════════════════════════════════════════════════════════
  loadUserInfo() {
    get('/api/user/profile').then(res => {
      if (res.code === 0) this.setData({ userInfo: res.data })
    })
  },

  onLogout() {
    wx.showModal({
      title: '退出登录',
      content: '确认退出当前账号？',
      success: (res) => { if (res.confirm) app.logout() }
    })
  }
})
