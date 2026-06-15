// pages/cats/cats.js
const { get, post } = require('../../utils/request')
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
    isCatKeeper: false,  // 是否是猫咪管家（显示健康管理入口）
    // 健康打卡弹窗
    showHealthForm: false,
    healthFormType: 'WEIGHT',  // WEIGHT / VACCINE / INTERACTION
    typeIndex: 0,               // picker 当前选中索引
    healthFormValue: '',
    healthFormNote: '',
    healthFormMood: 'happy',   // 互动心情
    healthFormNextDue: '',     // 疫苗下次日期
    healthFormVaccineDate: '', // 疫苗接种日期
    healthSubmitting: false,
    typeOptions: [
      { value: 'WEIGHT', label: '体重记录' },
      { value: 'VACCINE', label: '疫苗记录' },
      { value: 'INTERACTION', label: '互动记录' }
    ]
  },

  onLoad(options) {
    const userInfo = app.globalData.userInfo || {}
    const role = app.globalData.userRole || ''
    // 总部运营：不传 storeId，后端返回全部猫咪
    // 其他角色：从 userInfo 取 storeId，fallback 到 options.storeId 或 1
    let storeId = null
    if (role !== 'hq_ops') {
      storeId = userInfo.storeId || options.storeId || 1
    }
    this.setData({ storeId, role, isCatKeeper: ['cat_keeper', 'manager', 'hq_ops'].includes(role) })
    this.loadCats()
  },

  onPullDownRefresh() {
    this.loadCats()
  },

  loadCats() {
    this.setData({ loading: true })
    const url = this.data.storeId != null
      ? `/api/cats?storeId=${this.data.storeId}`
      : '/api/cats'
    get(url).then(res => {
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
        this.setData({ catDetail: res.data, detailLoading: false }, () => {
          setTimeout(() => this.drawWeightChart(res.data.weightHistory), 500)
        })
      }
    }).catch(() => {
      this.setData({ detailLoading: false })
    })
  },

  // 返回列表
  goBackList() {
    this.setData({ detailMode: false, catDetail: null })
  },

  // ══════════ 健康打卡相关 ══════════

  /** 打开健康打卡弹窗 */
  openHealthForm() {
    const today = new Date()
    const yyyy = today.getFullYear()
    const mm = String(today.getMonth() + 1).padStart(2, '0')
    const dd = String(today.getDate()).padStart(2, '0')
    this.setData({
      showHealthForm: true,
      healthFormType: 'WEIGHT',
      typeIndex: 0,
      healthFormValue: '',
      healthFormNote: '',
      healthFormMood: 'happy',
      healthFormNextDue: '',
      healthFormVaccineDate: `${yyyy}-${mm}-${dd}`
    })
  },

  /** 关闭弹窗 */
  closeHealthForm() {
    this.setData({ showHealthForm: false })
  },

  /** 切换记录类型 */
  onTypeChange(e) {
    const idx = e.detail.value
    this.setData({
      typeIndex: idx,
      healthFormType: this.data.typeOptions[idx].value
    })
  },

  /** 输入记录值 */
  onValueInput(e) {
    this.setData({ healthFormValue: e.detail.value })
  },

  /** 输入备注 */
  onNoteInput(e) {
    this.setData({ healthFormNote: e.detail.value })
  },

  /** 切换心情 */
  onMoodChange(e) {
    this.setData({ healthFormMood: e.currentTarget.dataset.mood })
  },

  /** 疫苗接种日期选择 */
  onVaccineDatePick(e) {
    this.setData({ healthFormVaccineDate: e.detail.value })
  },

  /** 疫苗下次接种日期选择 */
  onDatePick(e) {
    this.setData({ healthFormNextDue: e.detail.value })
  },

  /** 提交健康打卡 */
  submitHealthRecord() {
    const { healthFormType, healthFormValue, healthFormNote, healthFormMood, healthFormNextDue } = this.data
    const catDetail = this.data.catDetail
    if (!catDetail) return

    if (!healthFormValue.trim()) {
      wx.showToast({ title: '请填写记录值', icon: 'none' })
      return
    }

    // 组装 note
    let note = healthFormNote || ''
    // 互动：note 里存 mood
    if (healthFormType === 'INTERACTION') {
      note = `mood=${healthFormMood}${note ? '|' + note : ''}`
    }
    // 疫苗：note 里存 nextDue（如果有）
    if (healthFormType === 'VACCINE' && healthFormNextDue) {
      note = `${note ? note + ' ' : ''}nextDue=${healthFormNextDue}`
    }

    // 体重自动加 kg 后缀
    let value = healthFormValue.trim()
    if (healthFormType === 'WEIGHT' && !/kg$/i.test(value)) {
      value = value + 'kg'
    }

    // 组装请求体
    let payload = {
      catId: catDetail.id,
      recordType: healthFormType,
      recordValue: value,
      note: note || null,
      staffId: app.globalData.userInfo?.id || 2003
    }
    // 记录日期：疫苗用用户选择的日期，体重/互动默认今天
    if (healthFormType === 'VACCINE' && this.data.healthFormVaccineDate) {
      payload.recordDate = this.data.healthFormVaccineDate
    } else {
      const now = new Date()
      const yyyy = now.getFullYear()
      const mm = String(now.getMonth() + 1).padStart(2, '0')
      const dd = String(now.getDate()).padStart(2, '0')
      payload.recordDate = `${yyyy}-${mm}-${dd}`
    }

    this.setData({ healthSubmitting: true })
    post('/api/staff/cat/health', payload).then(res => {
      this.setData({ healthSubmitting: false })
      if (res.code === 0 && res.data.success) {
        wx.showToast({ title: '打卡成功', icon: 'success' })
        this.setData({ showHealthForm: false })
        // 刷新详情
        this.refreshDetail()
      } else {
        wx.showToast({ title: res.data?.message || '打卡失败', icon: 'none' })
      }
    }).catch(() => {
      this.setData({ healthSubmitting: false })
      wx.showToast({ title: '网络错误', icon: 'none' })
    })
  },

  /** 刷新猫咪详情 */
  refreshDetail() {
    const catId = this.data.catDetail.id
    this.setData({ detailLoading: true })
    get(`/api/cats/detail?catId=${catId}`).then(res => {
      if (res.code === 0) {
        // 先更新数据，等 canvas 节点挂载后再画图
        this.setData({ catDetail: res.data, detailLoading: false }, () => {
          // setData 回调里 DOM 已更新，canvas wx:if 条件已重新生效
          setTimeout(() => this.drawWeightChart(res.data.weightHistory), 500)
        })
      }
    }).catch(() => {
      this.setData({ detailLoading: false })
    })
  },

  // Canvas 2D 绘制体重趋势图
  drawWeightChart(weightHistory) {
    if (!weightHistory || !weightHistory.labels || weightHistory.labels.length < 2) return
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
