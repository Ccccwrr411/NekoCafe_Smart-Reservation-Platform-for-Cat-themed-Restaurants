// pages/menu/menu.js
const { get } = require('../../utils/request')
const { calcCartTotal, formatDistance } = require('../../utils/util')

Page({
  data: {
    storeId: null,
    storeName: '',
    stores: [],
    showStorePicker: false,

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

  loadStores() {
    get('/api/stores').then(res => {
      if (res.code === 0) {
        const stores = res.data.map(s => ({
          ...s,
          distanceText: formatDistance(s.distance)
        }))
        this.setData({ stores })

        if (!this.data.storeId && stores.length > 0) {
          const first = stores[0]
          this.setData({
            storeId: first.id,
            storeName: first.name
          })
          const app = getApp()
          app.globalData.currentStore = { id: first.id, name: first.name }
          this.loadMenu()
        } else if (this.data.storeId) {
          // 补充 storeName
          const current = stores.find(s => s.id === Number(this.data.storeId))
          if (current && !this.data.storeName) {
            this.setData({ storeName: current.name })
          }
          this.loadMenu()
        }
      }
    })
  },

  onStoreHeaderTap() {
    this.setData({ showStorePicker: true })
  },

  hideStorePicker() {
    this.setData({ showStorePicker: false })
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
