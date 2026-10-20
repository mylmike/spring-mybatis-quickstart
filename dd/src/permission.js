import { reactive } from 'vue'
import { queryUserPermission } from './api/pendingOrders.js'

/**
 * 模块编号（对应后端菜单字段 gzweuc002 / 权限返回 gzypuc002）
 * 需与后端权限配置保持一致。
 */
export const MODULE = {
  MAIN_SCHEDULE: '002',                 // 主计划排程（gzweuc002=002，父分类 001 计划编程）
  SHOP_SCHEDULE: '003',                 // 车间计划排程（gzweuc002=003）
  MATERIAL_SCHEDULE: '00103',           // 物料计划排程（gzweuc002=00103）
  ATTENDANCE: '004',                    // 人事考勤（gzweuc002=004）
  DELIVERY: '005',                      // 供应商送货待收货（gzweuc002=005）
  BUDGET_ENTRY: '007',                  // 预算录入（gzweuc002=007，父分类 006 财务核算）
  BUDGET_REPORT: '008',                 // 预算报表（gzweuc002=008）
  BUDGET_PURCHASE_PRICING: '009',       // 预算采购核价录入（gzweuc002=009）
  BUDGET_PURCHASE_PRICING_REPORT: '010' // 预算采购核价分析报表（gzweuc002=010）
}

/** 功能按钮类型（对应权限返回 gzypuc003） */
export const FUNC = {
  QUERY: '查询',
  SAVE: '保存',
  EDIT: '编辑'
}

/** 超级账号：拥有全部模块、全部功能的顶级权限（不依赖后端权限配置） */
export const SUPER_ACCOUNT = 'tiptop'

/**
 * 权限状态：
 *  modules - 允许访问的模块编号集合（Set<string>）
 *  funcs   - 每个模块允许的功能：'all' 表示全部开放，否则为 Set<功能名>
 *  isSuper - 是否为超级账号（tiptop）
 */
const state = reactive({
  loaded: false,
  userAccount: '',
  isSuper: false,
  modules: new Set(),
  funcs: new Map()
})

/**
 * 登录后调用：向后端 /queryUserPermission 拉取权限明细并写入 state。
 * @param {Object} opts
 *  - gzypuc001 用户账号（必填）
 *  - gzypucent 企业代码，默认 '60'
 *  - gzypucld  营运据点，默认 'NBYL'
 */
export async function fetchUserPermission({ gzypuc001, gzypucent = '60', gzypucld = 'NBYL' } = {}) {
  state.modules.clear()
  state.funcs.clear()
  state.userAccount = gzypuc001 || ''
  // 超级账号：跳过后端权限查询，直接开放所有模块与全部功能
  if (state.userAccount.trim().toLowerCase() === SUPER_ACCOUNT) {
    state.isSuper = true
    for (const mod of Object.values(MODULE)) {
      state.modules.add(mod)
      state.funcs.set(mod, 'all')
    }
    state.loaded = true
    return
  }
  state.isSuper = false
  try {
    const res = await queryUserPermission({ gzypuc001: gzypuc001 || '', gzypucent, gzypucld })
    const list = res.data?.list ?? []
    for (const item of list) {
      const mod = String(item.gzypuc002 ?? '').trim()
      const func = String(item.gzypuc003 ?? '').trim()
      if (!mod) continue
      state.modules.add(mod)
      if (!state.funcs.has(mod)) state.funcs.set(mod, new Set())
      if (func === 'all') {
        state.funcs.set(mod, 'all')        // 该模块全部功能开放
      } else if (func && state.funcs.get(mod) !== 'all') {
        state.funcs.get(mod).add(func)     // 仅开放指定功能
      }
    }
  } catch (e) {
    // 拉取失败：保守处理，不开放任何模块
    console.warn('获取用户权限失败:', e?.message || e)
  } finally {
    state.loaded = true
  }
}

/** 模块是否在权限范围内（菜单是否显示） */
export function canViewModule(moduleNo) {
  return state.modules.has(String(moduleNo))
}

/** 模块是否开放某功能按钮（查询/保存/编辑） */
export function canFunc(moduleNo, func) {
  if (!canViewModule(moduleNo)) return false
  const f = state.funcs.get(String(moduleNo))
  if (f === 'all') return true
  if (f instanceof Set) return f.has(func)
  return false
}
