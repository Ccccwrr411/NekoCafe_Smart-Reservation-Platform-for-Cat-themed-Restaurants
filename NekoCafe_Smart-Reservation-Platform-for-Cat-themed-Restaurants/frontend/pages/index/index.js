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
      { id: 1, imageUrl: 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/banners/banner_1.png', title: '布偶猫新成员入驻' },
      { id: 2, imageUrl: 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/banners/banner_2.png', title: '会员日专属折扣' },
      { id: 3, imageUrl: 'https://nekocafe-images.oss-cn-beijing.aliyuncs.com/uploads/banners/banner_3.png', title: '新品猫爪拿铁上线' }
    ],
    currentBanner: 0,
    userInfo: null,
    searchKeyword: '',
    // AI 推荐
    recommend: null,
    showRecommend: true,
    recommendExpanded: false,
    // 门店选择器
    showStorePicker: false,
    storePickerRange: [],
    storePickerIndex: 0,
    currentRecommendStoreId: null,
    // LBS 状态
    locating: false,
    locationFailed: false
  },

  onLoad() {
    // 允许未登录用户浏览首页门店列表
    let userInfo = wx.getStorageSync('userInfo') || null
    userInfo = this.fixAvatarUrl(userInfo)
    this.setData({ userInfo })
    this.loadStores()
  },

  onShow() {
    // 从后端数据库获取最新用户信息（含 storeId/storeName），确保编译后也是正确的
    const app = getApp()
    app.fetchAndSyncUserInfo().then((dbUserInfo) => {
      let userInfo = dbUserInfo || wx.getStorageSync('userInfo') || null
      userInfo = this.fixAvatarUrl(userInfo)
      this.setData({ userInfo })
    })
    // 同步也读一次 Storage，不等接口返回先展示（接口返回后会再更新）
    let userInfo = wx.getStorageSync('userInfo') || null
    userInfo = this.fixAvatarUrl(userInfo)
    this.setData({ userInfo })

    // 刷新 AI 推荐（仅登录用户，使用当前选中的门店）
    if (userInfo && !this._recommendLoaded) {
      this.loadRecommend(this.data.currentRecommendStoreId)
      this._recommendLoaded = true
    }
  },

  // 补全头像 URL：相对路径 → 完整 URL
  fixAvatarUrl(userInfo) {
    if (!userInfo || !userInfo.avatarUrl) return userInfo
    if (userInfo.avatarUrl.startsWith('http://') || userInfo.avatarUrl.startsWith('https://')) {
      return userInfo
    }
    const app = getApp()
    const baseUrl = app.globalData.baseUrl || 'http://127.0.0.1:8081'
    return { ...userInfo, avatarUrl: baseUrl + userInfo.avatarUrl }
  },

  // 加载门店列表
  loadStores() {
    this.setData({ loading: true })
    get('/api/stores').then(res => {
      if (res.code === 0) {
        const stores = res.data
        // 构建门店选择器范围
        const storePickerRange = stores.map(s => s.name)
        // 默认选中第一家店
        const defaultStoreId = stores.length > 0 ? stores[0].id : null
        // 先保存原始数据
        this.setData({
          stores: stores,
          storesOriginal: [...stores],
          loading: false,
          storePickerRange: storePickerRange,
          storePickerIndex: 0,
          currentRecommendStoreId: defaultStoreId
        })
        // 自动获取定位 → 计算距离 → 排序
        this.autoSortByLocation()
        // 加载 AI 推荐（使用默认门店）
        this.loadRecommend(defaultStoreId)
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
  goQueue()       { wx.navigateTo({ url: '/pages/queue/queue' }) },
  goMenu()        { wx.switchTab({ url: '/pages/menu/menu' }) },
  goCats()        { wx.navigateTo({ url: '/pages/cats/cats' }) },
  goCoupons()     { wx.navigateTo({ url: '/pages/coupons/coupons' }) },
  goProfile()     { wx.switchTab({ url: '/pages/profile/profile' }) },
  goMap()         { wx.navigateTo({ url: '/pages/map/map' }) },

  // ── 门店选择器 ──

  /** 显示门店选择器 */
  onShowStorePicker() {
    this.setData({ showStorePicker: true })
  },

  /** 门店 Picker 选择变更 */
  onStorePickerChange(e) {
    const index = Number(e.detail.value)
    const storeId = this.data.stores[index]?.id || null
    this.setData({
      storePickerIndex: index,
      currentRecommendStoreId: storeId,
      showStorePicker: false
    })
    // 重新加载推荐（按新选的门店）
    this.loadRecommend(storeId)
  },

  /** 关闭门店选择器（取消） */
  onHideStorePicker() {
    this.setData({ showStorePicker: false })
  },

  /** 获取当前推荐门店名称 */
  getCurrentStoreName() {
    const storeId = this.data.currentRecommendStoreId
    if (!storeId) return '全部门店'
    const store = this.data.stores.find(s => s.id === storeId)
    return store ? store.name : '全部门店'
  },

  // ── AI 推荐 ──
  // loadRecommend() {
  //   get('/api/recommend').then(res => {
  //     if (res.code === 0) {
  //       this.setData({ recommend: res.data })
  //     }
  //   }).catch(() => {})
  // },

  loadRecommend(storeId) {
    const userInfo = wx.getStorageSync('userInfo')
    const userId = userInfo?.id || userInfo?.userId
    if (!userId) return

    let url = `/api/recommend?userId=${userId}`
    if (storeId) {
      url += `&storeId=${storeId}`
    }

    get(url).then(res => {
      if (res.code === 0) {
        this.setData({ recommend: res.data })
      }
    }).catch(() => {})
  },

  // 点击推荐猫咪 → 跳猫咪档案页
  onRecommendCatTap(e) {
    const storeId = this.data.currentRecommendStoreId
    const storeName = this.getCurrentStoreName()
    const app = getApp()
    app.globalData.currentStore = { id: storeId, name: storeName }
    wx.navigateTo({ url: '/pages/cats/cats' })
  },

  // 点击推荐桌位 → 跳预约页（使用当前选中的推荐门店）
  onRecommendTableTap(e) {
    const table = e.currentTarget.dataset.table
    const storeId = this.data.currentRecommendStoreId || table.storeId
    const storeName = this.getCurrentStoreName()
    const app = getApp()
    app.globalData.currentStore = { id: storeId, name: storeName }
    wx.navigateTo({ url: `/pages/reservation/reservation?storeId=${storeId}&storeName=${storeName}` })
  },

  // 点击推荐菜品 → 跳点单页（使用 userInfo 中的 storeId）
  onRecommendDishTap(e) {
    const app = getApp()
    const userInfo = app.globalData.userInfo || {}
    const storeId = userInfo.storeId || 1
    const storeName = userInfo.storeName || ''
    app.globalData.currentStore = { id: storeId, name: storeName }
    wx.navigateTo({ url: `/pages/menu/menu?storeId=${storeId}` })
  },

  // 关闭推荐
  dismissRecommend() {
    this.setData({ showRecommend: false })
  },

  // 展开/收起推荐详情
  onToggleRecommend() {
    this.setData({ recommendExpanded: !this.data.recommendExpanded })
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
