<template>
  <div class="budget-purchase-pricing-report-page">
    <!-- 单头：查询条件 -->
    <el-card class="header-card" shadow="never">
      <el-form :model="header" label-width="56px" inline class="header-form" @submit.prevent>
        <el-form-item label="账套" required>
          <el-input v-model="header.ent" placeholder="企业代码" clearable style="width: 60px;" />
        </el-form-item>
        <el-form-item label="据点" required>
          <el-input v-model="header.site" placeholder="站点" clearable style="width: 80px;" />
        </el-form-item>
        <el-form-item label="语言">
          <el-input v-model="header.lang" placeholder="语言" clearable style="width: 80px;" />
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
        <el-form-item label="月份" required>
          <el-input-number
            v-model="header.month"
            :min="1"
            :max="12"
            :precision="0"
            controls-position="right"
            style="width: 80px;"
          />
        </el-form-item>
        <el-form-item class="btn-group">
          <el-button type="primary" icon="Search" :loading="loading" @click="handleQuery">查询</el-button>
          <el-button icon="Refresh" @click="handleReset">重置</el-button>
          <el-button icon="Download" :disabled="list.length === 0" @click="exportToExcel">导出Excel</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 单身：报表数据 -->
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

        <!-- 基础信息 -->
        <el-table-column label="基础信息" align="center">
          <el-table-column prop="年" label="年" width="60" align="center" sortable="custom" />
          <el-table-column prop="月" label="月" width="50" align="center" sortable="custom" />
          <el-table-column prop="品号" label="品号" min-width="120" sortable="custom" show-overflow-tooltip />
          <el-table-column prop="品名" label="品名" min-width="150" sortable="custom" show-overflow-tooltip />
          <el-table-column prop="规格" label="规格" min-width="120" sortable="custom" show-overflow-tooltip />
          <el-table-column prop="分群号" label="分群号" min-width="80" sortable="custom" show-overflow-tooltip />
          <el-table-column prop="分群名" label="分群名" min-width="120" sortable="custom" show-overflow-tooltip />
          <el-table-column prop="采购单号" label="采购单号" min-width="120" sortable="custom" show-overflow-tooltip />
          <el-table-column prop="入库单号" label="入库单号" min-width="120" sortable="custom" show-overflow-tooltip />
          <el-table-column prop="供应商" label="供应商" min-width="100" sortable="custom" show-overflow-tooltip />
          <el-table-column prop="供应商名称" label="供应商名称" min-width="150" sortable="custom" show-overflow-tooltip />
        </el-table-column>

        <!-- 数量单价 -->
        <el-table-column label="数量单价" align="center">
          <el-table-column prop="对账数量" label="对账数量" min-width="90" align="right" sortable="custom">
            <template #default="{ row }">{{ fmtNum(row['对账数量']) }}</template>
          </el-table-column>
          <el-table-column prop="单位" label="单位" width="60" align="center" sortable="custom" show-overflow-tooltip />
          <el-table-column prop="实际采购含税单价" label="实际采购含税单价" min-width="130" align="right" sortable="custom">
            <template #default="{ row }">{{ fmtPrice(row['实际采购含税单价']) }}</template>
          </el-table-column>
          <el-table-column prop="参考供应商" label="参考供应商" min-width="120" sortable="custom" show-overflow-tooltip />
          <el-table-column prop="预算采购含税单价" label="预算采购含税单价" min-width="130" align="right" sortable="custom">
            <template #default="{ row }">{{ fmtPrice(row['预算采购含税单价']) }}</template>
          </el-table-column>
        </el-table-column>

        <!-- 计算列 -->
        <el-table-column label="计算列" align="center">
          <el-table-column prop="采购价差" label="采购价差" min-width="100" align="right" sortable="custom">
            <template #default="{ row }">
              <span :class="numClass(row['采购价差'])">{{ fmtPrice(row['采购价差']) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="偏差率" label="偏差率" min-width="90" align="right" sortable="custom">
            <template #default="{ row }">
              <span :class="numClass(row['偏差率'])">{{ fmtPercent(row['偏差率']) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="差异额" label="差异额" min-width="100" align="right" sortable="custom">
            <template #default="{ row }">
              <span :class="numClass(row['差异额'])">{{ fmtPrice(row['差异额']) }}</span>
            </template>
          </el-table-column>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import * as XLSX from 'xlsx'
import { queryBudgetPurchaseAnalysis } from '../api/budget.js'

/** 单头查询条件 */
const header = reactive({
  ent: '60',
  site: 'NBYL',
  lang: 'zh_CN',
  year: new Date().getFullYear(),
  month: new Date().getMonth() + 1
})

/** 单身表格数据 */
const list = ref([])
const loading = ref(false)

/** 当前排序后的数据（不含合计行） */
const sorted = ref([])

/** 合计行 + 数据行，合计行固定显示在表格第一行 */
const displayList = computed(() => {
  const rows = sorted.value
  if (!rows.length) return []
  const sum = {
    年: '', 月: '', 品号: '', 品名: '', 规格: '', 分群号: '', 分群名: '',
    采购单号: '', 入库单号: '', 供应商: '', 供应商名称: '',
    单位: '', 参考供应商: '',
    _isSummary: true
  }
  const sumKeys = ['对账数量', '实际采购含税单价', '预算采购含税单价', '采购价差', '差异额']
  for (const k of sumKeys) {
    sum[k] = rows.reduce((acc, r) => acc + (Number(r[k]) || 0), 0)
  }
  // 偏差率合计用加权平均或留空，这里留空避免误导
  sum['偏差率'] = null
  return [sum, ...rows]
})

/** 合计行高亮样式 */
const rowClassName = ({ row }) => (row._isSummary ? 'summary-row' : '')

/** 取排序列的排序值 */
const getSortVal = (row, prop) => {
  const textCols = ['品号', '品名', '规格', '分群号', '分群名', '采购单号', '入库单号', '供应商', '供应商名称', '单位', '参考供应商']
  if (textCols.includes(prop)) {
    return String(row[prop] ?? '')
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

/** 数字格式化：千分位 + 两位小数 */
const fmtNum = v => {
  if (v === null || v === undefined || v === '') return ''
  const n = Number(v)
  if (isNaN(n)) return String(v)
  return n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

/** 单价格式化：千分位 + 四位小数 */
const fmtPrice = v => {
  if (v === null || v === undefined || v === '') return ''
  const n = Number(v)
  if (isNaN(n)) return String(v)
  return n.toLocaleString('zh-CN', { minimumFractionDigits: 4, maximumFractionDigits: 4 })
}

/** 百分比格式化 */
const fmtPercent = v => {
  if (v === null || v === undefined || v === '') return ''
  const n = Number(v)
  if (isNaN(n)) return String(v)
  return (n * 100).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + '%'
}

/** 差异为负数时标红 */
const numClass = v => {
  const n = Number(v)
  if (isNaN(n)) return ''
  return n < 0 ? 'num-neg' : ''
}

/** 查询 */
const handleQuery = async () => {
  if (!header.ent || String(header.ent).trim() === '') {
    ElMessage.warning('请填写账套')
    return
  }
  if (!header.site || String(header.site).trim() === '') {
    ElMessage.warning('请填写据点')
    return
  }
  if (!header.year) {
    ElMessage.warning('请填写年度')
    return
  }
  if (!header.month) {
    ElMessage.warning('请填写月份')
    return
  }
  const payload = {
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    ent: String(header.ent || '60'),
    site: String(header.site || 'NBYL'),
    lang: String(header.lang || 'zh_CN'),
    year: String(header.year),
    month: String(header.month)
  }
  console.log('预算采购价格分析表查询发送 JSON:', JSON.stringify(payload, null, 2))
  loading.value = true
  try {
    const res = await queryBudgetPurchaseAnalysis(payload)
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
  header.lang = 'zh_CN'
  header.year = new Date().getFullYear()
  header.month = new Date().getMonth() + 1
  list.value = []
  sorted.value = []
}

/** 导出 Excel */
const exportToExcel = () => {
  const headers = [
    '年', '月', '品号', '品名', '规格', '分群号', '分群名',
    '采购单号', '入库单号', '供应商', '供应商名称',
    '对账数量', '单位', '实际采购含税单价', '参考供应商', '预算采购含税单价',
    '采购价差', '偏差率', '差异额'
  ]
  const data = list.value.map(row => {
    const obj = {}
    for (const h of headers) {
      obj[h] = row[h] ?? ''
    }
    return obj
  })
  const ws = XLSX.utils.json_to_sheet(data, { header: headers })
  ws['!cols'] = headers.map(() => ({ wch: 14 }))
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '预算采购价格分析表')
  XLSX.writeFile(wb, `预算采购价格分析表_${header.year}_${String(header.month).padStart(2, '0')}.xlsx`)
  ElMessage.success('导出成功')
}
</script>

<style scoped>
.budget-purchase-pricing-report-page {
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
