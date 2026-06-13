// pages/changePassword/changePassword.js
const { get, post } = require('../../utils/request')

Page({
  data: {
    hasPassword: true,
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  },

  onLoad() {
    wx.setNavigationBarTitle({ title: '修改密码' })
    this.checkPasswordStatus()
  },

  checkPasswordStatus() {
    get('/api/user/profile').then(res => {
      if (res.code === 0) {
        this.setData({ hasPassword: res.data.hasPassword || false })
      }
    })
  },

  onOldPasswordInput(e) {
    this.setData({ oldPassword: e.detail.value })
  },

  onNewPasswordInput(e) {
    this.setData({ newPassword: e.detail.value })
  },

  onConfirmPasswordInput(e) {
    this.setData({ confirmPassword: e.detail.value })
  },

  submit() {
    const { hasPassword, oldPassword, newPassword, confirmPassword } = this.data
    
    if (hasPassword && !oldPassword) {
      wx.showToast({ title: '请输入旧密码', icon: 'none' })
      return
    }
    
    if (!newPassword) {
      wx.showToast({ title: '请输入新密码', icon: 'none' })
      return
    }
    
    if (newPassword.length < 6) {
      wx.showToast({ title: '新密码至少6位', icon: 'none' })
      return
    }
    
    if (newPassword !== confirmPassword) {
      wx.showToast({ title: '两次输入密码不一致', icon: 'none' })
      return
    }

    wx.showLoading({ title: '修改中...' })
    post('/api/user/change-password', {
      oldPassword: hasPassword ? oldPassword : '',
      newPassword
    }).then(result => {
      wx.hideLoading()
      if (result.code === 0 && result.data.success) {
        wx.showToast({ title: result.data.message, icon: 'success' })
        setTimeout(() => {
          wx.navigateBack()
        }, 1500)
      } else {
        wx.showToast({ title: result.data.message || '修改失败', icon: 'none' })
      }
    }).catch(() => {
      wx.hideLoading()
      wx.showToast({ title: '网络错误', icon: 'none' })
    })
  }
})