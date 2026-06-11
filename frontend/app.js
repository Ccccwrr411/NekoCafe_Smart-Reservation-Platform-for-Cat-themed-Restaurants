// app.js
App({
  onLaunch() {
    // 初始化本地存储日志
    const logs = wx.getStorageSync('logs') || []
    logs.unshift(Date.now())
    wx.setStorageSync('logs', logs)

    // 读取登录态，写入 globalData
    const token = wx.getStorageSync('token')
    const userInfo = wx.getStorageSync('userInfo') || null
    const userRole = wx.getStorageSync('userRole') || (userInfo ? userInfo.role : null)

    this.globalData.userInfo = userInfo
    this.globalData.userRole = userRole

    // 未登录则跳转到登录页
    if (!token) {
      wx.reLaunch({ url: '/pages/login/login' })
    }
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
    baseUrl: 'http://172.20.10.2:8081', // 后端地址（上线后替换）172.20.10.2     127.0.0.1:8081
    useMock: false,              // true = 纯前端 mock 开发；false = 对接真实后端
    cartItems: [],
    currentStore: null,
    selectedTable: null
  }
})
