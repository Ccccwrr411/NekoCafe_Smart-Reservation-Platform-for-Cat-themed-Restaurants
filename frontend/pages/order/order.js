// pages/order/order.js
const { post, get, isUseMock } = require('../../utils/request')
const { calcCartTotal } = require('../../utils/util')
const { requestWxPayment, getWxLoginCode } = require('../../utils/payment')

Page({
  data: {
    storeId: null,
    tableId: null,
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
    showCouponPicker: false,
    // 多预约选择
    hasReservation: false,       // 是否有可用预约（后端查询或 globalData）
    reservationList: [],         // 所有 BOOKED 预约列表
    selectedReservationIdx: -1,  // 当前选中的预约索引
    showReservationPicker: false
  },

  onLoad(options) {
    const app = getApp()
    const cartItems = app.globalData.cartItems || []
    const selectedTable = app.globalData.selectedTable
    const currentStore = app.globalData.currentStore
    // 从 URL 参数或 globalData 获取 storeId 和 tableId
    const storeId = options.storeId || (currentStore ? currentStore.id : null) || 1
    const tableId = options.tableId || (selectedTable ? selectedTable.id : null)
    // "再来一单"场景传入的 orderId（新创建的预约）
    const reorderOrderId = options.orderId || ''

    const cartTotal = calcCartTotal(cartItems)
    const discountResult = this.calcDiscount([], cartTotal)

    // 先设置 localData（从 globalData 或 URL 参数），然后尝试从后端加载预约信息覆盖
    const localReserveInfo = selectedTable ? {
      table: selectedTable,
      store: currentStore
    } : (currentStore ? { store: currentStore } : null)

    // 如果是"再来一单"，把新预约的 orderId 注入 reserveInfo
    if (reorderOrderId && localReserveInfo) {
      localReserveInfo.orderId = reorderOrderId
    }

    this.setData({
      storeId: storeId,
      tableId: tableId,
      cartItems,
      cartTotal,
      finalTotal: discountResult.finalTotal,
      totalDiscount: discountResult.totalDiscount,
      discountBreakdown: discountResult.discountBreakdown,
      reserveInfo: localReserveInfo
    })

    // 主动从后端查询用户当前所有 BOOKED 预约
    this.loadCurrentReservations()

    this.loadCoupons(cartTotal)
    this.loadPromotionRules()
  },

  /**
   * 从后端加载用户当前所有 BOOKED 预约列表，支持多预约选择
   */
  loadCurrentReservations() {
    get('/api/reservation/current').then(res => {
      if (res.code === 0 && res.data && res.data.length > 0) {
        // 有预约 → 列表 + 自动选中第一个
        const list = res.data
        this.setData({ reservationList: list, hasReservation: true })
        this.selectReservation(0)
      } else {
        // 无预约 → 尝试 globalData（从预约页跳转来的）
        const app = getApp()
        const selectedTable = app.globalData.selectedTable
        const currentStore = app.globalData.currentStore
        if (selectedTable && currentStore) {
          // 刚完成预约 → 有可用预约信息
          this.setData({
            hasReservation: true,
            reservationList: [],
            selectedReservationIdx: -1,
            reserveInfo: {
              store: { id: currentStore.id, name: currentStore.name },
              table: selectedTable
            },
            storeId: currentStore.id,
            tableId: selectedTable.id
          })
        } else {
          // 完全没有预约 → 引导去预约
          this.setData({
            hasReservation: false,
            reservationList: [],
            selectedReservationIdx: -1,
            reserveInfo: null,
            storeId: null,
            tableId: null
          })
        }
      }
    }).catch(err => {
      console.warn('[loadCurrentReservations] 查询预约列表失败:', err)
    })
  },

  /**
   * 选中某个预约
   */
  selectReservation(idx) {
    const list = this.data.reservationList
    if (idx < 0 || idx >= list.length) return
    const r = list[idx]
    const app = getApp()
    // 更新 globalData
    app.globalData.selectedTable = {
      id: r.tableId,
      name: r.tableName,
      type: r.tableType,
      capacity: r.tableCapacity,
      catName: r.catName,
      catTheme: r.catTheme
    }
    app.globalData.currentStore = {
      id: r.storeId,
      name: r.storeName
    }
    // 更新页面数据
    this.setData({
      selectedReservationIdx: idx,
      showReservationPicker: false,
      storeId: r.storeId,
      tableId: r.tableId,
      reserveInfo: {
        store: { id: r.storeId, name: r.storeName },
        table: {
          id: r.tableId,
          name: r.tableName,
          type: r.tableType,
          capacity: r.tableCapacity,
          catName: r.catName,
          catTheme: r.catTheme
        },
        reserveDate: r.reserveDate,
        reserveTime: r.reserveTime,
        duration: r.duration,
        persons: r.persons,
        orderId: r.orderId
      }
    })
  },

  /** 打开预约选择器 */
  onOpenReservationPicker() {
    if (this.data.reservationList.length === 0) return
    this.setData({ showReservationPicker: true })
  },

  /** 跳转预约页 */
  goReserve() {
    wx.switchTab({ url: '/pages/reservation/reservation' })
  },

  /** 关闭预约选择器 */
  onCloseReservationPicker() {
    this.setData({ showReservationPicker: false })
  },

  /** 在预约选择器中选中 */
  onSelectReservation(e) {
    const idx = e.currentTarget.dataset.index
    this.selectReservation(idx)
  },

  // 为优惠券列表附加选中态 UI 字段（避免 WXML indexOf 兼容问题）
  enrichCouponsWithSelection(coupons, selectedIds) {
    const selected = (selectedIds || []).map(String)
    const amount = this.data.cartTotal
    return (coupons || []).map(coupon => {
      const isSelected = selected.includes(coupon.id)
      const meetsMinAmount = amount >= (coupon.minAmount || 0)
      const isDisabled = !meetsMinAmount
      let amountText = ''
      if (coupon.type === 'discount') {
        amountText = (coupon.value != null && coupon.value > 0) ? Math.round(coupon.value * 10) + '折' : '规则异常'
      } else if (coupon.type === 'cashback') {
        amountText = (coupon.value != null) ? '¥' + coupon.value : '¥异常'
      } else {
        amountText = (coupon.value != null) ? '价值¥' + coupon.value : '异常'
      }
      return {
        ...coupon,
        isSelected,
        isDisabled,
        itemClass: isSelected ? 'coupon-checked' : (isDisabled ? 'coupon-disabled' : ''),
        checkboxClass: isSelected ? 'checkbox-on' : '',
        checkboxText: isSelected ? '✓' : '',
        unavailableHint: isDisabled ? `未满¥${coupon.minAmount}，不可用` : '',
        amountText
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
    const val = coupon.value
    if (type === 'discount') {
      if (val == null || val <= 0) {
        return (min != null ? '满¥' + min + ' ' : '') + '折扣券（规则数据异常）'
      }
      const zhe = (val * 10).toFixed(0)
      const max = coupon.maxDiscount
      return (min != null ? '满¥' + min + ' ' : '') + '享' + zhe + '折' + (max != null ? '，最高减¥' + max : '')
    }
    if (type === 'cashback') {
      if (val == null) {
        return (min != null ? '满¥' + min + ' ' : '') + '满减券（规则数据异常）'
      }
      return (min != null ? '满¥' + min + ' ' : '') + '减¥' + val
    }
    if (type === 'freebie') {
      if (val == null) {
        return '赠品券（规则数据异常）'
      }
      return (min != null ? '满¥' + min + ' ' : '') + '赠价值¥' + val + '商品'
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

    if (selected.includes(couponId)) {
      // 取消选中
      selected = selected.filter(id => id !== couponId)
    } else {
      // 选中前的各种校验

      // 规则1：同类型优惠券不可叠加（折扣券、满减券各只能选1张）
      const hasSameType = selected.some(id => {
        const c = this.data.coupons.find(co => co.id === id)
        return c && c.type === coupon.type
      })
      if (hasSameType && (coupon.type === 'discount' || coupon.type === 'cashback')) {
        const typeName = coupon.type === 'discount' ? '折扣券' : '满减券'
        wx.showToast({ title: `同类型${typeName}不可叠加使用`, icon: 'none' })
        return
      }

      // 规则2：赠品券不可与其他优惠叠加
      if (coupon.type === 'freebie' && selected.length > 0) {
        wx.showToast({ title: '赠品券不可与其他优惠叠加', icon: 'none' })
        return
      }
      // 已有其他券时不能再选赠品券（上面已覆盖）；已有赠品券时不能再选其他券
      const hasFreebie = selected.some(id => {
        const c = this.data.coupons.find(co => co.id === id)
        return c && c.type === 'freebie'
      })
      if (hasFreebie && coupon.type !== 'freebie') {
        wx.showToast({ title: '赠品券不可与其他优惠叠加', icon: 'none' })
        return
      }

      // 规则3：不可叠加券只能单独使用
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

      // 规则4：不超过最大叠加数
      if (selected.length >= maxStack) {
        wx.showToast({ title: '最多叠加' + maxStack + '张优惠券', icon: 'none' })
        return
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

  /**
   * 计算优惠折扣（与后端 CouponServiceImpl.calculatePromotion 逻辑一致）
   *
   * 叠加规则（遵循 stackingRules）：
   *   1. 同类型优惠券不可叠加使用
   *   2. 折扣券(discount)优先于满减券(cashback)计算
   *   3. 折扣券最多选 1 张（不可叠加），满减券取优惠力度最大的一张
   *   4. 折扣券按 minAmount 门槛判断，对当前剩余金额打折（递减式）
   *   5. 满减券固定减额，不能超过剩余金额
   *   6. 平台促销(platform)仅在无不可叠加优惠券时参与，与满减券互斥
   */
  calcDiscount(selectedCouponIds, cartTotalOverride) {
    const cartTotal = cartTotalOverride != null ? cartTotalOverride : this.data.cartTotal
    const { coupons, promotions } = this.data
    let totalDiscount = 0
    let remainingAmount = cartTotal
    const breakdown = [this.buildBreakdownItem('商品原价', cartTotal)]

    // ── 1. 折扣券（最多 1 张，优先计算，对剩余金额打折）──
    let bestDiscountCoupon = null
    let bestDiscountSaving = 0
    selectedCouponIds.forEach(id => {
      const coupon = coupons.find(c => c.id === id)
      if (!coupon || coupon.type !== 'discount') return
      if (coupon.value == null || coupon.value <= 0) return
      if (remainingAmount < (coupon.minAmount || 0)) return
      // 折扣券按当前剩余金额计算节省金额
      let saving = Math.round(remainingAmount * (1 - coupon.value))
      if (coupon.maxDiscount && saving > coupon.maxDiscount) {
        saving = coupon.maxDiscount
      }
      if (saving > bestDiscountSaving) {
        bestDiscountSaving = saving
        bestDiscountCoupon = coupon
      }
    })
    if (bestDiscountCoupon) {
      totalDiscount += bestDiscountSaving
      remainingAmount -= bestDiscountSaving
      breakdown.push(this.buildBreakdownItem(bestDiscountCoupon.name, -bestDiscountSaving, 'discount'))
    }

    // ── 2. 满减券（取优惠力度最大的一张，不可与多张叠加）──
    let bestCashbackCoupon = null
    let bestCashbackSaving = 0
    selectedCouponIds.forEach(id => {
      const coupon = coupons.find(c => c.id === id)
      if (!coupon || coupon.type !== 'cashback') return
      if (coupon.value == null || coupon.value <= 0) return
      if (remainingAmount < (coupon.minAmount || 0)) return
      let saving = Math.min(coupon.value, remainingAmount)
      if (saving > bestCashbackSaving) {
        bestCashbackSaving = saving
        bestCashbackCoupon = coupon
      }
    })
    if (bestCashbackCoupon) {
      totalDiscount += bestCashbackSaving
      remainingAmount -= bestCashbackSaving
      breakdown.push(this.buildBreakdownItem(bestCashbackCoupon.name, -bestCashbackSaving, 'cashback'))
    }

    // ── 3. 平台促销活动（与满减券互斥，仅在无不可叠加券时参与）──
    const hasNonStackableCoupon = selectedCouponIds.some(id => {
      const c = coupons.find(co => co.id === id)
      return c && !c.stackable
    })
    const hasCashback = bestCashbackCoupon != null

    // 筛选满足门槛的平台促销，按优惠力度排序
    const applicablePromo = (promotions || [])
      .filter(p => {
        const minAmt = p.minAmount != null ? p.minAmount : 0
        return remainingAmount >= minAmt
      })
      .sort((a, b) => (b.value || 0) - (a.value || 0))

    if (!hasNonStackableCoupon && applicablePromo.length > 0) {
      const bestPromo = applicablePromo[0]
      if (!hasCashback) {
        const promoValue = bestPromo.value || 0
        totalDiscount += promoValue
        remainingAmount -= promoValue
        breakdown.push(this.buildBreakdownItem(bestPromo.name, -promoValue, 'platform'))
      } else {
        breakdown.push(this.buildBreakdownItem(
          '平台' + bestPromo.name + '（与满减券冲突，已跳过）', 0, 'skipped'
        ))
      }
    } else if (applicablePromo.length > 0 && hasNonStackableCoupon) {
      breakdown.push(this.buildBreakdownItem(
        '平台满减（已有不可叠加优惠，已跳过）', 0, 'skipped'
      ))
    }

    // ── 4. 赠品券（单独计算，价值叠加）──
    selectedCouponIds.forEach(id => {
      const coupon = coupons.find(c => c.id === id)
      if (!coupon || coupon.type !== 'freebie') return
      if (coupon.value == null || coupon.value <= 0) return
      totalDiscount += coupon.value
      remainingAmount -= coupon.value
      breakdown.push(this.buildBreakdownItem(coupon.name, -coupon.value, 'freebie'))
    })

    const finalTotal = Math.max(0, remainingAmount)
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
    // orderId：优先使用选中的预约 orderId（格式 ORD0000000001），后端直接用其定位预约记录
    const orderId = (this.data.reserveInfo && this.data.reserveInfo.orderId)
    if (orderId) {
      payload.orderId = orderId
    }
    // tableId 作为 fallback（当没有 orderId 时后端用 userId+tableId 匹配）
    const tableId = this.data.tableId
      || (this.data.reserveInfo && this.data.reserveInfo.table && this.data.reserveInfo.table.id)
    if (tableId) {
      payload.tableId = Number(tableId)
    }
    if (wxCode) payload.code = wxCode
    return payload
  },

  onPaymentSuccess(orderId) {
    this.setData({ submitting: false })
    getApp().globalData.cartItems = []
    wx.showToast({ title: '下单成功！', icon: 'success', duration: 1500 })
    // 跳转到订单详情页
    const targetId = orderId || (this.data.reserveInfo && this.data.reserveInfo.orderId)
    setTimeout(() => {
      if (targetId) {
        wx.redirectTo({ url: `/pages/orderDetail/orderDetail?orderId=${targetId}` })
      } else {
        wx.switchTab({ url: '/pages/orderList/orderList' })
      }
    }, 1500)
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
    if (!this.data.hasReservation || !this.data.reserveInfo) {
      wx.showToast({ title: '请先选择预约', icon: 'none' })
      return
    }
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

        const { payInfo, finalAmount, orderId } = res.data

        // 0 元订单无需调起支付
        if (!payInfo || finalAmount === 0 || this.data.finalTotal === 0) {
          this.onPaymentSuccess(orderId)
          return
        }

        requestWxPayment(payInfo, {
          onSuccess: () => this.onPaymentSuccess(orderId),
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
