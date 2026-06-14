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
    loginTab: 'wx',        // 当前 Tab: 'wx' | 'phone'
    loading: false,
    selectedRole: '',
    roles: [
      { id: 'customer',   icon: '🧑‍💼', name: '顾客',     nameEn: 'Customer'   },
      { id: 'staff',      icon: '👨‍🍳', name: '店员',     nameEn: 'Staff'      },
      { id: 'manager',    icon: '🏪',  name: '店长',     nameEn: 'Manager'    },
      { id: 'hq_ops',     icon: '📊',  name: '总部运营', nameEn: 'HQ Ops'     },
      { id: 'cat_keeper', icon: '🐱',  name: '猫咪管家', nameEn: 'Cat Keeper' }
    ],
    // 手机号登录表单
    phone: '',
    password: '',
    // 微信登录用
    nickname: '',
    // 门店数据（微信登录用）
    stores: [],
    storeNames: [],
    selectedStoreId: null,
    selectedStoreIndex: -1,
    canWxLogin: false
  },

  // ── 页面加载：获取门店列表（微信登录用） ──
  onLoad() {
    this.getStores()
  },

  // ── 门店数据 ──
  getStores() {
    const { get } = require('../../utils/request')
    get('/api/stores').then(res => {
      if (res.code === 0 && res.data) {
        const stores = res.data
        const storeNames = stores.map(s => s.name)
        this.setData({ stores, storeNames })
      }
    }).catch(() => {})
  },

  // ── Tab 切换 ──
  switchTab(e) {
    this.setData({ loginTab: e.currentTarget.dataset.tab })
  },

  // ── 微信登录：角色选择 ──
  onSelectRole(e) {
    const role = e.currentTarget.dataset.role
    this.setData({
      selectedRole: role,
      selectedStoreIndex: -1,
      selectedStoreId: null
    })
    this.updateCanWxLogin()
  },

  // ── 微信登录：门店选择 ──
  onStorePick(e) {
    const idx = Number(e.detail.value)
    this.setData({
      selectedStoreIndex: idx,
      selectedStoreId: this.data.stores[idx].id
    })
    this.updateCanWxLogin()
  },

  // ── 微信登录：昵称输入 ──
  onNicknameInput(e) {
    this.setData({ nickname: e.detail.value })
  },

  // ── 计算微信登录按钮是否可用 ──
  updateCanWxLogin() {
    const { selectedRole, selectedStoreIndex } = this.data
    let can = !!selectedRole
    if (selectedRole && selectedRole !== 'customer' && selectedRole !== 'hq_ops') {
      can = can && selectedStoreIndex >= 0
    }
    this.setData({ canWxLogin: can })
  },

  // ── 微信登录 ──
  onMockLogin() {
    if (!this.data.canWxLogin) return

    const { selectedRole, selectedStoreId, nickname } = this.data
    this.setData({ loading: true })

    try {
      if (isUseMock()) {
        const mockUser = { ...ROLE_USERS[selectedRole] }
        // 用自定义昵称覆盖
        if (nickname && nickname.trim()) {
          mockUser.nickName = nickname.trim()
        }
        // 非顾客/非总部运营：用实际选择的门店覆盖
        if (selectedRole !== 'customer' && selectedRole !== 'hq_ops' && selectedStoreId != null) {
          mockUser.storeId = selectedStoreId
        }
        this.finishLogin(mockUser, selectedRole, `mock_token_${selectedRole}_20260604`)
        return
      }

      wx.login({
        success: (loginRes) => {
          try {
            if (!loginRes.code) {
              this.setData({ loading: false })
              wx.showToast({ title: '微信登录失败', icon: 'none' })
              return
            }
            const payload = { code: loginRes.code, roleId: this.roleToId(selectedRole) }
            if (selectedStoreId != null) payload.storeId = selectedStoreId
            if (nickname && nickname.trim()) payload.nickname = nickname.trim()
            post('/api/auth/login', payload).then(res => {
              if (res.code === 0 && res.data) {
                const roleLabelMap = {
                  customer: '顾客',
                  staff: '店员',
                  manager: '店长',
                  hq_ops: '总部运营',
                  cat_keeper: '猫咪管家'
                }
                const userInfo = {
                  ...res.data.userInfo,
                  role: selectedRole,
                  roleLabel: roleLabelMap[selectedRole]
                }
                this.finishLogin(userInfo, selectedRole, res.data.token)
              } else {
                this.setData({ loading: false })
                wx.showToast({ title: res.message || '登录失败', icon: 'none' })
              }
            }).catch(() => {
              this.setData({ loading: false })
              wx.showToast({ title: '网络异常，请稍后重试', icon: 'none' })
            })
          } catch (err) {
            console.error('[wxLogin] success callback error:', err)
            this.setData({ loading: false })
            wx.showToast({ title: '登录异常，请重试', icon: 'none' })
          }
        },
        fail: () => {
          this.setData({ loading: false })
          wx.showToast({ title: '微信登录失败', icon: 'none' })
        }
      })
    } catch (err) {
      console.error('[onMockLogin] error:', err)
      this.setData({ loading: false })
      wx.showToast({ title: '登录异常，请重试', icon: 'none' })
    }
  },

  // ── 手机号登录：表单输入 ──
  onPhoneInput(e) {
    this.setData({ phone: e.detail.value })
  },
  onPasswordInput(e) {
    this.setData({ password: e.detail.value })
  },

  // ── 手机号登录 ──
  onPhoneLogin() {
    const { phone, password } = this.data
    if (!phone || !password) {
      wx.showToast({ title: '请输入手机号和密码', icon: 'none' })
      return
    }
    if (!/^1\d{10}$/.test(phone)) {
      wx.showToast({ title: '手机号格式不正确', icon: 'none' })
      return
    }

    this.setData({ loading: true })

    post('/api/auth/login/phone', { phone, password }).then(res => {
      if (res.code === 0 && res.data) {
        const userInfo = {
          ...res.data.userInfo,
          role: 'customer',
          roleLabel: '顾客'
        }
        this.finishLogin(userInfo, 'customer', res.data.token)
      } else {
        this.setData({ loading: false })
        wx.showToast({ title: res.message || '登录失败', icon: 'none' })
      }
    }).catch(() => {
      this.setData({ loading: false })
      wx.showToast({ title: '网络异常，请稍后重试', icon: 'none' })
    })
  },

  // ── 跳转注册页 ──
  goRegister() {
    wx.navigateTo({ url: '/pages/register/register' })
  },

  // ── 角色 ID 映射（前端字符串 → 数据库 roleId） ──
  roleToId(role) {
    const map = { customer: 1, staff: 2, manager: 3, hq_ops: 4, cat_keeper: 5 }
    return map[role] || 1
  },

  // ── 补全头像 URL（相对路径 → 完整 URL）──
  resolveAvatarUrl(avatarUrl) {
    if (!avatarUrl) return avatarUrl
    if (avatarUrl.startsWith('http://') || avatarUrl.startsWith('https://')) {
      return avatarUrl
    }
    const app = getApp()
    const baseUrl = app.globalData.baseUrl || 'http://127.0.0.1:8081'
    return baseUrl + avatarUrl
  },

  // ── 登录完成：存储信息 + 跳转 ──
  finishLogin(userInfo, role, token) {
    // 补全头像路径为完整 URL
    if (userInfo.avatarUrl) {
      userInfo.avatarUrl = this.resolveAvatarUrl(userInfo.avatarUrl)
    }
    wx.setStorageSync('token', token)
    wx.setStorageSync('userInfo', userInfo)
    wx.setStorageSync('userRole', role)

    const app = getApp()
    app.globalData.userInfo = userInfo
    app.globalData.userRole = role
    app.globalData.cartItems = []
    app.globalData.currentStore = null
    app.globalData.selectedTable = null

    this.setData({ loading: false })
    // 所有角色统一跳转到首页
    wx.switchTab({ url: '/pages/index/index' })
  }
})
