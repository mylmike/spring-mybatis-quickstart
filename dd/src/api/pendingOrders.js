import axios from 'axios'

const api = axios.create({
  baseURL: (typeof window !== 'undefined' ? window.location.origin : '') + '/snapshot',
  timeout: 30000
})

/**
 * 用户登录
 * @param {Object} params - { username, password }
 */
export function login(params) {
  return api.post('/auth', params)
}

/**
 * 获取待排程工单列表
 * @param {Object} cond - 查询条件
 *   - orderNo     订单号
 *   - orderSeq    订单序号 (sfaa023)，仅传值时才发送
 *   - sfaa068     成本中心编号
 *   - sfaadocno   工单号
 *   - sfaa010     生产料号
 *   - sfaaent     企业代码（默认 60）
 *   - sfaasite    营运据点（默认 NBYL）
 *   - rowMax      上限（默认 200，<=1000）
 */
export function fetchPendingOrders(cond = {}) {
  const body = {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    sfaaent: cond.sfaaent ?? '60',
    sfaasite: cond.sfaasite ?? 'NBYL',
    orderNo: cond.orderNo ?? '',
    sfaa068: cond.sfaa068 ?? '',
    sfaadocno: cond.sfaadocno ?? '',
    sfaa010: cond.sfaa010 ?? '',
    rowMax: cond.rowMax ?? 200
  }
  if (cond.orderSeq) body.sfaa023 = cond.orderSeq
  return api.post('/queryOrder', body)
}

/**
 * 保存排程工单
 * @param {Array} list - 排程列表数据
 */
export function saveSchedule(list) {
  return api.post('/saveSfahuc', {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    list
  })
}

/**
 * 保存每日计划数量（工单号 + 产线 + 日期）
 * @param {Array} list - [{ sfajucent, sfajucsite, sfajuc001, sfajuc004, sfajuc007, sfajuc003 }]
 */
export function saveDailyPlan(list) {
  return api.post('/saveSfajucDailyPlan', {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    list
  })
}

/**
 * 查询每日计划数量
 * @param {Object} params - { sfajuc001 工单号, sfajuc004 产线, sfajuc007 日期(可空=查全部) }
 */
export function queryDailyPlan(params = {}) {
  return api.post('/querySfajucDailyPlan', {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    sfajuc001: params.sfajuc001 ?? '',
    sfajuc004: params.sfajuc004 ?? '',
    sfajuc007: params.sfajuc007 ?? ''
  })
}

/**
 * 查询已保存的排程明细
 * @param {string} docNo - 单号
 * @param {string} sfahuc008 - 成本中心编号
 */
/**
 * 查询已保存排程明细（querySfahuc）
 * 仅发送单头中已填写的条件，未填写的字段不发送到后端
 * @param {Object} cond
 *   - enterpriseCode 企业代码 (sfahucent)
 *   - site           营运据点 (sfahucsite)
 *   - costCenter     成本中心 (sfahuc008)
 *   - customerOrderNo 订单号 (sfahuc004)
 *   - orderSeq       订单序号 (sfahuc005)
 *   - workOrderNo    工单号 (sfahuc001)
 */
export function fetchSavedSchedule(cond = {}) {
  const body = {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb'
  }
  if (cond.enterpriseCode) body.sfahucent = cond.enterpriseCode
  if (cond.site) body.sfahucsite = cond.site
  if (cond.costCenter) body.sfahuc008 = cond.costCenter
  if (cond.customerOrderNo) body.sfahuc004 = cond.customerOrderNo
  if (cond.orderSeq) body.sfahuc005 = cond.orderSeq
  if (cond.workOrderNo) body.sfahuc001 = cond.workOrderNo
  return api.post('/querySfahuc', body)
}

/**
 * 按 订单号 + 订单序号 + 工单号 查数据库是否已存在该排程记录
 * 返回 master 数组，若非空则说明数据库已有该记录
 */
export function querySfahucExists({ orderNo, orderSeq, workOrderNo }) {
  return api.post('/querySfahuc', {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    sfahucdocno: '',
    sfahuc008: '',
    sfahuc004: orderNo || '',
    sfahuc005: orderSeq || '',
    sfahuc001: workOrderNo || ''
  })
}

/**
 * 删除排程明细
 * @param {Object} data - 删除条件
 */
export function deleteSchedule(data) {
  return api.post('/deleteSfahuc', {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    ...data
  })
}

/**
 * 查询主计划排程 BOM 结构树
 * @param {Object} params
 *   - xmdddocno 订单号
 *   - xmdd001   品号
 *   - xmddseq   订单序号
 *   - imaf013   '2' = 只返回自制件；不传或 '' 则由后端返回全部
 *   - ent / site 默认 60 / NBYL
 * @returns {Promise} - { tree: [...], flat: [...], total, success }
 */
export function fetchBomTree(params = {}) {
  const body = {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    ent: params.ent ?? '60',
    site: params.site ?? 'NBYL',
    xmdddocno: params.xmdddocno ?? '',
    xmddseq: params.xmddseq ?? '',
    xmdd001: params.xmdd001 ?? '',
    imaf013: params.imaf013 ?? '2'
  }
  return api.post('/queryOrderBom', body)
}

/**
 * 查询供应商送货待收货情况
 * @param {Object} params - { czf, receiptQty, status, deliveryDateStart, deliveryDateEnd }
 */
export function queryDeliveryMatch(params = {}) {
  return api.post('/queryDeliveryMatch', {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    ent: '60',
    site: 'NBYL',
    czf: params.czf ?? '',
    receiptQty: params.receiptQty ?? '',
    status: params.status ?? '',
    deliveryDateStart: params.deliveryDateStart ?? '',
    deliveryDateEnd: params.deliveryDateEnd ?? ''
  })
}

/**
 * 更新送货异常数据
 * @param {Array} list - 选中的明细列表
 */
export function updateDeliveryMatch(list) {
  return api.post('/syncReceiptManual', list)
}

/**
 * 删除送货记录
 * @param {Array} list - 选中的明细列表
 */
export function deleteDeliveryMatch(list) {
  return api.post('/deleteDelivery?token=e6338a4acxw502kmf5dwr316ss8u0ymb', list)
}

/**
 * 查询产线列表
 * @param {Object} params - { ooeluc003: 模糊匹配(可选), ooeluc004: 精确匹配(可选) }
 * @returns {Promise} - { data: [{ OOELUCSITE, OOELUCENT, OOELUC003, OOELUC004 }], total, success }
 */
export function queryLine(params = {}) {
  return api.post('/queryOoeluc', {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    ooelucent: params.ooelucent ?? '60',
    ooelucsite: params.ooelucsite ?? 'NBYL',
    ooeluc003: params.ooeluc003 ?? '',
    ooeluc004: params.ooeluc004 ?? ''
  })
}

/**
 * 查询未交订单列表（车间计划排程订单下拉）
 * @param {Object} params - { ent: 企业代码 }
 * @returns {Promise} - { list: [{ item, docDate, label, docno, undeliveredQty, value, seq, customer }] }
 */
export function queryUndeliveredOrders(params = {}) {
  return api.post('/queryUndeliveredOrders', {
    ent: params.ent ?? '60',
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb'
  })
}

/**
 * 查询品号基本信息（标准工时 / UPPH）
 * @param {string} imae001 - 品号
 * @returns {Promise} - { data: [{ imae051, 标准工时, UPPH, upph }], total, success }
 */
export function queryItemBasicInfo(imae001) {
  return api.post('/queryItemBasicInfo', {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    imae001: imae001 ?? ''
  })
}

/**
 * 查询工单状态（结案状态）
 * @param {string} sfaadocno - 工单号
 * @returns {Promise} - { head: [{ sfaastus, ... }], total, success }
 */
export function queryWorkOrder(sfaadocno) {
  return api.post('/queryWorkOrder', {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    sfaadocno: sfaadocno ?? ''
  })
}

/** 工单结案状态中文映射 */
export const WORK_ORDER_STATUS_MAP = {
  F: '已发放',
  C: '已结案',
  M: '成本结案',
  N: '未审核',
  Y: '已审核'
}
