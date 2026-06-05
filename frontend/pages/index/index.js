// pages/index/index.js
const { get } = require('../../utils/request')
const { formatDistance, calcDistance } = require('../../utils/util')

Page({
  data: {
    stores: [],
    storesOriginal: [],  // 保持原始顺序用于恢复
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
    // LBS
    userLocation: null,
    sortByNearest: false,
    locating: false
  },

  onLoad() {
    const userInfo = wx.getStorageSync('userInfo')
    this.setData({ userInfo })
    this.loadStores()
  },

  onShow() {
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
        const stores = res.data.map(s => ({
          ...s,
          distanceText: formatDistance(s.distance)
        }))
        this.setData({ stores, storesOriginal: [...stores], loading: false })
        // 尝试获取位置
        this.getUserLocation()
      }
    }).catch(() => {
      this.setData({ loading: false })
    })
  },

  // 搜索
  onSearchInput(e) {
    this.setData({ searchKeyword: e.detail.value })
  },

  onSearch() {
    const kw = this.data.searchKeyword.trim()
    if (!kw) { this.loadStores(); return }
    const filtered = this.data.stores.filter(s =>
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

  // ── LBS 定位与排序 ──

  // 获取用户位置
  getUserLocation() {
    if (this.data.userLocation) return // 已获取过
    this.setData({ locating: true })
    wx.getLocation({
      type: 'gcj02',
      success: (res) => {
        const loc = { lat: res.latitude, lng: res.longitude }
        this.setData({ userLocation: loc, locating: false })
        // 计算真实距离
        this.computeRealDistances(loc)
        // 如果已开启"距我最近"则排序
        if (this.data.sortByNearest) {
          this.sortByDistance()
        }
      },
      fail: () => {
        this.setData({ locating: false })
        // 定位失败则使用 mock 的 distance 字段
        wx.showToast({ title: '无法获取位置，显示默认距离', icon: 'none' })
      }
    })
  },

  // 用真实经纬度计算距离
  computeRealDistances(loc) {
    const stores = this.data.stores.map(s => {
      if (s.lat && s.lng) {
        const realDist = calcDistance(loc.lat, loc.lng, s.lat, s.lng)
        return { ...s, realDistance: realDist, distanceText: formatDistance(realDist) }
      }
      return s
    })
    this.setData({ stores, storesOriginal: [...stores] })
  },

  // 按实际距离排序
  sortByDistance() {
    const sorted = [...this.data.stores].sort((a, b) => {
      const da = a.realDistance !== undefined ? a.realDistance : a.distance
      const db = b.realDistance !== undefined ? b.realDistance : b.distance
      return da - db
    })
    this.setData({ stores: sorted, sortByNearest: true })
  },

  // 切换"距我最近"排序
  onToggleSortByDistance() {
    if (this.data.sortByNearest) {
      // 恢复原始顺序
      this.setData({ stores: [...this.data.storesOriginal], sortByNearest: false })
    } else {
      if (!this.data.userLocation) {
        this.getUserLocation()
      } else {
        this.sortByDistance()
      }
    }
  },
  goReservation() { wx.switchTab({ url: '/pages/reservation/reservation' }) },
  goMenu()        { wx.switchTab({ url: '/pages/menu/menu' }) },
  goCats()        { wx.navigateTo({ url: '/pages/cats/cats' }) },
  goCoupons()     { wx.navigateTo({ url: '/pages/coupons/coupons' }) },
  goProfile()     { wx.switchTab({ url: '/pages/profile/profile' }) },

  // ── AI 推荐 ──
  loadRecommend() {
    get('/api/recommend').then(res => {
      if (res.code === 0) {
        this.setData({ recommend: res.data })
      }
    })
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
})
