// pages/contact/contact.js
Page({
  data: {
    contactInfo: {
      phone: '111-222-3333',
      wechat: 'NekoCafe_Official',
      email: 'service@nekocafe.com',
      workTime: '10:00 - 22:00'
    }
  },

  onLoad() {
    wx.setNavigationBarTitle({ title: '联系客服' })
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