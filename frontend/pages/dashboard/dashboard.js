// pages/dashboard/dashboard.js
const { get } = require('../../utils/request')
const app = getApp()

const ALL_STORES = [
  { id: 1, name: 'NekoCafé 朝阳店' },
  { id: 2, name: 'NekoCafé 海淀店' },
  { id: 3, name: 'NekoCafé 通州店' },
  { id: 4, name: 'NekoCafé 西城店' },
  { id: 5, name: 'NekoCafé 丰台店' }
]

Page({
  data: {
    metrics: null,
    hqOverview: null,  // 总部运营门店概览数据
    loading: true,
    storeId: 1,
    storeName: 'NekoCafé 朝阳店',
    range: '7d',
    userRole: '',
    userRoleId: '',
    userName: '',
    showStorePicker: false,
    allStores: ALL_STORES,
    storePickerIndex: 0
  },

  onLoad() {
    if (!app.requireRole(['manager', 'hq_ops'])) return
    const userInfo = app.globalData.userInfo || {}
    const userRole = app.globalData.userRole || ''
    const isHqOps = (userRole === 'hq_ops')
    const storeId = isHqOps ? 1 : (userInfo.storeId || 1)
    const storeName = isHqOps ? '总部视角 · 朝阳店' : (userInfo.storeName || 'NekoCafé 朝阳店')
    const pickerIndex = isHqOps ? 0 : ALL_STORES.findIndex(s => s.id === storeId)
    this.setData({
      userRole: userInfo.roleLabel || '',
      userRoleId: userRole,
      userName: userInfo.nickName || '',
      storeId: storeId,
      storeName: storeName,
      showStorePicker: isHqOps,
      storePickerIndex: pickerIndex >= 0 ? pickerIndex : 0
    })
    this.loadAll()
  },

  // ── 门店切换（仅总部运营） ──────────────────────────────
  onStoreChange(e) {
    const idx = parseInt(e.detail.value)
    const store = ALL_STORES[idx]
    this.setData({ storeId: store.id, storeName: '总部视角 · ' + store.name, storePickerIndex: idx })
    this.loadMetrics()
  },

  loadAll() {
    this.loadMetrics()
    // 总部运营额外加载门店概览
    if (this.data.userRoleId === 'hq_ops') {
      this.loadHqOverview()
    }
  },

  loadHqOverview() {
    get('/api/hq/stores-overview').then(res => {
      if (res.code === 0) {
        this.setData({ hqOverview: res.data })
      }
    })
  },

  loadMetrics() {
    this.setData({ loading: true })
    get('/api/dashboard/metrics?storeId=' + this.data.storeId + '&range=' + this.data.range).then(res => {
      if (res.code === 0) {
        this.setData({ metrics: res.data, loading: false })
        setTimeout(() => {
          this.drawSpaceEfficiencyChart()
          this.drawTurnoverRateChart()
          this.drawRepurchaseRateChart()
        }, 300)
      }
    }).catch(() => {
      this.setData({ loading: false })
    })
  },

  // ========== 坪效折线图 ==========
  drawSpaceEfficiencyChart() {
    const data = this.data.metrics.spaceEfficiency
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

  // ========== 会员复购率饼图 ==========
  drawRepurchaseRateChart() {
    const data = this.data.metrics.repurchaseRate
    const query = wx.createSelectorQuery()
    query.select('#chartPie').fields({ node: true, size: true }).exec((res) => {
      if (!res[0] || !res[0].node) return
      const canvas = res[0].node
      const ctx = canvas.getContext('2d')
      const dpr = wx.getSystemInfoSync().pixelRatio
      const w = res[0].width
      const h = res[0].height
      canvas.width = w * dpr
      canvas.height = h * dpr
      ctx.scale(dpr, dpr)

      ctx.fillStyle = '#FAFBFC'
      ctx.fillRect(0, 0, w, h)

      const cx = w / 2
      const cy = h / 2
      const r = Math.min(cx, cy) - 20
      const values = data.values
      const labels = data.labels
      const colors = ['#3498DB', '#2ECC71', '#F39C12', '#E74C3C']
      const total = values.reduce((a, b) => a + b, 0)

      let startAngle = -Math.PI / 2
      values.forEach((v, i) => {
        const angle = (v / total) * Math.PI * 2
        ctx.fillStyle = colors[i]
        ctx.beginPath()
        ctx.moveTo(cx, cy)
        ctx.arc(cx, cy, r, startAngle, startAngle + angle)
        ctx.closePath()
        ctx.fill()
        const midAngle = startAngle + angle / 2
        const labelR = r + 20
        const lx = cx + Math.cos(midAngle) * labelR
        const ly = cy + Math.sin(midAngle) * labelR
        ctx.fillStyle = '#333'
        ctx.font = 'bold 12px sans-serif'
        ctx.textAlign = lx > cx ? 'left' : 'right'
        ctx.fillText(labels[i] + ' ' + v + '%', lx, ly + 4)
        startAngle += angle
      })
    })
  },

  onLogout() {
    wx.showModal({
      title: '退出登录',
      content: '确认退出当前账号？',
      success: (res) => { if (res.confirm) app.logout() }
    })
  },

  // ── 查看门店订单（店长跳转店员后台） ──────────────────
  onViewStoreOrders() {
    wx.navigateTo({
      url: '/pages/staff/staff?storeId=' + this.data.storeId
    })
  },

  // ── 总部运营点击门店卡片查看该店订单 ──────────────────
  onViewHqStoreOrders(e) {
    const storeId = e.currentTarget.dataset.storeid
    wx.navigateTo({
      url: '/pages/staff/staff?storeId=' + storeId
    })
  }
})
