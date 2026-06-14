// pages/hq/marketing.js
// 总部运营 - 运营管理页：活动管理 + 发券管理
const { get, post, put, del, patch } = require('../../utils/request')

Page({
  data: {
    // Tab
    activeTab: 'promotions',

    // 活动管理
    filter: 'all',          // all | active | inactive
    promotions: [],
    loading: false,

    // 发券管理 - 单人
    activePromos: [],       // 进行中的活动列表（用于选择器）
    activePromoLabels: [],
    singlePromoId: null,
    singlePromoLabel: '',
    singleUserId: '',
    singleExpireStr: '',
    singleExpireDate: null,
    singleResult: '',

    // 发券管理 - 批量
    batchPromoId: null,
    batchPromoLabel: '',
    batchTargetType: '',
    batchTargetLabel: '',
    batchRoleValue: '',
    batchRoleLabel: '',
    batchMemberLevel: '',
    batchMemberLevelLabel: '',
    batchExpireStr: '',
    batchExpireDate: null,
    batchResult: '',

    targetTypes: ['全部用户', '按角色', '按会员等级'],
    targetTypeValues: ['all', 'role', 'member_level'],
    roleLabels: ['顾客', '店员', '店长', '总部运营', '猫咪管家'],
    roleValues: [1, 2, 3, 4, 5],
    memberLevels: ['普通会员', '银卡会员', '金卡会员', '钻石会员'],
    memberLevelValues: ['普通会员', '银卡会员', '金卡会员', '钻石会员']
  },

  onShow() {
    this.loadPromotions()
    this.loadActivePromos()
  },

  // ═══════════════════════════════════════════════════════════
  // Tab 切换
  // ═══════════════════════════════════════════════════════════
  switchTab(e) {
    const tab = e.currentTarget.dataset.tab
    this.setData({ activeTab: tab })
    if (tab === 'coupons') {
      this.loadActivePromos()
    }
  },

  // ═══════════════════════════════════════════════════════════
  // 活动管理 - 加载列表
  // ═══════════════════════════════════════════════════════════
  loadPromotions() {
    this.setData({ loading: true })
    const params = { page: 1, pageSize: 50 }
    if (this.data.filter === 'active') params.isActive = true
    if (this.data.filter === 'inactive') params.isActive = false

    get('/api/hq/promotions', params)
      .then(res => {
        if (res.code === 0) {
          const list = (res.data && res.data.list) ? res.data.list : []
          // 格式化日期
          const formatted = list.map(p => ({
            ...p,
            startTimeStr: this.fmtDate(p.startTime),
            endTimeStr: this.fmtDate(p.endTime)
          }))
          this.setData({ promotions: formatted, loading: false })
        } else {
          wx.showToast({ title: res.message || '加载失败', icon: 'none' })
          this.setData({ loading: false })
        }
      })
      .catch(() => {
        wx.showToast({ title: '网络异常', icon: 'none' })
        this.setData({ loading: false })
      })
  },

  // 筛选
  setFilter(e) {
    const filter = e.currentTarget.dataset.filter
    this.setData({ filter }, () => this.loadPromotions())
  },

  // 加载进行中的活动（供发券选择）
  loadActivePromos() {
    get('/api/hq/promotions', { isActive: true, page: 1, pageSize: 100 })
      .then(res => {
        if (res.code === 0) {
          const list = (res.data && res.data.list) ? res.data.list : []
          const labels = list.map(p => p.name)
          this.setData({
            activePromos: list,
            activePromoLabels: labels
          })
        }
      })
  },

  // ═══════════════════════════════════════════════════════════
  // 活动管理 - 新建 / 编辑（跳转独立页面）
  // ═══════════════════════════════════════════════════════════
  showCreateModal() {
    wx.navigateTo({ url: '/pages/hq/promo-form/promo-form?mode=create' })
  },

  showEditModal(e) {
    const item = e.currentTarget.dataset.item
    wx.setStorageSync('__promo_edit_data__', item)
    wx.navigateTo({ url: '/pages/hq/promo-form/promo-form?mode=edit' })
  },

  // ═══════════════════════════════════════════════════════════
  // 启停切换
  togglePromo(e) {
    const promoId = e.currentTarget.dataset.id
    const currentActive = e.currentTarget.dataset.active
    const action = currentActive ? '停用' : '启用'

    wx.showModal({
      title: `确认${action}`,
      content: `确定要${action}该活动吗？`,
      success: (res) => {
        if (!res.confirm) return
        this.setData({ loading: true })
        patch(`/api/hq/promotions/${promoId}/toggle`)
          .then(res => {
            this.setData({ loading: false })
            if (res.code === 0) {
              wx.showToast({ title: `${action}成功`, icon: 'success' })
              this.loadPromotions()
              this.loadActivePromos()
            } else {
              wx.showToast({ title: res.message || '操作失败', icon: 'none' })
            }
          })
          .catch(() => {
            this.setData({ loading: false })
            wx.showToast({ title: '网络异常', icon: 'none' })
          })
      }
    })
  },

  // 删除活动
  deletePromo(e) {
    const promoId = e.currentTarget.dataset.id
    const name = e.currentTarget.dataset.name || '该活动'

    wx.showModal({
      title: '确认删除',
      content: `确定要删除"${name}"吗？若已有用户领取，将转为停用而非物理删除。`,
      success: (res) => {
        if (!res.confirm) return
        this.setData({ loading: true })
        del(`/api/hq/promotions/${promoId}`)
          .then(res => {
            this.setData({ loading: false })
            if (res.code === 0) {
              wx.showToast({ title: '删除成功', icon: 'success' })
              this.loadPromotions()
              this.loadActivePromos()
            } else {
              wx.showToast({ title: res.message || '删除失败', icon: 'none' })
            }
          })
          .catch(() => {
            this.setData({ loading: false })
            wx.showToast({ title: '网络异常', icon: 'none' })
          })
      }
    })
  },

  // ═══════════════════════════════════════════════════════════
  // 发券管理 - 单人发券
  // ═══════════════════════════════════════════════════════════
  onSinglePromoPick(e) {
    const idx = parseInt(e.detail.value)
    const promo = this.data.activePromos[idx]
    if (promo) {
      this.setData({
        singlePromoId: promo.promoId,
        singlePromoLabel: promo.name
      })
    }
  },

  onSingleUserIdInput(e) {
    this.setData({ singleUserId: e.detail.value })
  },

  onSingleExpirePick(e) {
    const val = e.detail.value
    this.setData({
      singleExpireStr: val,
      singleExpireDate: val
    })
  },

  sendSingleCoupon() {
    const { singlePromoId, singleUserId, singleExpireDate } = this.data
    const userId = parseInt(singleUserId)
    if (!userId) {
      wx.showToast({ title: '请输入有效的用户ID', icon: 'none' })
      return
    }

    const body = {
      promoId: singlePromoId,
      userId: userId
    }
    if (singleExpireDate) {
      body.expireTime = singleExpireDate + 'T23:59:59'
    }

    this.setData({ loading: true, singleResult: '' })
    post('/api/hq/coupons/send', body)
      .then(res => {
        this.setData({ loading: false })
        if (res.code === 0) {
          this.setData({ singleResult: '✅ 发券成功！' })
        } else {
          this.setData({ singleResult: `❌ 发券失败：${res.message || '未知错误'}` })
        }
      })
      .catch(() => {
        this.setData({ loading: false, singleResult: '❌ 网络异常，请重试' })
      })
  },

  // ═══════════════════════════════════════════════════════════
  // 发券管理 - 批量发券
  // ═══════════════════════════════════════════════════════════
  onBatchPromoPick(e) {
    const idx = parseInt(e.detail.value)
    const promo = this.data.activePromos[idx]
    if (promo) {
      this.setData({
        batchPromoId: promo.promoId,
        batchPromoLabel: promo.name
      })
    }
  },

  onTargetTypePick(e) {
    const idx = parseInt(e.detail.value)
    const label = this.data.targetTypes[idx]
    const value = this.data.targetTypeValues[idx]
    this.setData({
      batchTargetType: value,
      batchTargetLabel: label,
      batchRoleValue: '',
      batchRoleLabel: '',
      batchMemberLevel: '',
      batchMemberLevelLabel: ''
    })
  },

  onRolePick(e) {
    const idx = parseInt(e.detail.value)
    this.setData({
      batchRoleValue: this.data.roleValues[idx],
      batchRoleLabel: this.data.roleLabels[idx]
    })
  },

  onMemberLevelPick(e) {
    const idx = parseInt(e.detail.value)
    this.setData({
      batchMemberLevel: this.data.memberLevelValues[idx],
      batchMemberLevelLabel: this.data.memberLevels[idx]
    })
  },

  onBatchExpirePick(e) {
    const val = e.detail.value
    this.setData({
      batchExpireStr: val,
      batchExpireDate: val
    })
  },

  sendBatchCoupon() {
    const { batchPromoId, batchTargetType, batchRoleValue, batchMemberLevel, batchExpireDate } = this.data

    // 构建 targetValue
    let targetValue = null
    if (batchTargetType === 'role') {
      if (!batchRoleValue) {
        wx.showToast({ title: '请选择角色', icon: 'none' })
        return
      }
      targetValue = String(batchRoleValue)
    } else if (batchTargetType === 'member_level') {
      if (!batchMemberLevel) {
        wx.showToast({ title: '请选择会员等级', icon: 'none' })
        return
      }
      targetValue = batchMemberLevel
    }

    const body = {
      promoId: batchPromoId,
      targetType: batchTargetType,
      targetValue: targetValue,
      userIds: null
    }
    if (batchExpireDate) {
      body.expireTime = batchExpireDate + 'T23:59:59'
    }

    this.setData({ loading: true, batchResult: '' })
    post('/api/hq/coupons/batch-send', body)
      .then(res => {
        this.setData({ loading: false })
        if (res.code === 0) {
          const data = res.data || {}
          const successCount = data.successCount != null ? data.successCount : '?'
          const failCount = data.failCount != null ? data.failCount : '?'
          this.setData({
            batchResult: `✅ 批量发券完成\n<span class="success">成功：${successCount}</span>  <span class="fail">失败：${failCount}</span>`
          })
        } else {
          this.setData({ batchResult: `❌ 发券失败：${res.message || '未知错误'}` })
        }
      })
      .catch(() => {
        this.setData({ loading: false, batchResult: '❌ 网络异常，请重试' })
      })
  },

  // ═══════════════════════════════════════════════════════════
  // 工具函数
  // ═══════════════════════════════════════════════════════════
  fmtDate(dateStr) {
    if (!dateStr) return ''
    // 时间戳、ISO 字符串、"yyyy-MM-dd" 格式均适配
    if (typeof dateStr === 'number') {
      const d = new Date(dateStr)
      return this.padDate(d)
    }
    // 取前 10 位作为日期
    const s = String(dateStr)
    if (s.length >= 10) return s.substring(0, 10)
    return s
  },

  padDate(d) {
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    return `${y}-${m}-${day}`
  }
})
