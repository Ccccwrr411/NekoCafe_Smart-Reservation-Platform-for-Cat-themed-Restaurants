// pages/realname/realname.js
const { post } = require('../../utils/request')

Page({
  data: {
    realName: '',
    idCard: '',
    submitting: false,
    verified: false,
    verifiedInfo: null
  },

  onLoad() {
    // 检查是否已认证（可从缓存或接口读取）
    const cached = wx.getStorageSync('realnameInfo')
    if (cached) {
      this.setData({ verified: true, verifiedInfo: cached })
    }
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
        wx.setStorageSync('realnameInfo', res.data)
        wx.showToast({ title: '认证成功', icon: 'success' })
        setTimeout(() => wx.navigateBack(), 1200)
      }
    }).catch(() => this.setData({ submitting: false }))
  }
})
