// pages/contact/contact.js
const { get } = require('../../utils/request')

Page({
  data: {
    contactInfo: {
      phone: '111-222-3333',
      wechat: 'NekoCafe_Official',
      email: 'service@nekocafe.com',
      workTime: '10:00 - 22:00'
    },
    storeInfo: null
  },

  onLoad() {
    wx.setNavigationBarTitle({ title: '联系客服' })
    this.loadStoreInfo()
  },

  loadStoreInfo() {
    get('/api/stores').then(res => {
      if (res.code === 0 && res.data && res.data.length > 0) {
        this.setData({ storeInfo: res.data[0] })
      }
    }).catch(() => {})
  },

  makePhoneCall() {
    wx.makePhoneCall({
      phoneNumber: this.data.contactInfo.phone,
      fail: () => {
        wx.showToast({ title: '拨打失败', icon: 'none' })
      }
    })
  },

  copyWechat() {
    wx.setClipboardData({
      data: this.data.contactInfo.wechat,
      success: () => {
        wx.showToast({ title: '已复制微信号', icon: 'success' })
      }
    })
  },

  sendEmail() {
    wx.showToast({ title: '正在打开邮件应用...', icon: 'none' })
  }
})