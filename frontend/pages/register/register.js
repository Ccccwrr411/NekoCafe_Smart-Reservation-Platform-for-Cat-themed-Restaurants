// pages/register/register.js
const { get, post } = require('../../utils/request')

Page({
  data: {
    nickname: '',
    phone: '',
    code: '',
    sandboxCode: '',
    email: '',
    password: '',
    confirmPassword: '',
    loading: false,
    countdown: 0,
    canSubmit: false,
    errors: {},       // 各字段行内错误提示
    selectedRole: '5', // 默认选中顾客（roleId=5）
    roles: [
      { id: '5', icon: '🧑‍💼', name: '顾客',     nameEn: 'Customer'   },
      { id: '3', icon: '👨‍🍳', name: '店员',     nameEn: 'Staff'      },
      { id: '2', icon: '🏪',  name: '店长',     nameEn: 'Manager'    },
      { id: '1', icon: '📊',  name: '总部运营', nameEn: 'HQ Ops'     },
      { id: '4', icon: '🐱',  name: '猫咪管家', nameEn: 'Cat Keeper' }
    ],
    // 门店选择
    stores: [],           // 门店列表
    selectedStoreId: null,    // 选中的门店ID
    selectedStoreIndex: -1    // 选中的门店在数组中的下标
  },

  // ── 生命周期 ──
  onLoad(options) {
    // 如果从登录页跳转过来，自动填充手机号
    if (options && options.phone) {
      this.setData({ phone: options.phone })
    }
    // 获取门店列表（非顾客注册时需要选择门店）
    get('/api/stores').then(res => {
      if (res.code === 0 && res.data) {
        this.setData({ stores: res.data })
      }
    }).catch(() => {
      console.log('获取门店列表失败')
    })
  },

  // ── 角色选择 ──
  onSelectRole(e) {
    const roleId = e.currentTarget.dataset.role
    this.setData({ 
      selectedRole: roleId,
      // 切换角色时重置门店选择
      selectedStoreId: null,
      'errors.store': ''
    })
  },

  // ── 门店选择 ──
  onStorePick(e) {
    const index = parseInt(e.detail.value)
    if (index < 0 || index >= this.data.stores.length) return
    this.setData({
      selectedStoreIndex: index,
      selectedStoreId: this.data.stores[index].id,
      'errors.store': ''
    })
  },

  // ── 表单输入（输入时清除该字段的错误提示） ──
  onNicknameInput(e) {
    this.setData({ nickname: e.detail.value, 'errors.nickname': '' }, this.checkForm)
  },
  onPhoneInput(e) {
    this.setData({ phone: e.detail.value, 'errors.phone': '' }, this.checkForm)
  },
  onCodeInput(e) {
    this.setData({ code: e.detail.value, 'errors.code': '' }, this.checkForm)
  },
  onEmailInput(e) {
    this.setData({ email: e.detail.value, 'errors.email': '' }, this.checkForm)
  },
  onPasswordInput(e) {
    const password = e.detail.value
    const errors = { ...this.data.errors, password: '' }
    // 实时提示：输入了但不足6位
    if (password.length > 0 && password.length < 6) {
      errors.password = '密码至少6位'
    }
    // 如果确认密码已填，输入密码时同步检查一致性
    if (this.data.confirmPassword && password !== this.data.confirmPassword) {
      errors.confirmPassword = '两次密码不一致'
    } else if (this.data.confirmPassword && password === this.data.confirmPassword) {
      errors.confirmPassword = ''
    }
    this.setData({ password, errors }, this.checkForm)
  },
  onConfirmPasswordInput(e) {
    const confirmPassword = e.detail.value
    const errors = { ...this.data.errors, confirmPassword: '' }
    // 输入确认密码时同步检查是否一致
    if (confirmPassword && this.data.password && confirmPassword !== this.data.password) {
      errors.confirmPassword = '两次密码不一致'
    } else if (confirmPassword && this.data.password && confirmPassword === this.data.password) {
      errors.confirmPassword = ''
    }
    this.setData({ confirmPassword, errors }, this.checkForm)
  },

  // ── 校验表单是否可提交（只需非空即可点击，详细校验在 onRegister 里做） ──
  checkForm() {
    const { nickname, phone, code, password, confirmPassword } = this.data
    const canSubmit = nickname.trim().length > 0
      && phone.length > 0
      && code.length > 0
      && password.length > 0
      && confirmPassword.length > 0
    this.setData({ canSubmit })
  },
  // 供页面加载时初始化检查
  onReady() {
    if (this.data.phone) {
      this.checkForm()
    }
  },

  // ── 发送验证码 ──
  onSendCode() {
    const { phone, countdown } = this.data
    if (countdown > 0) return
    if (!/^1\d{10}$/.test(phone)) {
      this.setData({ 'errors.phone': '请输入正确的11位手机号' })
      return
    }

    post('/api/auth/send-code', { phone }).then(res => {
      if (res.code === 0 && res.data) {
        const code = res.data.code
        this.setData({ sandboxCode: code })

        wx.showModal({
          title: '验证码（沙箱模式）',
          content: '您的验证码是：' + code + '\n（也可在输入框下方查看）',
          showCancel: false,
          confirmText: '知道了'
        })

        this.startCountdown()
      } else {
        wx.showToast({ title: res.message || '发送失败', icon: 'none' })
      }
    }).catch(() => {
      wx.showToast({ title: '网络异常', icon: 'none' })
    })
  },

  startCountdown() {
    this.setData({ countdown: 60 })
    this._timer = setInterval(() => {
      if (this.data.countdown <= 1) {
        clearInterval(this._timer)
        this.setData({ countdown: 0 })
      } else {
        this.setData({ countdown: this.data.countdown - 1 })
      }
    }, 1000)
  },

  onUnload() {
    if (this._timer) clearInterval(this._timer)
  },

  // ── 提交注册（点击时校验，错误显示在对应输入框下方） ──
  onRegister() {
    const { selectedRole, nickname, phone, code, email, password, confirmPassword } = this.data
    const errors = {}

    // 逐字段校验
    if (!nickname.trim()) {
      errors.nickname = '请输入昵称'
    }
    if (!/^1\d{10}$/.test(phone)) {
      errors.phone = '请输入正确的11位手机号'
    }
    if (!code) {
      errors.code = '请输入验证码'
    }
    if (password.length < 6) {
      errors.password = '密码至少6位'
    }
    if (!confirmPassword) {
      errors.confirmPassword = '请再次输入密码'
    } else if (password !== confirmPassword) {
      errors.confirmPassword = '两次密码不一致'
    }
    if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      errors.email = '邮箱格式不正确'
    }
    // 门店校验：非顾客(5)且非总部运营(1)必须选择门店
    const roleId = parseInt(selectedRole)
    if (roleId !== 5 && roleId !== 1 && !this.data.selectedStoreId) {
      errors.store = '请选择所属门店'
    }

    // 有错误 → 显示行内提示，不提交
    if (Object.keys(errors).length > 0) {
      this.setData({ errors })
      return
    }

    this.setData({ loading: true, errors: {} })

    // 组装请求体，包含角色和门店信息
    const payload = {
      phone, code, password,
      nickname: nickname.trim(),
      email: email.trim() || null,
      roleId: parseInt(selectedRole)   // 角色ID传给后端写入 user_roles
    }
    // 非总部运营(1)且非顾客(5)的角色，传门店ID
    if (roleId !== 1 && roleId !== 5 && this.data.selectedStoreId) {
      payload.storeId = this.data.selectedStoreId
    }

    post('/api/auth/register', payload).then(res => {
      this.setData({ loading: false })
      if (res.code === 0 && res.data) {
        // 根据 roleId 映射角色名（数据库: 1=超管, 2=店长, 3=店员, 4=兽医, 5=顾客）
        const roleMap = {
          1: { role: 'hq_ops', label: '总部运营' },
          2: { role: 'manager', label: '店长' },
          3: { role: 'staff', label: '店员' },
          4: { role: 'cat_keeper', label: '猫咪管家' },
          5: { role: 'customer', label: '顾客' }
        }
        const roleInfo = roleMap[selectedRole] || roleMap[1]
        const userInfo = {
          ...res.data.userInfo,
          role: roleInfo.role,
          roleLabel: roleInfo.label
        }
        wx.setStorageSync('token', res.data.token)
        wx.setStorageSync('userInfo', userInfo)
        wx.setStorageSync('userRole', roleInfo.role)

        const app = getApp()
        app.globalData.userInfo = userInfo
        app.globalData.userRole = roleInfo.role

        wx.showToast({ title: '注册成功', icon: 'success' })
        setTimeout(() => {
          // 所有角色统一跳转到首页
          wx.switchTab({ url: '/pages/index/index' })
        }, 1200)
      } else {
        // 后端返回的错误提示（验证码错误、手机号已注册等）→ 用 toast
        wx.showToast({ title: res.message || '注册失败', icon: 'none' })
      }
    }).catch(() => {
      this.setData({ loading: false })
      wx.showToast({ title: '网络异常，请稍后重试', icon: 'none' })
    })
  },

  goLogin() {
    wx.navigateBack()
  }
})
