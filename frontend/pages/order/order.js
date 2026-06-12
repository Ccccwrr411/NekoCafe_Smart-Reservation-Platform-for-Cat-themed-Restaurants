// pages/order/order.js
const { post, get, isUseMock } = require('../../utils/request')
const { calcCartTotal } = require('../../utils/util')
const { requestWxPayment, getWxLoginCode } = require('../../utils/payment')

Page({
  data: {
    storeId: null,
    cartItems: [],
    cartTotal: 0,
    coupons: [],
    promotions: [],
    stackingRules: null,
    selectedCouponIds: [],
    totalDiscount: 0,
    finalTotal: 0,
    discountBreakdown: [],
    reserveInfo: null,
    remark: '',
    submitting: false,
    showCouponPicker: false
  },

  onLoad(options) {
    const app = getApp()
    const cartItems = app.globalData.cartItems || []
    const selectedTable = app.globalData.selectedTable
    const currentStore = app.globalData.currentStore

    const cartTotal = calcCartTotal(cartItems)
    const discountResult = this.calcDiscount([], cartTotal)

    this.setData({
      storeId: options.storeId || 1,
      cartItems,
      cartTotal,
      finalTotal: discountResult.finalTotal,
      totalDiscount: discountResult.totalDiscount,
      discountBreakdown: discountResult.discountBreakdown,
      reserveInfo: selectedTable ? {
        table: selectedTable,
        store: currentStore
      } : null
    })

    this.loadCoupons(cartTotal)
    this.loadPromotionRules()
  },

  // 为优惠券列表附加选中态 UI 字段（避免 WXML indexOf 兼容问题）
  enrichCouponsWithSelection(coupons, selectedIds) {
    const selected = (selectedIds || []).map(String)
    const amount = this.data.cartTotal
    return (coupons || []).map(coupon => {
      const isSelected = selected.includes(coupon.id)
      const meetsMinAmount = amount >= (coupon.minAmount || 0)
      const isDisabled = !meetsMinAmount
      return {
        ...coupon,
        isSelected,
        isDisabled,
        itemClass: isSelected ? 'coupon-checked' : (isDisabled ? 'coupon-disabled' : ''),
        checkboxClass: isSelected ? 'checkbox-on' : '',
        checkboxText: isSelected ? '✓' : '',
        unavailableHint: isDisabled ? `未满¥${coupon.minAmount}，不可用` : ''
      }
    })
  },

  loadCoupons(amount) {
    get(`/api/coupons/available?storeId=${this.data.storeId}&amount=${amount}`).then(res => {
      if (res.code === 0) {
        const rawCoupons = (res.data || []).map(coupon => ({
          ...coupon,
          id: String(coupon.id),
          ruleText: this.buildCouponRuleText(coupon)
        }))
        const coupons = this.enrichCouponsWithSelection(rawCoupons, this.data.selectedCouponIds)
        this.setData({ coupons })
      }
    }).catch(err => {
      console.error('[loadCoupons] 加载优惠券失败:', err)
      wx.showToast({ title: '优惠券加载失败', icon: 'none' })
    })
  },

  buildCouponRuleText(coupon) {
    const min = coupon.minAmount
    const type = coupon.type
    if (type === 'discount') {
      const zhe = (coupon.value * 10).toFixed(0)
      const max = coupon.maxDiscount
      return '满¥' + min + ' 享' + zhe + '折，最高减¥' + max
    }
    if (type === 'cashback') {
      return '满¥' + min + ' 减¥' + coupon.value
    }
    if (type === 'freebie') {
      return '赠价值¥' + coupon.value + '商品'
    }
    return ''
  },

  loadPromotionRules() {
    get('/api/promotions/rules').then(res => {
      if (res.code === 0) {
        const stackingRules = res.data.stackingRules
        if (stackingRules && stackingRules.rules) {
          stackingRules.rules = stackingRules.rules.map((text, i) => ({ idx: i, text }))
        }
        this.setData({
          promotions: res.data.activePromotions || [],
          stackingRules: stackingRules || null
        })
      }
    }).catch(err => {
      console.error('[loadPromotionRules] 加载促销规则失败:', err)
    })
  },

  onRemarkInput(e) {
    this.setData({ remark: e.detail.value })
  },

  onOpenCouponPicker() {
    this.setData({ showCouponPicker: true })
  },

  onCloseCouponPicker() {
    this.setData({ showCouponPicker: false })
  },

  onPickerPanelTap() {
    // 阻止点击穿透到遮罩层
  },

  onToggleCoupon(e) {
    const rawId = e.currentTarget.dataset.couponId
    if (!rawId) {
      console.warn('[onToggleCoupon] dataset.couponId 为空')
      return
    }
    const couponId = String(rawId)
    let selected = this.data.selectedCouponIds.map(String)
    const maxStack = this.data.stackingRules ? this.data.stackingRules.maxStackCount : 1

    const coupon = this.data.coupons.find(c => c.id === couponId)
    if (!coupon) {
      console.warn('[onToggleCoupon] 未找到优惠券:', couponId)
      return
    }

    if (coupon.isDisabled) {
      wx.showToast({ title: `未满¥${coupon.minAmount}不可用`, icon: 'none' })
      return
    }

    if (coupon.type === 'freebie' && selected.length > 0) {
      wx.showToast({ title: '赠品券不可与其他优惠叠加', icon: 'none' })
      return
    }

    if (selected.includes(couponId)) {
      selected = selected.filter(id => id !== couponId)
    } else {
      if (selected.length >= maxStack) {
        wx.showToast({ title: '最多叠加' + maxStack + '张优惠券', icon: 'none' })
        return
      }
      if (!coupon.stackable && selected.length > 0) {
        wx.showToast({ title: '该优惠券不可与其他优惠叠加', icon: 'none' })
        return
      }
      if (selected.length > 0) {
        const hasNonStackable = selected.some(id => {
          const c = this.data.coupons.find(co => co.id === id)
          return c && !c.stackable
        })
        if (hasNonStackable) {
          wx.showToast({ title: '已有不可叠加的优惠券', icon: 'none' })
          return
        }
      }
      selected.push(couponId)
    }

    const discountResult = this.calcDiscount(selected)
    const coupons = this.enrichCouponsWithSelection(this.data.coupons, selected)
    this.setData({
      selectedCouponIds: selected,
      coupons,
      ...discountResult
    })
  },

  formatBreakdownAmount(amount) {
    if (amount < 0) return '-¥' + (-amount)
    if (amount === 0) return '--'
    return '¥' + amount
  },

  buildBreakdownItem(label, amount, type) {
    const isSave = type === 'discount' || type === 'cashback' || type === 'platform' || type === 'freebie'
    return {
      label,
      amount,
      type,
      amountText: this.formatBreakdownAmount(amount),
      rowClass: type === 'skipped' ? 'price-skipped' : '',
      labelClass: isSave ? 'price-save' : '',
      amountClass: isSave ? 'price-save discount-val' : ''
    }
  },

  calcDiscount(selectedCouponIds, cartTotalOverride) {
    const cartTotal = cartTotalOverride != null ? cartTotalOverride : this.data.cartTotal
    const { coupons, promotions } = this.data
    let totalDiscount = 0
    const breakdown = [this.buildBreakdownItem('商品原价', cartTotal)]

    selectedCouponIds.forEach(id => {
      const coupon = coupons.find(c => c.id === id)
      if (!coupon) return
      if (coupon.type === 'discount') {
        if (cartTotal < (coupon.minAmount || 0)) return
        let saving = Math.round(cartTotal * (1 - coupon.value))
        if (coupon.maxDiscount && saving > coupon.maxDiscount) saving = coupon.maxDiscount
        totalDiscount += saving
        breakdown.push(this.buildBreakdownItem(coupon.name, -saving, 'discount'))
      }
    })

    const applicablePromo = promotions
      .filter(p => cartTotal >= p.minAmount)
      .sort((a, b) => b.value - a.value)

    selectedCouponIds.forEach(id => {
      const coupon = coupons.find(c => c.id === id)
      if (!coupon || coupon.type !== 'cashback') return
      if (cartTotal >= coupon.minAmount) {
        totalDiscount += coupon.value
        breakdown.push(this.buildBreakdownItem(coupon.name, -coupon.value, 'cashback'))
      }
    })

    const hasNonStackableCoupon = selectedCouponIds.some(id => {
      const c = coupons.find(co => co.id === id)
      return c && !c.stackable
    })

    if (!hasNonStackableCoupon && applicablePromo.length > 0) {
      const bestPromo = applicablePromo[0]
      const hasCashback = selectedCouponIds.some(id => {
        const c = coupons.find(co => co.id === id)
        return c && c.type === 'cashback'
      })
      if (!hasCashback) {
        totalDiscount += bestPromo.value
        breakdown.push(this.buildBreakdownItem(bestPromo.name, -bestPromo.value, 'platform'))
      } else {
        breakdown.push(this.buildBreakdownItem(
          '平台' + bestPromo.name + '（与满减券冲突，已跳过）', 0, 'skipped'
        ))
      }
    } else if (applicablePromo.length > 0) {
      breakdown.push(this.buildBreakdownItem(
        '平台满减（已有不可叠加优惠，已跳过）', 0, 'skipped'
      ))
    }

    selectedCouponIds.forEach(id => {
      const coupon = coupons.find(c => c.id === id)
      if (!coupon || coupon.type !== 'freebie') return
      totalDiscount += coupon.value
      breakdown.push(this.buildBreakdownItem(coupon.name, -coupon.value, 'freebie'))
    })

    const finalTotal = Math.max(0, cartTotal - totalDiscount)
    return { totalDiscount, finalTotal, discountBreakdown: breakdown }
  },

  buildSubmitPayload(wxCode) {
    const app = getApp()
    const userInfo = app.globalData.userInfo || wx.getStorageSync('userInfo') || {}
    const payload = {
      storeId: Number(this.data.storeId),
      userId: userInfo.id,
      items: this.data.cartItems.map(item => ({
        menuId: item.id,
        name: item.name,
        price: item.price,
        qty: item.qty
      })),
      totalAmount: this.data.cartTotal,
      finalAmount: this.data.finalTotal,
      discount: this.data.totalDiscount,
      couponIds: this.data.selectedCouponIds,
      remark: this.data.remark
    }
    if (this.data.reserveInfo && this.data.reserveInfo.table) {
      payload.tableId = this.data.reserveInfo.table.id
    }
    if (wxCode) payload.code = wxCode
    return payload
  },

  onPaymentSuccess() {
    this.setData({ submitting: false })
    getApp().globalData.cartItems = []
    wx.showToast({ title: '支付成功！', icon: 'success' })
    setTimeout(() => {
      wx.reLaunch({ url: '/pages/profile/profile' })
    }, 1000)
  },

  onPaymentFail(err) {
    this.setData({ submitting: false })
    console.error('[onSubmit] 支付失败:', err)
    wx.showToast({ title: '支付失败，请重试', icon: 'none' })
  },

  onPaymentCancel() {
    this.setData({ submitting: false })
    wx.showToast({ title: '已取消支付', icon: 'none' })
  },

  onSubmit() {
    if (this.data.cartItems.length === 0) {
      wx.showToast({ title: '购物车是空的', icon: 'none' })
      return
    }
    this.setData({ submitting: true })

    const doSubmit = (wxCode) => {
      const payload = this.buildSubmitPayload(wxCode)
      post('/api/order/submit', payload).then(res => {
        if (res.code !== 0 || !res.data) {
          this.setData({ submitting: false })
          wx.showToast({ title: res.message || '下单失败，请重试', icon: 'none' })
          return
        }

        const { payInfo, finalAmount } = res.data

        // 0 元订单无需调起支付
        if (!payInfo || finalAmount === 0 || this.data.finalTotal === 0) {
          this.onPaymentSuccess()
          return
        }

        requestWxPayment(payInfo, {
          onSuccess: () => this.onPaymentSuccess(),
          onFail: (err) => this.onPaymentFail(err),
          onCancel: () => this.onPaymentCancel()
        })
      }).catch((err) => {
        this.setData({ submitting: false })
        console.error('[onSubmit] 下单请求失败:', err)
        const msg = err && err.message ? err.message : '下单失败，请重试'
        wx.showToast({ title: msg, icon: 'none' })
      })
    }

    // 对接真实后端时附带 wx.login code，供后端换取 openid 发起沙箱支付
    if (!isUseMock()) {
      getWxLoginCode()
        .then(code => doSubmit(code))
        .catch((err) => {
          console.warn('[onSubmit] wx.login 失败，尝试不带 code 提交:', err)
          doSubmit(null)
        })
    } else {
      doSubmit(null)
    }
  }
})
