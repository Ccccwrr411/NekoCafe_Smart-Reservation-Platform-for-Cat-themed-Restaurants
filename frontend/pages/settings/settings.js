// pages/settings/settings.js
const app = getApp()

Page({
  data: {
    settings: []
  },

  onLoad() {
    wx.setNavigationBarTitle({ title: '设置' })
    this.loadSettings()
  },

  loadSettings() {
    const savedSettings = wx.getStorageSync('appSettings') || {}
    const settings = [
      { icon: '🔐', label: '修改密码', path: '/pages/changePassword/changePassword' },
      { icon: '🔔', label: '消息通知', toggle: true, value: savedSettings.notifications !== undefined ? savedSettings.notifications : true, key: 'notifications' },
      { icon: '📱', label: '版本号', value: 'v1.0.0' }
    ]
    this.setData({ settings })
  },

  goPage(e) {
    const path = e.currentTarget.dataset.path
    if (path) {
      wx.navigateTo({ url: path })
    }
  },

  toggleSetting(e) {
    const index = parseInt(e.currentTarget.dataset.index)
    const settings = [...this.data.settings]
    settings[index].value = e.detail.value
    this.setData({ settings })
    
    const savedSettings = wx.getStorageSync('appSettings') || {}
    savedSettings[settings[index].key] = settings[index].value
    wx.setStorageSync('appSettings', savedSettings)
    
    wx.showToast({ 
      title: settings[index].value ? '已开启' : '已关闭', 
      icon: 'none' 
    })
  },

  clearCache() {
    wx.showModal({
      title: '清除缓存',
      content: '确定清除所有缓存数据？包括用户信息、设置等。',
      success: (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '清除中...' })
          setTimeout(() => {
            wx.clearStorageSync()
            wx.hideLoading()
            wx.showToast({ title: '清除成功', icon: 'success' })
            this.loadSettings()
          }, 1000)
        }
      }
    })
  },

  goAbout() {
    wx.showModal({
      title: '关于 NekoCafé',
      content: 'NekoCafé - 爱猫人的专属咖啡馆\n\n版本：v1.0.0\n\n我们致力于为猫咖爱好者提供最优质的预约体验，让每一次光临都充满温馨与快乐。🐱',
      showCancel: false,
      confirmText: '我知道了'
    })
  },

  onLogout() {
    wx.showModal({
      title: '退出登录',
      content: '确认退出当前账号？',
      success: (res) => {
        if (res.confirm) {
          app.logout()
        }
      }
    })
  }
})