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
 *   - sfaastus    工单状态码（如 'C,F,M,N'），仅传值时才发送
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
  if (cond.sfaastus) body.sfaastus = cond.sfaastus
  // 扩展查询条件（开单/预计开工/预计完工 日期范围），仅传值时发送
  const orderDateKeys = ['sfaadocdt_start', 'sfaadocdt_end', 'sfaa019_start', 'sfaa019_end', 'sfaa020_start', 'sfaa020_end']
  orderDateKeys.forEach(k => { if (cond[k]) body[k] = cond[k] })
  return api.post('/queryOrder', body)
}

/**
 * 查询可排程工单（用于右表工单号下拉）
 * @param {Object} cond
 *   - sfaaent   企业代码（默认 60）
 *   - sfaasite  营运据点（默认 NBYL）
 *   - sfaa010   品号（必填，当前行品号）
 *   - sfaastus  状态码（默认 'M,F,N,C'）
 *   - sfaa022   来源单号（订单号，关联用，仅传值时发送）
 *   - sfaa023   来源序号（订单序号，关联用，仅传值时发送）
 * 注：sfaadocno(工单号) 不发送；sfaa022/sfaa023 仅调用方（关联）传入时才发送，下拉框不传
 * 返回 master[]：sfaadocno / sfaa010 / sfaa012 / kpsl(可排数量) /
 *                sfaa050 / sfaa019 / sfaa020 / sfaa068
 */
export function fetchSchedulableWorkOrders(cond = {}) {
  const body = {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    sfaaent: cond.sfaaent ?? '60',
    sfaasite: cond.sfaasite ?? 'NBYL',
    sfaa010: cond.sfaa010 ?? '',
    sfaastus: cond.sfaastus ?? 'M,F,N,C'
  }
  if (cond.sfaa022) body.sfaa022 = cond.sfaa022
  if (cond.sfaa023) body.sfaa023 = cond.sfaa023
  return api.post('/querySchedulableWorkOrders', body)
}

/**
 * 查询可排工单（关联订单专用，与绑定工单下拉分开，避免混用导致出错）
 * @param {Object} cond - sfaaent / sfaasite / sfaa010 品号 / sfaastus 状态码 /
 *   sfaa022 来源单号(订单号) / sfaa023 来源序号(订单序号)
 * 返回 master[]：sfaadocno / sfaa010 / sfaa012 / kpsl(可排数量) /
 *                sfaa050 / sfaa019 / sfaa020 / sfaa068
 */
export function queryWorkOrderBySource(cond = {}) {
  const body = {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    sfaaent: cond.sfaaent ?? '60',
    sfaasite: cond.sfaasite ?? 'NBYL',
    sfaa010: cond.sfaa010 ?? '',
    sfaastus: cond.sfaastus ?? 'M,F,N,C'
  }
  if (cond.sfaa022) body.sfaa022 = cond.sfaa022
  if (cond.sfaa023) body.sfaa023 = cond.sfaa023
  return api.post('/queryWorkOrderBySource', body)
}

/**
 * 删除已排程明细（/deleteSfahuc）
 * @param {Object} cond - sfahucent / sfahucsite / sfahuc001 工单号 /
 *   sfahuc002 品号 / sfahuc008 成本中心 / sfahuc004 订单号 / sfahuc005 订单序号
 */
export function deleteSfahucSchedule(cond = {}) {
  return api.post('/deleteSfahuc', {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    sfahucent: cond.sfahucent ?? '60',
    sfahucsite: cond.sfahucsite ?? 'NBYL',
    sfahuc001: cond.sfahuc001 ?? '',
    sfahuc002: cond.sfahuc002 ?? '',
    sfahuc008: cond.sfahuc008 ?? '',
    sfahuc004: cond.sfahuc004 ?? '',
    sfahuc005: cond.sfahuc005 ?? ''
  })
}

/**
 * 查询订单项已排数量
 * @param {Object} cond
 *   - sfahucent  企业代码（默认 60）
 *   - sfahucsite 营运据点（默认 NBYL）
 *   - sfahuc004  订单号（必填）
 *   - sfahuc005  订单序号（必填）
 *   - sfahuc002  品号（必填）
 * 返回：{ ypgds 已排数量, sfahuc004, sfahuc005, success }
 */
export function queryOrderScheduledQty(cond = {}) {
  return api.post('/queryOrderScheduledQty', {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    sfahucent: cond.sfahucent ?? '60',
    sfahucsite: cond.sfahucsite ?? 'NBYL',
    sfahuc004: cond.sfahuc004 ?? '',
    sfahuc005: cond.sfahuc005 ?? '',
    sfahuc002: cond.sfahuc002 ?? ''
  })
}

/**
 * 保存排程工单
 * @param {Array} list - 排程列表数据
 */
export function saveSchedule(list, userid = '') {
  return api.post('/saveSfahuc', {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    userid,
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
  // 扩展查询条件（开单/预计开工/预计完工 日期范围 + 状态码 + 生产日期范围），仅传值时发送
  const sfahucDateKeys = ['sfaadocdt_start', 'sfaadocdt_end', 'sfaa019_start', 'sfaa019_end', 'sfaa020_start', 'sfaa020_end', 'sfaastus', 'prod_start', 'prod_end']
  sfahucDateKeys.forEach(k => { if (cond[k]) body[k] = cond[k] })
  return api.post('/querySfahuc', body)
}

/**
 * 查询已保存排程明细（主计划排程右侧明细表用）
 * 始终发送账套 sfahucent 与营运据点 sfahucsite 作为条件，
 * 并按单头订单号(sfahuc004) + 订单序号(sfahuc005) 过滤。
 * @param {Object} cond
 *   - sfahucent  企业代码（默认 60）
 *   - sfahucsite 营运据点（默认 NBYL）
 *   - sfahuc004  订单号（单头订单号）
 *   - sfahuc005  订单序号（单头订单序号）
 *   - sfahuc008  成本中心（可选）
 *   - sfahuc001  工单号（可选）
 */
export function querySfahucSchedule(cond = {}) {
  const body = {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    sfahucent: cond.sfahucent ?? '60',
    sfahucsite: cond.sfahucsite ?? 'NBYL',
    sfahuc004: cond.sfahuc004 ?? '',
    sfahuc005: cond.sfahuc005 ?? ''
  }
  if (cond.sfahuc008) body.sfahuc008 = cond.sfahuc008
  if (cond.sfahuc001) body.sfahuc001 = cond.sfahuc001
  return api.post('/querySfahuc', body)
}

/**
 * 保存主计划排程明细（批量插入/更新 sfahuc 表）
 * @param {Array} list 行数据数组，每行字段见 querySfahucSchedule 返回结构
 *   sfahuc001(工单号) + sfahuc002(品号) 为判重主键
 *   可选：sfahucdocno/sfahucseq/sfahuc003/sfahuc004/sfahuc005/sfahuc006/sfahuc007/sfahuc008/sfahuc009/sfahuc010/sfahuc011
 */
export function saveSfahucSchedule(list = []) {
  const body = {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    sfahucent: '60',
    sfahucsite: 'NBYL',
    list: list.map(r => ({
      sfahucent: r.sfahucent ?? '60',
      sfahucsite: r.sfahucsite ?? 'NBYL',
      sfahuc001: r.sfahuc001 ?? '',
      sfahuc002: r.sfahuc002 ?? '',
      sfahucdocno: r.sfahucdocno ?? '',
      sfahucseq: r.sfahucseq ?? '',
      sfahuc003: r.sfahuc003 ?? '',
      sfahuc004: r.sfahuc004 ?? '',
      sfahuc005: r.sfahuc005 ?? '',
      sfahuc006: r.sfahuc006 ?? '',
      sfahuc007: r.sfahuc007 ?? '',
      sfahuc008: r.sfahuc008 ?? '',
      sfahuc009: r.sfahuc009 ?? '',
      sfahuc010: r.sfahuc010 ?? '',
      sfahuc011: r.sfahuc011 ?? '',
      sfahuc016: r.sfahuc016 ?? ''
    }))
  }
  return api.post('/saveSfahuc', body)
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
 *   - x_flag    'N' = 不展示虚拟件，且虚拟件的下阶上移到其父级；默认传 'N'
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
    imaf013: params.imaf013 ?? '2',
    x_flag: params.x_flag ?? 'N'
  }
  return api.post('/queryOrderBom', body)
}

/**
 * 查询未出货订单明细（主计划排程最左侧明细表）
 * 仅发送单头中已填写的条件，未填写的字段不发送到后端
 * @param {Object} cond
 *   - xmddent        企业代码（默认 60）
 *   - xmddsite       营运据点（默认 NBYL）
 *   - xmdddocno      订单号（单头，可选）
 *   - xmddseq        订单序号（单头，可选）
 *   - xmdd001        品号（单头，可选）
 *   - xmdd011_start  交货日期下限（可选）
 *   - xmdd011_end    交货日期上限（可选）
 *   - xmdadocdt_start 订单日期下限（可选）
 *   - xmdadocdt_end   订单日期上限（可选）
 *   - imaal003       品名（可选）
 *   - ooag011        业务员（可选）
 *   - imaf013        补货策略编码（可选：1=采购件 / 2=自制件 / 3=委外件）
 * 返回：{ list: [{ xmdddocno, xmddseq, xmdd001, imaal003 品名,
 *                  gzcbl004 补货策略名称, xmdd006, xmdd014,
 *                  undqty, xmdd011, pmaal004, ooag011, salesempno }], total, success }
 */
export function queryUnfinishedOrders(cond = {}) {
  const body = {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    xmddent: cond.xmddent ?? '60',
    xmddsite: cond.xmddsite ?? 'NBYL'
  }
  if (cond.xmdddocno) body.xmdddocno = cond.xmdddocno
  if (cond.xmddseq) body.xmddseq = cond.xmddseq
  if (cond.xmdd001) body.xmdd001 = cond.xmdd001
  if (cond.xmdd011_start) body.xmdd011_start = cond.xmdd011_start
  if (cond.xmdd011_end) body.xmdd011_end = cond.xmdd011_end
  if (cond.xmdadocdt_start) body.xmdadocdt_start = cond.xmdadocdt_start
  if (cond.xmdadocdt_end) body.xmdadocdt_end = cond.xmdadocdt_end
  if (cond.imaal003) body.imaal003 = cond.imaal003
  if (cond.ooag011) body.ooag011 = cond.ooag011
  if (cond.imaf013) body.imaf013 = cond.imaf013
  return api.post('/queryUnfinishedOrders', body)
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
 * 清除 SRM 在线用户记录（MySQL srm_login_number 表）
 * 请求体：{ token, name }，name 默认 'ALL' 表示清除全部
 * 后端返回字符串 '成功' / '失败'
 */
export function clearSrmLoginNumber(params = {}) {
  return api.post('/clearSrmLoginNumber', {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    name: params.name ?? 'ALL'
  })
}

/**
 * 查询自动清除在线用户的定时配置
 * 返回：{ enabled(布尔), clearTime("HH:mm"), success(布尔) }
 */
export function getClearSrmLoginConfig() {
  return api.post('/getClearSrmLoginConfig', {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb'
  })
}

/**
 * 保存自动清除在线用户的定时配置（启用/停用 + 时间）
 * @param {Object} params - { enabled(布尔), time("HH:mm") }
 */
export function setClearSrmLoginTime(params = {}) {
  return api.post('/setClearSrmLoginTime', {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    enabled: params.enabled ?? false,
    time: params.time ?? ''
  })
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
 * 查询品号描述（品名 / 规格）
 * @param {Object} cond - imaalent(默认60) / imaal002(默认zh_CN) / imaal001 品号
 * 返回：{ list: [{ imaal001 品号, imaal003 品名, imaal004 规格 }], total, success }
 */
export function searchItemDesc(cond = {}) {
  return api.post('/searchItemDesc', {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    imaalent: cond.imaalent ?? '60',
    imaal002: cond.imaal002 ?? 'zh_CN',
    imaal001: cond.imaal001 ?? ''
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

/**
 * 查询用户权限明细（菜单 + 功能按钮）
 * @param {Object} cond
 *   - gzypuc001 用户账号（必填）
 *   - gzypucent 企业代码，默认 '60'
 *   - gzypucld  营运据点，默认 'NBYL'
 * 返回：{ list: [{ gzypuc002 模块编号, gzypuc003 功能名(all/查询/保存/编辑) }], total, success }
 */
export function queryUserPermission(cond = {}) {
  return api.post('/queryUserPermission', {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    gzypucent: cond.gzypucent ?? '60',
    gzypucld: cond.gzypucld ?? 'NBYL',
    gzypuc001: cond.gzypuc001 ?? ''
  })
}

/**
 * 查询管理系统菜单目录树（gzweuc_t）
 * @param {Object} cond
 *   - ent 企业代码，默认 '60'
 * 返回: { success, tree: [{gzweuc001, gzweuc002, gzweuc003, gzweuc004, children}], total }
 */
export function fetchMenuTree(cond = {}) {
  return api.post('/queryMenuTree', {
    token: TOKEN,
    ent: cond.ent ?? '60'
  })
}

/** 固定 token（各接口请求体均带此值） */
export const TOKEN = 'e6338a4acxw502kmf5dwr316ss8u0ymb'

/**
 * 工单状态码 sfaastus 中文映射（权威定义，全项目统一使用）
 * M 成本结案 / C 生管结案 / F 已发放 / N 未审核 / X 已作废 / Y 已审核
 */
export const WORK_ORDER_STATUS_MAP = {
  M: '成本结案',
  C: '生管结案',
  F: '已发放',
  N: '未审核',
  X: '已作废',
  Y: '已审核'
}
