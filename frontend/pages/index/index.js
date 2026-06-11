// pages/index/index.js
const { get } = require('../../utils/request')
const { formatDistance } = require('../../utils/util')
const { getUserLocation, applyDistanceAndSort } = require('../../utils/lbs')

Page({
  data: {
    stores: [],
    storesOriginal: [],  // 保持原始顺序用于搜索恢复
    loading: true,
    banners: [
      { id: 1, imageUrl: 'https://placehold.co/750x350/C97E5A/white?text=Banner+1', title: '布偶猫新成员入驻' },
      { id: 2, imageUrl: 'https://placehold.co/750x350/C97E5A/white?text=Banner+2', title: '会员日专属折扣' },
      { id: 3, imageUrl: 'https://placehold.co/750x350/C97E5A/white?text=Banner+3', title: '新品猫爪拿铁上线' }
    ],
    currentBanner: 0,
    userInfo: null,
    searchKeyword: '',
    // AI 推荐
    recommend: null,
    showRecommend: true,
    // LBS 状态
    locating: false,
    locationFailed: false
  },

  onLoad() {
    // 先检查 token：没有就不加载数据，app.js 的 onLaunch 会 reLaunch 到登录页
    const token = wx.getStorageSync('token')
    if (!token) {
      this.setData({ loading: false })
      return
    }
    const userInfo = wx.getStorageSync('userInfo')
    this.setData({ userInfo })
    this.loadStores()
  },

  onShow() {
    // 先检查 token：没有就跳过（等待跳转登录）
    const token = wx.getStorageSync('token')
    if (!token) return

    // 每次显示刷新用户信息（比如积分变动）
    const userInfo = wx.getStorageSync('userInfo')
    this.setData({ userInfo })
    // 刷新 AI 推荐
    if (!this._recommendLoaded) {
      this.loadRecommend()
      this._recommendLoaded = true
    }
  },

  // 加载门店列表
  loadStores() {
    this.setData({ loading: true })
    get('/api/stores').then(res => {
      if (res.code === 0) {
        const stores = res.data
        // 先保存原始数据
        this.setData({ stores, storesOriginal: [...stores], loading: false })
        // 自动获取定位 → 计算距离 → 排序
        this.autoSortByLocation()
      }
    }).catch(() => {
      this.setData({ loading: false })
    })
  },

  // 自动获取定位并排序（定位失败时降级处理）
  autoSortByLocation() {
    this.setData({ locating: true })
    getUserLocation().then(userLoc => {
      // 定位成功 → 计算距离 + 升序排列
      const sorted = applyDistanceAndSort(this.data.storesOriginal, userLoc)
      this.setData({
        stores: sorted,
        storesOriginal: [...sorted],
        locating: false,
        locationFailed: false
      })
    }).catch(() => {
      // 定位失败 → 保留原始顺序，距离显示「未知距离」
      const fallback = applyDistanceAndSort(this.data.storesOriginal, null)
      this.setData({
        stores: fallback,
        storesOriginal: [...fallback],
        locating: false,
        locationFailed: true
      })
      wx.showToast({ title: '无法获取位置，显示未知距离', icon: 'none' })
    })
  },

  // 搜索
  onSearchInput(e) {
    this.setData({ searchKeyword: e.detail.value })
  },

  onSearch() {
    const kw = this.data.searchKeyword.trim()
    if (!kw) {
      // 恢复排序后的完整列表
      this.setData({ stores: [...this.data.storesOriginal] })
      return
    }
    const filtered = this.data.storesOriginal.filter(s =>
      s.name.includes(kw) || s.address.includes(kw) || s.tags.some(t => t.includes(kw))
    )
    this.setData({ stores: filtered })
  },

  // 点击门店卡片
  onStoreClick(e) {
    const store = e.currentTarget.dataset.store
    const app = getApp()
    app.globalData.currentStore = store
    // 跳到预约页，带上 storeId
    wx.navigateTo({ url: `/pages/reservation/reservation?storeId=${store.id}&storeName=${store.name}` })
  },

  // Banner 轮播切换
  onBannerChange(e) {
    this.setData({ currentBanner: e.detail.current })
  },

  // 下拉刷新
  onPullDownRefresh() {
    this.loadStores()
    wx.stopPullDownRefresh()
  },

  goReservation() { wx.switchTab({ url: '/pages/reservation/reservation' }) },
  goMenu()        { wx.switchTab({ url: '/pages/menu/menu' }) },
  goCats()        { wx.navigateTo({ url: '/pages/cats/cats' }) },
  goCoupons()     { wx.navigateTo({ url: '/pages/coupons/coupons' }) },
  goProfile()     { wx.switchTab({ url: '/pages/profile/profile' }) },
  goMap()         { wx.navigateTo({ url: '/pages/map/map' }) },

  // ── AI 推荐 ──
  loadRecommend() {
    get('/api/recommend').then(res => {
      if (res.code === 0) {
        this.setData({ recommend: res.data })
      }
    }).catch(() => {})
  },

  // 点击推荐桌位 → 跳预约页
  onRecommendTableTap(e) {
    const table = e.currentTarget.dataset.table
    const app = getApp()
    const storeId = table.id < 200 ? 1 : 2
    app.globalData.currentStore = { id: storeId, name: 'NekoCafé 朝阳店' }
    wx.navigateTo({ url: `/pages/reservation/reservation?storeId=${storeId}` })
  },

  // 点击推荐菜品 → 跳点单页
  onRecommendDishTap(e) {
    const app = getApp()
    app.globalData.currentStore = { id: 1, name: 'NekoCafé 朝阳店' }
    wx.navigateTo({ url: '/pages/menu/menu?storeId=1' })
  },

  // 关闭推荐
  dismissRecommend() {
    this.setData({ showRecommend: false })
  },

  // ── 导航：调用微信原生 wx.openLocation ──
  onNavigate(e) {
    const store = e.currentTarget.dataset.store
    if (!store) return

    wx.openLocation({
      latitude: store.lat,
      longitude: store.lng,
      name: store.name,
      address: store.address,
      scale: 16
    })
  },
})
