// pages/editProfile/editProfile.js
const { get, post } = require('../../utils/request')

Page({
  data: {
    userInfo: {
      nickName: '',
      avatarUrl: '',
      phone: '',
      email: ''
    }
  },

  onLoad() {
    wx.setNavigationBarTitle({ title: '修改个人档案' })
    this.loadUserInfo()
  },

  loadUserInfo() {
    const storedInfo = wx.getStorageSync('userInfo') || {}
    this.setData({
      userInfo: {
        nickName: storedInfo.nickName || '',
        avatarUrl: storedInfo.avatarUrl || '',
        phone: storedInfo.phone || '',
        email: storedInfo.email || ''
      }
    })
  },

  onNickNameInput(e) {
    this.setData({ 'userInfo.nickName': e.detail.value })
  },

  onPhoneInput(e) {
    this.setData({ 'userInfo.phone': e.detail.value })
  },

  onEmailInput(e) {
    this.setData({ 'userInfo.email': e.detail.value })
  },

  chooseAvatar() {
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        this.setData({ 'userInfo.avatarUrl': res.tempFilePaths[0] })
      }
    })
  },

  submitForm() {
    const { nickName, phone } = this.data.userInfo
    if (!nickName.trim()) {
      wx.showToast({ title: '请输入昵称', icon: 'none' })
      return
    }
    if (!phone.trim()) {
      wx.showToast({ title: '请输入手机号', icon: 'none' })
      return
    }
    
    wx.showLoading({ title: '保存中...' })
    post('/api/user/profile', this.data.userInfo).then(res => {
      wx.hideLoading()
      if (res.code === 0) {
        wx.showToast({ title: '保存成功', icon: 'success' })
        wx.setStorageSync('userInfo', { ...wx.getStorageSync('userInfo'), ...this.data.userInfo })
        setTimeout(() => {
          wx.navigateBack()
        }, 1500)
      } else {
        wx.showToast({ title: res.message || '保存失败', icon: 'none' })
      }
    }).catch(() => {
      wx.hideLoading()
      wx.showToast({ title: '保存失败', icon: 'none' })
    })
  }
})