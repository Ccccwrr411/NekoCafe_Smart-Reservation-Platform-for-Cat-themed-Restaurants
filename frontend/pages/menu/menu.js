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
    showCartDetail: false
  },

  onLoad(options) {
    // 先检查 token
    if (!wx.getStorageSync('token')) return

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
    if (!wx.getStorageSync('token')) return

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
    wx.navigateTo({ url: `/pages/order/order?storeId=${this.data.storeId}` })
  }
})
