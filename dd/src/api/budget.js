import axios from 'axios'

const api = axios.create({
  baseURL: 'http://192.168.0.85:85',
  timeout: 10000
})

/**
 * 查询预算明细
 * @param {Object} query - 查询条件，包含任意有值的字段
 */
export function queryBgbsuc(query) {
  return api.post('/snapshot/queryBgbsuc', {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    ...query
  })
}

/**
 * 保存预算明细（批量）
 * @param {Array} list - 预算明细列表
 */
export function saveBgbsuc(list) {
  return api.post('/snapshot/saveBgbsuc', {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    list
  })
}

/**
 * 删除预算（按单头条件批量删除）
 * @param {Object} params - { bgbsucent, bgbsucld, bgbsuc001, bgbsuc002 }
 */
export function deleteBgbsuc(params) {
  return api.post('/snapshot/deleteBgbsuc', {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    ...params
  })
}

/**
 * 查询部门列表
 * @param {Object} params - { ooefl001: 部门编号, ooefl003: 部门名称, ooeg003: 责任中心类型(可选) }
 */
export function queryDept(params) {
  const body = {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    ooefl001: params.ooefl001 || '',
    ooefl003: params.ooefl003 || '',
    ooefl002: 'zh_CN'
  }
  if (params.ooeg003) body.ooeg003 = params.ooeg003
  return api.post('/snapshot/queryDept', body)
}

/**
 * 查询科目列表
 * @param {Object} params - { glacl002: 科目编号, glacl004: 科目名称, glacl003: 默认"1", glacl007: 默认"6" }
 */
export function querySubject(params) {
  return api.post('/snapshot/querySubject', {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    glacl002: params.glacl002 || '',
    glacl004: params.glacl004 || '',
    glac003: params.glac003 || '2',
    glac007: params.glac007 || '6'
  })
}

/**
 * 查询预算报表
 * @param {Object} params - { ent, site, year, dept, subjectName, summary }
 */
export function queryBudgetReport(params) {
  return api.post('/snapshot/queryBudgetActualVariance', {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    ent: params.ent || '60',
    site: params.site || 'NBYL',
    year: params.year || '',
    dept: params.dept || '',
    subjectName: params.subjectName || '',
    summary: params.summary || ''
  })
}

/**
 * 查询预算采购核价
 * @param {Object} query - { bgbtucent, bgbtucld, bgbtuc001, bgbtuc002 }
 */
export function queryBgbtuc(query) {
  return api.post('/snapshot/queryBgbtuc', {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    ...query
  })
}

/**
 * 保存预算采购核价（批量：DB 无主键→插入；有→更新；前端没传→删除）
 * @param {Array} list - 预算采购核价列表
 */
export function saveBgbtuc(list) {
  return api.post('/snapshot/saveBgbtuc', {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    list
  })
}

/**
 * 删除预算采购核价（按所有非空字段匹配删除；bgbtuc002 必填）
 * @param {Object} params - { bgbtucent, bgbtucld, bgbtuc001, bgbtuc002 }
 */
export function deleteBgbtuc(params) {
  return api.post('/snapshot/deleteBgbtuc', {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    ...params
  })
}

/**
 * 查询预算采购价格分析表
 * @param {Object} params - { ent, site, lang, year, month }
 */
export function queryBudgetPurchaseAnalysis(params) {
  return api.post('/snapshot/queryBudgetPurchaseAnalysis', {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    ent: params.ent || '60',
    site: params.site || 'NBYL',
    lang: params.lang || 'zh_CN',
    year: params.year || '',
    month: params.month || ''
  })
}
