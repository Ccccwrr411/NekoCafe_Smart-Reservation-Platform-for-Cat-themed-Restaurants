// pages/login/login.js
const { post, isUseMock } = require('../../utils/request')

// 每种角色的模拟用户数据
const ROLE_USERS = {
  customer: {
    id: 1001,
    nickName: '猫咖爱好者',
    avatarUrl: 'https://placehold.co/200x200/C97E5A/white?text=Avatar',
    role: 'customer',
    roleLabel: '顾客',
    memberLevel: '银卡会员',
    memberLevelIcon: '🥈',
    points: 320,
    pointsToNext: 680,
    nextLevel: '金卡会员',
    totalOrders: 12,
    totalSpent: 896,
    couponCount: 3
  },
  staff: {
    id: 2001,
    nickName: '李小明',
    avatarUrl: 'https://placehold.co/200x200/C97E5A/white?text=Avatar',
    role: 'staff',
    roleLabel: '店员',
    storeId: 1,
    storeName: 'NekoCafé 朝阳店'
  },
  manager: {
    id: 2002,
    nickName: '王店长',
    avatarUrl: 'https://placehold.co/200x200/C97E5A/white?text=Avatar',
    role: 'manager',
    roleLabel: '店长',
    storeId: 1,
    storeName: 'NekoCafé 朝阳店'
  },
  hq_ops: {
    id: 3001,
    nickName: '总部运营',
    avatarUrl: 'https://placehold.co/200x200/C97E5A/white?text=Avatar',
    role: 'hq_ops',
    roleLabel: '总部运营',
    storeIds: [1, 2, 3, 4, 5]
  },
  cat_keeper: {
    id: 2003,
    nickName: '猫咪管家陈云',
    avatarUrl: 'https://placehold.co/200x200/C97E5A/white?text=Avatar',
    role: 'cat_keeper',
    roleLabel: '猫咪管家',
    storeId: 1,
    storeName: 'NekoCafé 朝阳店'
  }
}

Page({
  data: {
    loading: false,
    selectedRole: '',
    roles: [
      { id: 'customer',   icon: '🧑‍💼', name: '顾客',     nameEn: 'Customer'   },
      { id: 'staff',      icon: '👨‍🍳', name: '店员',     nameEn: 'Staff'      },
      { id: 'manager',    icon: '🏪',  name: '店长',     nameEn: 'Manager'    },
      { id: 'hq_ops',     icon: '📊',  name: '总部运营', nameEn: 'HQ Ops'     },
      { id: 'cat_keeper', icon: '🐱',  name: '猫咪管家', nameEn: 'Cat Keeper' }
    ]
  },

  onSelectRole(e) {
    this.setData({ selectedRole: e.currentTarget.dataset.role })
  },

  onMockLogin() {
    const role = this.data.selectedRole
    if (!role) return

    this.setData({ loading: true })

    if (isUseMock()) {
      this.finishLogin(ROLE_USERS[role], role, `mock_token_${role}_20260604`)
      return
    }

    // 对接微信沙箱后端：wx.login 换取真实 token
    wx.login({
      success: (loginRes) => {
        if (!loginRes.code) {
          this.setData({ loading: false })
          wx.showToast({ title: '微信登录失败', icon: 'none' })
          return
        }
        post('/api/auth/login', { code: loginRes.code, role }).then(res => {
          if (res.code === 0 && res.data) {
            const userInfo = { ...res.data.userInfo, role, roleLabel: ROLE_USERS[role].roleLabel }
            this.finishLogin(userInfo, role, res.data.token)
          } else {
            this.setData({ loading: false })
            wx.showToast({ title: res.message || '登录失败', icon: 'none' })
          }
        }).catch(() => {
          this.setData({ loading: false })
          wx.showToast({ title: '网络异常，请稍后重试', icon: 'none' })
        })
      },
      fail: () => {
        this.setData({ loading: false })
        wx.showToast({ title: '微信登录失败', icon: 'none' })
      }
    })
  },

  finishLogin(userInfo, role, token) {
    wx.setStorageSync('token', token)
    wx.setStorageSync('userInfo', userInfo)
    wx.setStorageSync('userRole', role)

    const app = getApp()
    app.globalData.userInfo = userInfo
    app.globalData.userRole = role
    app.globalData.cartItems = []
    app.globalData.currentStore = null
    app.globalData.selectedTable = null

    const routeMap = {
      customer:   '/pages/index/index',
      staff:      '/pages/staff/staff',
      manager:    '/pages/dashboard/dashboard',
      hq_ops:     '/pages/dashboard/dashboard',
      cat_keeper: '/pages/cats/cats'
    }
    const targetUrl = routeMap[role]
    const tabBarPages = ['/pages/index/index', '/pages/reservation/reservation', '/pages/menu/menu', '/pages/profile/profile']

    this.setData({ loading: false })
    if (tabBarPages.includes(targetUrl)) {
      wx.switchTab({ url: targetUrl })
    } else {
      wx.reLaunch({ url: targetUrl })
    }
  }
})
