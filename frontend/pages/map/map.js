// pages/map/map.js
// 门店可视化地图 — 微信原生 map + 门店列表联动
// 导航全部使用 wx.openLocation（微信官方标准 API），不依赖第三方地图 SDK
const { get } = require('../../utils/request')
const { getUserLocation, applyDistanceAndSort } = require('../../utils/lbs')

Page({
  data: {
    stores: [],           // 已排序门店列表
    markers: [],          // 地图标记点
    userLocation: null,   // 用户定位 { lat, lng }
    mapLat: 39.9042,      // 地图中心纬度（默认北京）
    mapLng: 116.4074,     // 地图中心经度
    mapScale: 11,         // 地图缩放级别
    activeStoreId: null,  // 当前高亮门店 ID
    loading: true,
    locating: false,
    markersLoaded: false  // 标记点是否已加载（避免重复渲染）
  },

  onLoad() {
    this.loadData()
  },

  onShow() {
    // 从其他页面返回时可能需刷新
  },

  // ── 核心：加载门店 + 定位 + 构建标记点 ──
  async loadData() {
    this.setData({ loading: true, locating: true })

    try {
      // 并行获取：用户定位 + 门店列表（零后端改动）
      const [userLoc, storeRes] = await Promise.all([
        getUserLocation().catch(() => null),
        get('/api/stores')
      ])

      let stores = []
      if (storeRes && storeRes.code === 0) {
        stores = storeRes.data
      }

      // 距离排序（复用已有 utils/lbs.js 逻辑，与首页完全一致）
      const sorted = applyDistanceAndSort(stores, userLoc)

      // 构建地图标记点
      const markers = this.buildMarkers(sorted)

      // 地图中心：优先用户定位 → 最近门店 → 默认北京
      const center = userLoc || (sorted[0]
        ? { lat: sorted[0].lat, lng: sorted[0].lng }
        : { lat: 39.9042, lng: 116.4074 })

      this.setData({
        stores: sorted,
        markers,
        userLocation: userLoc,
        mapLat: center.lat,
        mapLng: center.lng,
        loading: false,
        locating: false,
        markersLoaded: true
      })
    } catch (err) {
      console.error('[map] loadData error:', err)
      this.setData({ loading: false, locating: false })
      wx.showToast({ title: '加载失败，请下拉重试', icon: 'none' })
    }
  },

  // ── 构建地图标记点 ──
  buildMarkers(stores) {
    return stores.map((s, index) => ({
      id: s.id,
      latitude: s.lat,
      longitude: s.lng,
      title: s.name,
      // 不设 callout，避免原生导航箭头触发 qqmap:// URL scheme
      // （开发者工具不支持此 scheme，真机上无此问题；导航改用 wx.openLocation）
      // 序号角标
      label: {
        content: String(index + 1),
        color: '#ffffff',
        fontSize: 14,
        x: 15,
        y: -30,
        anchorX: 0,
        anchorY: 0,
        bgColor: '#C97E5A',
        borderRadius: 20,
        padding: 5
      },
      width: 30,
      height: 30
    }))
  },

  // ── 点击地图标记点 → 高亮列表 + 可选导航 ──
  onMarkerTap(e) {
    const id = e.detail && e.detail.markerId
    if (!id) return

    // 找到对应门店
    const store = this.data.stores.find(s => s.id === id)
    if (!store) return

    // 先高亮对应列表项
    this.setData({ activeStoreId: id })

    // 弹出 ActionSheet：查看详情 / 导航
    wx.showActionSheet({
      itemList: ['导航到此', '查看详情'],
      success: (res) => {
        if (res.tapIndex === 0) {
          // 导航
          wx.openLocation({
            latitude: store.lat,
            longitude: store.lng,
            name: store.name,
            address: store.address,
            scale: 16
          })
        } else if (res.tapIndex === 1) {
          // 查看详情：地图定位到该门店 + 列表滚动已由 activeStoreId 处理
          this.setData({
            mapLat: store.lat,
            mapLng: store.lng,
            mapScale: 15
          })
        }
      }
    })
  },

  // ── 点击门店列表项 → 地图中心移至该门店 ──
  onStoreTap(e) {
    const store = e.currentTarget.dataset.store
    if (!store) return

    this.setData({
      mapLat: store.lat,
      mapLng: store.lng,
      mapScale: 15,
      activeStoreId: store.id
    })
  },

  // ── 导航：调用微信原生 openLocation ──
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

  // ─� 定位回用户位置 ──
  onLocateMe() {
    if (this.data.userLocation) {
      this.setData({
        mapLat: this.data.userLocation.lat,
        mapLng: this.data.userLocation.lng,
        mapScale: 14
      })
      wx.showToast({ title: '已定位到当前位置', icon: 'success', duration: 1500 })
    } else {
      wx.showToast({ title: '定位不可用, 请检查权限', icon: 'none' })
    }
  },

  // ── 全览所有门店 ──
  onOverview() {
    const { stores } = this.data
    if (stores.length === 0) return

    // 计算包含所有门店和用户位置的区域
    const allPoints = stores.map(s => ({ lat: s.lat, lng: s.lng }))
    if (this.data.userLocation) {
      allPoints.push({ lat: this.data.userLocation.lat, lng: this.data.userLocation.lng })
    }

    const lats = allPoints.map(p => p.lat)
    const lngs = allPoints.map(p => p.lng)
    const centerLat = (Math.min(...lats) + Math.max(...lats)) / 2
    const centerLng = (Math.min(...lngs) + Math.max(...lngs)) / 2

    this.setData({
      mapLat: centerLat,
      mapLng: centerLng,
      mapScale: 11,
      activeStoreId: null
    })
  },

  // ── 下拉刷新 ──
  onPullDownRefresh() {
    this.loadData()
    wx.stopPullDownRefresh()
  }
})
