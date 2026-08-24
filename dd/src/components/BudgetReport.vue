<template>
  <div class="budget-report-page">
    <!-- 单头：查询条件（发送到后端） -->
    <el-card class="header-card" shadow="never">
      <el-form :model="header" label-width="56px" inline class="header-form" @submit.prevent>
        <el-form-item label="账套" required>
          <el-input v-model="header.ent" placeholder="企业代码" clearable style="width: 60px;" />
        </el-form-item>
        <el-form-item label="账别" required>
          <el-input v-model="header.site" placeholder="站点" clearable style="width: 80px;" />
        </el-form-item>
        <el-form-item label="年度" required>
          <el-input-number
            v-model="header.year"
            :min="2000"
            :max="2100"
            :precision="0"
            controls-position="right"
            style="width: 100px;"
          />
        </el-form-item>
        <el-form-item label="部门">
          <div class="dept-select">
            <el-select
              v-model="header.dept"
              filterable
              clearable
              :loading="deptLoading"
              placeholder="请输入或选择"
              style="width: 140px;"
              @change="handleDeptChange"
              @visible-change="handleDeptVisible"
            >
              <el-option
                v-for="item in deptOptions"
                :key="item.value"
                :label="item.value"
                :value="item.value"
              >
                <span>{{ item.label }}</span>
              </el-option>
            </el-select>
            <span v-if="deptName" class="dept-name-tag">{{ deptName }}</span>
          </div>
        </el-form-item>
        <el-form-item label="科目名称" label-width="84px">
          <el-input v-model="header.subjectName" placeholder="科目名称（模糊）" clearable style="width: 150px;" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="header.summary" placeholder="摘要" clearable style="width: 120px;" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item class="btn-group">
          <el-button type="primary" icon="Search" :loading="loading" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="handleReset">重置</el-button>
          <el-button icon="Download" :disabled="list.length === 0" @click="exportToExcel">导出Excel</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 单身：报表数据（后端返回） -->
    <el-card class="detail-card" shadow="never">
      <el-table
        :data="displayList"
        border
        height="100%"
        size="small"
        empty-text="请在上方填写条件后点击查询"
        :row-class-name="rowClassName"
        @sort-change="handleSortChange"
      >
        <el-table-column type="index" label="序号" width="41" fixed="left" align="center" />
        <el-table-column prop="部门" label="部门" min-width="82" fixed="left" sortable="custom" show-overflow-tooltip />
        <el-table-column prop="科目" label="科目" min-width="75" fixed="left" sortable="custom" show-overflow-tooltip />
        <el-table-column prop="科目名称" label="科目名称" min-width="120" fixed="left" sortable="custom" show-overflow-tooltip />
        <el-table-column prop="预算合计" label="预算合计" min-width="82" fixed="left" sortable="custom" align="right">
          <template #default="{ row }">{{ fmtNum(row['预算合计']) }}</template>
        </el-table-column>
        <el-table-column prop="实际合计" label="实际合计" min-width="82" fixed="left" sortable="custom" align="right">
          <template #default="{ row }">{{ fmtNum(row['实际合计']) }}</template>
        </el-table-column>
        <el-table-column prop="合计差异" label="合计差异" min-width="82" fixed="left" sortable="custom" align="right">
          <template #default="{ row }">
            <span :class="numClass(row['合计差异'])">{{ fmtNum(row['合计差异']) }}</span>
          </template>
        </el-table-column>
        <el-table-column v-for="m in 12" :key="m" :label="`${m}月`" align="center" min-width="248">
          <el-table-column :prop="`实际${pad(m)}月`" label="实际" min-width="79" align="right" sortable="custom">
            <template #default="{ row }">{{ fmtNum(row[`实际${pad(m)}月`]) }}</template>
          </el-table-column>
          <el-table-column :prop="`预算${pad(m)}月`" label="预算" min-width="79" align="right" sortable="custom">
            <template #default="{ row }">{{ fmtNum(getBudget(row, m)) }}</template>
          </el-table-column>
          <el-table-column :prop="`差异${pad(m)}月`" label="差异" min-width="79" align="right" sortable="custom">
            <template #default="{ row }">
              <span :class="numClass(row[`差异${pad(m)}月`])">{{ fmtNum(row[`差异${pad(m)}月`]) }}</span>
            </template>
          </el-table-column>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import * as XLSX from 'xlsx'
import { queryBudgetReport, queryDept } from '../api/budget.js'

/** 单头查询条件（对应发送到后端的 JSON） */
const header = reactive({
  ent: '60',
  site: 'NBYL',
  year: new Date().getFullYear(),
  dept: '',
  subjectName: '',
  summary: ''
})

/** 部门下拉选项 */
const deptOptions = ref([])
/** 部门加载状态 */
const deptLoading = ref(false)
/** 部门名称 */
const deptName = ref('')

/** 单身表格数据 */
const list = ref([])
const loading = ref(false)

/** 当前排序后的数据（不含合计行） */
const sorted = ref([])

/** 加载全部部门列表 */
const fetchAllDepts = async () => {
  if (deptLoading.value) return
  deptLoading.value = true
  try {
    const res = await queryDept({ ooefl001: '', ooefl003: '' })
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
    console.log('部门选项:', deptOptions.value)
  } catch (err) {
    console.warn('加载部门失败:', err?.message || err)
  } finally {
    deptLoading.value = false
  }
}

/** 组件挂载时主动预加载部门列表，避免下拉空 */
onMounted(() => {
  fetchAllDepts()
})

/** 部门值改变时更新部门名称 */
const handleDeptChange = (val) => {
  const found = deptOptions.value.find(o => o.value === val)
  deptName.value = found ? found.label.split(' - ').slice(1).join(' - ') : ''
}

/** 下拉框展开时加载全部部门 */
const handleDeptVisible = (visible) => {
  if (visible) {
    fetchAllDepts()
  }
}

/** 合计行 + 数据行，合计行固定显示在表格第一行 */
const displayList = computed(() => {
  const rows = sorted.value
  if (!rows.length) return []
  const sum = { 部门: '合计', 科目: '', 科目名称: '', _isSummary: true }
  for (const k of ['预算合计', '实际合计', '合计差异']) {
    sum[k] = rows.reduce((acc, r) => acc + (Number(r[k]) || 0), 0)
  }
  for (let m = 1; m <= 12; m++) {
    sum[`实际${pad(m)}月`] = rows.reduce((acc, r) => acc + (Number(r[`实际${pad(m)}月`]) || 0), 0)
    sum[`预算${pad(m)}月`] = rows.reduce((acc, r) => acc + (Number(getBudget(r, m)) || 0), 0)
    sum[`差异${pad(m)}月`] = rows.reduce((acc, r) => acc + (Number(r[`差异${pad(m)}月`]) || 0), 0)
  }
  return [sum, ...rows]
})

/** 合计行高亮样式 */
const rowClassName = ({ row }) => (row._isSummary ? 'summary-row' : '')

/** 取排序列的排序值 */
const getSortVal = (row, prop) => {
  if (prop === '部门' || prop === '科目' || prop === '科目名称') {
    return String(row[prop] ?? '')
  }
  if (typeof prop === 'string' && prop.startsWith('预算')) {
    const m = Number(prop.replace(/预算(\d+)月/, '$1'))
    return Number(getBudget(row, m)) || 0
  }
  return Number(row[prop]) || 0
}

/** 自定义排序：只排数据行，合计行始终在最前 */
const handleSortChange = ({ prop, order }) => {
  if (!order) {
    sorted.value = [...list.value]
    return
  }
  const dir = order === 'ascending' ? 1 : -1
  const rows = [...list.value].sort((a, b) => {
    const av = getSortVal(a, prop)
    const bv = getSortVal(b, prop)
    if (av < bv) return -1 * dir
    if (av > bv) return 1 * dir
    return 0
  })
  sorted.value = rows
}

/** 月份补零 */
const pad = n => String(n).padStart(2, '0')

/**
 * 从返回行中取某月预算值。
 * 后端把 NVL(YS.预算0X月,0) 拆成对象 "NVL(YS" 下的键 "预算0X月,0)"
 */
const getBudget = (row, m) => {
  const nested = row['NVL(YS']
  if (nested && typeof nested === 'object') {
    const v = nested[`预算${pad(m)}月,0)`]
    if (v !== undefined && v !== null) return v
  }
  const flat = row[`预算${pad(m)}月`]
  return flat !== undefined && flat !== null ? flat : 0
}

/** 数字格式化：千分位 + 两位小数 */
const fmtNum = v => {
  if (v === null || v === undefined || v === '') return ''
  const n = Number(v)
  if (isNaN(n)) return String(v)
  return n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

/** 差异为负数时标红 */
const numClass = v => {
  const n = Number(v)
  if (isNaN(n)) return ''
  return n < 0 ? 'num-neg' : ''
}

/** 查询：构造发送到后端的 JSON 并请求 */
const handleQuery = async () => {
  if (!header.ent || String(header.ent).trim() === '') {
    ElMessage.warning('请填写账套')
    return
  }
  if (!header.site || String(header.site).trim() === '') {
    ElMessage.warning('请填写账别')
    return
  }
  if (!header.year) {
    ElMessage.warning('请填写年度')
    return
  }
  const payload = {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    ent: String(header.ent || '60'),
    site: String(header.site || 'NBYL'),
    year: String(header.year),
    dept: String(header.dept || ''),
    subjectName: String(header.subjectName || ''),
    summary: String(header.summary || '')
  }
  console.log('预算报表查询发送 JSON:', JSON.stringify(payload, null, 2))
  loading.value = true
  try {
    const res = await queryBudgetReport(payload)
    const raw = res.data?.data ?? []
    list.value = raw
    sorted.value = [...raw]
    ElMessage.success(`查询成功，共 ${raw.length} 条`)
  } catch (err) {
    list.value = []
    sorted.value = []
    ElMessage.error('查询失败: ' + (err.response?.data?.message || err.message))
  } finally {
    loading.value = false
  }
}

/** 重置查询条件 */
const handleReset = () => {
  header.ent = '60'
  header.site = 'NBYL'
  header.year = new Date().getFullYear()
  header.dept = ''
  deptName.value = ''
  header.subjectName = ''
  header.summary = ''
  list.value = []
  sorted.value = []
}

/** 导出 Excel */
const exportToExcel = () => {
  const headers = ['部门', '科目', '科目名称', '预算合计', '实际合计', '合计差异']
  for (let m = 1; m <= 12; m++) {
    headers.push(`实际${pad(m)}月`, `预算${pad(m)}月`, `差异${pad(m)}月`)
  }
  const data = list.value.map(row => {
    const obj = {}
    obj['部门'] = row['部门'] ?? ''
    obj['科目'] = row['科目'] ?? ''
    obj['科目名称'] = row['科目名称'] ?? ''
    obj['预算合计'] = row['预算合计'] ?? 0
    obj['实际合计'] = row['实际合计'] ?? 0
    obj['合计差异'] = row['合计差异'] ?? 0
    for (let m = 1; m <= 12; m++) {
      obj[`实际${pad(m)}月`] = row[`实际${pad(m)}月`] ?? 0
      obj[`预算${pad(m)}月`] = getBudget(row, m)
      obj[`差异${pad(m)}月`] = row[`差异${pad(m)}月`] ?? 0
    }
    return obj
  })
  const ws = XLSX.utils.json_to_sheet(data, { header: headers })
  ws['!cols'] = headers.map(() => ({ wch: 14 }))
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '预算报表')
  XLSX.writeFile(wb, `预算报表_${header.year}.xlsx`)
  ElMessage.success('导出成功')
}
</script>

<style scoped>
.budget-report-page {
  height: 100%;
  padding: 0;
  margin: -16px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: 4px;
  background: #f5f7fa;
}

/* 单头区 */
.header-card {
  flex-shrink: 0;
}

.header-card :deep(.el-card__body) {
  padding: 8px 16px;
}

.header-form :deep(.el-form-item__label) {
  font-size: 12px;
}

.header-form :deep(.el-form-item__content) {
  font-size: 12px;
}

.header-form :deep(.el-form-item) {
  margin-bottom: 6px;
  margin-right: 8px;
}

.header-form :deep(.el-form-item.btn-group) {
  margin-left: auto;
  margin-right: auto;
}

/* 部门下拉：选项 + 名称标签 */
.dept-select {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.dept-name-tag {
  white-space: nowrap;
  font-size: 13px;
  color: #409eff;
  background: #ecf5ff;
  padding: 2px 8px;
  border-radius: 3px;
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 单身区 */
.detail-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.detail-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 0 16px 8px;
}

.detail-card :deep(.el-table) {
  margin-top: 0;
}

/* 差异负数标红 */
:deep(.num-neg) {
  color: #f56c6c;
  font-weight: bold;
}

/* 合计行：背景高亮 */
.detail-card :deep(.summary-row td) {
  background: #f0f2f5;
  font-weight: bold;
  color: #303133;
}
</style>
