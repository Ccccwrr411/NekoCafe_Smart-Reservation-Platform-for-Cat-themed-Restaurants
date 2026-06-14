// pages/settings/settings.js
const { get, post } = require('../../utils/request')
const app = getApp()

Page({
  data: {
    userInfo: null,
    settings: []
  },

  onShow() {
    wx.setNavigationBarTitle({ title: '设置' })
    this.loadUserInfo()
    this.loadSettings()
  },

  loadUserInfo() {
    get('/api/user/profile').then(res => {
      if (res.code === 0) {
        const u = res.data
        // 补全头像 URL
        if (u.avatarUrl && !u.avatarUrl.startsWith('http')) {
          const baseUrl = app.globalData.baseUrl || 'http://127.0.0.1:8081'
          u.avatarUrl = baseUrl + u.avatarUrl
        }
        this.setData({ userInfo: u })
        wx.setStorageSync('userInfo', u)
      }
    }).catch(() => {})
  },

  // ── 头像（上传到后端）──
  onChangeAvatar() {
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempPath = res.tempFilePaths[0]
        this.uploadAvatar(tempPath)
      }
    })
  },

  uploadAvatar(filePath) {
    const app = getApp()
    const baseUrl = app.globalData.baseUrl || 'http://127.0.0.1:8081'
    const token = wx.getStorageSync('token') || ''

    wx.showLoading({ title: '上传中...' })
    wx.uploadFile({
      url: baseUrl + '/api/user/upload-avatar',
      filePath,
      name: 'file',
      header: { 'Authorization': `Bearer ${token}` },
      success: (res) => {
        wx.hideLoading()
        try {
          const data = JSON.parse(res.data)
          if (data.code === 0) {
            let avatarUrl = data.data.avatarUrl
            // 补全完整 URL
            if (avatarUrl && !avatarUrl.startsWith('http')) {
              avatarUrl = baseUrl + avatarUrl
            }
            const userInfo = { ...this.data.userInfo, avatarUrl }
            this.setData({ userInfo })
            wx.setStorageSync('userInfo', userInfo)
            wx.showToast({ title: '头像更新成功', icon: 'success' })
          } else {
            wx.showToast({ title: data.message || '上传失败', icon: 'none' })
          }
        } catch (e) {
          wx.showToast({ title: '服务器返回异常', icon: 'none' })
        }
      },
      fail: () => {
        wx.hideLoading()
        wx.showToast({ title: '网络异常', icon: 'none' })
      }
    })
  },

  // ── 昵称 ──
  onEditNickname() {
    const currentNick = (this.data.userInfo && this.data.userInfo.nickName) || ''
    this.showEditDialog('修改昵称', currentNick, (newVal) => {
      if (!newVal) { wx.showToast({ title: '昵称不能为空', icon: 'none' }); return }
      this.updateProfile({ nickName: newVal })
    })
  },

  // ── 手机号 ──
  onEditPhone() {
    const currentPhone = (this.data.userInfo && this.data.userInfo.phone) || ''
    this.showEditDialog('修改手机号', currentPhone, (newVal) => {
      if (!/^1\d{10}$/.test(newVal)) { wx.showToast({ title: '手机号格式不正确', icon: 'none' }); return }
      this.updateProfile({ phone: newVal })
    })
  },

  // ── 邮箱 ──
  onEditEmail() {
    const currentEmail = (this.data.userInfo && this.data.userInfo.email) || ''
    this.showEditDialog('修改邮箱', currentEmail, (newVal) => {
      if (!/^[\w.-]+@[\w.-]+\.\w+$/.test(newVal)) { wx.showToast({ title: '邮箱格式不正确', icon: 'none' }); return }
      this.updateProfile({ email: newVal })
    })
  },

  // ── 弹窗编辑通用方法 ──
  showEditDialog(title, value, onConfirm) {
    wx.showModal({
      title: title,
      editable: true,
      placeholderText: '请输入',
      content: value,
      success: (res) => {
        if (res.confirm && res.content) {
          onConfirm(res.content.trim())
        }
      }
    })
  },

  // ── 提交更新 ──
  updateProfile(data) {
    wx.showLoading({ title: '保存中...' })
    post('/api/user/profile', data).then(res => {
      wx.hideLoading()
      if (res.code === 0) {
        const u = res.data
        if (u.avatarUrl && !u.avatarUrl.startsWith('http')) {
          const baseUrl = app.globalData.baseUrl || 'http://127.0.0.1:8081'
          u.avatarUrl = baseUrl + u.avatarUrl
        }
        this.setData({ userInfo: u })
        wx.setStorageSync('userInfo', u)
        wx.showToast({ title: '保存成功', icon: 'success' })
      } else {
        wx.showToast({ title: res.message || '保存失败', icon: 'none' })
      }
    }).catch(err => {
      wx.hideLoading()
      const msg = (err && err.data && err.data.message) || '网络错误'
      wx.showToast({ title: msg, icon: 'none' })
    })
  },

  // ── 修改密码 ──
  onChangePassword() {
    wx.showModal({
      title: '修改密码',
      editable: true,
      placeholderText: '请输入新密码（至少6位）',
      content: '',
      success: (res) => {
        if (res.confirm && res.content) {
          const newPwd = res.content.trim()
          if (newPwd.length < 6) {
            wx.showToast({ title: '密码至少6位', icon: 'none' })
            return
          }
          this.submitPassword(newPwd)
        }
      }
    })
  },

  submitPassword(newPwd) {
    wx.showLoading({ title: '修改中...' })
    post('/api/user/change-password', {
      oldPassword: '',
      newPassword: newPwd
    }).then(res => {
      wx.hideLoading()
      if (res.code === 0 && res.data && res.data.success) {
        wx.showToast({ title: res.data.message, icon: 'success' })
      } else {
        wx.showToast({ title: (res.data && res.data.message) || '修改失败', icon: 'none' })
      }
    }).catch(err => {
      wx.hideLoading()
      const msg = (err && err.data && err.data.message) || '网络错误'
      wx.showToast({ title: msg, icon: 'none' })
    })
  },

  // ── 通用设置（保留原有功能）──
  loadSettings() {
    const savedSettings = wx.getStorageSync('appSettings') || {}
    const settings = [
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
