// pages/order/order.js
const { post, get } = require('../../utils/request')
const { calcCartTotal } = require('../../utils/util')

Page({
  data: {
    storeId: null,
    cartItems: [],
    cartTotal: 0,
    // 优惠
    coupons: [],
    promotions: [],
    stackingRules: null,
    selectedCouponIds: [],   // 允许叠加多张券
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
    this.setData({
      storeId: options.storeId || 1,
      cartItems,
      cartTotal,
      finalTotal: cartTotal,
      reserveInfo: selectedTable ? {
        table: selectedTable,
        store: currentStore
      } : null
    })

    // 加载可用优惠券和促销规则
    this.loadCoupons(cartTotal)
    this.loadPromotionRules()
  },

  // 加载可用优惠券
  loadCoupons(amount) {
    get(`/api/coupons/available?storeId=${this.data.storeId}&amount=${amount}`).then(res => {
      if (res.code === 0) {
        // 预计算每条优惠券的规则描述文本（WXML 不支持复杂表达式）
        const coupons = (res.data || []).map(coupon => ({
          ...coupon,
          id: String(coupon.id),          // 归一化为字符串，保证与 dataset/selectedCouponIds 类型一致
          ruleText: this.buildCouponRuleText(coupon)
        }))
        this.setData({ coupons })
      }
    }).catch(err => {
      console.error('[loadCoupons] 加载优惠券失败:', err)
    })
  },

  // 生成优惠券规则描述文本
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

  // 加载促销活动规则
  loadPromotionRules() {
    get('/api/promotions/rules').then(res => {
      if (res.code === 0) {
        const stackingRules = res.data.stackingRules
        // 为规则文本数组添加唯一索引，避免 WXML 中 wx:key="*this" 的兼容性问题
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

  // 打开优惠券选择器
  onOpenCouponPicker() {
    this.setData({ showCouponPicker: true })
  },

  // 关闭优惠券选择器
  onCloseCouponPicker() {
    this.setData({ showCouponPicker: false })
  },

  // 切换选中优惠券
  onToggleCoupon(e) {
    const rawId = e.currentTarget.dataset.couponId
    if (!rawId) {
      console.warn('[onToggleCoupon] dataset.couponId 为空，无法选中优惠券')
      return
    }
    const couponId = String(rawId)
    let selected = this.data.selectedCouponIds.map(String)
    const maxStack = this.data.stackingRules ? this.data.stackingRules.maxStackCount : 1

    // 从已加载的 coupons 中查找
    const coupon = this.data.coupons.find(c => c.id === couponId)
    if (!coupon) {
      console.warn('[onToggleCoupon] 未找到匹配的优惠券, couponId:', couponId, 'coupons:', this.data.coupons.map(c => c.id))
      return
    }

    // 赠品券不可叠加
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
      // 检查叠加规则
      if (!coupon.stackable && selected.length > 0) {
        wx.showToast({ title: '该优惠券不可与其他优惠叠加', icon: 'none' })
        return
      }
      // 检查是否选了不可叠加的券
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

    // 一次性 setData，避免两次异步 setData 的竞态问题
    const discountResult = this.calcDiscount(selected)
    this.setData({
      selectedCouponIds: selected,
      ...discountResult
    })
  },

  // 格式化明细行金额为展示文本（WXML 不支持字符串拼接）
  formatBreakdownAmount(amount) {
    if (amount < 0) return '-¥' + (-amount)
    if (amount === 0) return '--'
    return '¥' + amount
  },

  // 构建明细行（预计算展示文本和 CSS 类）
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

  // 计算优惠金额（selectedCouponIds 已归一化为字符串数组）
  // 返回 { totalDiscount, finalTotal, discountBreakdown }，不调用 setData
  calcDiscount(selectedCouponIds) {
    const { cartTotal, coupons, promotions } = this.data
    let totalDiscount = 0
    const breakdown = [this.buildBreakdownItem('商品原价', cartTotal)]

    // 先计算折扣券
    selectedCouponIds.forEach(id => {
      const coupon = coupons.find(c => c.id === id)
      if (!coupon) return
      if (coupon.type === 'discount') {
        let saving = Math.round(cartTotal * (1 - coupon.value))
        if (coupon.maxDiscount && saving > coupon.maxDiscount) saving = coupon.maxDiscount
        totalDiscount += saving
        breakdown.push(this.buildBreakdownItem(coupon.name, -saving, 'discount'))
      }
    })

    // 计算满减（自动匹配最优平台满减）
    const applicablePromo = promotions
      .filter(p => cartTotal >= p.minAmount)
      .sort((a, b) => b.value - a.value)

    // 满减券
    selectedCouponIds.forEach(id => {
      const coupon = coupons.find(c => c.id === id)
      if (!coupon || coupon.type !== 'cashback') return
      if (cartTotal >= coupon.minAmount) {
        totalDiscount += coupon.value
        breakdown.push(this.buildBreakdownItem(coupon.name, -coupon.value, 'cashback'))
      }
    })

    // 平台满减（如果未与不可叠加的券冲突）
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

    // 赠品券
    selectedCouponIds.forEach(id => {
      const coupon = coupons.find(c => c.id === id)
      if (!coupon || coupon.type !== 'freebie') return
      totalDiscount += coupon.value
      breakdown.push(this.buildBreakdownItem(coupon.name, -coupon.value, 'freebie'))
    })

    const finalTotal = Math.max(0, cartTotal - totalDiscount)
    return { totalDiscount, finalTotal, discountBreakdown: breakdown }
  },

  // 提交订单
  onSubmit() {
    if (this.data.cartItems.length === 0) {
      wx.showToast({ title: '购物车是空的', icon: 'none' }); return
    }
    this.setData({ submitting: true })
    post('/api/order/submit', {
      storeId: this.data.storeId,
      items: this.data.cartItems,
      totalAmount: this.data.cartTotal,
      finalAmount: this.data.finalTotal,
      discount: this.data.totalDiscount,
      couponIds: this.data.selectedCouponIds,
      remark: this.data.remark
    }).then(res => {
      if (res.code === 0 && res.data && res.data.payInfo) {
        wx.requestPayment({
          ...res.data.payInfo,
          success: () => {
            this.setData({ submitting: false })
            getApp().globalData.cartItems = []
            wx.showToast({ title: '支付成功！', icon: 'success' })
            setTimeout(() => {
              wx.reLaunch({ url: '/pages/profile/profile' })
            }, 1000)
          },
          fail: (err) => {
            this.setData({ submitting: false })
            if (err.errMsg && err.errMsg.includes('cancel')) {
              wx.showToast({ title: '已取消支付', icon: 'none' })
            } else {
              wx.showToast({ title: '支付失败，请重试', icon: 'none' })
            }
          }
        })
      } else {
        this.setData({ submitting: false })
        wx.showToast({ title: res.message || '下单失败', icon: 'none' })
      }
    }).catch(() => this.setData({ submitting: false }))
  }
})
