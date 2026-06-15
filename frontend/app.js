// app.js
App({
  onLaunch() {
    // 初始化本地存储日志
    const logs = wx.getStorageSync('logs') || []
    logs.unshift(Date.now())
    wx.setStorageSync('logs', logs)

    // 读取登录态，写入 globalData
    this.syncAuthFromStorage()

    // 未登录不再强制跳转，允许浏览首页（预约/点餐页会单独检查）
  },

  /**
   * 从 Storage 同步最新的登录态到 globalData
   * 每次登录/退出/页面 onShow 时调用，确保 globalData 与 Storage 一致
   */
  syncAuthFromStorage() {
    const token = wx.getStorageSync('token')
    const userInfo = wx.getStorageSync('userInfo') || null
    const userRole = wx.getStorageSync('userRole') || (userInfo ? userInfo.role : null)

    this.globalData.userInfo = userInfo
    this.globalData.userRole = userRole
    return { token, userInfo, userRole }
  },

  /**
   * 从后端数据库获取最新用户信息（含 storeId/storeName），同步到 Storage + globalData
   * 返回 Promise，resolve 时带上最新的 userInfo
   * 这是所有页面获取 storeId/storeName 的唯一可靠来源
   */
  fetchAndSyncUserInfo() {
    return new Promise((resolve) => {
      const token = wx.getStorageSync('token')
      if (!token) {
        resolve(null)
        return
      }
      const { get } = require('./utils/request')
      get('/api/user/profile').then(res => {
        if (res.code === 0 && res.data) {
          const profile = res.data
          // 保留 Storage 中已有的 role/roleLabel 等字段，用数据库返回的数据更新
          const oldInfo = wx.getStorageSync('userInfo') || {}
          const userInfo = {
            ...oldInfo,
            id: profile.id || oldInfo.id,
            nickName: profile.nickName || oldInfo.nickName,
            avatarUrl: profile.avatarUrl || oldInfo.avatarUrl,
            storeId: profile.storeId != null ? profile.storeId : oldInfo.storeId,
            storeName: profile.storeName || oldInfo.storeName || ''
          }
          wx.setStorageSync('userInfo', userInfo)
          this.globalData.userInfo = userInfo
          resolve(userInfo)
        } else {
          resolve(this.globalData.userInfo)
        }
      }).catch(() => {
        // 网络错误时，用 Storage 缓存兜底
        resolve(this.globalData.userInfo)
      })
    })
  },

  // ─── 权限判断工具 ───────────────────────────────────────
  /**
   * 判断当前角色是否在允许列表内
   * @param {string[]} allowedRoles  ['customer','staff','manager','hq_ops','cat_keeper']
   * @returns {boolean}
   */
  checkRole(allowedRoles) {
    const role = this.globalData.userRole
    return !!role && allowedRoles.includes(role)
  },

  /**
   * 权限守卫：角色不在允许列表则弹提示并返回上一页
   * @param {string[]} allowedRoles
   * @returns {boolean} true=通过, false=已拦截
   */
  requireRole(allowedRoles) {
    if (this.checkRole(allowedRoles)) return true
    wx.showModal({
      title: '权限不足',
      content: '您的角色暂无权访问此页面',
      showCancel: false,
      confirmText: '返回',
      success: () => { wx.navigateBack({ delta: 1 }) }
    })
    return false
  },

  // ─── 退出登录 ────────────────────────────────────────────
  logout() {
    wx.removeStorageSync('token')
    wx.removeStorageSync('userInfo')
    wx.removeStorageSync('userRole')
    this.globalData.userInfo = null
    this.globalData.userRole = null
    this.globalData.cartItems = []
    this.globalData.currentStore = null
    this.globalData.selectedTable = null
    wx.reLaunch({ url: '/pages/login/login' })
  },

  globalData: {
    userInfo: null,
    userRole: null,            // 'customer' | 'staff' | 'manager' | 'hq_ops' | 'cat_keeper'
    baseUrl: 'http://127.0.0.1:8081', // 后端地址（上线后替换）172.20.10.2     127.0.0.1:8081
    useMock: false,              // true = 纯前端 mock 开发；false = 对接真实后端
    cartItems: [],
    currentStore: null,
    selectedTable: null
  }
})
