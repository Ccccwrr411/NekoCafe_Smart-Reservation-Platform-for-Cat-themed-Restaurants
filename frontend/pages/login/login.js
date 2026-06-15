// pages/login/login.js
const { post, isUseMock } = require('../../utils/request')

// 每种角色的模拟用户数据（仅 mock 模式使用，storeId/storeName 应从后端动态获取）
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
    roleLabel: '店员'
  },
  manager: {
    id: 2002,
    nickName: '王店长',
    avatarUrl: 'https://placehold.co/200x200/C97E5A/white?text=Avatar',
    role: 'manager',
    roleLabel: '店长'
  },
  hq_ops: {
    id: 3001,
    nickName: '总部运营',
    avatarUrl: 'https://placehold.co/200x200/C97E5A/white?text=Avatar',
    role: 'hq_ops',
    roleLabel: '总部运营'
  },
  cat_keeper: {
    id: 2003,
    nickName: '猫咪管家陈云',
    avatarUrl: 'https://placehold.co/200x200/C97E5A/white?text=Avatar',
    role: 'cat_keeper',
    roleLabel: '猫咪管家'
  }
}

Page({
  data: {
    loginTab: 'wx',        // 当前 Tab: 'wx' | 'phone'
    loading: false,
    wechatLoading: false,  // 微信快捷登录 loading
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
    wxPhone: '',
    wxCode: '',
    wxCountdown: 0,
    wxSandboxCode: '',
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

  onUnload() {
    if (this._wxTimer) clearInterval(this._wxTimer)
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

  // ── 微信登录：手机号输入 ──
  onWxPhoneInput(e) {
    this.setData({ wxPhone: e.detail.value }, this.updateCanWxLogin)
  },

  // ── 微信登录：验证码输入 ──
  onWxCodeInput(e) {
    this.setData({ wxCode: e.detail.value }, this.updateCanWxLogin)
  },

  // ── 微信登录：发送验证码 ──
  onWxSendCode() {
    const { wxPhone, wxCountdown } = this.data
    if (wxCountdown > 0) return
    if (!/^1\d{10}$/.test(wxPhone)) {
      wx.showToast({ title: '请输入正确的11位手机号', icon: 'none' })
      return
    }
    post('/api/auth/send-code', { phone: wxPhone }).then(res => {
      if (res.code === 0 && res.data) {
        const code = res.data.code
        this.setData({ wxSandboxCode: code })
        wx.showModal({
          title: '验证码（沙箱模式）',
          content: '您的验证码是：' + code,
          showCancel: false,
          confirmText: '知道了'
        })
        this.startWxCountdown()
      } else {
        wx.showToast({ title: res.message || '发送失败', icon: 'none' })
      }
    }).catch(() => {
      wx.showToast({ title: '网络异常', icon: 'none' })
    })
  },

  startWxCountdown() {
    this.setData({ wxCountdown: 60 })
    this._wxTimer = setInterval(() => {
      if (this.data.wxCountdown <= 1) {
        clearInterval(this._wxTimer)
        this.setData({ wxCountdown: 0 })
      } else {
        this.setData({ wxCountdown: this.data.wxCountdown - 1 })
      }
    }, 1000)
  },

  // ── 微信快捷登录 ──
  onWechatQuickLogin() {
    const that = this
    this.setData({ wechatLoading: true })

    // 先调用 wx.login 获取 code
    wx.login({
      success: (loginRes) => {
        if (!loginRes.code) {
          that.setData({ wechatLoading: false })
          wx.showToast({ title: '获取微信授权失败', icon: 'none' })
          return
        }

        // 用微信 code 尝试登录（后端用 code 查 openid）
        post('/api/auth/wx-login', { code: loginRes.code }).then(res => {
          that.setData({ wechatLoading: false })
          if (res.code === 0 && res.data) {
            // 登录成功（后端返回的 roleId → 前端角色标识）
            // 数据库: 1=超级管理员, 2=店长, 3=店员, 4=兽医, 5=普通顾客
            const roleMap = {
              1: { role: 'hq_ops', label: '总部运营' },
              2: { role: 'manager', label: '店长' },
              3: { role: 'staff', label: '店员' },
              4: { role: 'cat_keeper', label: '猫咪管家' },
              5: { role: 'customer', label: '顾客' }
            }
            const roleId = res.data.userInfo.roleId || 1
            const roleInfo = roleMap[roleId] || roleMap[1]
            const userInfo = {
              ...res.data.userInfo,
              role: roleInfo.role,
              roleLabel: roleInfo.label
            }
            that.finishLogin(userInfo, roleInfo.role, res.data.token)
          } else if (res.code === 404 || (res.message && res.message.includes('未注册'))) {
            // 未注册 → 跳转注册页
            wx.showModal({
              title: '提示',
              content: '该微信账号尚未绑定，请先注册',
              confirmText: '去注册',
              cancelText: '取消',
              success: (modalRes) => {
                if (modalRes.confirm) {
                  wx.navigateTo({ url: '/pages/register/register' })
                }
              }
            })
          } else {
            wx.showToast({ title: res.message || '登录失败', icon: 'none' })
          }
        }).catch(() => {
          that.setData({ wechatLoading: false })
          wx.showToast({ title: '网络异常，请稍后重试', icon: 'none' })
        })
      },
      fail: () => {
        that.setData({ wechatLoading: false })
        wx.showToast({ title: '微信登录失败，请使用手机号登录', icon: 'none' })
      }
    })
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

  // ── 计算快捷登录按钮是否可用 ──
  updateCanWxLogin() {
    const { selectedRole, wxPhone, wxCode } = this.data
    const can = !!selectedRole && wxPhone.length > 0 && wxCode.length > 0
    this.setData({ canWxLogin: can })
  },

  // ── 微信登录（手机号+验证码） ──
  onWxLogin() {
    if (!this.data.canWxLogin) return

    const { selectedRole, selectedStoreId, wxPhone, wxCode } = this.data
    this.setData({ loading: true })

    try {
      if (isUseMock()) {
        const mockUser = { ...ROLE_USERS[selectedRole] }
        this.finishLogin(mockUser, selectedRole, `mock_token_${selectedRole}_20260604`)
        return
      }

      // 真实请求：手机号+验证码 → 后端用手机号查用户
      wx.login({
        success: (loginRes) => {
          try {
            const payload = {
              phone: wxPhone,
              smsCode: wxCode,
              roleId: this.roleToId(selectedRole)
            }
            // 附带微信 code 仅作记录
            if (loginRes.code) payload.code = loginRes.code
            if (selectedStoreId != null) payload.storeId = selectedStoreId

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
              } else if (res.message && res.message.includes('未注册')) {
                // 未注册 → 弹窗确认后跳转注册页，携带手机号
                this.setData({ loading: false })
                wx.showModal({
                  title: '未注册',
                  content: '该手机号尚未注册，是否前往注册？',
                  confirmText: '去注册',
                  cancelText: '取消',
                  success: (modalRes) => {
                    if (modalRes.confirm) {
                      wx.navigateTo({ url: '/pages/register/register?phone=' + wxPhone })
                    }
                  }
                })
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
          // wx.login 失败时，仍然可以用手机号+验证码登录（不传 code）
          const payload = {
            phone: wxPhone,
            smsCode: wxCode,
            roleId: this.roleToId(selectedRole)
          }
          if (selectedStoreId != null) payload.storeId = selectedStoreId

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
            } else if (res.message && res.message.includes('未注册')) {
              this.setData({ loading: false })
              wx.showModal({
                title: '未注册',
                content: '该手机号尚未注册，是否前往注册？',
                confirmText: '去注册',
                cancelText: '取消',
                success: (modalRes) => {
                  if (modalRes.confirm) {
                    wx.navigateTo({ url: '/pages/register/register?phone=' + wxPhone })
                  }
                }
              })
            } else {
              this.setData({ loading: false })
              wx.showToast({ title: res.message || '登录失败', icon: 'none' })
            }
          }).catch(() => {
            this.setData({ loading: false })
            wx.showToast({ title: '网络异常，请稍后重试', icon: 'none' })
          })
        }
      })
    } catch (err) {
      console.error('[onWxLogin] error:', err)
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
    const { phone, password, selectedRole, selectedStoreId } = this.data
    if (!phone || !password) {
      wx.showToast({ title: '请输入手机号和密码', icon: 'none' })
      return
    }
    if (!selectedRole) {
      wx.showToast({ title: '请选择身份', icon: 'none' })
      return
    }
    if (!/^1\d{10}$/.test(phone)) {
      wx.showToast({ title: '手机号格式不正确', icon: 'none' })
      return
    }

    this.setData({ loading: true })

    const payload = {
      phone,
      password,
      roleId: this.roleToId(selectedRole)
    }
    if (selectedStoreId != null) payload.storeId = selectedStoreId

    post('/api/auth/login/phone', payload).then(res => {
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
  },

  // ── 跳转注册页 ──
  goRegister() {
    wx.navigateTo({ url: '/pages/register/register' })
  },

  // ── 角色 ID 映射（前端字符串 → 数据库 roleId） ──
  // 数据库: 1=超级管理员, 2=店长, 3=店员, 4=兽医, 5=普通顾客
  roleToId(role) {
    const map = { customer: 5, staff: 3, manager: 2, hq_ops: 1, cat_keeper: 4 }
    return map[role] || 5
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


    const routeMap = {
      customer:   '/pages/index/index',
      staff:      '/pages/staff/staff',
      manager:    '/pages/staffDashboard/staffDashboard',
      hq_ops:     '/pages/staffDashboard/staffDashboard',
      cat_keeper: '/pages/cats/cats'
    }
    const targetUrl = routeMap[role] || '/pages/index/index'
    const tabBarPages = ['/pages/index/index', '/pages/reservation/reservation', '/pages/menu/menu', '/pages/profile/profile']

    // 根据目标页面类型选择跳转方式
    if (tabBarPages.includes(targetUrl)) {
      wx.switchTab({ url: targetUrl })
    } else {
      wx.redirectTo({ url: targetUrl })
    }
  }
})
