<template>
  <div class="schedule-page">
    <el-card class="basic-card" shadow="never">
      <el-form :model="form" label-width="90px" class="basic-form">
        <el-row class="compact-row no-gutter-row">
          <el-col class="tight-col">
            <el-form-item label="企业代码" required class="required-field">
              <el-input v-model="form.enterpriseCode" style="width: 40px" />
            </el-form-item>
          </el-col>
          <el-col class="tight-col">
            <el-form-item label="单号" required class="required-field">
              <el-input v-model="form.orderNo" clearable style="width: 80px" />
            </el-form-item>
          </el-col>
          <el-col class="tight-col">
            <el-form-item label="成本中心">
              <div class="dept-select">
                <el-select-v2
                  v-model="form.costCenter"
                  filterable
                  clearable
                  :loading="deptLoading"
                  :options="deptOptions"
                  placeholder="请输入或选择"
                  class="dept-select-input"
                  @change="handleCostCenterChange"
                  @visible-change="handleCostCenterVisible"
                />
                <span v-if="deptName" class="dept-name-tag">{{ deptName }}</span>
              </div>
            </el-form-item>
          </el-col>
          <el-col class="tight-col">
            <el-form-item label="订单号">
              <el-select
                v-model="orderSelectValue"
                filterable
                clearable
                :loading="orderLoading"
                placeholder="请输入或选择"
                class="order-select-input"
                @change="handleOrderChange"
                @visible-change="handleOrderVisible"
                @keyup.enter="loadPendingOrders"
              >
                <el-option
                  v-for="opt in orderOptions"
                  :key="opt.key"
                  :label="opt.docno"
                  :value="opt.key"
                >
                  <div class="order-option">
                    <span class="col-docno">{{ opt.docno }}</span>
                    <span class="col-seq">{{ opt.seq }}</span>
                    <span class="col-item">{{ opt.item }}</span>
                    <span class="col-date">{{ opt.docDate }}</span>
                    <span class="col-customer">{{ opt.customer }}</span>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col class="tight-col">
            <el-form-item label="订单序号">
              <el-input v-model="form.orderSeq" clearable style="width: 48px" />
            </el-form-item>
          </el-col>
          <el-col class="tight-col">
            <el-form-item label="工单号">
              <el-input v-model="form.workOrderNo" clearable style="width: 150px" @keyup.enter="loadPendingOrders" />
            </el-form-item>
          </el-col>
          <el-col class="tight-col">
            <el-form-item label="生产料号">
              <el-input v-model="form.itemNo" clearable style="width: 110px" @keyup.enter="loadPendingOrders" />
            </el-form-item>
          </el-col>
          <el-col class="tight-col">
            <el-form-item label="预计日期">
              <el-date-picker v-model="form.estimatedDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 104px" />
            </el-form-item>
          </el-col>
          <el-col class="tight-col">
            <el-form-item label="营运据点">
              <el-input v-model="form.site" clearable style="width: 78px" />
            </el-form-item>
          </el-col>
          <el-col class="tight-col form-actions-col">
            <el-form-item label=" ">
              <div class="form-actions">
                <el-button type="primary" size="small" icon="Search" @click="loadPendingOrders">查询</el-button>
                <el-button type="primary" size="small" :disabled="leftSelected.length === 0" @click="moveToRight">&gt;</el-button>
                <el-button type="warning" size="small" :disabled="rightSelected.length === 0" @click="moveToLeft">&lt;</el-button>
                <el-button type="success" size="small" :disabled="scheduledList.length === 0" @click="handleSave">保存</el-button>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <div class="table-area" ref="tableArea">
      <div class="left-panel" :style="{ width: leftPanelWidth }">
        <el-table
          ref="leftTable"
          :data="pendingList"
          v-loading="loading"
          border
          height="100%"
          @selection-change="handleLeftSelection"
        >
          <el-table-column type="selection" width="26" label="选取" />
          <el-table-column prop="sourceOrderNo" label="来源单号" :width="leftSourceOrderNoWidth" sortable :sort-method="sortByText('sourceOrderNo')" />
          <el-table-column prop="seq" label="序号" width="42" align="right" sortable :sort-method="sortByText('seq')" />
          <el-table-column prop="itemNo" label="品号" width="104" sortable :sort-method="sortByText('itemNo')" />
          <el-table-column prop="costCenterCode" label="站点" width="65" sortable :sort-method="sortByText('costCenterCode')" />
          <el-table-column prop="costCenter" label="成本中心" min-width="113" sortable :sort-method="sortByText('costCenter')" />
          <el-table-column prop="orderNo" label="生产数量" width="65" align="right" sortable :sort-method="sortByNumber('orderNo')" />
          <el-table-column prop="stockInQty" label="入库量" width="70" align="right" sortable :sort-method="sortByNumber('stockInQty')" />
          <el-table-column prop="balanceQty" label="余量" width="70" align="right" sortable :sort-method="sortByNumber('balanceQty')" />
          <el-table-column prop="upph" label="UPPH值" width="70" align="right" sortable :sort-method="sortByNumber('upph')" />
          <el-table-column prop="workOrderNo" label="工单号" :width="leftWorkOrderNoWidth" sortable :sort-method="sortByText('workOrderNo')" />
          <el-table-column prop="workOrderStatus" label="工单结案" width="80" sortable :sort-method="sortByText('workOrderStatus')" />
          <el-table-column prop="estimatedStartDate" label="预计开工" min-width="110" :formatter="formatDate" sortable />
          <el-table-column prop="estimatedEndDate" label="预计完工" min-width="110" :formatter="formatDate" sortable />
        </el-table>
      </div>

      <div class="table-resizer" title="拖拽调整左表宽度" @mousedown="startResize" />

      <div class="right-panel">
        <el-table
          ref="rightTable"
          :data="scheduledList"
          border
          height="100%"
          @selection-change="handleRightSelection"
        >
          <el-table-column type="selection" width="26" label="选取" />
          <el-table-column type="index" label="项次" width="42" />
          <el-table-column prop="sourceOrderNo" label="来源单号" :width="rightSourceOrderNoWidth" />
          <el-table-column prop="seq" label="序号" width="42" />
          <el-table-column prop="workOrderNo" label="工单号" :width="rightWorkOrderNoWidth" />
          <el-table-column prop="workOrderStatus" label="工单结案" width="80" />
          <el-table-column prop="itemNo" label="品号" width="104" />
          <el-table-column prop="costCenterCode" label="站点" width="65" />
          <el-table-column prop="costCenter" label="成本中心" min-width="113" />
          <el-table-column label="产线" width="156">
            <template #default="{ row }">
              <el-select
                v-model="row.productionLine"
                filterable
                clearable
                :loading="lineLoading"
                placeholder="选择产线"
                class="line-select"
                style="width: 100%"
                @visible-change="handleLineVisible(row, $event)"
              >
                <el-option
                  v-for="opt in lineOptions"
                  :key="opt.value"
                  :label="opt.value"
                  :value="opt.value"
                >
                  <span>{{ opt.label }}</span>
                </el-option>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column prop="orderNo" label="生产数量" width="65" />
          <el-table-column prop="stockInQty" label="入库量" width="70" />
          <el-table-column prop="balanceQty" label="余量" width="70" />
          <el-table-column prop="upph" label="UPPH值" width="70" />
          <el-table-column label="预计开工" min-width="160">
            <template #default="{ row }">
              <el-date-picker
                v-model="row.estimatedStartDate"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                style="width: 140px"
              />
            </template>
          </el-table-column>
          <el-table-column label="预计完工" min-width="160">
            <template #default="{ row }">
              <el-date-picker
                v-model="row.estimatedEndDate"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                style="width: 140px"
              />
            </template>
          </el-table-column>
          <!-- 动态日期列：所有工单的最早预计开工 ~ 最晚预计完工，每天一列（可编辑，默认 0） -->
          <el-table-column
            v-for="date in dateColumns"
            :key="date"
            :label="formatColumnLabel(date)"
            min-width="80"
            align="center"
          >
            <template #header>
              <div class="date-col-header">
                <div class="date-col-date">{{ formatColumnLabel(date) }}</div>
                <div class="date-col-week">{{ weekdayOf(date) }}</div>
              </div>
            </template>
            <template #default="{ row }">
              <el-input-number
                v-if="isInRange(row, date)"
                :model-value="getDailyValue(row, date)"
                :min="0"
                :controls="false"
                size="small"
                :class="['daily-input', 'center-input', `daily-status-${row.dailyStatus || 'none'}`]"
                @update:model-value="setDailyValue(row, date, $event)"
              />
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchPendingOrders, saveSchedule, fetchSavedSchedule, deleteSchedule, queryLine, queryUndeliveredOrders, saveDailyPlan, queryDailyPlan, queryItemBasicInfo, querySfahucExists, queryWorkOrder, WORK_ORDER_STATUS_MAP } from '../api/pendingOrders.js'
import { queryDept } from '../api/budget.js'

/** 生成默认单号：年月日 + 001 */
const genDocNo = () => {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}${m}${day}001`
}

const form = reactive({
  orderNo: genDocNo(),
  costCenter: '',
  customerOrderNo: '',
  orderSeq: '',
  estimatedDate: '',
  site: 'NBYL',
  enterpriseCode: '60',
  workOrderNo: '',
  itemNo: ''
})

/** 成本中心下拉选项 */
const deptOptions = ref([])
/** 成本中心下拉加载状态 */
const deptLoading = ref(false)
/** 选中成本中心的名称 */
const deptName = ref('')

/** 产线下拉选项（value=OOELUC003，label 显示 OOELUC003 - OOELUC004） */
const lineOptions = ref([])
/** 产线下拉加载状态 */
const lineLoading = ref(false)

/** 订单下拉选项（未交订单） */
const orderOptions = ref([])
/** 订单下拉加载状态 */
const orderLoading = ref(false)
/** 订单下拉选中值：docno||seq */
const orderSelectValue = ref('')

/** 加载未交订单列表 */
let ordersLoaded = false
let ordersLoadPromise = null
const fetchOrders = async () => {
  if (ordersLoaded) return ordersLoadPromise
  if (orderLoading.value) return ordersLoadPromise
  orderLoading.value = true
  ordersLoadPromise = (async () => {
  try {
    const res = await queryUndeliveredOrders({ ent: form.enterpriseCode || '60' })
    const list = res.data?.list ?? res.data?.master ?? res.data?.data ?? []
    orderOptions.value = list.map(item => ({
      key: `${item.docno || ''}||${item.seq || ''}`,
      docno: item.docno || '',
      seq: item.seq || '',
      item: item.item || '',
      docDate: String(item.docDate || '').substring(0, 10),
      customer: item.customer || '',
      label: item.label || '',
      undeliveredQty: item.undeliveredQty ?? ''
    }))
    ordersLoaded = true
  } catch (err) {
    console.warn('加载订单列表失败:', err?.message || err)
    ordersLoaded = false
  } finally {
    orderLoading.value = false
  }
  })()
  return ordersLoadPromise
}

/** 选中订单后回填：docno -> 订单号，seq -> 订单序号 */
const handleOrderChange = (val) => {
  const hit = orderOptions.value.find(o => o.key === val)
  if (hit) {
    form.customerOrderNo = hit.docno
    form.orderSeq = hit.seq
  } else {
    form.customerOrderNo = ''
    form.orderSeq = ''
  }
}

/** 下拉展开时刷新订单列表 */
const handleOrderVisible = (visible) => {
  if (visible) {
    fetchOrders()
  }
}

/** 产线选项缓存：key = 成本中心编号（空串表示不筛选） */
const lineOptionCache = {}

/** 请求产线列表并缓存（内部调用，不做 loading 去重） */
const loadLines = async (ooeluc004 = '') => {
  const res = await queryLine({ ooeluc003: '', ooeluc004 })
  const list = res.data?.master ?? res.data?.data ?? []
  const get = (obj, ...keys) => {
    for (const k of keys) {
      const hit = Object.keys(obj).find(x => x.toLowerCase() === k.toLowerCase())
      if (hit && obj[hit] != null && obj[hit] !== '') return obj[hit]
    }
    return ''
  }
  const options = list.map(item => {
    const code = get(item, 'OOELUC003')
    const name = get(item, 'OOELUC004')
    return { value: code, label: `${code} - ${name}` }
  })
  lineOptionCache[ooeluc004 || ''] = options
  lineOptions.value = options
  return options
}

/** 加载产线列表（ooeluc004 精确匹配站点） */
const fetchLines = async (ooeluc004 = '') => {
  if (lineLoading.value) return
  lineLoading.value = true
  try {
    await loadLines(ooeluc004)
  } catch (err) {
    console.warn('加载产线失败:', err?.message || err)
  } finally {
    lineLoading.value = false
  }
}

/** 取某成本中心对应的默认产线（该成本中心产线列表的第一条） */
const getDefaultLine = async (costCenterCode) => {
  const key = costCenterCode || ''
  let options = lineOptionCache[key]
  if (!options || options.length === 0) {
    try {
      options = await loadLines(key)
    } catch (err) {
      console.warn('获取默认产线失败:', err?.message || err)
      return ''
    }
  }
  return options && options.length > 0 ? options[0].value : ''
}

const pendingList = ref([])
const loading = ref(false)

const scheduledList = ref([])
/** 加载时保存的原始快照，用于判断是否有修改 */
const originalScheduledJson = ref('')
const leftSelected = ref([])
const rightSelected = ref([])
const leftTable = ref(null)
const rightTable = ref(null)
/** 左明细表宽度（初始 37.5%，拖拽后变为像素值） */
const leftPanelWidth = ref('37.5%')
const tableArea = ref(null)
const resizing = ref(false)

/** 拖拽调整左表宽度 */
const startResize = (e) => {
  resizing.value = true
  document.body.style.cursor = 'col-resize'
  document.body.style.userSelect = 'none'
  document.addEventListener('mousemove', onResizeMove)
  document.addEventListener('mouseup', stopResize)
  e.preventDefault()
}

const onResizeMove = (e) => {
  if (!resizing.value) return
  const area = tableArea.value
  if (!area) return
  const rect = area.getBoundingClientRect()
  const minW = 10
  const maxW = Math.max(rect.width - 420, minW)
  const w = Math.min(Math.max(e.clientX - rect.left, minW), maxW)
  leftPanelWidth.value = `${w}px`
}

const stopResize = () => {
  resizing.value = false
  document.body.style.cursor = ''
  document.body.style.userSelect = ''
  document.removeEventListener('mousemove', onResizeMove)
  document.removeEventListener('mouseup', stopResize)
}

onUnmounted(() => {
  document.removeEventListener('mousemove', onResizeMove)
  document.removeEventListener('mouseup', stopResize)
})

/** 日期格式化，去掉时间部分，只保留 YYYY-MM-DD */
const formatDate = (row, column, cellValue) => {
  if (!cellValue) return ''
  return String(cellValue).substring(0, 10)
}

/**
 * 生成数值排序比较器（el-table 的 sort-method 入参是行对象，需自行取字段）。
 * 用 Number 转换，避免字符串比较出现 "10" < "9"。
 */
const sortByNumber = (prop) => (a, b) =>
  (Number(a?.[prop]) || 0) - (Number(b?.[prop]) || 0)

/**
 * 生成文本排序比较器：空值恒排末尾，其余按中文 + 数字混合规则排序
 * （numeric: true 使 A2 排在 A10 前面）。
 */
const sortByText = (prop) => (a, b) => {
  const sa = String(a?.[prop] ?? '').trim()
  const sb = String(b?.[prop] ?? '').trim()
  if (sa === '' && sb === '') return 0
  if (sa === '') return 1
  if (sb === '') return -1
  return sa.localeCompare(sb, 'zh-Hans-CN', { numeric: true })
}

/** 加载成本中心列表（与预算录入部门一致），已加载过则直接使用缓存 */
let deptLoaded = false
let deptLoadPromise = null
const fetchAllDepts = async () => {
  if (deptLoaded) return deptLoadPromise
  if (deptLoading.value) return deptLoadPromise
  deptLoading.value = true
  deptLoadPromise = (async () => {
    try {
      const res = await queryDept({ ooefl001: '', ooefl003: '', ooeg003: '3' })
      const list = res.data?.master ?? res.data?.data ?? []
      // 大小写不敏感取值
      const get = (obj, ...keys) => {
        for (const k of keys) {
          const hit = Object.keys(obj).find(x => x.toLowerCase() === k.toLowerCase())
          if (hit && obj[hit] != null && obj[hit] !== '') return obj[hit]
        }
        return ''
      }
      const options = list.map(item => {
        const code = get(item, 'ooefl001')
        const name = get(item, 'ooefl003')
        return { value: code, label: `${code} - ${name}` }
      })
      if (options.length > 0) {
        deptOptions.value = options
        // 站点（成本中心编码）-> 成本中心名称，供明细行按站点显示成本中心
        const map = {}
        for (const o of options) {
          map[o.value] = o.label.split(' - ').slice(1).join(' - ')
        }
        deptNameMap.value = map
        deptLoaded = true
      }
    } catch (err) {
      console.warn('加载成本中心失败:', err?.message || err)
      deptLoaded = false
    } finally {
      deptLoading.value = false
    }
  })()
  return deptLoadPromise
}

/** 成本中心编码（站点）-> 成本中心名称 映射 */
const deptNameMap = ref({})

/** 根据站点（成本中心编码）取成本中心名称 */
const getCostCenterName = (code) => {
  if (!code) return ''
  return deptNameMap.value[code] || ''
}

/** 同步某行的成本中心名称：始终以站点（costCenterCode）为准 */
const syncCostCenterName = (row) => {
  if (!row) return
  const name = getCostCenterName(row.costCenterCode)
  if (name && row.costCenter !== name) {
    row.costCenter = name
  }
}

/** 确保成本中心映射已加载 */
const ensureDeptMap = async () => {
  if (Object.keys(deptNameMap.value).length > 0) return
  await fetchAllDepts()
}

/**
 * 只要站点（costCenterCode）变动，成本中心名称就跟着变。
 * 覆盖查询回载、插入新行、保存等所有场景。
 */
watch(
  [scheduledList, pendingList],
  () => {
    for (const row of scheduledList.value) syncCostCenterName(row)
    for (const row of pendingList.value) syncCostCenterName(row)
  },
  { deep: true }
)

/** 下拉展开时按当前行站点过滤产线 */
const handleLineVisible = (row, visible) => {
  if (visible) {
    fetchLines(row.costCenterCode || '')
  }
}

/** 组件挂载时只预加载成本中心和产线（都很小、快）；
 *  订单下拉数据量大，改为用户首次点开时才加载，避免首屏三个请求并发挤占 */
onMounted(() => {
  fetchAllDepts()
  fetchLines()
})

/** 成本中心值改变时回填名称 */
const handleCostCenterChange = (val) => {
  const found = deptOptions.value.find(o => o.value === val)
  deptName.value = found ? found.label.split(' - ').slice(1).join(' - ') : ''
}

/** 下拉框展开时确保列表已加载（复用首次请求的 Promise，避免重复建连） */
const handleCostCenterVisible = (visible) => {
  if (visible) {
    fetchAllDepts()
  }
}

/** 从后端加载待排程工单和已保存的排程明细 */
const loadPendingOrders = async () => {
  loading.value = true
  try {
    // 1. 加载左侧待排程工单
    const reqBody = {
      token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
      sfaaent: form.enterpriseCode || '60',
      sfaasite: form.site || 'NBYL',
      orderNo: form.customerOrderNo || '',
      sfaa068: form.costCenter || '',
      sfaadocno: form.workOrderNo || '',
      sfaa010: form.itemNo || '',
      rowMax: 200
    }
    const [res] = await Promise.all([
      fetchPendingOrders(reqBody),
      ensureDeptMap()
    ])
    const rawList = res.data?.master ?? res.data?.data ?? []
    pendingList.value = rawList.map(item => ({
      orderNo: item.sfaa012 || '',
      seq: item.sfaa023 || '',
      itemNo: item.sfaa010 || '',
      costCenterCode: item.sfaa068 || '',
      costCenter: item.ooefl003 || '',
      sourceOrderNo: item.sfaa022 || '',
      workOrderNo: item.sfaadocno || '',
      estimatedStartDate: item.sfaa019 || '',
      estimatedEndDate: item.sfaa020 || '',
      stockInQty: item.sfaa050 ?? '',
      balanceQty: item['余量'] ?? '',
      workOrderStatus: mapWorkOrderStatus(item.sfaastus)
    }))
    await fillUpph(pendingList.value)

    // 2. 加载右侧已保存排程明细（fetchSavedSchedule 内部只发送已填写的条件）
    let savedRes
    try {
      savedRes = await fetchSavedSchedule({
        enterpriseCode: form.enterpriseCode,
        site: form.site,
        costCenter: form.costCenter,
        customerOrderNo: form.customerOrderNo,
        orderSeq: form.orderSeq
      })
    } catch (e) {
      console.error('[querySfahuc] 查询异常', e)
      scheduledList.value = []
      // 异常不阻断继续（左侧查询仍可走）
    }
    if (savedRes) {
      const rawSaved = savedRes.data?.master ?? savedRes.data?.data ?? []
      scheduledList.value = rawSaved.map(item => ({
        sfahucseq: item.sfahucseq || '',
        workOrderNo: item.sfahuc001 || '',
        itemNo: item.sfahuc002 || '',
        orderNo: item.sfahuc003 || '',
        sourceOrderNo: item.sfahuc004 || '',
        seq: item.sfahuc005 || '',
        estimatedStartDate: item.sfahuc006 || '',
        estimatedEndDate: item.sfahuc007 || '',
        costCenterCode: item.sfahuc008 || '',
        costCenter: item.costCenter || '',
        productionLine: item.sfahuc010 || item.productionLine || '',
        dailyPlan: item.dailyPlan || {}
      }))
      // 已保存明细若产线为空：按本行成本中心取第一条产线作为默认
      await Promise.all(scheduledList.value.map(async (r) => {
        if (!r.productionLine) {
          r.productionLine = await getDefaultLine(r.costCenterCode)
        }
      }))
      await fillUpph(scheduledList.value)
      await fillOrderExtra(scheduledList.value)
      scheduledList.value.forEach(refreshDailyStatus)
    }

    // 3. 过滤左侧：已存在于右侧的（订单号+工单号一致）不显示
    if (scheduledList.value.length > 0) {
      const scheduledKeys = new Set(scheduledList.value.map(r => `${r.sourceOrderNo}||${r.workOrderNo}`))
      pendingList.value = pendingList.value.filter(r => !scheduledKeys.has(`${r.sourceOrderNo}||${r.workOrderNo}`))
    }

    // 4. 二次校验：左侧每条记录按 订单号+订单序号+工单号 查数据库，
    //    若 querySfahuc 返回 sfahuc001 有值，说明数据库已存在该记录，则不显示在左边。
    //    限制并发（一次最多 4 个），避免大量记录同时请求拖慢首屏。
    const existingInDb = new Set()
    const pendingRows = pendingList.value
    const CONCURRENCY = 4
    for (let i = 0; i < pendingRows.length; i += CONCURRENCY) {
      const batch = pendingRows.slice(i, i + CONCURRENCY)
      await Promise.all(batch.map(async (r) => {
        try {
          const res = await querySfahucExists({
            orderNo: r.sourceOrderNo,
            orderSeq: r.orderSeq,
            workOrderNo: r.workOrderNo
          })
          const master = res.data?.master ?? res.data?.data ?? []
          if (Array.isArray(master) && master.length > 0 && master[0].sfahuc001) {
            existingInDb.add(`${r.sourceOrderNo}||${r.workOrderNo}`)
          }
        } catch (e) {
          // 查询异常不阻断：默认视为不存在，保留显示
        }
      }))
    }
    if (existingInDb.size > 0) {
      pendingList.value = pendingList.value.filter(r => !existingInDb.has(`${r.sourceOrderNo}||${r.workOrderNo}`))
    }

    // 3.5 加载右侧每行的每日计划数量
    await loadDailyPlans()

    // 4. 保存原始快照，用于判断保存时是否有修改
    originalScheduledJson.value = JSON.stringify(scheduledList.value)
  } catch (err) {
    ElMessage.error('获取待排程工单失败: ' + (err.response?.data?.message || err.message))
  } finally {
    loading.value = false
  }
}

/** 生成某行的日期列表（预计开工 ~ 预计完工，每天一个），用于逐日查询兜底 */
const rowDateRange = (row) => {
  const start = toDate(row.estimatedStartDate)
  const end = toDate(row.estimatedEndDate)
  if (!start || !end || start > end) return []
  const list = []
  const cur = parseYMD(start)
  const last = parseYMD(end)
  while (cur.getTime() <= last.getTime()) {
    list.push(formatYMD(cur))
    cur.setDate(cur.getDate() + 1)
  }
  return list
}

/** 加载右侧每行的每日计划数量，回填到 row.dailyPlan */
const loadDailyPlans = async () => {
  const rows = scheduledList.value.filter(r => r.workOrderNo)
  if (rows.length === 0) return

  const pick = (plan, list) => {
    for (const item of list || []) {
      const date = String(item.sfajuc007 ?? '').substring(0, 10)
      const qty = item.sfajuc003 ?? item['数量']
      if (date && qty !== undefined && qty !== null && qty !== '') {
        plan[date] = Number(qty)
      }
    }
  }

  await Promise.all(rows.map(async (row) => {
    const plan = {}
    const base = { sfajuc001: row.workOrderNo || '', sfajuc004: row.productionLine || '' }
    try {
      // 先按工单号 + 产线查询（日期留空，取全部日期）
      const res = await queryDailyPlan({ ...base, sfajuc007: '' })
      pick(plan, res.data?.master ?? res.data?.data ?? [])
    } catch (err) {
      console.warn('查询日计划失败:', err?.message || err)
    }
    // 后端必须按日期查询时，按该行日期范围逐日查询兜底
    if (Object.keys(plan).length === 0) {
      const dates = rowDateRange(row)
      if (dates.length > 0) {
        const results = await Promise.allSettled(dates.map(d => queryDailyPlan({ ...base, sfajuc007: d })))
        results.forEach(r => {
          if (r.status === 'fulfilled') pick(plan, r.value.data?.master ?? r.value.data?.data ?? [])
        })
      }
    }
    row.dailyPlan = plan
  }))
}

/** 品号 → UPPH 缓存，避免重复请求 */
const upphCache = {}
/** 按品号调 queryItemBasicInfo 取 UPPH，回填到 rows 的 upph 字段 */
const fillUpph = async (rows) => {
  const targets = rows.filter(r => r.itemNo && r.upph === undefined)
  if (targets.length === 0) return
  // 去重：同一品号只查一次
  const itemNos = [...new Set(targets.map(r => r.itemNo))]
  await Promise.all(itemNos.map(async (imae001) => {
    if (upphCache[imae001] !== undefined) return
    try {
      const res = await queryItemBasicInfo(imae001)
      const rec = res.data?.data?.[0] ?? {}
      upphCache[imae001] = rec.UPPH ?? rec.upph ?? ''
    } catch (e) {
      upphCache[imae001] = ''
    }
  }))
  targets.forEach(r => { r.upph = upphCache[r.itemNo] ?? '' })
}

/** 工单状态码 sfaastus → 中文；未匹配时原样返回 */
const mapWorkOrderStatus = (sfaastus) => {
  const code = (sfaastus ?? '').toString().trim()
  if (!code) return ''
  return WORK_ORDER_STATUS_MAP[code] ?? code
}

/**
 * 工单号 → { 工单结案 workOrderStatus, 入库量 stockInQty, 余量 balanceQty }
 * 缓存避免重复请求；已带这些字段的行（如从左侧移过来的）不重复请求。
 * 用于右侧从数据库加载的场景（querySfahuc 返回的 sfahuc* 字段不含这三项）。
 */
const orderExtraCache = {}
const fillOrderExtra = async (rows) => {
  const targets = rows.filter(r =>
    r.workOrderNo && (r.stockInQty === undefined || r.workOrderStatus === undefined)
  )
  if (targets.length === 0) return
  const docnos = [...new Set(targets.map(r => r.workOrderNo))]
  const CONCURRENCY = 4
  for (let i = 0; i < docnos.length; i += CONCURRENCY) {
    const batch = docnos.slice(i, i + CONCURRENCY)
    await Promise.all(batch.map(async (sfaadocno) => {
      if (orderExtraCache[sfaadocno] !== undefined) return
      // 1) 优先：一次 queryOrder 同时取 sfaastus / sfaa050 / 余量
      let hit = null
      try {
        const res = await fetchPendingOrders({ sfaadocno, rowMax: 1 })
        const master = res.data?.master ?? res.data?.data ?? []
        hit = master.find(m => m.sfaadocno === sfaadocno) || master[0] || null
      } catch (e) {
        hit = null
      }
      if (hit) {
        orderExtraCache[sfaadocno] = {
          workOrderStatus: mapWorkOrderStatus(hit.sfaastus),
          stockInQty: hit.sfaa050 ?? '',
          balanceQty: hit['余量'] ?? ''
        }
        return
      }
      // 2) 兜底：queryOrder 查不到该工单（例如工单已结案，不在待排程列表中），
      //    改用 queryWorkOrder 单独取状态码，数量字段留空
      let status = ''
      try {
        const wres = await queryWorkOrder(sfaadocno)
        const head = wres.data?.head ?? wres.data?.data ?? []
        status = Array.isArray(head) && head.length > 0 ? (head[0].sfaastus ?? '') : ''
      } catch (e) {
        status = ''
      }
      orderExtraCache[sfaadocno] = {
        workOrderStatus: mapWorkOrderStatus(status),
        stockInQty: '',
        balanceQty: ''
      }
    }))
  }
  targets.forEach(r => {
    const c = orderExtraCache[r.workOrderNo]
    if (r.workOrderStatus === undefined) r.workOrderStatus = c?.workOrderStatus ?? ''
    if (r.stockInQty === undefined) r.stockInQty = c?.stockInQty ?? ''
    if (r.balanceQty === undefined) r.balanceQty = c?.balanceQty ?? ''
  })
}

const handleLeftSelection = (rows) => {
  leftSelected.value = rows
}

const handleRightSelection = (rows) => {
  rightSelected.value = rows
}

const moveToRight = async () => {
  if (leftSelected.value.length === 0) {
    ElMessage.warning('请选择待排程工单')
    return
  }
  const selected = [...leftSelected.value]

  const selectedKeys = new Set(selected.map(item => `${item.sourceOrderNo}||${item.workOrderNo}`))
  await ensureDeptMap()
  // 新插入记录：按本行成本中心取第一条产线作为默认
  const newRows = await Promise.all(selected.map(async (item) => ({
    ...item,
    productionLine: item.productionLine || await getDefaultLine(item.costCenterCode),
    dailyPlan: {}
  })))
  scheduledList.value.push(...newRows)
  pendingList.value = pendingList.value.filter(item => !selectedKeys.has(`${item.sourceOrderNo}||${item.workOrderNo}`))
  leftSelected.value = []
  leftTable.value?.clearSelection()
  await fillUpph(scheduledList.value)
  scheduledList.value.forEach(refreshDailyStatus)
  ElMessage.success('已加入排程')
}

const moveToLeft = async () => {
  if (rightSelected.value.length === 0) {
    ElMessage.warning('请选择排程工单')
    return
  }
  const selected = [...rightSelected.value]

  // 向后端发送删除请求：按 工单号(sfahuc001) + 品号(sfahuc002) + 成本中心(sfahuc008) 定位
  const deletePayloads = selected.map(item => ({
    sfahucent: form.enterpriseCode || '',
    sfahucsite: form.site || '',
    sfahuc001: item.workOrderNo || '',
    sfahuc002: item.itemNo || '',
    sfahuc008: item.costCenterCode || ''
  }))

  const deleteResults = await Promise.allSettled(deletePayloads.map(payload => deleteSchedule(payload)))
  // 检查是否全部删除成功（后端返回 success=true 且 deleted=true）
  const failedIndexes = []
  deleteResults.forEach((result, i) => {
    if (result.status === 'fulfilled') {
      const data = result.value?.data
      const ok = data?.success === true && data?.deleted === true
      if (!ok) failedIndexes.push(i)
    } else {
      failedIndexes.push(i)
    }
  })

  if (failedIndexes.length > 0) {
    ElMessage.error(`有 ${failedIndexes.length} 条删除失败，已取消移回操作`)
    return
  }

  const selectedKeys = new Set(selected.map(item => `${item.sourceOrderNo}||${item.workOrderNo}`))
  pendingList.value = [...selected, ...pendingList.value]
  scheduledList.value = scheduledList.value.filter(item => !selectedKeys.has(`${item.sourceOrderNo}||${item.workOrderNo}`))
  rightSelected.value = []
  rightTable.value?.clearSelection()
  ElMessage.success('已移回待排程')
}

/** 只取日期部分 YYYY-MM-DD */
const toDate = (value) => {
  if (!value) return ''
  return String(value).substring(0, 10)
}

/** 解析 YYYY-MM-DD 为本地日期对象 */
const parseYMD = (s) => {
  const [y, m, d] = String(s).split('-').map(Number)
  return new Date(y, (m || 1) - 1, d || 1)
}

/** 生成 YYYY-MM-DD 字符串 */
const formatYMD = (d) => {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

/** 列名显示为 YYYY/M/D（与示例一致，无前导零） */
const formatColumnLabel = (date) => {
  const [y, m, d] = String(date).split('-')
  return `${parseInt(y, 10)}/${parseInt(m, 10)}/${parseInt(d, 10)}`
}

/** 周几中文（一/二/.../日） */
const weekdayOf = (date) => {
  if (!date) return ''
  const d = new Date(String(date))
  if (isNaN(d.getTime())) return ''
  return ['日', '一', '二', '三', '四', '五', '六'][d.getDay()]
}

/** 动态日期列：根据右侧所有工单的最早预计开工 ~ 最晚预计完工 生成每一天的列 */
const dateColumns = computed(() => {
  if (scheduledList.value.length === 0) return []
  let minDate = ''
  let maxDate = ''
  for (const row of scheduledList.value) {
    const start = toDate(row.estimatedStartDate)
    const end = toDate(row.estimatedEndDate)
    if (start && (!minDate || start < minDate)) minDate = start
    if (end && (!maxDate || end > maxDate)) maxDate = end
  }
  if (!minDate || !maxDate) return []
  const columns = []
  const cur = parseYMD(minDate)
  const last = parseYMD(maxDate)
  while (cur.getTime() <= last.getTime()) {
    columns.push(formatYMD(cur))
    cur.setDate(cur.getDate() + 1)
  }
  return columns
})

/** 固定 10 字符宽的列宽 */
const charWidth10 = () => 10 * 13

/** 左右表「来源单号」「工单号」固定 10 字符宽 */
const leftSourceOrderNoWidth = computed(charWidth10)
const leftWorkOrderNoWidth = computed(charWidth10)
const rightSourceOrderNoWidth = computed(charWidth10)
const rightWorkOrderNoWidth = computed(charWidth10)

/** 判断某工单在某日期是否处于生产区间内 */
const isInRange = (row, date) => {
  const start = toDate(row.estimatedStartDate)
  const end = toDate(row.estimatedEndDate)
  return !!(start && end && date >= start && date <= end)
}

/** 获取某天计划数量，未填写时默认 0 */
const getDailyValue = (row, date) => {
  if (!row.dailyPlan) row.dailyPlan = {}
  if (row.dailyPlan[date] === undefined || row.dailyPlan[date] === null || row.dailyPlan[date] === '') {
    row.dailyPlan[date] = 0
  }
  return Number(row.dailyPlan[date])
}

/** 计算某行日期数量合计 */
const getDailyTotal = (row) => {
  if (!row.dailyPlan) return 0
  return Object.values(row.dailyPlan).reduce((s, v) => {
    const n = Number(v)
    return s + (isNaN(n) ? 0 : n)
  }, 0)
}

/** 把某行日期数量合计与生产数量对比状态写入 row.dailyStatus */
const refreshDailyStatus = (row) => {
  const total = getDailyTotal(row)
  const orderQty = Number(row.orderNo) || 0
  if (total > orderQty) row.dailyStatus = 'over'
  else if (orderQty > 0 && total === orderQty) row.dailyStatus = 'equal'
  else if (orderQty === 0 || total < orderQty) row.dailyStatus = 'less'
  else row.dailyStatus = 'none'
}

/** 设置某天计划数量：超量则拒绝更新、报警 */
const setDailyValue = (row, date, val) => {
  if (!row.dailyPlan) row.dailyPlan = {}
  const oldVal = Number(row.dailyPlan[date] || 0)
  const newVal = Number(val) || 0
  // 计算替换后的新合计
  const total = getDailyTotal(row) - oldVal + newVal
  const orderQty = Number(row.orderNo) || 0
  if (total > orderQty) {
    // 拒绝更新，保持原值；报警；焦点停留（el-input-number 通过不响应@update 实现）
    ElMessage.warning(`日期 ${date} 录入 ${newVal} 后合计 ${total} 超过生产数量 ${orderQty}，请重新输入`)
    refreshDailyStatus(row)
    return false
  }
  row.dailyPlan[date] = val
  refreshDailyStatus(row)
  return true
}

/** 保存右侧排程明细 */
const handleSave = async () => {
  if (scheduledList.value.length === 0) {
    ElMessage.warning('没有需要保存的排程数据')
    return
  }
  // 校验预计开工、预计完工必填
  const invalid = scheduledList.value.find((item, i) => !item.estimatedStartDate || !item.estimatedEndDate)
  if (invalid) {
    const idx = scheduledList.value.indexOf(invalid) + 1
    ElMessage.warning(`第${idx}项 预计开工和预计完工不能为空`)
    return
  }
  // 校验日计划数量合计 <= 生产数量（超量则阻止保存）
  const overRow = scheduledList.value.find(item => (item.dailyStatus === 'over'))
  if (overRow) {
    const idx = scheduledList.value.indexOf(overRow) + 1
    ElMessage.warning(`第${idx}项日计划数量合计超过生产数量，请调整后再保存`)
    return
  }
  // 保存前按站点刷新成本中心名称，保证落库的是站点对应的名称
  await ensureDeptMap()
  for (const row of scheduledList.value) syncCostCenterName(row)
  // 与加载时的原始数据对比，无修改则跳过保存
  const currentJson = JSON.stringify(scheduledList.value)
  if (currentJson === originalScheduledJson.value) {
    ElMessage.info('排程数据无修改，无需保存')
    return
  }
  try {
    const payload = scheduledList.value.map((item, index) => ({
      sfahucent: '60',                    // 企业代码（暂无）
      sfahucsite: form.site || '',      // 营运据点
      sfahucdocno: form.orderNo || '',  // 单号
      sfahucseq: index + 1,             // 项次
      sfahuc001: item.workOrderNo || '',        // 工单单号
      sfahuc008: item.costCenterCode || '',      // 成本中心编码
      sfahuc002: item.itemNo || '',             // 品号
      sfahuc003: item.orderNo || '',            // 数量（生产数量）
      sfahuc004: item.sourceOrderNo || '',      // 订单号
      sfahuc005: item.seq || '',                // 订单序号
      sfahuc006: toDate(item.estimatedStartDate), // 开工日期
      sfahuc007: toDate(item.estimatedEndDate),   // 完工日期
      sfahuc009: '',                     // 已入库数量（暂无）
      sfahuc010: item.productionLine || ''  // 产线（OOELUC003）
    }))
    await saveSchedule(payload)

    // 保存每日计划数量：只送数量大于 0 的日期，每个日期一条
    const dailyList = []
    for (const item of scheduledList.value) {
      const plan = item.dailyPlan || {}
      for (const date of Object.keys(plan)) {
        const qty = Number(plan[date])
        if (!date || !Number.isFinite(qty) || qty <= 0) continue
        dailyList.push({
          sfajucent: '60',                          // 企业代码
          sfajucsite: form.site || 'NBYL',         // 营运据点
          sfajuc001: item.workOrderNo || '',       // 工单号
          sfajuc004: item.productionLine || '',    // 产线
          sfajuc007: date,                         // 日期
          sfajuc003: qty.toFixed(2)                // 数量
        })
      }
    }
    if (dailyList.length > 0) {
      await saveDailyPlan(dailyList)
    }

    originalScheduledJson.value = currentJson
    ElMessage.success('保存成功')
  } catch (err) {
    ElMessage.error('保存失败: ' + (err.response?.data?.message || err.message))
  }
}
</script>

<style scoped>
.schedule-page {
  height: 100%;
  padding: 16px 0 16px 16px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
  font-size: 10px;
}

/* 全局字号 10 号：表格、表头、输入框、按钮、表单标签 */
.schedule-page,
.schedule-page :deep(.el-table),
.schedule-page :deep(.el-table .el-table__cell),
.schedule-page :deep(.el-table th.el-table__cell),
.schedule-page :deep(.el-form-item__label),
.schedule-page :deep(.el-input__inner),
.schedule-page :deep(.el-input__wrapper),
.schedule-page :deep(.el-textarea__inner),
.schedule-page :deep(.el-select__selected-item),
.schedule-page :deep(.el-select .el-input__inner),
.schedule-page :deep(.el-date-editor),
.schedule-page :deep(.el-button) {
  font-size: 10px;
}

/* 表头居中 */
.schedule-page :deep(.el-table) {
  border-color: #000;
}
.schedule-page :deep(.el-table th.el-table__cell),
.schedule-page :deep(.el-table td.el-table__cell) {
  border-color: #000;
}

.schedule-page :deep(.el-table th.el-table__cell) {
  text-align: center;
  background: #d6eaf8 !important;
  color: #303133;
}

/* 表头与勾选框居中 */
.schedule-page :deep(.el-table th.el-table__cell .cell) {
  display: flex;
  align-items: center;
  justify-content: center;
}
.schedule-page :deep(.el-table .el-table-column--selection .cell) {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}
.schedule-page :deep(.el-table .el-table__header-wrapper .el-table-column--selection .cell) {
  padding: 0;
}
.schedule-page :deep(.el-table .el-checkbox) {
  margin-right: 0;
}
.schedule-page :deep(.el-table .el-table-column--selection .el-checkbox__input) {
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 表头 hover 也保持浅蓝 */
.schedule-page :deep(.el-table th.el-table__cell:hover) {
  background: #d6eaf8 !important;
}

.basic-card {
  margin-bottom: 8px;
  flex-shrink: 0;
}



/* 去掉 header 后，压缩卡片内边距让表单整体上移 */
.basic-card :deep(.el-card__body) {
  padding: 12px 16px;
}

.basic-form {
  margin-right:-10px ;
}

/* 第一行字段之间不留空隙 */
.no-gutter-row {
  margin-left: -10 !important;
  margin-right: -10 !important;
}
.no-gutter-row > .tight-col {
  flex: 0 0 auto;
  width: auto;
  max-width: none;
  margin-left: -10px !important;
  margin-right: -10px !important;
}
.no-gutter-row > .tight-col .el-form-item {
  margin-right: 0;
}
.no-gutter-row > .tight-col:not(:last-child) .el-form-item {
  margin-right: -1px;
}
.no-gutter-row > .tight-col .el-form-item__label {
  padding-right: 0px;
}
.no-gutter-row > .tight-col .el-input,
.no-gutter-row > .tight-col .el-select {
  width: auto;
}

/* 成本中心下拉：选项 + 名称标签 */
.dept-select {
  display: flex;
  align-items: center;
  gap: 6px;
  width: 100%;
  min-width: 0;
}

.dept-select-input {
  width: auto;
  flex: 1 1 auto;
  min-width: 160px;
  max-width: 360px;
}

/* 成本中心下拉弹层字体 11px（el-select-v2 虚拟列表 + el-select 兼容） */
:deep(.el-select-v2__list),
:deep(.el-select-v2__item),
:deep(.el-select-v2__item-text),
:deep(.el-select-v2__list .el-virtual-list-item),
:deep(.el-select-dropdown__item) {
  font-size: 11px !important;
  line-height: 22px !important;
}
:deep(.el-select-v2__item) {
  height: 22px !important;
  line-height: 22px !important;
  padding: 0 8px !important;
}

/* 右侧动态日期列表头：日期 + 周几 两行 */
.date-col-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  line-height: 1.3;
  padding: 2px 0;
}
.date-col-header .date-col-date {
  font-size: 10px;
}
.date-col-header .date-col-week {
  font-size: 9px;
  color: #909399;
  margin-top: 1px;
}
.schedule-page :deep(.el-table .el-table__cell .cell) {
  padding: 0 4px;
}

.order-select-input {
  width: auto;
  flex: 0 0 auto;
  min-width: 160px;
  max-width: 200px;
}

/* 订单下拉：订单号 / 订单序号 / 订单品号 / 开单日期 / 客户代码 五列 */
.order-option {
  display: flex;
  align-items: center;
  font-size: 10px;
}
.order-option .col-docno {
  flex: 0 0 150px;
}
.order-option .col-seq {
  flex: 0 0 50px;
  text-align: center;
}
.order-option .col-item {
  flex: 0 0 110px;
}
.order-option .col-date {
  flex: 0 0 90px;
}
.order-option .col-customer {
  flex: 1 1 auto;
  color: #909399;
}

.dept-name-tag {
  flex: 0 1 auto;
  min-width: 0;
  max-width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 13px;
  color: #409eff;
  background: #ecf5ff;
  padding: 2px 8px;
  border-radius: 3px;
  box-sizing: border-box;
}

.table-area {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.left-panel {
  flex: 0 0 auto;
  min-width: 0;
  background: #fff;
  border: 1px solid #000;
  border-right: none;
  border-radius: 4px 0 0 4px;
  overflow: hidden;
}

.table-resizer {
  flex: 0 0 6px;
  cursor: col-resize;
  background: transparent;
  transition: background-color 0.2s;
}
.table-resizer:hover,
.table-resizer:active {
  background: #409eff;
}

.right-panel {
  flex: 1;
  min-width: 0;
  background: #fff;
  border: 1px solid #000;
  border-radius: 0 4px 4px 0;
  overflow: hidden;
}

/* 表单操作按钮：与营运据点同一行并居中 */
.form-actions-col {
  flex: 1 1 auto;
}

.form-actions-col .el-form-item__content {
  display: flex;
  justify-content: center;
}

.form-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.form-actions .el-button {
  min-width: 64px;
}

/* 必填项粉色背景 */
.required-field :deep(.el-input__wrapper) {
  background-color: #ffc0cb;
  box-shadow: 0 0 0 1px #ff69b4 inset;
}

/* 日期列可编辑输入框：铺满单元格，四周无空隙 */
/* 1) 整个外层 cell 与 .cell 容器内边距归零，避免上下左右留白 */
.schedule-page :deep(.el-table td.el-table__cell .cell:has(.daily-input.center-input)) {
  padding: 0 !important;
}
.schedule-page :deep(.el-table td.el-table__cell:has(.daily-input.center-input)) {
  padding: 0 !important;
}

/* 2) el-input-number 自身撑满 cell */
.daily-input {
  width: 100% !important;
  display: block;
  background-color: #a8d8ff !important;
}
/* 3) input-number 内嵌的 el-input / wrapper 全部去掉内边距、阴影、圆角 */
.daily-input :deep(.el-input-number),
.daily-input :deep(.el-input),
.daily-input :deep(.el-input__wrapper) {
  width: 100%;
  padding: 0;
  border-radius: 0;
  box-shadow: none !important;
  background-color: #a8d8ff !important;
}
/* 4) 日期列输入框背景设为浅蓝色（铺满根 + 内部各层） */
.daily-input :deep(.el-input__inner) {
  background-color: #a8d8ff !important;
}
.daily-input :deep(.el-input__wrapper.is-focus),
.daily-input :deep(.el-input__wrapper:hover) {
  box-shadow: none !important;
}
.daily-input :deep(.el-input-number__decrease),
.daily-input :deep(.el-input-number__increase) {
  display: none;
}
.daily-input :deep(.el-input__inner) {
  text-align: center;
  padding: 0 !important;
  border: 0 !important;
  background-color: #a8d8ff !important;
}

/* 日计划数量校验：合计 vs 生产数量 — 颜色提示 */
/* 红色：超量；绿色：等于；黑色：不足 */
.daily-status-over :deep(.el-input__inner) { color: #f56c6c !important; font-weight: 600; }
.daily-status-equal :deep(.el-input__inner) { color: #67c23a !important; font-weight: 600; }
.daily-status-less :deep(.el-input__inner) { color: #303133 !important; }
.daily-status-none :deep(.el-input__inner) { color: #303133 !important; }

/* 产线选择框：去掉 .el-input__wrapper 左右内边距，使选中文本撑满 */
.schedule-page :deep(.el-table td.el-table__cell .line-select .el-input__wrapper) {
  padding: 0 2px;
}
.schedule-page :deep(.el-table td.el-table__cell .line-select .el-input__inner) {
  padding: 0;
  text-align: center;
}
.schedule-page :deep(.el-table td.el-table__cell .line-select .el-select__suffix) {
  right: 0;
}
.daily-input :deep(.el-input__inner) {
  text-align: center;
  padding: 0;
}

/* 动态日期列内容整体居中 */
.schedule-page :deep(.el-table .daily-input.center-input) {
  width: 100%;
  display: block;
}
.schedule-page :deep(.el-table .daily-input.center-input .el-input__wrapper) {
  justify-content: center;
}

/* 左右两表行高一致：固定行高 + 文本单行省略 */
.schedule-page :deep(.el-table td.el-table__cell) {
  height: 40px;
  padding: 0 8px;
}
.schedule-page :deep(.el-table td.el-table__cell .cell) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
