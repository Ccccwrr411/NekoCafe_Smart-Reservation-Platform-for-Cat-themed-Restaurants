// pages/login/login.js

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
    setTimeout(() => {
      const userInfo = ROLE_USERS[role]
      wx.setStorageSync('token', `mock_token_${role}_20260604`)
      wx.setStorageSync('userInfo', userInfo)
      wx.setStorageSync('userRole', role)

      // 同步更新 app.globalData（关键：onLaunch 只执行一次，reLaunch 不会重新触发）
      const app = getApp()
      app.globalData.userInfo = userInfo
      app.globalData.userRole = role
      app.globalData.cartItems = []
      app.globalData.currentStore = null
      app.globalData.selectedTable = null

      // 按角色跳转不同首页
      const routeMap = {
        customer:   '/pages/index/index',
        staff:      '/pages/staff/staff',
        manager:    '/pages/dashboard/dashboard',
        hq_ops:     '/pages/dashboard/dashboard',
        cat_keeper: '/pages/cats/cats'
      }
      const targetUrl = routeMap[role]

      // TabBar 页面用 switchTab，普通页面用 reLaunch
      const tabBarPages = ['/pages/index/index', '/pages/reservation/reservation', '/pages/menu/menu', '/pages/profile/profile']
      if (tabBarPages.includes(targetUrl)) {
        wx.switchTab({ url: targetUrl })
      } else {
        wx.reLaunch({ url: targetUrl })
      }
    }, 600)
  }
})
