import axios from 'axios'

const api = axios.create({
  baseURL: 'http://192.168.0.85:85',
  timeout: 10000
})

/**
 * 用户登录
 * @param {Object} params - { username, password }
 */
export function login(params) {
  return api.post('/snapshot/auth', params)
}

/**
 * 获取待排程工单列表
 * @param {string} orderNo - 订单号
 */
export function fetchPendingOrders(orderNo) {
  return api.post('/snapshot/queryOrder', {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    orderNo
  })
}

/**
 * 保存排程工单
 * @param {Array} list - 排程列表数据
 */
export function saveSchedule(list) {
  return api.post('/snapshot/saveSfahuc', { list })
}

/**
 * 查询已保存的排程明细
 * @param {string} docNo - 单号
 */
export function fetchSavedSchedule(docNo) {
  return api.post('/snapshot/querySfahuc', { sfahucdocno: docNo })
}

/**
 * 删除排程明细
 * @param {Object} data - 删除条件
 */
export function deleteSchedule(data) {
  return api.post('/snapshot/deleteSfahuc', data)
}

/**
 * 查询主计划排程 BOM 结构树
 * @param {Object} params - { xmdddocno 订单号, xmdd001 品号 }
 * @returns {Promise} - { tree: [...] }
 */
export function fetchBomTree(params = {}) {
  return api.post('/snapshot/queryOrderBom', {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    ent: '60',
    site: 'NBYL',
    xmdddocno: params.xmdddocno ?? '',
    xmdd001: params.xmdd001 ?? '',
    xmddseq: params.xmddseq ?? ''
  })
}

/**
 * 查询供应商送货待收货情况
 * @param {Object} params - { czf, receiptQty, status, deliveryDateStart, deliveryDateEnd }
 */
export function queryDeliveryMatch(params = {}) {
  return api.post('/snapshot/queryDeliveryMatch', {
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
  return api.post('/snapshot/syncReceiptManual', list)
}

/**
 * 删除送货记录
 * @param {Array} list - 选中的明细列表
 */
export function deleteDeliveryMatch(list) {
  return api.post('/snapshot/deleteDelivery?token=e6338a4acxw502kmf5dwr316ss8u0ymb', list)
}

/**
 * 查询产线列表
 * @param {Object} params - { ooeluc003: 模糊匹配(可选), ooeluc004: 精确匹配(可选) }
 * @returns {Promise} - { data: [{ OOELUCSITE, OOELUCENT, OOELUC003, OOELUC004 }], total, success }
 */
export function queryLine(params = {}) {
  return api.post('/snapshot/queryOoeluc', {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    ooelucent: params.ooelucent ?? '60',
    ooelucsite: params.ooelucsite ?? 'NBYL',
    ooeluc003: params.ooeluc003 ?? '',
    ooeluc004: params.ooeluc004 ?? ''
  })
}
