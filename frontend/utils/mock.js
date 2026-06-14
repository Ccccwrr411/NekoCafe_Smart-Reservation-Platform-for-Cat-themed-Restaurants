// utils/mock.js
// ============================================================
// NekoCafé 所有假数据集中在这里
// 等后端接口就绪后，只需把 request.js 的 USE_MOCK 改成 false
// ============================================================

module.exports = {

  // ─────────────────────────────────────────────
  // 门店列表  GET /api/stores
  // ─────────────────────────────────────────────
  '/api/stores': {
    code: 0,
    message: 'success',
    data: [
      {
        id: 1,
        name: 'NekoCafé 朝阳店',
        address: '朝阳区三里屯太古里南区 B1-01',
        distance: 1.2,
        lat: 39.9325,
        lng: 116.4551,
        avgPrice: 68,
        rating: 4.8,
        catCount: 12,
        imageUrl: 'https://placehold.co/400x300/C97E5A/white?text=NekoCafe+Store+1',
        tags: ['环境好', '猫咪多', '适合拍照'],
        openTime: '10:00 - 22:00',
        status: 'open'
      },
      {
        id: 2,
        name: 'NekoCafé 海淀店',
        address: '海淀区中关村大街 27号 2F',
        distance: 3.5,
        lat: 39.9836,
        lng: 116.3051,
        avgPrice: 72,
        rating: 4.6,
        catCount: 8,
        imageUrl: 'https://placehold.co/400x300/C97E5A/white?text=NekoCafe+Store+2',
        tags: ['安静', '适合学习', '猫咪温顺'],
        openTime: '09:00 - 21:30',
        status: 'open'
      },
      {
        id: 3,
        name: 'NekoCafé 通州店',
        address: '通州区运河广场 3号楼 101',
        distance: 8.0,
        lat: 39.9021,
        lng: 116.6573,
        avgPrice: 65,
        rating: 4.5,
        catCount: 10,
        imageUrl: 'https://placehold.co/400x300/C97E5A/white?text=NekoCafe+Store+3',
        tags: ['宽敞', '停车方便', '周末推荐'],
        openTime: '10:00 - 22:00',
        status: 'open'
      },
      {
        id: 4,
        name: 'NekoCafé 西城店',
        address: '西城区西单北大街 120号 3F',
        distance: 5.1,
        lat: 39.9132,
        lng: 116.3736,
        avgPrice: 75,
        rating: 4.9,
        catCount: 15,
        imageUrl: 'https://placehold.co/400x300/C97E5A/white?text=NekoCafe+Store+4',
        tags: ['旗舰店', '猫品种多', '网红打卡'],
        openTime: '10:00 - 22:30',
        status: 'open'
      },
      {
        id: 5,
        name: 'NekoCafé 丰台店',
        address: '丰台区南四环西路 128号',
        distance: 12.3,
        lat: 39.8273,
        lng: 116.2869,
        avgPrice: 60,
        rating: 4.3,
        catCount: 6,
        imageUrl: 'https://placehold.co/400x300/C97E5A/white?text=NekoCafe+Store+5',
        tags: ['价格实惠', '新店'],
        openTime: '11:00 - 21:00',
        status: 'closed'
      }
    ]
  },

  // ─────────────────────────────────────────────
  // 桌位列表  GET /api/tables?storeId=1
  // ─────────────────────────────────────────────
  '/api/tables': {
    code: 0,
    message: 'success',
    data: [
      { id: 101, name: 'A1', type: '双人桌', capacity: 2, status: 'available', catType: '布偶猫', catName: '奶油', price: 0 },
      { id: 102, name: 'A2', type: '双人桌', capacity: 2, status: 'booked',    catType: '英短',   catName: '豆腐', price: 0 },
      { id: 103, name: 'A3', type: '双人桌', capacity: 2, status: 'available', catType: '橘猫',   catName: '胖橘', price: 0 },
      { id: 104, name: 'B1', type: '四人桌', capacity: 4, status: 'available', catType: '缅因猫', catName: '大毛', price: 0 },
      { id: 105, name: 'B2', type: '四人桌', capacity: 4, status: 'booked',    catType: '暹罗猫', catName: '小蓝', price: 0 },
      { id: 106, name: 'B3', type: '四人桌', capacity: 4, status: 'available', catType: '美短',   catName: '斑斑', price: 0 },
      { id: 107, name: 'C1', type: '包间',   capacity: 6, status: 'available', catType: 'VIP猫区', catName: '多只', price: 30 },
      { id: 108, name: 'C2', type: '包间',   capacity: 6, status: 'booked',    catType: 'VIP猫区', catName: '多只', price: 30 },
      { id: 109, name: 'D1', type: '吧台位', capacity: 1, status: 'available', catType: '无固定猫', catName: '-', price: 0 },
      { id: 110, name: 'D2', type: '吧台位', capacity: 1, status: 'available', catType: '无固定猫', catName: '-', price: 0 },
      { id: 111, name: 'D3', type: '吧台位', capacity: 1, status: 'maintenance', catType: '-', catName: '-', price: 0 }
    ]
  },

  // ─────────────────────────────────────────────
  // 菜品列表  GET /api/menu?storeId=1
  // ─────────────────────────────────────────────
  '/api/menu': {
    code: 0,
    message: 'success',
    data: {
      categories: [
        { id: 1, name: '招牌饮品', icon: '☕' },
        { id: 2, name: '特调咖啡', icon: '🍵' },
        { id: 3, name: '甜品蛋糕', icon: '🎂' },
        { id: 4, name: '轻食正餐', icon: '🍽' },
        { id: 5, name: '猫主题特饮', icon: '🐱' }
      ],
      items: [
        // 招牌饮品
        { id: 201, categoryId: 1, name: '猫爪拿铁', price: 38, imageUrl: 'https://placehold.co/400x300/C97E5A/white?text=Menu+201', desc: '布偶猫爪造型，每天限量30杯', sales: 328, rating: 4.9, isHot: true, isNew: false },
        { id: 202, categoryId: 1, name: '厚乳燕麦拿铁', price: 32, imageUrl: 'https://placehold.co/400x300/C97E5A/white?text=Menu+202', desc: '澳洲进口燕麦奶，丝滑顺口', sales: 156, rating: 4.7, isHot: true, isNew: false },
        { id: 203, categoryId: 1, name: '抹茶红豆拿铁', price: 35, imageUrl: 'https://placehold.co/400x300/C97E5A/white?text=Menu+203', desc: '宇治抹茶粉，香气浓郁', sales: 98, rating: 4.6, isHot: false, isNew: false },
        { id: 204, categoryId: 1, name: '生椰拿铁', price: 33, imageUrl: 'https://placehold.co/400x300/C97E5A/white?text=Menu+204', desc: '鲜榨椰汁，清爽不腻', sales: 210, rating: 4.8, isHot: true, isNew: false },
        // 特调咖啡
        { id: 205, categoryId: 2, name: '橘猫美式', price: 28, imageUrl: 'https://placehold.co/400x300/C97E5A/white?text=Menu+205', desc: '双份浓缩，橘猫拉花', sales: 88, rating: 4.5, isHot: false, isNew: false },
        { id: 206, categoryId: 2, name: '布偶卡布奇诺', price: 36, imageUrl: 'https://placehold.co/400x300/C97E5A/white?text=Menu+206', desc: '奶泡厚实，布偶造型艺术', sales: 145, rating: 4.8, isHot: false, isNew: true },
        { id: 207, categoryId: 2, name: '焦糖玛奇朵', price: 34, imageUrl: 'https://placehold.co/400x300/C97E5A/white?text=Menu+207', desc: '手工焦糖酱，香甜平衡', sales: 67, rating: 4.4, isHot: false, isNew: false },
        // 甜品蛋糕
        { id: 208, categoryId: 3, name: '猫脸芝士蛋糕', price: 42, imageUrl: 'https://placehold.co/400x300/C97E5A/white?text=Menu+208', desc: '北海道芝士，猫脸造型', sales: 203, rating: 4.9, isHot: true, isNew: false },
        { id: 209, categoryId: 3, name: '抹茶生巧', price: 38, imageUrl: 'https://placehold.co/400x300/C97E5A/white?text=Menu+209', desc: '日式抹茶生巧克力，入口即化', sales: 119, rating: 4.7, isHot: false, isNew: false },
        { id: 210, categoryId: 3, name: '舒芙蕾华夫饼', price: 45, imageUrl: 'https://placehold.co/400x300/C97E5A/white?text=Menu+210', desc: '现烤舒芙蕾，5分钟等待', sales: 76, rating: 4.6, isHot: false, isNew: true },
        // 轻食正餐
        { id: 211, categoryId: 4, name: '鸡腿蘑菇焗饭', price: 52, imageUrl: 'https://placehold.co/400x300/C97E5A/white?text=Menu+211', desc: '芝士拉丝，鸡腿嫩滑', sales: 134, rating: 4.7, isHot: true, isNew: false },
        { id: 212, categoryId: 4, name: '三文鱼牛油果沙拉', price: 48, imageUrl: 'https://placehold.co/400x300/C97E5A/white?text=Menu+212', desc: '健康轻食，挪威三文鱼', sales: 89, rating: 4.5, isHot: false, isNew: false },
        { id: 213, categoryId: 4, name: '法式火腿三明治', price: 38, imageUrl: 'https://placehold.co/400x300/C97E5A/white?text=Menu+213', desc: '现烤法棍，丹麦火腿', sales: 56, rating: 4.4, isHot: false, isNew: false },
        // 猫主题特饮
        { id: 214, categoryId: 5, name: '小猫奶冻摇摇茶', price: 29, imageUrl: 'https://placehold.co/400x300/C97E5A/white?text=Menu+214', desc: '猫形奶冻，边摇边喝', sales: 187, rating: 4.8, isHot: true, isNew: false },
        { id: 215, categoryId: 5, name: '猫猫气泡水', price: 22, imageUrl: 'https://placehold.co/400x300/C97E5A/white?text=Menu+215', desc: '天然果汁气泡，猫耳造型吸管', sales: 245, rating: 4.7, isHot: false, isNew: false },
        { id: 216, categoryId: 5, name: '缅因猫草莓冰沙', price: 35, imageUrl: 'https://placehold.co/400x300/C97E5A/white?text=Menu+216', desc: '新鲜草莓，清爽夏日款', sales: 92, rating: 4.6, isHot: false, isNew: true }
      ]
    }
  },

  // ─────────────────────────────────────────────
  // 订单列表  GET /api/orders?userId=1001
  // ─────────────────────────────────────────────
  '/api/orders': {
    code: 0,
    message: 'success',
    data: [
      {
        id: 'ORD20260601001',
        storeId: 1,
        storeName: 'NekoCafé 朝阳店',
        tableId: 101,
        tableName: 'A1双人桌',
        reserveDate: '2026-06-01',
        reserveTime: '14:00',
        duration: 2,
        persons: 2,
        status: 'completed',
        totalAmount: 146,
        createTime: '2026-05-30 10:22:15',
        items: [
          { name: '猫爪拿铁', qty: 2, price: 38 },
          { name: '猫脸芝士蛋糕', qty: 1, price: 42 },
          { name: '法式火腿三明治', qty: 1, price: 38 }
        ],
        hasReview: true
      },
      {
        id: 'ORD20260603001',
        storeId: 2,
        storeName: 'NekoCafé 海淀店',
        tableId: 104,
        tableName: 'B1四人桌',
        reserveDate: '2026-06-05',
        reserveTime: '15:30',
        duration: 2,
        persons: 3,
        status: 'confirmed',
        totalAmount: 0,
        createTime: '2026-06-03 09:10:00',
        items: [],
        hasReview: false
      },
      {
        id: 'ORD20260525001',
        storeId: 1,
        storeName: 'NekoCafé 朝阳店',
        tableId: 107,
        tableName: 'C1包间',
        reserveDate: '2026-05-25',
        reserveTime: '18:00',
        duration: 3,
        persons: 5,
        status: 'completed',
        totalAmount: 328,
        createTime: '2026-05-23 16:45:00',
        items: [
          { name: '猫爪拿铁', qty: 4, price: 38 },
          { name: '鸡腿蘑菇焗饭', qty: 2, price: 52 },
          { name: '小猫奶冻摇摇茶', qty: 3, price: 29 }
        ],
        hasReview: false
      }
    ]
  },

  // ─────────────────────────────────────────────
  // 用户信息  GET /api/user/profile
  // ─────────────────────────────────────────────
  '/api/user/profile': {
    code: 0,
    message: 'success',
    data: {
      id: 1001,
      nickName: '猫咖爱好者',
      avatarUrl: 'https://placehold.co/200x200/C97E5A/white?text=Avatar',
      phone: '138****8888',
      memberLevel: '银卡会员',
      memberLevelIcon: '🥈',
      points: 320,
      pointsToNext: 680,       // 距下一级还需积分
      nextLevel: '金卡会员',
      totalOrders: 12,
      totalSpent: 896,
      couponCount: 3,
      favoriteStores: [1, 4],
      joinDate: '2025-09-01'
    }
  },

  // ─────────────────────────────────────────────
  // 优惠券列表  GET /api/coupons?userId=1001
  // ─────────────────────────────────────────────
  '/api/coupons': {
    code: 0,
    message: 'success',
    data: [
      { id: 'CPN001', name: '新人专享 8折券', type: 'discount', value: 0.8, maxDiscount: 20, minAmount: 50, expireDate: '2026-07-31', status: 'unused', stackable: true, ruleId: 'RULE_DISCOUNT_20' },
      { id: 'CPN002', name: '满100减20', type: 'cashback', value: 20, minAmount: 100, expireDate: '2026-06-30', status: 'unused', stackable: false, ruleId: 'RULE_CASHBACK_100_20' },
      { id: 'CPN003', name: '免费猫爪拿铁', type: 'freebie', value: 38, minAmount: 0, expireDate: '2026-06-15', status: 'unused', stackable: true, ruleId: 'RULE_FREEBIE_DRINK' }
    ]
  },

  // ─────────────────────────────────────────────
  // 可用优惠券  GET /api/coupons/available?storeId=1&amount=146
  // ─────────────────────────────────────────────
  '/api/coupons/available': function (queryParams) {
    const amount = Number(queryParams.amount || 0)
    const allCoupons = [
      { id: 'CPN001', name: '新人专享 8折券', type: 'discount', value: 0.8, maxDiscount: 20, minAmount: 50, expireDate: '2026-07-31', stackable: true, ruleId: 'RULE_DISCOUNT_20' },
      { id: 'CPN002', name: '满100减20', type: 'cashback', value: 20, minAmount: 100, expireDate: '2026-06-30', stackable: false, ruleId: 'RULE_CASHBACK_100_20' },
      { id: 'CPN003', name: '免费猫爪拿铁', type: 'freebie', value: 38, minAmount: 0, expireDate: '2026-06-15', stackable: true, ruleId: 'RULE_FREEBIE_DRINK' }
    ]
    // 返回用户持有的券，前端根据金额判断门槛并展示不可用状态
    const data = allCoupons.map(c => {
      let saving = 0
      if (c.type === 'discount' && amount >= c.minAmount) {
        saving = Math.min(Math.round(amount * (1 - c.value)), c.maxDiscount || 999)
      } else if (c.type === 'cashback' && amount >= c.minAmount) {
        saving = c.value
      } else if (c.type === 'freebie') {
        saving = c.value
      }
      return { ...c, saving }
    })
    return { code: 0, message: 'success', data }
  },

  // ─────────────────────────────────────────────
  // 运营活动规则  GET /api/promotions/rules
  // ─────────────────────────────────────────────
  '/api/promotions/rules': {
    code: 0,
    message: 'success',
    data: {
      // 当前生效的促销活动
      activePromotions: [
        { id: 'PROMO001', name: '工作日满减', type: 'cashback', rule: '满150减15', desc: '周一至周五可用，可与折扣券叠加', minAmount: 150, value: 15, stackable: true },
        { id: 'PROMO002', name: '满200减30', type: 'cashback', rule: '满200减30', desc: '所有门店通用', minAmount: 200, value: 30, stackable: false }
      ],
      // 叠加规则说明
      stackingRules: {
        enabled: true,
        maxStackCount: 2,           // 最多叠加2个优惠
        rules: [
          '平台满减活动 + 优惠券可叠加使用',
          '两个满减类优惠不可叠加，系统自动选最优',
          '折扣券与满减券可叠加，折扣券先计算',
          '赠品券不可与其他优惠叠加'
        ]
      }
    }
  },

  // ─────────────────────────────────────────────
  // 订单优惠计算  POST /api/promotions/calculate
  // ─────────────────────────────────────────────
  '/api/promotions/calculate': {
    code: 0,
    message: 'success',
    data: {
      originalAmount: 146,
      // 叠加规则：先算折扣券(8折, 最多减20)，再算满减(满150减15 不满足，满200减30 不满足)，最终不叠加平台满减
      appliedPromotions: [
        { name: '新人专享 8折券', type: 'discount', saved: 20 }
      ],
      totalDiscount: 20,
      finalAmount: 126,
      breakdown: [
        { label: '商品原价', amount: 146 },
        { label: '新人专享 8折券', amount: -20, type: 'discount' },
        { label: '平台满减（未满足门槛）', amount: 0, type: 'skipped' }
      ]
    }
  },

  // ─────────────────────────────────────────────
  // AI 推荐接口  GET /api/recommend?userId=1001
  // ─────────────────────────────────────────────
  '/api/recommend': {
    code: 0,
    message: 'success',
    data: {
      reason: '根据您的偏好和猫咪性格匹配，为您推荐',
      // 推荐桌位（基于猫咪性格：您喜欢粘人的猫 → 推荐布偶猫桌位）
      tables: [
        { id: 101, name: 'A1', type: '双人桌', catName: '奶油', catBreed: '布偶猫', catPersonality: ['粘人', '爱睡觉', '不抓人'], matchScore: 98, matchReason: '您偏好温顺粘人的猫咪，奶油性格完美匹配' },
        { id: 104, name: 'B1', type: '四人桌', catName: '大毛', catBreed: '缅因猫', catPersonality: ['温柔', '粘人', '爱叫唤'], matchScore: 92, matchReason: '像大毛这样温柔的巨人最适合您' },
        { id: 103, name: 'A3', type: '双人桌', catName: '胖橘', catBreed: '橘猫', catPersonality: ['活泼', '贪吃', '爱玩'], matchScore: 85, matchReason: '胖橘活泼可爱，适合喜欢互动的您' }
      ],
      // 推荐菜品（基于历史点单和热销）
      dishes: [
        { id: 201, name: '猫爪拿铁', price: 38, imageUrl: 'https://placehold.co/400x300/C97E5A/white?text=Menu+201', reason: '您的最爱，累计点了6次', isHot: true },
        { id: 208, name: '猫脸芝士蛋糕', price: 42, imageUrl: 'https://placehold.co/400x300/C97E5A/white?text=Menu+208', reason: '和猫爪拿铁是经典搭配', isHot: true },
        { id: 214, name: '小猫奶冻摇摇茶', price: 29, imageUrl: 'https://placehold.co/400x300/C97E5A/white?text=Menu+214', reason: '本月新品，同类用户给出4.8分', isHot: true },
        { id: 203, name: '抹茶红豆拿铁', price: 35, imageUrl: 'https://placehold.co/400x300/C97E5A/white?text=Menu+203', reason: '根据相似用户推荐', isHot: false }
      ],
      // 用户偏好标签
      userProfile: {
        favoriteBreeds: ['布偶猫', '英短'],
        favoritePersonalities: ['粘人', '温柔'],
        tastePreference: ['咖啡', '甜品'],
        visitFrequency: '每周1-2次'
      }
    }
  },

  // ─────────────────────────────────────────────
  // 猫咪列表  GET /api/cats?storeId=1
  // ─────────────────────────────────────────────
  '/api/cats': {
    code: 0,
    message: 'success',
    data: [
      { id: 1, name: '奶油', breed: '布偶猫', age: 2, gender: '母', imageUrl: 'https://placehold.co/400x300/C97E5A/white?text=Cat+1', weight: 4.2, desc: '温顺爱撒娇，最受欢迎的网红猫', personality: ['粘人', '爱睡觉', '不抓人'], vaccineDue: '2026-08-15' },
      { id: 2, name: '胖橘', breed: '橘猫', age: 3, gender: '公', imageUrl: 'https://placehold.co/400x300/C97E5A/white?text=Cat+2', weight: 6.8, desc: '吃货一枚，美食面前没有尊严', personality: ['活泼', '贪吃', '爱玩'], vaccineDue: '2026-09-01' },
      { id: 3, name: '豆腐', breed: '英国短毛猫', age: 1, gender: '母', imageUrl: 'https://placehold.co/400x300/C97E5A/white?text=Cat+3', weight: 3.5, desc: '蓝灰色毛发，圆滚滚超可爱', personality: ['高冷', '偶尔撒娇', '爱干净'], vaccineDue: '2026-07-20' },
      { id: 4, name: '大毛', breed: '缅因猫', age: 4, gender: '公', imageUrl: 'https://placehold.co/400x300/C97E5A/white?text=Cat+4', weight: 9.2, desc: '体型最大的猫，温柔的巨人', personality: ['温柔', '粘人', '爱叫唤'], vaccineDue: '2026-10-01' },
      { id: 5, name: '小蓝', breed: '暹罗猫', age: 2, gender: '母', imageUrl: 'https://placehold.co/400x300/C97E5A/white?text=Cat+5', weight: 3.8, desc: '蓝眼睛，叫声悦耳像在唱歌', personality: ['话多', '好奇', '爱运动'], vaccineDue: '2026-06-30' }
    ]
  },

  // ─────────────────────────────────────────────
  // 猫咪健康详情  GET /api/cats/detail?catId=1
  // ─────────────────────────────────────────────
  '/api/cats/detail': {
    code: 0,
    message: 'success',
    data: {
      id: 1,
      name: '奶油',
      breed: '布偶猫',
      age: 2,
      gender: '母',
      imageUrl: 'https://placehold.co/400x300/C97E5A/white?text=Cat+1+Detail',
      desc: '温顺爱撒娇，最受欢迎的网红猫。每天最喜欢趴在靠窗的猫爬架上晒太阳，对每一位客人都很友好。',
      personality: ['粘人', '爱睡觉', '不抓人'],
      currentWeight: 4.2,
      idealWeight: { min: 3.5, max: 5.0 },
      // 体重变化记录（近6个月）
      weightHistory: {
        labels: ['1月', '2月', '3月', '4月', '5月', '6月'],
        values: [3.9, 4.0, 4.1, 4.3, 4.2, 4.2]
      },
      // 疫苗记录
      vaccines: [
        { name: '猫三联', date: '2025-12-15', nextDue: '2026-12-15', status: 'valid' },
        { name: '狂犬疫苗', date: '2026-01-20', nextDue: '2027-01-20', status: 'valid' },
        { name: '猫瘟疫苗', date: '2025-08-10', nextDue: '2026-08-10', status: 'expiring' }
      ],
      // 互动记录
      interactions: [
        { date: '06-03', type: '客人撸猫', desc: '被客人抱了30分钟，发出咕噜声', mood: 'happy' },
        { date: '06-02', type: '洗澡', desc: '每月例行洗澡，不太配合但最终完成', mood: 'grumpy' },
        { date: '06-01', type: '体检', desc: '体重4.2kg，心率正常，医生评价健康', mood: 'neutral' },
        { date: '05-28', type: '玩耍', desc: '玩了30分钟逗猫棒，运动量达标', mood: 'happy' },
        { date: '05-25', type: '客人撸猫', desc: '儿童节活动，被小朋友们轻轻抚摸', mood: 'happy' },
        { date: '05-22', type: '梳理', desc: '毛发梳理，掉毛量正常', mood: 'neutral' }
      ],
      // 健康指标
      healthScore: 95,
      healthAdvice: '奶油目前整体健康状态良好。体重在理想范围内，疫苗均在有效期内。建议下月安排一次体检。'
    }
  },

  // ─────────────────────────────────────────────
  // 提交预约  POST /api/reservation/create
  // ─────────────────────────────────────────────
  '/api/reservation/create': {
    code: 0,
    message: 'success',
    data: { orderId: 'ORD20260603002', status: 'confirmed' }
  },

  // ─────────────────────────────────────────────
  // 提交订单  POST /api/order/submit
  // ─────────────────────────────────────────────
  '/api/order/submit': function (queryParams, body) {
    const items = (body && body.items) || []
    if (!items.length) {
      return { code: 400, message: '购物车不能为空', data: null }
    }
    const finalAmount = body && body.finalAmount != null ? body.finalAmount : 0
    const orderId = 'ORD' + Date.now()
    const data = {
      orderId,
      totalAmount: body.totalAmount || 0,
      finalAmount,
      discount: body.discount || 0
    }
    // 实付金额 > 0 时返回 mock 支付参数（前端会识别并走模拟支付）
    if (finalAmount > 0) {
      data.payInfo = {
        timeStamp: String(Math.floor(Date.now() / 1000)),
        nonceStr: 'mock_nonce_str_' + orderId,
        package: 'prepay_id=mock_prepay_id_' + orderId,
        signType: 'RSA',
        paySign: 'mock_pay_sign_' + orderId
      }
    }
    return { code: 0, message: 'success', data }
  },

  // ─────────────────────────────────────────────
  // 订单详情  GET /api/order/detail?orderId=xxx
  // 动态 mock：根据 orderId 返回对应订单的完整详情
  // ─────────────────────────────────────────────
  '/api/order/detail': function (queryParams) {
    var orderId = queryParams.orderId || 'ORD20260603001'
    // 全部订单详情数据库（与 /api/staff/orders 对齐，字段更全）
    var detailDB = {
      'ORD20260601001': {
        id: 'ORD20260601001', storeId: 1, storeName: 'NekoCafé 朝阳店',
        tableId: 101, tableName: 'A1双人桌',
        reserveDate: '2026-06-01', reserveTime: '14:00', duration: 2, persons: 2,
        status: 'completed', totalAmount: 146, discountAmount: 0, finalAmount: 146,
        createTime: '2026-05-30 10:22:15', payTime: '2026-05-30 10:23:00',
        payType: '微信支付', paymentMethod: '微信支付',
        customerName: '猫咖爱好者', customerPhone: '138****8888',
        remark: '靠窗位置，麻烦留猫爬架附近的座位',
        items: [
          { name: '猫爪拿铁', qty: 2, price: 38 },
          { name: '猫脸芝士蛋糕', qty: 1, price: 42 },
          { name: '法式火腿三明治', qty: 1, price: 38 }
        ],
        timeline: [
          { time: '2026-05-30 10:22:15', title: '订单创建', desc: '用户提交预约' },
          { time: '2026-05-30 10:23:00', title: '支付成功', desc: '微信支付 ¥146.00' },
          { time: '2026-05-30 11:00:00', title: '门店确认', desc: '店员已确认预约' },
          { time: '2026-06-01 14:05:00', title: '顾客到店', desc: '已签到入座 A1双人桌' },
          { time: '2026-06-01 16:10:00', title: '订单完成', desc: '顾客离店，消费 ¥146.00' }
        ],
        canCancel: false, canReschedule: false, canRefund: false, hasReview: true
      },
      'ORD20260525001': {
        id: 'ORD20260525001', storeId: 1, storeName: 'NekoCafé 朝阳店',
        tableId: 107, tableName: 'C1包间',
        reserveDate: '2026-05-25', reserveTime: '18:00', duration: 3, persons: 5,
        status: 'completed', totalAmount: 328, discountAmount: 30, finalAmount: 298,
        createTime: '2026-05-23 16:45:00', payTime: '2026-05-23 16:46:00',
        payType: '支付宝', paymentMethod: '支付宝',
        customerName: '生日派对王小姐', customerPhone: '139****1234',
        remark: '生日派对，需要装饰气球，麻烦准备猫咪主题蛋糕',
        items: [
          { name: '猫爪拿铁', qty: 4, price: 38 },
          { name: '鸡腿蘑菇焗饭', qty: 2, price: 52 },
          { name: '小猫奶冻摇摇茶', qty: 3, price: 29 }
        ],
        timeline: [
          { time: '2026-05-23 16:45:00', title: '订单创建', desc: '用户提交预约（生日派对）' },
          { time: '2026-05-23 16:46:00', title: '支付成功', desc: '支付宝支付 ¥298.00' },
          { time: '2026-05-23 17:30:00', title: '门店确认', desc: '店员已确认并安排包间布置' },
          { time: '2026-05-25 18:05:00', title: '顾客到店', desc: '5人派对到店，入座C1包间' },
          { time: '2026-05-25 21:00:00', title: '订单完成', desc: '派对结束，顾客满意离店' }
        ],
        canCancel: false, canReschedule: false, canRefund: false, hasReview: false
      },
      'ORD20260603001': {
        id: 'ORD20260603001', storeId: 2, storeName: 'NekoCafé 海淀店',
        tableId: 104, tableName: 'B1四人桌',
        reserveDate: '2026-06-05', reserveTime: '15:30', duration: 2, persons: 3,
        status: 'confirmed', totalAmount: 0, discountAmount: 0, finalAmount: 0,
        createTime: '2026-06-03 09:10:00', payTime: '', payType: '-', paymentMethod: '-',
        customerName: '中关村码农', customerPhone: '136****5678',
        remark: '需要安静角落，带电脑工作用',
        items: [],
        timeline: [
          { time: '2026-06-03 09:10:00', title: '订单创建', desc: '用户提交预约（到店支付）' },
          { time: '2026-06-03 09:15:00', title: '门店确认', desc: '店员已确认预约' }
        ],
        canCancel: true, canReschedule: true, canRefund: false, hasReview: false
      },
      'ORD20260602001': {
        id: 'ORD20260602001', storeId: 2, storeName: 'NekoCafé 海淀店',
        tableId: 102, tableName: 'A2双人桌',
        reserveDate: '2026-06-02', reserveTime: '11:00', duration: 1, persons: 2,
        status: 'completed', totalAmount: 96, discountAmount: 0, finalAmount: 96,
        createTime: '2026-05-31 20:30:00', payTime: '2026-05-31 20:31:00',
        payType: '微信支付', paymentMethod: '微信支付',
        customerName: '学生小张', customerPhone: '185****9012',
        remark: '第一次来猫咖，期待！',
        items: [
          { name: '猫脸芝士蛋糕', qty: 1, price: 42 },
          { name: '美式咖啡', qty: 1, price: 28 },
          { name: '猫爪饼干', qty: 1, price: 26 }
        ],
        timeline: [
          { time: '2026-05-31 20:30:00', title: '订单创建', desc: '用户提交预约' },
          { time: '2026-05-31 20:31:00', title: '支付成功', desc: '微信支付 ¥96.00' },
          { time: '2026-06-01 09:00:00', title: '门店确认', desc: '店员已确认预约' },
          { time: '2026-06-02 11:02:00', title: '顾客到店', desc: '已签到入座 A2双人桌' },
          { time: '2026-06-02 12:05:00', title: '订单完成', desc: '顾客离店，首次体验愉快' }
        ],
        canCancel: false, canReschedule: false, canRefund: false, hasReview: false
      },
      'ORD20260604001': {
        id: 'ORD20260604001', storeId: 3, storeName: 'NekoCafé 通州店',
        tableId: 105, tableName: 'B2四人桌',
        reserveDate: '2026-06-04', reserveTime: '16:00', duration: 2, persons: 4,
        status: 'occupied', totalAmount: 212, discountAmount: 15, finalAmount: 197,
        createTime: '2026-06-03 21:15:00', payTime: '2026-06-03 21:16:00',
        payType: '微信支付', paymentMethod: '微信支付',
        customerName: '李先生一家', customerPhone: '158****3456',
        remark: '带两个孩子，希望靠近猫咪活动区',
        items: [
          { name: '猫爪拿铁', qty: 3, price: 38 },
          { name: '鸡腿蘑菇焗饭', qty: 1, price: 52 },
          { name: '法式火腿三明治', qty: 1, price: 38 },
          { name: '小猫奶冻摇摇茶', qty: 1, price: 29 }
        ],
        timeline: [
          { time: '2026-06-03 21:15:00', title: '订单创建', desc: '用户提交预约（家庭出行）' },
          { time: '2026-06-03 21:16:00', title: '支付成功', desc: '微信支付 ¥197.00' },
          { time: '2026-06-04 08:30:00', title: '门店确认', desc: '店员已确认预约' },
          { time: '2026-06-04 16:02:00', title: '顾客到店', desc: '一家四口签到入座 B2四人桌' }
        ],
        canCancel: false, canReschedule: false, canRefund: false, hasReview: false
      },
      'ORD20260604002': {
        id: 'ORD20260604002', storeId: 3, storeName: 'NekoCafé 通州店',
        tableId: 101, tableName: 'A1双人桌',
        reserveDate: '2026-06-04', reserveTime: '13:30', duration: 2, persons: 2,
        status: 'occupied', totalAmount: 134, discountAmount: 0, finalAmount: 134,
        createTime: '2026-06-04 08:00:00', payTime: '2026-06-04 08:01:00',
        payType: '支付宝', paymentMethod: '支付宝',
        customerName: '情侣小赵', customerPhone: '177****7890',
        remark: '纪念日约会，麻烦安排安静角落',
        items: [
          { name: '猫爪拿铁', qty: 2, price: 38 },
          { name: '猫脸芝士蛋糕', qty: 2, price: 42 }
        ],
        timeline: [
          { time: '2026-06-04 08:00:00', title: '订单创建', desc: '用户提交预约（纪念日）' },
          { time: '2026-06-04 08:01:00', title: '支付成功', desc: '支付宝支付 ¥134.00' },
          { time: '2026-06-04 09:00:00', title: '门店确认', desc: '店员已确认并预留位置' },
          { time: '2026-06-04 13:32:00', title: '顾客到店', desc: '情侣签到入座 A1双人桌' }
        ],
        canCancel: false, canReschedule: false, canRefund: false, hasReview: false
      },
      'ORD20260603002': {
        id: 'ORD20260603002', storeId: 4, storeName: 'NekoCafé 西城店',
        tableId: 108, tableName: '包间VIP',
        reserveDate: '2026-06-03', reserveTime: '14:00', duration: 3, persons: 8,
        status: 'completed', totalAmount: 856, discountAmount: 80, finalAmount: 776,
        createTime: '2026-05-28 15:30:00', payTime: '2026-05-28 15:32:00',
        payType: '企业转账', paymentMethod: '企业转账',
        customerName: '公司团建刘总', customerPhone: '189****0001',
        remark: '公司团建活动，8人包场，需要投影设备',
        items: [
          { name: '猫爪拿铁', qty: 8, price: 38 },
          { name: '鸡腿蘑菇焗饭', qty: 4, price: 52 },
          { name: '法式火腿三明治', qty: 4, price: 38 },
          { name: '小猫奶冻摇摇茶', qty: 6, price: 29 },
          { name: '猫脸芝士蛋糕', qty: 4, price: 42 }
        ],
        timeline: [
          { time: '2026-05-28 15:30:00', title: '订单创建', desc: '用户提交团建包场预约' },
          { time: '2026-05-28 15:32:00', title: '支付成功', desc: '企业转账 ¥776.00' },
          { time: '2026-05-28 16:00:00', title: '门店确认', desc: '店长确认包场并安排投影设备' },
          { time: '2026-06-03 14:00:00', title: '顾客到店', desc: '8人团建团队签到入座VIP包间' },
          { time: '2026-06-03 17:05:00', title: '订单完成', desc: '团建结束，顾客满意离店' }
        ],
        canCancel: false, canReschedule: false, canRefund: false, hasReview: false
      },
      'ORD20260605001': {
        id: 'ORD20260605001', storeId: 4, storeName: 'NekoCafé 西城店',
        tableId: 103, tableName: 'A3双人桌',
        reserveDate: '2026-06-05', reserveTime: '19:00', duration: 2, persons: 2,
        status: 'confirmed', totalAmount: 0, discountAmount: 0, finalAmount: 0,
        createTime: '2026-06-04 12:00:00', payTime: '', payType: '-', paymentMethod: '-',
        customerName: '网红博主莉莉', customerPhone: '133****6666',
        remark: '来探店拍视频，需要光线好的位置',
        items: [],
        timeline: [
          { time: '2026-06-04 12:00:00', title: '订单创建', desc: '用户提交预约（到店支付）' },
          { time: '2026-06-04 12:30:00', title: '门店确认', desc: '店长已确认，安排靠窗光线位' }
        ],
        canCancel: true, canReschedule: true, canRefund: false, hasReview: false
      },
      'ORD20260601002': {
        id: 'ORD20260601002', storeId: 5, storeName: 'NekoCafé 丰台店',
        tableId: 104, tableName: 'B1四人桌',
        reserveDate: '2026-06-01', reserveTime: '15:00', duration: 2, persons: 4,
        status: 'completed', totalAmount: 168, discountAmount: 0, finalAmount: 168,
        createTime: '2026-05-30 18:30:00', payTime: '2026-05-30 18:31:00',
        payType: '微信支付', paymentMethod: '微信支付',
        customerName: '邻居陈阿姨', customerPhone: '150****2345',
        remark: '老顾客，帮忙安排常坐的B1桌',
        items: [
          { name: '猫爪拿铁', qty: 2, price: 38 },
          { name: '鸡腿蘑菇焗饭', qty: 2, price: 52 }
        ],
        timeline: [
          { time: '2026-05-30 18:30:00', title: '订单创建', desc: '用户提交预约' },
          { time: '2026-05-30 18:31:00', title: '支付成功', desc: '微信支付 ¥168.00' },
          { time: '2026-05-31 09:00:00', title: '门店确认', desc: '店员确认并安排B1桌' },
          { time: '2026-06-01 15:02:00', title: '顾客到店', desc: '陈阿姨签到入座 B1四人桌' },
          { time: '2026-06-01 17:00:00', title: '订单完成', desc: '老顾客满意离店' }
        ],
        canCancel: false, canReschedule: false, canRefund: false, hasReview: false
      }
    }
    var orderData = detailDB[orderId]
    if (!orderData) {
      // fallback：未知 orderId 返回默认数据
      orderData = {
        id: orderId, storeId: 1, storeName: 'NekoCafé 朝阳店',
        tableId: 101, tableName: 'A1双人桌',
        reserveDate: '2026-06-05', reserveTime: '14:00', duration: 2, persons: 2,
        status: 'confirmed', totalAmount: 146, discountAmount: 20, finalAmount: 126,
        createTime: '2026-06-03 09:10:00', payTime: '2026-06-03 09:10:30',
        payType: '微信支付', paymentMethod: '微信支付',
        customerName: '未知顾客', customerPhone: '---',
        remark: '',
        items: [],
        timeline: [
          { time: '2026-06-03 09:10:00', title: '订单创建', desc: '用户提交预约' }
        ],
        canCancel: true, canReschedule: true, canRefund: true, hasReview: false
      }
    }
    return { code: 0, message: 'success', data: orderData }
  },

  // ─────────────────────────────────────────────
  // 取消订单  POST /api/order/cancel
  // ─────────────────────────────────────────────
  '/api/order/cancel': {
    code: 0,
    message: 'success',
    data: { status: 'cancelled', refundAmount: 126 }
  },

  // ─────────────────────────────────────────────
  // 改约  POST /api/order/reschedule
  // ─────────────────────────────────────────────
  '/api/order/reschedule': {
    code: 0,
    message: 'success',
    data: { orderId: 'ORD20260603001', newReserveDate: '2026-06-06', newReserveTime: '15:00' }
  },

  // ─────────────────────────────────────────────
  // 申请退款  POST /api/order/refund
  // ─────────────────────────────────────────────
  '/api/order/refund': {
    code: 0,
    message: 'success',
    data: { refundId: 'REF20260603001', refundAmount: 126, status: 'processing' }
  },

  // ─────────────────────────────────────────────
  // 实名认证  POST /api/user/realname
  // ─────────────────────────────────────────────
  '/api/user/realname': {
    code: 0,
    message: 'success',
    data: { verified: true, realName: '张三', idCardMask: '110101********1234' }
  },

  // ─────────────────────────────────────────────
  // 排队状态  GET /api/queue/status?storeId=1
  // ─────────────────────────────────────────────
  '/api/queue/status': {
    code: 0,
    message: 'success',
    data: {
      storeId: 1,
      waitingCount: 4,
      avgWaitMinutes: 15,
      currentNumber: 12,
      myNumber: null,
      myWaitMinutes: 0,
      queueList: [
        { number: 13, persons: 2, type: '双人桌', ahead: 0 },
        { number: 14, persons: 4, type: '四人桌', ahead: 1 },
        { number: 15, persons: 2, type: '双人桌', ahead: 2 },
        { number: 16, persons: 6, type: '包间', ahead: 3 }
      ]
    }
  },

  // ─────────────────────────────────────────────
  // 取号  POST /api/queue/take
  // ─────────────────────────────────────────────
  '/api/queue/take': {
    code: 0,
    message: 'success',
    data: { number: 17, persons: 2, type: '双人桌', ahead: 4, estWaitMinutes: 20 }
  },

  // ─────────────────────────────────────────────
  // 数据看板  GET /api/dashboard/metrics?storeId=1&range=7d
  // ─────────────────────────────────────────────
  '/api/dashboard/metrics': {
    code: 0,
    message: 'success',
    data: {
      storeId: 1,
      range: '7d',
      // 坪效：近7天每日坪效（元/m²/天）
      spaceEfficiency: {
        labels: ['05-28', '05-29', '05-30', '05-31', '06-01', '06-02', '06-03'],
        values: [85, 92, 78, 105, 120, 98, 88]
      },
      // 翻台率：近7天每日翻台率（%）
      turnoverRate: {
        labels: ['05-28', '05-29', '05-30', '05-31', '06-01', '06-02', '06-03'],
        values: [2.1, 2.4, 1.8, 3.0, 3.5, 2.8, 2.3]
      },
      // 会员复购率：按会员等级分组
      repurchaseRate: {
        labels: ['普通会员', '银卡会员', '金卡会员', '黑卡会员'],
        values: [28, 45, 62, 78]
      },
      // 今日概览
      todayOverview: {
        revenue: 8650,
        orderCount: 42,
        newMembers: 5,
        avgOrderValue: 206
      }
    }
  },

  // ─────────────────────────────────────────────
  // 店员桌位状态  GET /api/staff/tables?storeId=1
  // ─────────────────────────────────────────────
  '/api/staff/tables': {
    code: 0,
    message: 'success',
    data: [
      { id: 101, name: 'A1', type: '双人桌', status: 'occupied', customer: '猫咖爱好者', arriveTime: '14:00', estLeaveTime: '16:00', catType: '布偶猫' },
      { id: 102, name: 'A2', type: '双人桌', status: 'booked', customer: '豆腐粉丝', arriveTime: '15:00', estLeaveTime: '17:00', catType: '英短' },
      { id: 103, name: 'A3', type: '双人桌', status: 'cleaning', customer: '-', arriveTime: '-', estLeaveTime: '-', catType: '橘猫' },
      { id: 104, name: 'B1', type: '四人桌', status: 'available', customer: '-', arriveTime: '-', estLeaveTime: '-', catType: '缅因猫' },
      { id: 105, name: 'B2', type: '四人桌', status: 'occupied', customer: '橘猫爱好者', arriveTime: '13:30', estLeaveTime: '15:30', catType: '暹罗猫' },
      { id: 106, name: 'B3', type: '四人桌', status: 'available', customer: '-', arriveTime: '-', estLeaveTime: '-', catType: '美短' },
      { id: 107, name: 'C1', type: '包间', status: 'occupied', customer: '生日派对', arriveTime: '12:00', estLeaveTime: '15:00', catType: 'VIP猫区' },
      { id: 108, name: 'C2', type: '包间', status: 'maintenance', customer: '-', arriveTime: '-', estLeaveTime: '-', catType: 'VIP猫区' }
    ]
  },

  // ─────────────────────────────────────────────
  // 店员异常告警  GET /api/staff/alerts?storeId=1
  // ─────────────────────────────────────────────
  '/api/staff/alerts': {
    code: 0,
    message: 'success',
    data: [
      { alertId: 1, storeId: 1, staffId: 2, type: 'OVERTIME', level: 'high', status: 'PENDING', statusLabel: '待处理', reason: '客人超时占座 | 桌号 102 | 预约 1001', exceptionDate: '2026-06-12', createdAt: '2026-06-12 14:20' },
      { alertId: 2, storeId: 1, staffId: 3, type: 'NO_SHOW', level: 'high', status: 'PENDING', statusLabel: '待处理', reason: '客人预约未到店 | 预约 1005 | 时间 2026-06-12 13:00', exceptionDate: '2026-06-12', createdAt: '2026-06-12 13:20' },
      { alertId: 3, storeId: 1, staffId: 3, type: 'LEAVE', level: 'medium', status: 'ACKNOWLEDGED', statusLabel: '已知晓', reason: '病假', exceptionDate: '2026-06-11', createdAt: '2026-06-10 09:00' },
      { alertId: 4, storeId: 1, staffId: 2, type: 'SWAP', level: 'medium', status: 'PENDING', statusLabel: '待处理', reason: '与同事换班', exceptionDate: '2026-06-15', createdAt: '2026-06-12 10:30' },
      { alertId: 5, storeId: 2, staffId: 5, type: 'LEAVE', level: 'medium', status: 'REJECTED', statusLabel: '已驳回', reason: '事假', exceptionDate: '2026-06-16', createdAt: '2026-06-11 15:00' }
    ]
  },

  // ─────────────────────────────────────────────
  // 提交评价  POST /api/review/submit
  // ─────────────────────────────────────────────
  '/api/review/submit': {
    code: 0,
    message: 'success',
    data: { reviewId: 'REV20260603001', status: 'published', pointsEarned: 10 }
  },

  // ─────────────────────────────────────────────
  // 微信登录  POST /api/auth/login
  // 真实场景：后端用 code 换取 openid，再返回 token + userInfo
  // Mock：根据请求体中的 role 字段返回对应用户信息
  // ─────────────────────────────────────────────
  '/api/auth/login': {
    code: 0,
    message: 'success',
    data: {
      token: 'mock_jwt_token_20260604',
      userInfo: {
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
      }
    }
  },

  // ─────────────────────────────────────────────
  // 店员/店长查看订单  GET /api/staff/orders?storeId=1
  // ─────────────────────────────────────────────
  '/api/staff/orders': {
    code: 0,
    message: 'success',
    data: [
      { id: 'ORD20260601001', storeId: 1, storeName: 'NekoCafé 朝阳店', tableName: 'A1双人桌', customerName: '猫咖爱好者', customerPhone: '138****8888', persons: 2, reserveDate: '2026-06-01', reserveTime: '14:00', duration: 2, status: 'completed', totalAmount: 146, paymentMethod: '微信支付', createTime: '2026-05-30 10:22:15', items: [{name:'猫爪拿铁',qty:2,price:38},{name:'猫脸芝士蛋糕',qty:1,price:42},{name:'法式火腿三明治',qty:1,price:38}] },
      { id: 'ORD20260525001', storeId: 1, storeName: 'NekoCafé 朝阳店', tableName: 'C1包间', customerName: '生日派对王小姐', customerPhone: '139****1234', persons: 5, reserveDate: '2026-05-25', reserveTime: '18:00', duration: 3, status: 'completed', totalAmount: 328, paymentMethod: '支付宝', createTime: '2026-05-23 16:45:00', items: [{name:'猫爪拿铁',qty:4,price:38},{name:'鸡腿蘑菇焗饭',qty:2,price:52},{name:'小猫奶冻摇摇茶',qty:3,price:29}] },
      { id: 'ORD20260603001', storeId: 2, storeName: 'NekoCafé 海淀店', tableName: 'B1四人桌', customerName: '中关村码农', customerPhone: '136****5678', persons: 3, reserveDate: '2026-06-05', reserveTime: '15:30', duration: 2, status: 'confirmed', totalAmount: 0, paymentMethod: '-', createTime: '2026-06-03 09:10:00', items: [] },
      { id: 'ORD20260602001', storeId: 2, storeName: 'NekoCafé 海淀店', tableName: 'A2双人桌', customerName: '学生小张', customerPhone: '185****9012', persons: 2, reserveDate: '2026-06-02', reserveTime: '11:00', duration: 1, status: 'completed', totalAmount: 96, paymentMethod: '微信支付', createTime: '2026-05-31 20:30:00', items: [{name:'猫脸芝士蛋糕',qty:1,price:42},{name:'美式咖啡',qty:1,price:28},{name:'猫爪饼干',qty:1,price:26}] },
      { id: 'ORD20260604001', storeId: 3, storeName: 'NekoCafé 通州店', tableName: 'B2四人桌', customerName: '李先生一家', customerPhone: '158****3456', persons: 4, reserveDate: '2026-06-04', reserveTime: '16:00', duration: 2, status: 'occupied', totalAmount: 212, paymentMethod: '微信支付', createTime: '2026-06-03 21:15:00', items: [{name:'猫爪拿铁',qty:3,price:38},{name:'鸡腿蘑菇焗饭',qty:1,price:52},{name:'法式火腿三明治',qty:1,price:38},{name:'小猫奶冻摇摇茶',qty:1,price:29}] },
      { id: 'ORD20260604002', storeId: 3, storeName: 'NekoCafé 通州店', tableName: 'A1双人桌', customerName: '情侣小赵', customerPhone: '177****7890', persons: 2, reserveDate: '2026-06-04', reserveTime: '13:30', duration: 2, status: 'occupied', totalAmount: 134, paymentMethod: '支付宝', createTime: '2026-06-04 08:00:00', items: [{name:'猫爪拿铁',qty:2,price:38},{name:'猫脸芝士蛋糕',qty:2,price:42}] },
      { id: 'ORD20260603002', storeId: 4, storeName: 'NekoCafé 西城店', tableName: '包间VIP', customerName: '公司团建刘总', customerPhone: '189****0001', persons: 8, reserveDate: '2026-06-03', reserveTime: '14:00', duration: 3, status: 'completed', totalAmount: 856, paymentMethod: '企业转账', createTime: '2026-05-28 15:30:00', items: [{name:'猫爪拿铁',qty:8,price:38},{name:'鸡腿蘑菇焗饭',qty:4,price:52},{name:'法式火腿三明治',qty:4,price:38},{name:'小猫奶冻摇摇茶',qty:6,price:29},{name:'猫脸芝士蛋糕',qty:4,price:42}] },
      { id: 'ORD20260605001', storeId: 4, storeName: 'NekoCafé 西城店', tableName: 'A3双人桌', customerName: '网红博主莉莉', customerPhone: '133****6666', persons: 2, reserveDate: '2026-06-05', reserveTime: '19:00', duration: 2, status: 'confirmed', totalAmount: 0, paymentMethod: '-', createTime: '2026-06-04 12:00:00', items: [] },
      { id: 'ORD20260601002', storeId: 5, storeName: 'NekoCafé 丰台店', tableName: 'B1四人桌', customerName: '邻居陈阿姨', customerPhone: '150****2345', persons: 4, reserveDate: '2026-06-01', reserveTime: '15:00', duration: 2, status: 'completed', totalAmount: 168, paymentMethod: '微信支付', createTime: '2026-05-30 18:30:00', items: [{name:'猫爪拿铁',qty:2,price:38},{name:'鸡腿蘑菇焗饭',qty:2,price:52}] }
    ]
  },

  // ─────────────────────────────────────────────
  // 店员-订单进度推进  POST /api/staff/order/progress
  // ─────────────────────────────────────────────
  '/api/staff/order/progress': function(body) {
    return { code: 0, message: 'success', data: { success: true, message: '订单状态已更新', reservationId: body.reservationId, status: body.targetStatus } }
  },

  // ─────────────────────────────────────────────
  // 店员-退款申请列表  GET /api/staff/refunds?storeId=
  // ─────────────────────────────────────────────
  '/api/staff/refunds': {
    code: 0,
    message: 'success',
    data: [
      { refundId: 1, reservationId: 1001, refundAmount: 68, refundReason: '菜品售罄', status: 'PENDING', createdAt: '2026-06-12 12:30', tableNo: 'A01', tableType: '双人桌', totalAmount: 68, userId: 10, reservationTime: '2026-06-12 12:00' },
      { refundId: 2, reservationId: 1002, refundAmount: 146, refundReason: '等待时间过长', status: 'PENDING', createdAt: '2026-06-12 11:20', tableNo: 'B03', tableType: '四人桌', totalAmount: 146, userId: 11, reservationTime: '2026-06-12 10:30' },
      { refundId: 3, reservationId: 1003, refundAmount: 52, refundReason: '改变主意', status: 'APPROVED', createdAt: '2026-06-11 15:00', completedAt: '2026-06-11 15:30', tableNo: 'A02', tableType: '双人桌', totalAmount: 52, userId: 12, reservationTime: '2026-06-11 14:00' }
    ]
  },

  // ─────────────────────────────────────────────
  // 店员-审核退款  POST /api/staff/refund/review
  // ─────────────────────────────────────────────
  '/api/staff/refund/review': function(body) {
    return { code: 0, message: 'success', data: { success: true, message: body.action === 'approve' ? '退款已通过' : '退款已拒绝', refundId: body.refundId, status: body.action === 'approve' ? 'APPROVED' : 'REJECTED' } }
  },

  // ─────────────────────────────────────────────
  // 总部运营-各门店概览  GET /api/hq/stores-overview
  // ─────────────────────────────────────────────
  '/api/hq/stores-overview': {
    code: 0,
    message: 'success',
    data: {
      totalRevenue: 36520,
      totalOrders: 248,
      totalMembers: 87,
      avgDailyTurnover: 2.6,
      stores: [
        { id: 1, name: 'NekoCafé 朝阳店', manager: '王店长', managerPhone: '138****0001', status: 'open', todayRevenue: 8650, todayOrders: 42, tableCount: 8, availableTables: 3, occupancyRate: 0.625, rating: 4.8 },
        { id: 2, name: 'NekoCafé 海淀店', manager: '赵店长', managerPhone: '138****0002', status: 'open', todayRevenue: 7200, todayOrders: 35, tableCount: 6, availableTables: 2, occupancyRate: 0.667, rating: 4.6 },
        { id: 3, name: 'NekoCafé 通州店', manager: '孙店长', managerPhone: '138****0003', status: 'open', todayRevenue: 5430, todayOrders: 28, tableCount: 6, availableTables: 4, occupancyRate: 0.333, rating: 4.5 },
        { id: 4, name: 'NekoCafé 西城店', manager: '周店长', managerPhone: '138****0004', status: 'open', todayRevenue: 10200, todayOrders: 51, tableCount: 10, availableTables: 2, occupancyRate: 0.800, rating: 4.9 },
        { id: 5, name: 'NekoCafé 丰台店', manager: '吴店长', managerPhone: '138****0005', status: 'closed', todayRevenue: 0, todayOrders: 0, tableCount: 4, availableTables: 4, occupancyRate: 0, rating: 4.3 }
      ]
    }
  },

  // ─────────────────────────────────────────────
  // 店员-告警已知晓  POST /api/staff/alert/acknowledge
  // ─────────────────────────────────────────────
  '/api/staff/alert/acknowledge': function(body) {
    return { code: 0, message: 'success', data: { success: true, message: '已标记为已知晓', exceptionId: body.exceptionId } }
  },

  // ─────────────────────────────────────────────
  // 店员-解决告警  POST /api/staff/alert/resolve
  // ─────────────────────────────────────────────
  '/api/staff/alert/resolve': function(body) {
    return { code: 0, message: 'success', data: { success: true, message: '告警已解决', exceptionId: body.exceptionId } }
  },

  // ─────────────────────────────────────────────
  // 通知列表  GET /api/notifications/store?storeId=1&page=1&size=50
  // ─────────────────────────────────────────────
  '/api/notifications/store': {
    code: 0,
    message: 'success',
    data: [
      { notificationId: 1, storeId: 1, type: 'order_new', title: '新订单提醒', content: '有新的预约订单 #1001 已确认到店', relatedType: 'reservation', relatedId: 1001, isRead: false, createdAt: '2026-06-12 15:30' },
      { notificationId: 2, storeId: 1, type: 'order_progress', title: '订单已完成', content: '预约 #1002 已用餐完成，桌位已设为清洁状态', relatedType: 'reservation', relatedId: 1002, isRead: false, createdAt: '2026-06-12 14:45' },
      { notificationId: 3, storeId: 1, type: 'refund_result', title: '退款已通过', content: '预约 #1003 的退款申请已通过', relatedType: 'refund', relatedId: 1, isRead: false, createdAt: '2026-06-12 11:20' },
      { notificationId: 4, storeId: 1, type: 'new_order', title: '新订单提醒', content: '有新的预约订单 #1004 已确认到店', relatedType: 'reservation', relatedId: 1004, isRead: true, createdAt: '2026-06-11 16:00' },
      { notificationId: 5, storeId: 1, type: 'order_progress', title: '订单已完成', content: '预约 #1005 已用餐完成', relatedType: 'reservation', relatedId: 1005, isRead: true, createdAt: '2026-06-11 12:00' }
    ]
  },

  // ─────────────────────────────────────────────
  // 通知未读数量  GET /api/notifications/unread/store?storeId=1
  // ─────────────────────────────────────────────
  '/api/notifications/unread/store': {
    code: 0,
    message: 'success',
    data: 3
  },

  // ─────────────────────────────────────────────
  // 标记单条已读  POST /api/notifications/read
  // ─────────────────────────────────────────────
  '/api/notifications/read': function(body) {
    return { code: 0, message: 'success', data: { success: true, message: '已标记为已读' } }
  },

  // ─────────────────────────────────────────────
  // 门店全部已读  POST /api/notifications/read-all/store
  // ─────────────────────────────────────────────
  '/api/notifications/read-all/store': function(body) {
    return { code: 0, message: 'success', data: { success: true, message: '已全部标记为已读', count: 3 } }
  }
}
