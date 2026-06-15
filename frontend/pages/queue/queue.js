// pages/queue/queue.js
const { get, post } = require('../../utils/request')
const { getUserLocation, applyDistanceAndSort } = require('../../utils/lbs')

// 桌型选项（存储值 + 显示标签一致，均为中文）
const TABLE_TYPES = ['双人桌', '四人桌', '大桌', '吧台']
// 桌型映射：数据库可能存的各种值 → 统一显示名称
const TYPE_LABEL_MAP = {
  '双人桌': '双人桌', '四人桌': '四人桌', '大桌': '大桌', '吧台': '吧台',
  'window': '靠窗座', '靠窗座': '靠窗座',
  '沙发座': '沙发座', '包厢': '包厢', '包间': '包间',
  '任意': '任意', 'any': '任意',
}

// 轮询间隔（毫秒）
const POLL_INTERVAL = 10000

Page({
  data: {
    storeId: null,
    storeName: '',
    stores: [],            // 所有门店列表（按距离排序）
    showStorePicker: false, // 门店选择弹层

    queue: null,
    loading: true,
    taking: false,
    showTakeForm: false,
    hasTakenNumber: false,       // 是否已取号（本地状态，不受后端 myNumber 影响）
    persons: 2,
    personsRange: [1, 2, 3, 4, 5, 6, 7, 8],
    tableTypes: TABLE_TYPES,
    tableTypeIndex: 0,

    showCalledModal: false,      // 叫号提示弹窗
    calledNumber: null,          // 被叫到的号码
    myQueueId: null,             // 我的排队记录ID（用于确认叫号）
    confirming: false,           // 确认叫号中
    myWaitText: ''              // 等待文案（友好格式）
  },

  pollTimer: null,  // 轮询定时器

  onLoad(options) {
    // 1. 优先从 URL 参数获取门店
    if (options.storeId) {
      const sid = parseInt(options.storeId)
      this.setData({
        storeId: sid,
        storeName: decodeURIComponent(options.storeName || '')
      })
      return this.loadStores()
    }

    // 2. 从用户登录信息获取绑定的门店（最可靠的来源）
    const app = getApp()
    const userInfo = wx.getStorageSync('userInfo') || app.globalData.userInfo || {}
    if (userInfo.storeId != null) {
      this.setData({
        storeId: userInfo.storeId,
        storeName: userInfo.storeName || ''
      })
      return this.loadStores()
    }

    // 3. 兜底：从全局缓存 currentStore 获取
    if (app.globalData.currentStore) {
      this.setData({
        storeId: parseInt(app.globalData.currentStore.id) || app.globalData.currentStore.id,
        storeName: app.globalData.currentStore.name
      })
    }

    this.loadStores()
  },

  onShow() {
    const app = getApp()
    // 从后端数据库获取最新用户信息（含 storeId/storeName）
    app.fetchAndSyncUserInfo().then((userInfo) => {
      // 如果还没有门店，从数据库返回的用户信息获取
      if (!this.data.storeId && userInfo && userInfo.storeId != null) {
        this.setData({
          storeId: userInfo.storeId,
          storeName: userInfo.storeName || ''
        })
      }

      // 刷新排队数据
      if (this.data.storeId) {
        this.loadQueue()
        this.startPolling()
      }
    })
  },

  onHide() {
    // 页面隐藏时停止轮询
    this.stopPolling()
  },

  onUnload() {
    this.stopPolling()
  },

  onPullDownRefresh() {
    this.loadStores()
  },

  // ═══ 轮询 ═══
  startPolling() {
    this.stopPolling()
    this.pollTimer = setInterval(() => {
      if (this.data.storeId) {
        this.loadQueue()
      }
    }, POLL_INTERVAL)
  },

  stopPolling() {
    if (this.pollTimer) {
      clearInterval(this.pollTimer)
      this.pollTimer = null
    }
  },

  // ═══ 门店相关 ═══

  loadStores() {
    get('/api/stores').then(res => {
      if (res.code === 0) {
        const rawStores = res.data
        this.autoSortByLocation(rawStores)
      }
    }).catch(() => {
      this.setData({ loading: false })
    })
  },

  autoSortByLocation(rawStores) {
    getUserLocation().then(userLoc => {
      const sorted = applyDistanceAndSort(rawStores, userLoc)
      this.applySortedStores(sorted)
    }).catch(() => {
      const fallback = applyDistanceAndSort(rawStores, null)
      this.applySortedStores(fallback)
    })
  },

  applySortedStores(sorted) {
    this.setData({ stores: sorted })

    // 如果还没有选中门店，优先用用户绑定的门店，其次用第一个（最近）
    if (!this.data.storeId && sorted.length > 0) {
      const userInfo = wx.getStorageSync('userInfo') || {}
      let defaultStore = sorted[0]
      // 如果用户绑定了门店，在列表中匹配
      if (userInfo.storeId != null) {
        const matched = sorted.find(s => s.id === userInfo.storeId)
        if (matched) defaultStore = matched
      }
      this.setData({
        storeId: defaultStore.id,
        storeName: defaultStore.name
      })
      const app = getApp()
      app.globalData.currentStore = { id: defaultStore.id, name: defaultStore.name }
      this.loadQueue()
    } else if (this.data.storeId) {
      this.loadQueue()
    } else {
      this.setData({ loading: false })
    }
  },

  // 点击门店头部 → 弹出切换面板
  onStoreHeaderTap() {
    this.setData({ showStorePicker: true })
  },

  // 隐藏门店选择器
  hideStorePicker() {
    this.setData({ showStorePicker: false })
  },

  // 切换门店
  onStoreSelect(e) {
    const store = e.currentTarget.dataset.store
    if (store.id === this.data.storeId) {
      this.hideStorePicker()
      return
    }
    this.setData({
      storeId: store.id,
      storeName: store.name,
      hasTakenNumber: false,  // 切换门店后重置取号状态
      showStorePicker: false,
      loading: true
    })
    const app = getApp()
    app.globalData.currentStore = { id: store.id, name: store.name }
    this.loadQueue()
  },

  // ═══ 排队相关 ═══

  loadQueue() {
    if (!this.data.storeId) return
    get(`/api/queue/status?storeId=${this.data.storeId}`).then(res => {
      wx.stopPullDownRefresh()
      if (res.code === 0) {
        const q = res.data
        // 为等待列表统一处理 typeDisplay
        if (q.queueList) {
          q.queueList = q.queueList.map(item => ({
            ...item,
            typeDisplay: TYPE_LABEL_MAP[item.type] || item.type || '—'
          }))
        }
        // 计算友好的等待文案 + 同步 hasTakenNumber 状态
        let myWaitText = ''
        let hasTakenNumber = this.data.hasTakenNumber  // 默认保留当前值
        if (q.myNumber != null && q.myWaitMinutes != null) {
          // 后端返回了 myNumber → 用户已在排队中（可能是刚取的，也可能是之前取的）
          hasTakenNumber = true
          if (q.myWaitMinutes <= 0) {
            myWaitText = '即将叫到您，请留意！'
          } else if (q.myWaitMinutes < 5) {
            myWaitText = `预计还需等待 ${q.myWaitMinutes} 分钟`
          } else {
            myWaitText = `前面还有 ${Math.floor(q.myWaitMinutes / 5)} 桌 · 预计 ${q.myWaitMinutes} 分钟`
          }
        }

        this.setData({ queue: q, loading: false, myWaitText, hasTakenNumber })

        // 保存我的 queueId（用于确认叫号）
        if (q.myNumber != null && q.queueList) {
          const mine = q.queueList.find(item => item.isMine)
          if (mine && mine.queueId) {
            this.setData({ myQueueId: mine.queueId })
          }
        }

        // 检测是否被叫号（CALLED 状态）
        if (q.called) {
          this.stopPolling()
          this.setData({
            hasTakenNumber: false,
            showCalledModal: true,
            calledNumber: q.myNumber,
            myQueueId: q.myQueueId || this.data.myQueueId
          })
        }
      } else {
        // 接口返回非成功码（如未登录），也关闭加载并初始化空 queue
        this.setData({ loading: false, queue: { currentNumber: 0, waitingCount: 0, avgWaitMinutes: 0, queueList: [], calledList: [], missedList: [] } })
      }
    }).catch(() => {
      wx.stopPullDownRefresh()
      // 请求失败也要关闭 loading，允许用户看到取号按钮
      this.setData({ loading: false, queue: { currentNumber: 0, waitingCount: 0, avgWaitMinutes: 0, queueList: [], calledList: [], missedList: [] } })
    })
  },

  openTakeForm() {
    if (!this.data.storeId) {
      wx.showToast({ title: '请先选择门店', icon: 'none' })
      return
    }
    this.setData({ showTakeForm: true })
  },

  closeTakeForm() { this.setData({ showTakeForm: false }) },

  onSelectPersons(e) {
    this.setData({ persons: e.currentTarget.dataset.persons })
  },

  onSelectTableType(e) {
    this.setData({ tableTypeIndex: e.currentTarget.dataset.index })
  },

  onTakeNumber() {
    const { storeId, persons, tableTypes, tableTypeIndex } = this.data
    if (!storeId) {
      wx.showToast({ title: '请先选择门店', icon: 'none' })
      return
    }
    this.setData({ taking: true })
    post('/api/queue/take', {
      storeId: parseInt(storeId) || storeId,
      persons: parseInt(persons) || persons,
      type: tableTypes[tableTypeIndex]
    }).then(res => {
      this.setData({ taking: false, showTakeForm: false })
      if (res.code === 0) {
        this.setData({ hasTakenNumber: true })
        wx.showToast({ title: `取号成功：${res.data.number}号`, icon: 'success' })
        this.loadQueue()
        this.startPolling()
      } else if (res.code === -1) {
        // token 过期或未登录，已由 request.js 跳转登录页
        return
      } else {
        wx.showToast({ title: res.message || '取号失败，请重试', icon: 'none' })
      }
    }).catch(err => {
      console.error('[Queue] 取号失败:', err)
      this.setData({ taking: false })
      wx.showToast({ title: '取号失败，请重试', icon: 'none' })
    })
  },

  // 关闭叫号提示弹窗（用户点"稍后再说"，轮询继续，超时后端会自动 MISSED）
  closeCalledModal() {
    this.setData({ showCalledModal: false, calledNumber: null })
    this.startPolling()
  },

  // 确认叫号 — 调后端接口将 CALLED 改为 KNOWN，不跳预约页
  onConfirmCalled() {
    const { myQueueId } = this.data
    if (!myQueueId) {
      wx.showToast({ title: '排队记录丢失，请刷新', icon: 'none' })
      return
    }
    this.setData({ confirming: true })
    post('/api/queue/confirm', { queueId: myQueueId }).then(() => {
      this.setData({ confirming: false, showCalledModal: false, calledNumber: null })
      wx.showToast({ title: '已知晓，请留意叫号', icon: 'success' })
      // 确认后恢复轮询
      this.startPolling()
    }).catch(err => {
      console.error('[Queue] 确认叫号失败:', err)
      this.setData({ confirming: false })
      wx.showToast({ title: '确认失败，请重试', icon: 'none' })
    })
  }
})
