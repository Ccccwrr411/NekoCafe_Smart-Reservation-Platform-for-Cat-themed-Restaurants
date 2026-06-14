// pages/menu/menu.js
const { get } = require('../../utils/request')
const { calcCartTotal, formatDistance } = require('../../utils/util')
const { getUserLocation, applyDistanceAndSort } = require('../../utils/lbs')

Page({
  data: {
    storeId: null,
    storeName: '',
    stores: [],
    showStorePicker: false,

    // 门店选择器地图
    pickerMapLat: 39.9042,
    pickerMapLng: 116.4074,
    pickerMarkers: [],
    activePickerStoreId: null,
    userLocation: null,

    categories: [],
    items: [],
    activeCategoryId: 1,
    cart: [],
    cartTotal: 0,
    cartCount: 0,
    loading: true,
    showCartDetail: false,

    // 预约状态
    reservationList: [],           // 用户所有 BOOKED 预约
    selectedReservationIdx: -1,     // 当前选中预约索引
    showReservationPicker: false,   // 预约选择弹层
    currentReserve: null            // 当前选中的预约信息（精简展示用）
  },

  onLoad(options) {
    // 检查登录态，未登录跳转到登录页
    if (!wx.getStorageSync('token')) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }

    // 优先 URL 参数（从预约页跳转）
    if (options.storeId) {
      this.setData({ storeId: options.storeId })
      const app = getApp()
      if (!app.globalData.currentStore || app.globalData.currentStore.id !== Number(options.storeId)) {
        app.globalData.currentStore = { id: Number(options.storeId), name: '' }
      }
    } else {
      const app = getApp()
      if (app.globalData.currentStore) {
        this.setData({
          storeId: app.globalData.currentStore.id,
          storeName: app.globalData.currentStore.name
        })
      }
    }
    this.loadStores()
  },

  onShow() {
    // 检查登录态，未登录跳转到登录页
    if (!wx.getStorageSync('token')) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }

    const app = getApp()
    if (app.globalData.currentStore && (!this.data.storeId || app.globalData.currentStore.id !== this.data.storeId)) {
      this.setData({
        storeId: app.globalData.currentStore.id,
        storeName: app.globalData.currentStore.name
      })
      this.loadMenu()
    }
    if (!this.data.storeId && this.data.stores.length > 0) {
      this.setData({ showStorePicker: true })
    }

    // 每次显示时刷新预约列表
    this.loadCurrentReservations()
  },

  /**
   * 加载用户当前所有 BOOKED 预约，支持多预约切换
   */
  loadCurrentReservations() {
    get('/api/reservation/current').then(res => {
      if (res.code === 0 && res.data && res.data.length > 0) {
        const list = res.data
        // 尝试与 globalData 中的 selectedTable 匹配，自动选中
        const app = getApp()
        const globalTable = app.globalData.selectedTable
        let matchIdx = 0
        if (globalTable && globalTable.id) {
          const found = list.findIndex(r => r.tableId === globalTable.id)
          if (found >= 0) matchIdx = found
        }
        this.setData({ reservationList: list })
        this.selectReservation(matchIdx)
      } else {
        // 无预约
        this.setData({
          reservationList: [],
          selectedReservationIdx: -1,
          currentReserve: null
        })
      }
    }).catch(err => {
      console.warn('[loadCurrentReservations] 加载预约列表失败:', err)
    })
  },

  /**
   * 选中某个预约（更新当前预约状态 + 同步 globalData）
   */
  selectReservation(idx) {
    const list = this.data.reservationList
    if (idx < 0 || idx >= list.length) return
    const r = list[idx]
    const app = getApp()
    // 更新 globalData，确保下单页能获取到
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
    // 如果切换了门店，清空购物车
    const isStoreChanged = this.data.storeId && Number(this.data.storeId) !== r.storeId
    const updateData = {
      selectedReservationIdx: idx,
      showReservationPicker: false,
      storeId: r.storeId,
      storeName: r.storeName,
      currentReserve: {
        storeName: r.storeName,
        tableName: r.tableName,
        tableType: r.tableType,
        reserveDate: r.reserveDate,
        reserveTime: r.reserveTime,
        persons: r.persons,
        duration: r.duration,
        orderId: r.orderId
      }
    }
    if (isStoreChanged) {
      updateData.cart = []
      updateData.cartTotal = 0
      updateData.cartCount = 0
      app.globalData.cartItems = []
    }
    this.setData(updateData)
    // 切换预约后重新加载对应门店菜单
    this.loadMenu()
  },

  /** 打开预约选择器 */
  onOpenReservationPicker() {
    if (this.data.reservationList.length === 0) return
    this.setData({ showReservationPicker: true })
  },

  /** 关闭预约选择器 */
  onCloseReservationPicker() {
    this.setData({ showReservationPicker: false })
  },

  /** 在选择器中选中某预约 */
  onSelectReservation(e) {
    const idx = e.currentTarget.dataset.index
    this.selectReservation(idx)
  },

  /** 跳转预约页 */
  goReserve() {
    wx.switchTab({ url: '/pages/reservation/reservation' })
  },

  // 加载所有门店 → 自动定位 → 按距离排序
  loadStores() {
    get('/api/stores').then(res => {
      if (res.code === 0) {
        const rawStores = res.data
        // 自动获取定位 → 计算距离 → 排序
        this.autoSortByLocation(rawStores)
      }
    })
  },

  // 自动定位 + 距离排序
  autoSortByLocation(rawStores) {
    getUserLocation().then(userLoc => {
      this.setData({ userLocation: userLoc })
      const sorted = applyDistanceAndSort(rawStores, userLoc)
      this.applySortedStores(sorted)
    }).catch(() => {
      const fallback = applyDistanceAndSort(rawStores, null)
      this.applySortedStores(fallback)
      wx.showToast({ title: '无法获取位置，显示未知距离', icon: 'none' })
    })
  },

  // 应用排序后的门店列表
  applySortedStores(sorted) {
    this.setData({ stores: sorted })

    if (!this.data.storeId && sorted.length > 0) {
      const first = sorted[0]
      this.setData({
        storeId: first.id,
        storeName: first.name
      })
      const app = getApp()
      app.globalData.currentStore = { id: first.id, name: first.name }
      this.loadMenu()
    } else if (this.data.storeId) {
      // 补充 storeName
      const current = sorted.find(s => s.id === Number(this.data.storeId))
      if (current && !this.data.storeName) {
        this.setData({ storeName: current.name })
      }
      this.loadMenu()
    }
  },

  onStoreHeaderTap() {
    this.buildPickerMarkers()
    this.setData({ showStorePicker: true, activePickerStoreId: null })
  },

  hideStorePicker() {
    this.setData({ showStorePicker: false })
  },

  buildPickerMarkers() {
    const { stores, userLocation } = this.data
    if (!stores || stores.length === 0) return

    const markers = stores.map((s, index) => ({
      id: s.id,
      latitude: s.lat,
      longitude: s.lng,
      title: s.name,
      // 不设 callout（门店选择器地图不需要导航；避免 qqmap:// scheme 报错）
      label: {
        content: String(index + 1),
        color: '#ffffff',
        fontSize: 12,
        x: 12,
        y: -24,
        bgColor: '#C97E5A',
        borderRadius: 16,
        padding: 4
      },
      width: 26,
      height: 26
    }))

    const loc = userLocation
    let centerLat, centerLng
    if (loc && loc.lat) {
      centerLat = loc.lat
      centerLng = loc.lng
    } else {
      const lats = stores.map(s => s.lat)
      const lngs = stores.map(s => s.lng)
      centerLat = (Math.min(...lats) + Math.max(...lats)) / 2
      centerLng = (Math.min(...lngs) + Math.max(...lngs)) / 2
    }

    this.setData({
      pickerMarkers: markers,
      pickerMapLat: centerLat,
      pickerMapLng: centerLng
    })
  },

  onPickerMarkerTap(e) {
    const id = e.detail && e.detail.markerId
    if (!id) return
    this.setData({ activePickerStoreId: id })
  },

  onStoreSelect(e) {
    const store = e.currentTarget.dataset.store
    if (store.id === this.data.storeId) {
      this.hideStorePicker()
      return
    }
    this.setData({
      storeId: store.id,
      storeName: store.name,
      cart: [],          // 切换门店清空购物车
      cartTotal: 0,
      cartCount: 0,
      loading: true,
      showStorePicker: false
    })
    const app = getApp()
    app.globalData.currentStore = { id: store.id, name: store.name }
    app.globalData.cartItems = []
    this.loadMenu()
  },

  loadMenu() {
    if (!this.data.storeId) return
    get(`/api/menu?storeId=${this.data.storeId}`).then(res => {
      if (res.code === 0) {
        const { categories, items } = res.data
        this.setData({
          categories,
          items,
          activeCategoryId: categories[0]?.id,
          loading: false
        })
      }
    })
  },

  // 切换分类
  onCategoryClick(e) {
    const id = e.currentTarget.dataset.id
    this.setData({ activeCategoryId: id })
  },

  // 加入购物车
  onAddCart(e) {
    const item = e.currentTarget.dataset.item
    const cart = this.data.cart
    const idx = cart.findIndex(c => c.id === item.id)
    if (idx >= 0) {
      cart[idx].qty++
    } else {
      cart.push({ ...item, qty: 1 })
    }
    this.updateCart(cart)
  },

  // 减少购物车
  onMinusCart(e) {
    const item = e.currentTarget.dataset.item
    const cart = this.data.cart
    const idx = cart.findIndex(c => c.id === item.id)
    if (idx >= 0) {
      cart[idx].qty--
      if (cart[idx].qty <= 0) cart.splice(idx, 1)
    }
    this.updateCart(cart)
  },

  // 更新购物车并同步全局
  updateCart(cart) {
    const cartTotal = calcCartTotal(cart)
    const cartCount = cart.reduce((s, c) => s + c.qty, 0)
    this.setData({ cart, cartTotal, cartCount })
    getApp().globalData.cartItems = cart
  },

  // 显示购物车详情
  toggleCartDetail() {
    if (this.data.cart.length === 0) return
    this.setData({ showCartDetail: !this.data.showCartDetail })
  },

  // 清空购物车
  clearCart() {
    wx.showModal({
      title: '清空购物车',
      content: '确定要清空购物车吗？',
      success: (res) => {
        if (res.confirm) this.updateCart([])
      }
    })
  },

  // 去结算
  goOrder() {
    if (this.data.cart.length === 0) { wx.showToast({ title: '购物车是空的', icon: 'none' }); return }
    if (!this.data.currentReserve) {
      wx.showToast({ title: '请先预约桌位', icon: 'none' })
      return
    }
    const reserve = this.data.currentReserve
    wx.navigateTo({
      url: `/pages/order/order?storeId=${this.data.storeId}&tableId=${reserve.orderId || ''}`
    })
  }
})
