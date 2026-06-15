// pages/hq/workspace.js
// 总部运营工作台：看板 / 运营 / 通知 / 我的 四 Tab
const { get, post, put, del, patch } = require('../../utils/request')
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
        if (res.code === 0) {
          this.setData({ metrics: res.data })
          setTimeout(() => {
            this.drawSpaceEfficiencyChart()
            this.drawTurnoverRateChart()
            this.drawRepurchaseRateChart()
          }, 300)
        }
      })
      .catch(() => { this.setData({ loading: false }) })
  },

  // ========== 坪效折线图 ==========
  drawSpaceEfficiencyChart() {
    const data = this.data.metrics.spaceEfficiency
    if (!data) return
    const query = wx.createSelectorQuery()
    query.select('#chartLine').fields({ node: true, size: true }).exec((res) => {
      if (!res[0] || !res[0].node) return
      const canvas = res[0].node
      const ctx = canvas.getContext('2d')
      const dpr = wx.getSystemInfoSync().pixelRatio
      const w = res[0].width
      const h = res[0].height
      canvas.width = w * dpr
      canvas.height = h * dpr
      ctx.scale(dpr, dpr)

      const pad = { top: 20, right: 20, bottom: 40, left: 50 }
      const chartW = w - pad.left - pad.right
      const chartH = h - pad.top - pad.bottom
      const labels = data.labels
      const values = data.values
      if (!values || values.length === 0) return
      const maxVal = Math.max(...values)
      const minVal = Math.min(...values)

      ctx.fillStyle = '#FAFBFC'
      ctx.fillRect(0, 0, w, h)

      ctx.fillStyle = '#999'
      ctx.font = '10px sans-serif'
      ctx.textAlign = 'right'
      for (let i = 0; i <= 4; i++) {
        const y = pad.top + (chartH / 4) * (4 - i)
        const val = Math.round(minVal + (maxVal - minVal) * i / 4)
        ctx.fillText(val, pad.left - 6, y + 4)
        ctx.strokeStyle = '#E8E8E8'
        ctx.beginPath()
        ctx.moveTo(pad.left, y)
        ctx.lineTo(w - pad.right, y)
        ctx.stroke()
      }

      ctx.strokeStyle = '#C97E5A'
      ctx.lineWidth = 2.5
      ctx.lineJoin = 'round'
      ctx.beginPath()
      const points = values.map((v, i) => ({
        x: pad.left + (chartW / (values.length - 1)) * i,
        y: pad.top + chartH - ((v - minVal) / (maxVal - minVal || 1)) * chartH
      }))
      points.forEach((p, i) => i === 0 ? ctx.moveTo(p.x, p.y) : ctx.lineTo(p.x, p.y))
      ctx.stroke()

      points.forEach(p => {
        ctx.fillStyle = '#fff'
        ctx.beginPath()
        ctx.arc(p.x, p.y, 4, 0, Math.PI * 2)
        ctx.fill()
        ctx.strokeStyle = '#C97E5A'
        ctx.lineWidth = 2
        ctx.stroke()
      })

      ctx.fillStyle = '#333'
      ctx.font = 'bold 11px sans-serif'
      ctx.textAlign = 'center'
      points.forEach((p, i) => { ctx.fillText(values[i], p.x, p.y - 10) })

      ctx.fillStyle = '#888'
      ctx.font = '10px sans-serif'
      ctx.textAlign = 'center'
      labels.forEach((l, i) => {
        const x = pad.left + (chartW / (labels.length - 1)) * i
        ctx.fillText(l, x, h - 8)
      })
    })
  },

  // ========== 翻台率柱状图 ==========
  drawTurnoverRateChart() {
    const data = this.data.metrics.turnoverRate
    if (!data) return
    const query = wx.createSelectorQuery()
    query.select('#chartBar').fields({ node: true, size: true }).exec((res) => {
      if (!res[0] || !res[0].node) return
      const canvas = res[0].node
      const ctx = canvas.getContext('2d')
      const dpr = wx.getSystemInfoSync().pixelRatio
      const w = res[0].width
      const h = res[0].height
      canvas.width = w * dpr
      canvas.height = h * dpr
      ctx.scale(dpr, dpr)

      const pad = { top: 20, right: 20, bottom: 40, left: 50 }
      const chartW = w - pad.left - pad.right
      const chartH = h - pad.top - pad.bottom
      const labels = data.labels
      const values = data.values
      if (!values || values.length === 0) return
      const maxVal = Math.max(...values)

      ctx.fillStyle = '#FAFBFC'
      ctx.fillRect(0, 0, w, h)

      ctx.fillStyle = '#999'
      ctx.font = '10px sans-serif'
      ctx.textAlign = 'right'
      for (let i = 0; i <= 4; i++) {
        const y = pad.top + (chartH / 4) * (4 - i)
        const val = (maxVal / 4 * i).toFixed(1)
        ctx.fillText(val + 'x', pad.left - 6, y + 4)
        ctx.strokeStyle = '#E8E8E8'
        ctx.beginPath()
        ctx.moveTo(pad.left, y)
        ctx.lineTo(w - pad.right, y)
        ctx.stroke()
      }

      const barCount = values.length
      const barW = chartW / barCount * 0.6
      const gap = chartW / barCount * 0.4
      const colors = ['#E74C3C', '#E67E22', '#F1C40F', '#2ECC71', '#3498DB', '#9B59B6', '#1ABC9C']

      values.forEach((v, i) => {
        const barH = (v / maxVal) * chartH
        const x = pad.left + (chartW / barCount) * i + gap / 2
        const y = pad.top + chartH - barH
        ctx.fillStyle = colors[i % colors.length]
        ctx.beginPath()
        const r = 4
        ctx.moveTo(x + r, y)
        ctx.lineTo(x + barW - r, y)
        ctx.arcTo(x + barW, y, x + barW, y + r, r)
        ctx.lineTo(x + barW, pad.top + chartH)
        ctx.lineTo(x, pad.top + chartH)
        ctx.lineTo(x, y + r)
        ctx.arcTo(x, y, x + r, y, r)
        ctx.fill()
        ctx.fillStyle = '#333'
        ctx.font = 'bold 11px sans-serif'
        ctx.textAlign = 'center'
        ctx.fillText(v.toFixed(1) + 'x', x + barW / 2, y - 6)
      })

      ctx.fillStyle = '#888'
      ctx.font = '10px sans-serif'
      ctx.textAlign = 'center'
      labels.forEach((l, i) => {
        const x = pad.left + (chartW / barCount) * i + barW / 2 + gap / 2
        ctx.fillText(l, x, h - 8)
      })
    })
  },

  // ========== 会员复购率折线图 ==========
  drawRepurchaseRateChart() {
    const data = this.data.metrics.repurchaseRate
    if (!data) return
    const query = wx.createSelectorQuery()
    query.select('#chartLine2').fields({ node: true, size: true }).exec((res) => {
      if (!res[0] || !res[0].node) return
      const canvas = res[0].node
      const ctx = canvas.getContext('2d')
      const dpr = wx.getSystemInfoSync().pixelRatio
      const w = res[0].width
      const h = res[0].height
      canvas.width = w * dpr
      canvas.height = h * dpr
      ctx.scale(dpr, dpr)

      const pad = { top: 20, right: 20, bottom: 40, left: 50 }
      const chartW = w - pad.left - pad.right
      const chartH = h - pad.top - pad.bottom
      const labels = data.labels
      const values = data.values
      if (!values || values.length === 0) return
      const maxVal = Math.max(...values)
      const minVal = 0  // 复购率从0开始

      ctx.fillStyle = '#FAFBFC'
      ctx.fillRect(0, 0, w, h)

      ctx.fillStyle = '#999'
      ctx.font = '10px sans-serif'
      ctx.textAlign = 'right'
      for (let i = 0; i <= 4; i++) {
        const y = pad.top + (chartH / 4) * (4 - i)
        const val = Math.round(maxVal * i / 4)
        ctx.fillText(val + '%', pad.left - 6, y + 4)
        ctx.strokeStyle = '#E8E8E8'
        ctx.beginPath()
        ctx.moveTo(pad.left, y)
        ctx.lineTo(w - pad.right, y)
        ctx.stroke()
      }

      ctx.strokeStyle = '#2ECC71'
      ctx.lineWidth = 2.5
      ctx.lineJoin = 'round'
      ctx.beginPath()
      const points = values.map((v, i) => ({
        x: pad.left + (chartW / (values.length - 1)) * i,
        y: pad.top + chartH - ((v - minVal) / (maxVal - minVal || 1)) * chartH
      }))
      points.forEach((p, i) => i === 0 ? ctx.moveTo(p.x, p.y) : ctx.lineTo(p.x, p.y))
      ctx.stroke()

      points.forEach(p => {
        ctx.fillStyle = '#fff'
        ctx.beginPath()
        ctx.arc(p.x, p.y, 4, 0, Math.PI * 2)
        ctx.fill()
        ctx.strokeStyle = '#2ECC71'
        ctx.lineWidth = 2
        ctx.stroke()
      })

      ctx.fillStyle = '#333'
      ctx.font = 'bold 11px sans-serif'
      ctx.textAlign = 'center'
      points.forEach((p, i) => { ctx.fillText(values[i] + '%', p.x, p.y - 10) })

      ctx.fillStyle = '#888'
      ctx.font = '10px sans-serif'
      ctx.textAlign = 'center'
      labels.forEach((l, i) => {
        const x = pad.left + (chartW / (labels.length - 1)) * i
        ctx.fillText(l, x, h - 8)
      })
    })
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
