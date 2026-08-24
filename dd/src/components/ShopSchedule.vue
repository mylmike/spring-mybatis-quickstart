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
                <el-select
                  v-model="form.costCenter"
                  filterable
                  clearable
                  :loading="deptLoading"
                  placeholder="请输入或选择"
                  class="dept-select-input"
                  @change="handleCostCenterChange"
                  @visible-change="handleCostCenterVisible"
                >
                  <el-option
                    v-for="item in deptOptions"
                    :key="item.value"
                    :label="item.value"
                    :value="item.value"
                  >
                    <span>{{ item.value }} - {{ item.label.split(' - ').slice(1).join(' - ') }}</span>
                  </el-option>
                </el-select>
                <span v-if="deptName" class="dept-name-tag">{{ deptName }}</span>
              </div>
            </el-form-item>
          </el-col>
          <el-col class="tight-col">
            <el-form-item label="订单号">
              <el-input v-model="form.customerOrderNo" clearable @keyup.enter="loadPendingOrders" />
            </el-form-item>
          </el-col>
          <el-col class="tight-col">
            <el-form-item label="订单序号">
              <el-input v-model="form.orderSeq" clearable style="width: 48px" />
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
        </el-row>
        <el-row class="second-form-row">
          <el-col :span="24" class="form-actions">
            <el-button type="primary" size="small" icon="Search" @click="loadPendingOrders">查询</el-button>
            <el-button type="primary" size="small" :disabled="leftSelected.length === 0" @click="moveToRight">&gt;</el-button>
            <el-button type="warning" size="small" :disabled="rightSelected.length === 0" @click="moveToLeft">&lt;</el-button>
            <el-button type="success" size="small" :disabled="scheduledList.length === 0" @click="handleSave">保存</el-button>
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
          <el-table-column prop="sourceOrderNo" label="来源单号" :width="leftSourceOrderNoWidth" />
          <el-table-column prop="seq" label="序号" width="42" />
          <el-table-column prop="itemNo" label="品号" width="104" />
          <el-table-column prop="costCenterCode" label="站点" width="65" />
          <el-table-column prop="costCenter" label="成本中心" min-width="113" />
          <el-table-column prop="orderNo" label="生产数量" width="65" />
          <el-table-column prop="workOrderNo" label="工单号" :width="leftWorkOrderNoWidth" />
          <el-table-column prop="estimatedStartDate" label="预计开工" min-width="110" :formatter="formatDate" />
          <el-table-column prop="estimatedEndDate" label="预计完工" min-width="110" :formatter="formatDate" />
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
          <el-table-column prop="workOrderNo" label="工单号" :width="rightWorkOrderNoWidth" />
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
          <el-table-column prop="seq" label="序号" width="42" />
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
            <template #default="{ row }">
              <el-input-number
                v-if="isInRange(row, date)"
                :model-value="getDailyValue(row, date)"
                :min="0"
                :controls="false"
                size="small"
                class="daily-input center-input"
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
import { reactive, ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchPendingOrders, saveSchedule, fetchSavedSchedule, deleteSchedule, queryLine } from '../api/pendingOrders.js'
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
  enterpriseCode: '60'
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

/** 加载产线列表（ooeluc004 精确匹配站点） */
const fetchLines = async (ooeluc004 = '') => {
  if (lineLoading.value) return
  lineLoading.value = true
  try {
    const res = await queryLine({ ooeluc003: '', ooeluc004 })
    const list = res.data?.master ?? res.data?.data ?? []
    const get = (obj, ...keys) => {
      for (const k of keys) {
        const hit = Object.keys(obj).find(x => x.toLowerCase() === k.toLowerCase())
        if (hit && obj[hit] != null && obj[hit] !== '') return obj[hit]
      }
      return ''
    }
    lineOptions.value = list.map(item => {
      const code = get(item, 'OOELUC003')
      const name = get(item, 'OOELUC004')
      return { value: code, label: `${code} - ${name}` }
    })
    console.log('产线选项:', lineOptions.value)
  } catch (err) {
    console.warn('加载产线失败:', err?.message || err)
  } finally {
    lineLoading.value = false
  }
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

/** 加载成本中心列表（与预算录入部门一致） */
const fetchAllDepts = async () => {
  if (deptLoading.value) return
  deptLoading.value = true
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
    }
    console.log('成本中心选项:', deptOptions.value)
  } catch (err) {
    console.warn('加载成本中心失败:', err?.message || err)
  } finally {
    deptLoading.value = false
  }
}

/** 下拉展开时按当前行站点过滤产线 */
const handleLineVisible = (row, visible) => {
  if (visible) {
    fetchLines(row.costCenterCode || '')
  }
}

/** 组件挂载时主动预加载成本中心、产线列表 */
onMounted(() => {
  fetchAllDepts()
  fetchLines()
})

/** 成本中心值改变时回填名称 */
const handleCostCenterChange = (val) => {
  const found = deptOptions.value.find(o => o.value === val)
  deptName.value = found ? found.label.split(' - ').slice(1).join(' - ') : ''
}

/** 下拉框展开时刷新列表 */
const handleCostCenterVisible = (visible) => {
  if (visible) {
    fetchAllDepts()
  }
}

/** 从后端加载待排程工单和已保存的排程明细 */
const loadPendingOrders = async () => {
  if (!form.customerOrderNo) {
    ElMessage.warning('请输入订单号')
    return
  }
  loading.value = true
  try {
    // 1. 加载左侧待排程工单
    const res = await fetchPendingOrders(form.customerOrderNo)
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
      estimatedEndDate: item.sfaa020 || ''
    }))

    // 2. 加载右侧已保存排程明细
    if (form.orderNo) {
      const savedRes = await fetchSavedSchedule(form.orderNo)
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
    }

    // 3. 过滤左侧：已存在于右侧的（订单号+工单号一致）不显示
    if (scheduledList.value.length > 0) {
      const scheduledKeys = new Set(scheduledList.value.map(r => `${r.sourceOrderNo}||${r.workOrderNo}`))
      pendingList.value = pendingList.value.filter(r => !scheduledKeys.has(`${r.sourceOrderNo}||${r.workOrderNo}`))
    }

    // 4. 保存原始快照，用于判断保存时是否有修改
    originalScheduledJson.value = JSON.stringify(scheduledList.value)
  } catch (err) {
    ElMessage.error('获取待排程工单失败: ' + (err.response?.data?.message || err.message))
  } finally {
    loading.value = false
  }
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
  scheduledList.value.push(...selected.map(item => ({ ...item, dailyPlan: {} })))
  pendingList.value = pendingList.value.filter(item => !selectedKeys.has(`${item.sourceOrderNo}||${item.workOrderNo}`))
  leftSelected.value = []
  leftTable.value?.clearSelection()
  ElMessage.success('已加入排程')
}

const moveToLeft = async () => {
  if (rightSelected.value.length === 0) {
    ElMessage.warning('请选择排程工单')
    return
  }
  const selected = [...rightSelected.value]

  // 向后端发送删除请求，传右侧记录自带的 sfahucseq
  const deletePayloads = selected.map(item => ({
    sfahucent: form.enterpriseCode || '',
    sfahucsite: form.site || '',
    sfahucdocno: form.orderNo || '',
    sfahucseq: item.sfahucseq || '',
    sfahuc001: item.workOrderNo || '',
    sfahuc002: item.itemNo || ''
  }))

  const deleteResults = await Promise.allSettled(deletePayloads.map(payload => deleteSchedule(payload)))
  deleteResults.forEach((result, i) => {
    if (result.status === 'fulfilled') {
      const data = result.value?.data
      console.log(`左移删除[${i}]:`, JSON.stringify(deletePayloads[i]), '→ deleted:', data?.deleted, 'success:', data?.success)
    } else {
      console.log(`左移删除[${i}] 失败:`, result.reason)
    }
  })

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

/** 设置某天计划数量 */
const setDailyValue = (row, date, val) => {
  if (!row.dailyPlan) row.dailyPlan = {}
  row.dailyPlan[date] = val
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
  flex: 0 0 auto;
  min-width: 120px;
  max-width: 140px;
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

/* 表单操作按钮：与查询按钮同一行并居中 */
.second-form-row {
  align-items: center;
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

/* 日期列可编辑输入框 */
.daily-input :deep(.el-input__wrapper) {
  padding: 0 4px;
}

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
}
.schedule-page :deep(.el-table .daily-input.center-input .el-input__wrapper) {
  justify-content: center;
}
.schedule-page :deep(.el-table td.el-table__cell .cell:has(.daily-input.center-input)) {
  text-align: center;
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
