// pages/cats/cats.js
const { get } = require('../../utils/request')
const app = getApp()

Page({
  data: {
    cats: [],
    loading: true,
    storeId: 1,
    // 详情模式
    detailMode: false,
    catDetail: null,
    detailLoading: false,
    isCatKeeper: false  // 是否是猫咪管家（显示健康管理入口）
  },

  onLoad(options) {
    const storeId = options.storeId || 1
    const role = app.globalData.userRole || ''
    this.setData({ storeId, isCatKeeper: ['cat_keeper', 'manager', 'hq_ops'].includes(role) })
    this.loadCats()
  },

  onPullDownRefresh() {
    this.loadCats()
  },

  loadCats() {
    this.setData({ loading: true })
    get(`/api/cats?storeId=${this.data.storeId}`).then(res => {
      wx.stopPullDownRefresh()
      if (res.code === 0) {
        this.setData({ cats: res.data, loading: false })
      }
    }).catch(() => {
      wx.stopPullDownRefresh()
      this.setData({ loading: false })
    })
  },

  // 点击猫咪 → 打开健康详情
  onCatClick(e) {
    const cat = e.currentTarget.dataset.cat
    this.setData({ detailMode: true, detailLoading: true })
    get(`/api/cats/detail?catId=${cat.id}`).then(res => {
      if (res.code === 0) {
        this.setData({ catDetail: res.data, detailLoading: false })
        // 延迟绘制趋势图（等待 canvas 渲染）
        setTimeout(() => this.drawWeightChart(res.data.weightHistory), 300)
      }
    }).catch(() => {
      this.setData({ detailLoading: false })
    })
  },

  // 返回列表
  goBackList() {
    this.setData({ detailMode: false, catDetail: null })
  },

  // Canvas 2D 绘制体重趋势图
  drawWeightChart(weightHistory) {
    const query = wx.createSelectorQuery()
    query.select('#weightChart')
      .fields({ node: true, size: true })
      .exec((res) => {
        if (!res || !res[0]) return
        const canvas = res[0].node
        const ctx = canvas.getContext('2d')
        const dpr = wx.getSystemInfoSync().pixelRatio
        const width = res[0].width
        const height = res[0].height
        canvas.width = width * dpr
        canvas.height = height * dpr
        ctx.scale(dpr, dpr)

        const { labels, values } = weightHistory
        const padding = { top: 20, right: 20, bottom: 30, left: 40 }
        const chartW = width - padding.left - padding.right
        const chartH = height - padding.top - padding.bottom
        const minVal = Math.min(...values) - 0.3
        const maxVal = Math.max(...values) + 0.3
        const scale = (v) => padding.top + chartH - ((v - minVal) / (maxVal - minVal)) * chartH

        // 背景
        ctx.fillStyle = '#fff'
        ctx.fillRect(0, 0, width, height)

        // 网格线
        ctx.strokeStyle = '#eee'
        ctx.lineWidth = 0.5
        for (let i = 0; i <= 4; i++) {
          const y = padding.top + (chartH / 4) * i
          ctx.beginPath(); ctx.moveTo(padding.left, y); ctx.lineTo(width - padding.right, y); ctx.stroke()
          // Y 轴标签
          const val = (maxVal - ((maxVal - minVal) / 4) * i).toFixed(1)
          ctx.fillStyle = '#999'
          ctx.font = '10px sans-serif'
          ctx.textAlign = 'right'
          ctx.fillText(val, padding.left - 6, y + 3)
        }

        // X 轴标签
        ctx.fillStyle = '#999'
        ctx.font = '10px sans-serif'
        ctx.textAlign = 'center'
        labels.forEach((label, i) => {
          const x = padding.left + (chartW / (labels.length - 1)) * i
          ctx.fillText(label, x, height - padding.bottom + 16)
        })

        // 折线
        ctx.strokeStyle = '#C97E5A'
        ctx.lineWidth = 2
        ctx.beginPath()
        labels.forEach((_, i) => {
          const x = padding.left + (chartW / (labels.length - 1)) * i
          const y = scale(values[i])
          if (i === 0) ctx.moveTo(x, y); else ctx.lineTo(x, y)
        })
        ctx.stroke()

        // 数据点
        labels.forEach((_, i) => {
          const x = padding.left + (chartW / (labels.length - 1)) * i
          const y = scale(values[i])
          ctx.fillStyle = '#C97E5A'
          ctx.beginPath(); ctx.arc(x, y, 4, 0, 2 * Math.PI); ctx.fill()
          ctx.fillStyle = '#fff'
          ctx.beginPath(); ctx.arc(x, y, 2, 0, 2 * Math.PI); ctx.fill()
          // 数据标签
          ctx.fillStyle = '#333'
          ctx.font = 'bold 11px sans-serif'
          ctx.textAlign = 'center'
          ctx.fillText(values[i].toFixed(1), x, y - 10)
        })
      })
  },

  onShareAppMessage() {
    const cat = this.data.catDetail
    return {
      title: cat ? `来看看NekoCafé的${cat.name}（${cat.breed}）` : 'NekoCafé 猫咪档案',
      path: '/pages/cats/cats'
    }
  }
})
