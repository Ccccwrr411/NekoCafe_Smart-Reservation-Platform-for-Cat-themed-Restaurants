// pages/hq/promo-form/promo-form.js
// 活动新建/编辑页面 — 替代弹窗模式
const { get, post, put } = require('../../../utils/request')

Page({
  data: {
    mode: 'create',       // create | edit
    promoId: null,
    loading: false,

    typeLabels: ['折扣 DISCOUNT', '代金券 VOUCHER'],
    typeValues: ['DISCOUNT', 'VOUCHER'],
    formTypeLabel: '',
    form: {
      name: '',
      type: '',
      ruleJson: { discount: '', max_discount: '', min_spend: '', reduction: '', stackable: false },
      startTime: null,
      startTimeStr: '',
      endTime: null,
      endTimeStr: '',
      isActive: true
    }
  },

  onLoad(options) {
    const mode = options.mode || 'create'
    this.setData({ mode })

    if (mode === 'edit') {
      // 从 storage 读取编辑数据
      const editData = wx.getStorageSync('__promo_edit_data__')
      if (editData) {
        wx.removeStorageSync('__promo_edit_data__')
        const item = editData
        const rule = item.ruleJson || {}
        const typeIdx = this.data.typeValues.indexOf(item.type)
        this.setData({
          promoId: item.promoId,
          formTypeLabel: typeIdx >= 0 ? this.data.typeLabels[typeIdx] : '',
          form: {
            name: item.name || '',
            type: item.type || '',
            ruleJson: {
              discount: rule.discount != null ? String(rule.discount) : '',
              max_discount: rule.max_discount != null ? String(rule.max_discount) : '',
              min_spend: rule.min_spend != null ? String(rule.min_spend) : '',
              reduction: rule.reduction != null ? String(rule.reduction) : '',
              stackable: !!rule.stackable
            },
            startTime: item.startTime || null,
            startTimeStr: this.fmtDate(item.startTime),
            endTime: item.endTime || null,
            endTimeStr: this.fmtDate(item.endTime),
            isActive: !!item.isActive
          }
        })
      }
    }
  },

  // ═══════════════════════════════════════════════════════════
  // 表单交互
  // ═══════════════════════════════════════════════════════════
  onNameInput(e) {
    const val = e.detail.value
    var now = Date.now()
    if (this._lastInput && now - this._lastInput < 200) {
      if (this._inputTimer) clearTimeout(this._inputTimer)
      this._inputTimer = setTimeout(() => { this.setData({ 'form.name': val }); this._lastInput = Date.now() }, 200)
      return
    }
    this._lastInput = now
    this.setData({ 'form.name': val })
  },

  onTypePick(e) {
    const idx = parseInt(e.detail.value)
    const label = this.data.typeLabels[idx]
    const value = this.data.typeValues[idx]
    this.setData({
      'form.type': value,
      'form.ruleJson': { discount: '', max_discount: '', min_spend: '', reduction: '', stackable: false },
      formTypeLabel: label
    })
  },

  onRuleInput(e) {
    const field = e.currentTarget.dataset.field
    const val = e.detail.value
    var key = 'form.ruleJson.' + field
    var now = Date.now()
    if (this._lastRuleInput && now - this._lastRuleInput < 200) {
      if (this._ruleTimer) clearTimeout(this._ruleTimer)
      this._ruleTimer = setTimeout(() => { this.setData({ [key]: val }); this._lastRuleInput = Date.now() }, 200)
      return
    }
    this._lastRuleInput = now
    this.setData({ [key]: val })
  },

  onRuleSwitch(e) {
    const field = e.currentTarget.dataset.field
    this.setData({ ['form.ruleJson.' + field]: e.detail.value })
  },

  onActiveSwitch(e) {
    this.setData({ 'form.isActive': e.detail.value })
  },

  onDatePick(e) {
    const field = e.currentTarget.dataset.field
    const val = e.detail.value
    this.setData({
      ['form.' + field]: val,
      ['form.' + field + 'Str']: val
    })
  },

  // ═══════════════════════════════════════════════════════════
  // 返回 & 提交
  // ═══════════════════════════════════════════════════════════
  goBack() {
    wx.navigateBack()
  },

  submitPromo() {
    const { form, mode, promoId } = this.data

    // 校验
    if (!form.name.trim()) { wx.showToast({ title: '请输入活动名称', icon: 'none' }); return }
    if (!form.type) { wx.showToast({ title: '请选择活动类型', icon: 'none' }); return }
    if (!form.startTime) { wx.showToast({ title: '请选择开始时间', icon: 'none' }); return }
    if (!form.endTime) { wx.showToast({ title: '请选择结束时间', icon: 'none' }); return }

    // 构建 ruleJson
    let ruleJson = {}
    if (form.type === 'DISCOUNT') {
      const discount = parseFloat(form.ruleJson.discount)
      if (isNaN(discount) || discount <= 0 || discount > 1) {
        wx.showToast({ title: '折扣需在 0~1 之间', icon: 'none' }); return
      }
      ruleJson = {
        discount,
        max_discount: parseFloat(form.ruleJson.max_discount) || 0,
        min_spend: parseFloat(form.ruleJson.min_spend) || 0,
        stackable: !!form.ruleJson.stackable
      }
    } else {
      const reduction = parseFloat(form.ruleJson.reduction)
      if (isNaN(reduction) || reduction <= 0) {
        wx.showToast({ title: '减免金额需大于0', icon: 'none' }); return
      }
      ruleJson = {
        reduction,
        min_spend: parseFloat(form.ruleJson.min_spend) || 0,
        stackable: !!form.ruleJson.stackable
      }
    }

    const body = {
      name: form.name.trim(),
      type: form.type,
      ruleJson,
      startTime: form.startTime + 'T00:00:00',
      endTime: form.endTime + 'T23:59:59',
      applicableStores: null,
      isActive: !!form.isActive
    }

    this.setData({ loading: true })
    const apiCall = mode === 'create'
      ? post('/api/hq/promotions', body)
      : put(`/api/hq/promotions/${promoId}`, body)

    apiCall.then(res => {
      this.setData({ loading: false })
      if (res.code === 0) {
        wx.showToast({ title: mode === 'create' ? '创建成功' : '保存成功', icon: 'success' })
        setTimeout(() => {
          wx.navigateBack()
        }, 1000)
      } else {
        wx.showToast({ title: res.message || '操作失败', icon: 'none' })
      }
    }).catch(() => {
      this.setData({ loading: false })
      wx.showToast({ title: '网络异常', icon: 'none' })
    })
  },

  // ═══════════════════════════════════════════════════════════
  // 工具函数
  // ═══════════════════════════════════════════════════════════
  fmtDate(dateStr) {
    if (!dateStr) return ''
    if (typeof dateStr === 'number') {
      const d = new Date(dateStr)
      return this.padDate(d)
    }
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
