// pages/realname/realname.js
const { get, post } = require('../../utils/request')

Page({
  data: {
    realName: '',
    idCard: '',
    submitting: false,
    verified: false,
    verifiedInfo: null
  },

  onLoad() {
    this.loadRealnameStatus()
  },

  /** 从后端查询当前用户的实名认证状态（不依赖本地缓存，避免多用户混用） */
  loadRealnameStatus() {
    get('/api/user/profile').then(res => {
      if (res.code === 0 && res.data && res.data.isVerified) {
        this.setData({
          verified: true,
          verifiedInfo: {
            realName: res.data.realName,
            idCardMask: res.data.idCardMask
          }
        })
      }
    })
  },

  onRealNameInput(e) {
    this.setData({ realName: e.detail.value })
  },

  onIdCardInput(e) {
    this.setData({ idCard: e.detail.value })
  },

  onSubmit() {
    const { realName, idCard } = this.data
    if (!realName.trim()) {
      wx.showToast({ title: '请输入真实姓名', icon: 'none' }); return
    }
    if (!idCard.trim() || idCard.length !== 18) {
      wx.showToast({ title: '请输入18位身份证号', icon: 'none' }); return
    }

    this.setData({ submitting: true })
    post('/api/user/realname', { realName, idCard }).then(res => {
      this.setData({ submitting: false })
      if (res.code === 0) {
        wx.showToast({ title: '认证成功', icon: 'success' })
        // 更新本地缓存的用户信息（profile 里的实名字段）
        const profile = wx.getStorageSync('userInfo') || {}
        profile.isVerified = true
        profile.realName = realName
        profile.idCardMask = res.data.idCardMask
        wx.setStorageSync('userInfo', profile)
        setTimeout(() => wx.navigateBack(), 1200)
      } else {
        wx.showToast({ title: res.message || '认证失败', icon: 'none' })
      }
    }).catch(() => this.setData({ submitting: false }))
  }
})
