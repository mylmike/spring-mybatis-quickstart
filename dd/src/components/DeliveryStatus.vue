<template>
  <div class="delivery-page">
    <div class="query-bar">
      <el-form :inline="true" :model="queryParams" size="small" class="query-form" @submit.prevent>
        <el-form-item label="操作符">
          <el-select v-model="queryParams.czf" placeholder="操作符" clearable style="width:80px">
            <el-option v-for="op in czfOptions" :key="op" :label="op" :value="op" />
          </el-select>
        </el-form-item>
        <el-form-item label="收货数量">
          <el-input v-model="queryParams.receiptQty" placeholder="如 0" clearable style="width:100px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-input v-model="queryParams.status" placeholder="如 2" clearable style="width:100px" />
        </el-form-item>
        <el-form-item label="送货日期">
          <el-date-picker
            v-model="queryParams.deliveryDateStart"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="开始日期"
            style="width:150px"
          />
          <span style="margin:0 4px">至</span>
          <el-date-picker
            v-model="queryParams.deliveryDateEnd"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="结束日期"
            style="width:150px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleQuery">查询</el-button>
          <el-button @click="resetQueryParams">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="toolbar">
      <el-button type="success" size="small" style="margin-left:8px" :disabled="list.length === 0" @click="exportToExcel">导出Excel</el-button>
      <el-radio-group v-model="filter" size="small" style="margin-left:16px">
        <el-radio-button value="all">全部</el-radio-button>
        <el-radio-button value="delayed">只看延迟</el-radio-button>
        <el-radio-button value="received">收货数量&gt;0</el-radio-button>
      </el-radio-group>
      <span style="margin-left:16px;color:#666">共 {{ filteredList.length }} 条记录</span>
      <el-button size="small" style="margin-left:8px" @click="clearFilters">清除筛选</el-button>
      <el-button
        type="danger"
        style="margin-left:auto"
        :disabled="selectedRows.length === 0"
        :loading="deleting"
        @click="handleDelete"
      >删除</el-button>
      <el-button
        type="warning"
        style="margin-left:8px"
        :disabled="selectedRows.length === 0"
        :loading="updating"
        @click="handleUpdate"
      >更新</el-button>
    </div>

    <el-table
      ref="tableRef"
      :data="filteredList"
      border
      height="100%"
      style="flex:1"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column type="index" label="项次" width="60" />
      <el-table-column prop="deliveryNo" label="送货单号" min-width="170">
        <template #header>
          <span class="col-header">
            <span>送货单号</span>
            <el-popover :width="220" trigger="click" placement="bottom-start" :show-arrow="false" popper-class="filter-popover">
              <template #reference>
                <el-icon class="filter-icon" :class="{ active: filterTexts.deliveryNo }"><Filter /></el-icon>
              </template>
              <el-select v-model="filterTexts.deliveryNo" clearable filterable allow-create placeholder="筛选..." size="small" style="width:100%">
                <el-option v-for="v in uniqueValues.deliveryNo" :key="v" :label="v" :value="v" />
              </el-select>
            </el-popover>
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="deliverySeq" label="送货序号" width="100">
        <template #header>
          <span class="col-header">
            <span>送货序号</span>
            <el-popover :width="220" trigger="click" placement="bottom-start" :show-arrow="false" popper-class="filter-popover">
              <template #reference>
                <el-icon class="filter-icon" :class="{ active: filterTexts.deliverySeq }"><Filter /></el-icon>
              </template>
              <el-select v-model="filterTexts.deliverySeq" clearable filterable allow-create placeholder="筛选..." size="small" style="width:100%">
                <el-option v-for="v in uniqueValues.deliverySeq" :key="v" :label="v" :value="v" />
              </el-select>
            </el-popover>
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="purchaseNo" label="采购单号" min-width="160">
        <template #header>
          <span class="col-header">
            <span>采购单号</span>
            <el-popover :width="220" trigger="click" placement="bottom-start" :show-arrow="false" popper-class="filter-popover">
              <template #reference>
                <el-icon class="filter-icon" :class="{ active: filterTexts.purchaseNo }"><Filter /></el-icon>
              </template>
              <el-select v-model="filterTexts.purchaseNo" clearable filterable allow-create placeholder="筛选..." size="small" style="width:100%">
                <el-option v-for="v in uniqueValues.purchaseNo" :key="v" :label="v" :value="v" />
              </el-select>
            </el-popover>
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="purchaseSeq" label="采购序号" width="100">
        <template #header>
          <span class="col-header">
            <span>采购序号</span>
            <el-popover :width="220" trigger="click" placement="bottom-start" :show-arrow="false" popper-class="filter-popover">
              <template #reference>
                <el-icon class="filter-icon" :class="{ active: filterTexts.purchaseSeq }"><Filter /></el-icon>
              </template>
              <el-select v-model="filterTexts.purchaseSeq" clearable filterable allow-create placeholder="筛选..." size="small" style="width:100%">
                <el-option v-for="v in uniqueValues.purchaseSeq" :key="v" :label="v" :value="v" />
              </el-select>
            </el-popover>
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="itemNo" label="品号" min-width="120">
        <template #header>
          <span class="col-header">
            <span>品号</span>
            <el-popover :width="220" trigger="click" placement="bottom-start" :show-arrow="false" popper-class="filter-popover">
              <template #reference>
                <el-icon class="filter-icon" :class="{ active: filterTexts.itemNo }"><Filter /></el-icon>
              </template>
              <el-select v-model="filterTexts.itemNo" clearable filterable allow-create placeholder="筛选..." size="small" style="width:100%">
                <el-option v-for="v in uniqueValues.itemNo" :key="v" :label="v" :value="v" />
              </el-select>
            </el-popover>
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="supplierNo" label="供应商编码" min-width="120">
        <template #header>
          <span class="col-header">
            <span>供应商编码</span>
            <el-popover :width="220" trigger="click" placement="bottom-start" :show-arrow="false" popper-class="filter-popover">
              <template #reference>
                <el-icon class="filter-icon" :class="{ active: filterTexts.supplierNo }"><Filter /></el-icon>
              </template>
              <el-select v-model="filterTexts.supplierNo" clearable filterable allow-create placeholder="筛选..." size="small" style="width:100%">
                <el-option v-for="v in uniqueValues.supplierNo" :key="v" :label="v" :value="v" />
              </el-select>
            </el-popover>
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="purchaserName" label="采购员" width="100">
        <template #header>
          <span class="col-header">
            <span>采购员</span>
            <el-popover :width="220" trigger="click" placement="bottom-start" :show-arrow="false" popper-class="filter-popover">
              <template #reference>
                <el-icon class="filter-icon" :class="{ active: filterTexts.purchaserName }"><Filter /></el-icon>
              </template>
              <el-select v-model="filterTexts.purchaserName" clearable filterable allow-create placeholder="筛选..." size="small" style="width:100%">
                <el-option v-for="v in uniqueValues.purchaserName" :key="v" :label="v" :value="v" />
              </el-select>
            </el-popover>
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="deliveryQty" label="送货数量" min-width="110" :formatter="formatQty">
        <template #header>
          <span class="col-header">
            <span>送货数量</span>
            <el-popover :width="220" trigger="click" placement="bottom-start" :show-arrow="false" popper-class="filter-popover">
              <template #reference>
                <el-icon class="filter-icon" :class="{ active: filterTexts.deliveryQty }"><Filter /></el-icon>
              </template>
              <el-select v-model="filterTexts.deliveryQty" clearable filterable allow-create placeholder="筛选..." size="small" style="width:100%">
                <el-option v-for="v in uniqueValues.deliveryQty" :key="v" :label="v" :value="v" />
              </el-select>
            </el-popover>
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="deliveryDate" label="送货日期" min-width="120" :formatter="formatDate">
        <template #header>
          <span class="col-header">
            <span>送货日期</span>
            <el-popover :width="220" trigger="click" placement="bottom-start" :show-arrow="false" popper-class="filter-popover">
              <template #reference>
                <el-icon class="filter-icon" :class="{ active: filterTexts.deliveryDate }"><Filter /></el-icon>
              </template>
              <el-select v-model="filterTexts.deliveryDate" clearable filterable allow-create placeholder="筛选..." size="small" style="width:100%">
                <el-option v-for="v in uniqueValues.deliveryDate" :key="v" :label="v" :value="v" />
              </el-select>
            </el-popover>
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="delayDays" label="延期天数" min-width="100">
        <template #header>
          <span class="col-header">
            <span>延期天数</span>
            <el-popover :width="220" trigger="click" placement="bottom-start" :show-arrow="false" popper-class="filter-popover">
              <template #reference>
                <el-icon class="filter-icon" :class="{ active: filterTexts.delayDays }"><Filter /></el-icon>
              </template>
              <el-select v-model="filterTexts.delayDays" clearable filterable allow-create placeholder="筛选..." size="small" style="width:100%">
                <el-option v-for="v in uniqueValues.delayDays" :key="v" :label="v" :value="v" />
              </el-select>
            </el-popover>
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="pmdtdocno" label="收货单号" min-width="140">
        <template #header>
          <span class="col-header">
            <span>收货单号</span>
            <el-popover :width="220" trigger="click" placement="bottom-start" :show-arrow="false" popper-class="filter-popover">
              <template #reference>
                <el-icon class="filter-icon" :class="{ active: filterTexts.pmdtdocno }"><Filter /></el-icon>
              </template>
              <el-select v-model="filterTexts.pmdtdocno" clearable filterable allow-create placeholder="筛选..." size="small" style="width:100%">
                <el-option v-for="v in uniqueValues.pmdtdocno" :key="v" :label="v" :value="v" />
              </el-select>
            </el-popover>
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="pmdtseq" label="收货序号" width="100">
        <template #header>
          <span class="col-header">
            <span>收货序号</span>
            <el-popover :width="220" trigger="click" placement="bottom-start" :show-arrow="false" popper-class="filter-popover">
              <template #reference>
                <el-icon class="filter-icon" :class="{ active: filterTexts.pmdtseq }"><Filter /></el-icon>
              </template>
              <el-select v-model="filterTexts.pmdtseq" clearable filterable allow-create placeholder="筛选..." size="small" style="width:100%">
                <el-option v-for="v in uniqueValues.pmdtseq" :key="v" :label="v" :value="v" />
              </el-select>
            </el-popover>
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="pmdt020" label="收货数量" min-width="140">
        <template #header>
          <span class="col-header">
            <span>收货数量</span>
            <el-popover :width="220" trigger="click" placement="bottom-start" :show-arrow="false" popper-class="filter-popover">
              <template #reference>
                <el-icon class="filter-icon" :class="{ active: filterTexts.pmdt020 }"><Filter /></el-icon>
              </template>
              <el-select v-model="filterTexts.pmdt020" clearable filterable allow-create placeholder="筛选..." size="small" style="width:100%">
                <el-option v-for="v in uniqueValues.pmdt020" :key="v" :label="v" :value="v" />
              </el-select>
            </el-popover>
          </span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Filter } from '@element-plus/icons-vue'
import * as XLSX from 'xlsx'
import { queryDeliveryMatch, updateDeliveryMatch, deleteDeliveryMatch } from '../api/pendingOrders.js'

const list = ref([])
const loading = ref(false)
const updating = ref(false)
const deleting = ref(false)
const filter = ref('all')
const tableRef = ref(null)
const selectedRows = ref([])

const czfOptions = ['=', '>', '>=', '<', '<=']

// 查询条件：默认送货日期为最近 12 天（含今天），其他为空
const initQueryParams = () => {
  const fmt = (d) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
  const end = new Date()
  const start = new Date(Date.now() - 11 * 24 * 3600 * 1000)
  return {
    czf: '',
    receiptQty: '',
    status: '',
    deliveryDateStart: fmt(start),
    deliveryDateEnd: fmt(end)
  }
}
const queryParams = ref(initQueryParams())

const resetQueryParams = () => {
  queryParams.value = initQueryParams()
}

const filteredList = computed(() => {
  let result = list.value
  if (filter.value === 'delayed') {
    result = result.filter(item => Number(item.delayDays) > 0)
  } else if (filter.value === 'received') {
    result = result.filter(item => Number(item.pmdt020) > 0)
  }
  // apply column text filters (substring match, case-insensitive)
  filterProps.forEach(prop => {
    const keyword = filterTexts.value[prop]
    if (keyword) {
      result = result.filter(item => String(item[prop] || '').toLowerCase().includes(keyword.toLowerCase()))
    }
  })
  // sort by deliveryDate descending (most recent first)
  result = [...result].sort((a, b) => {
    const da = a.deliveryDate ? new Date(a.deliveryDate) : null
    const db = b.deliveryDate ? new Date(b.deliveryDate) : null
    if (!da && !db) return 0
    if (!da) return 1
    if (!db) return -1
    return db - da
  })
  return result
})

const filterProps = ['deliveryNo', 'deliverySeq', 'purchaseNo', 'purchaseSeq', 'itemNo', 'supplierNo', 'purchaserName', 'pmdtdocno', 'pmdtseq', 'pmdt020', 'deliveryQty', 'deliveryDate', 'delayDays']

const initFilterTexts = () => {
  const obj = {}
  filterProps.forEach(p => { obj[p] = '' })
  return obj
}
const filterTexts = ref(initFilterTexts())

const uniqueValues = computed(() => {
  const result = {}
  filterProps.forEach(prop => {
    const set = new Set()
    list.value.forEach(item => {
      const val = item[prop]
      if (val !== '' && val !== null && val !== undefined) {
        set.add(String(val))
      }
    })
    result[prop] = [...set].sort()
  })
  return result
})

const clearFilters = () => {
  filterTexts.value = initFilterTexts()
}

const handleQuery = async () => {
  loading.value = true
  try {
    const res = await queryDeliveryMatch({
      czf: queryParams.value.czf || '',
      receiptQty: queryParams.value.receiptQty || '',
      status: queryParams.value.status || '',
      deliveryDateStart: queryParams.value.deliveryDateStart || '',
      deliveryDateEnd: queryParams.value.deliveryDateEnd || ''
    })
    const rawList = res.data?.matches ?? res.data?.data ?? []
    list.value = rawList.map(item => ({
      purchaseNo: item.purchaseNo || '',
      purchaseSeq: item.purchaseSeq || '',
      itemNo: item.itemNo || '',
      deliveryNo: item.deliveryNo || '',
      supplierNo: item.supplierNo || '',
      purchaserName: item.purchaserName || '',
      deliverySeq: item.deliverySeq || '',
      pmdtdocno: item.pmdtdocno || '',
      pmdtseq: item.pmdtseq || '',
      pmdt020: item.pmdt020 || '',
      deliveryQty: item.deliveryQty || '0',
      deliveryDate: item.deliveryDate || '',
      delayDays: item.delayDays ?? ''
    }))
  } catch (err) {
    ElMessage.error('查询失败: ' + (err.response?.data?.message || err.message))
  } finally {
    loading.value = false
  }
}

const formatDate = (row, column, value) => {
  if (!value) return ''
  const d = new Date(value)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

const formatQty = (row, column, value) => {
  if (!value) return '0'
  return parseFloat(value).toString()
}

const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

const handleUpdate = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择记录')
    return
  }
  const payload = selectedRows.value.map(item => ({
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    deliveryNo: item.deliveryNo || '',
    deliverySeq: item.deliverySeq || '',
    purchaseNo: item.purchaseNo || '',
    purchaseSeq: item.purchaseSeq || '',
    deliveryQty: item.deliveryQty || '0',
    pmdt020Qty: item.pmdt020 || '0',
    ent: '60',
    site: 'NBYL'
  }))
  updating.value = true
  let successCount = 0
  try {
    for (let i = 0; i < payload.length; i++) {
      const item = payload[i]
      console.log(`=== 更新第 ${i + 1}/${payload.length} 条, 送货单号: ${item.deliveryNo} ===`)
      console.log('请求体:', JSON.stringify([item], null, 2))
      const res = await updateDeliveryMatch([item])
      console.log('响应:', res.data)
      successCount++
    }
    ElMessage.success(`更新完成, 共 ${successCount}/${payload.length} 条`)
    await handleQuery()
    tableRef.value?.clearSelection()
    selectedRows.value = []
  } catch (err) {
    ElMessage.error(`第 ${successCount + 1}/${payload.length} 条更新失败: ` + (err.response?.data?.message || err.message))
  } finally {
    updating.value = false
  }
}

const handleDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择记录')
    return
  }
  const shortDelay = selectedRows.value.filter(item => Number(item.delayDays) <= 30)
  if (shortDelay.length > 0) {
    const list = shortDelay.map(i => `${i.deliveryNo}(${i.deliverySeq}) ${i.delayDays}天`).join('、')
    ElMessage.error(`以下记录延期天数未超过30天，不允许删除：${list}`)
    return
  }
  const payload = selectedRows.value.map(item => ({
    token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
    ent: '60',
    site: 'NBYL',
    deliveryNo: item.deliveryNo || '',
    deliverySeq: item.deliverySeq || '',
    purchaseNo: item.purchaseNo || '',
    purchaseSeq: item.purchaseSeq || '',
    deliveryQty: item.deliveryQty || '0'
  }))
  deleting.value = true
  let successCount = 0
  try {
    for (let i = 0; i < payload.length; i++) {
      const item = payload[i]
      console.log(`=== 删除第 ${i + 1}/${payload.length} 条, 送货单号: ${item.deliveryNo} ===`)
      console.log('请求体:', JSON.stringify([item], null, 2))
      const res = await deleteDeliveryMatch([item])
      console.log('响应:', res.data)
      if (res.data && res.data.success) {
        successCount++
      } else {
        throw new Error(res.data?.message || '删除失败')
      }
    }
    ElMessage.success(`删除完成, 共 ${successCount}/${payload.length} 条`)
    await handleQuery()
    tableRef.value?.clearSelection()
    selectedRows.value = []
  } catch (err) {
    ElMessage.error(`第 ${successCount + 1}/${payload.length} 条删除失败: ` + (err.response?.data?.message || err.message))
  } finally {
    deleting.value = false
  }
}

const exportToExcel = () => {
  const columns = [
    { prop: 'deliveryNo', label: '送货单号' },
    { prop: 'deliverySeq', label: '送货序号' },
    { prop: 'purchaseNo', label: '采购单号' },
    { prop: 'purchaseSeq', label: '采购序号' },
    { prop: 'itemNo', label: '品号' },
    { prop: 'supplierNo', label: '供应商编码' },
    { prop: 'purchaserName', label: '采购员' },
    { prop: 'deliveryQty', label: '送货数量' },
    { prop: 'deliveryDate', label: '送货日期' },
    { prop: 'delayDays', label: '延期天数' },
    { prop: 'pmdtdocno', label: '收货单号' },
    { prop: 'pmdtseq', label: '收货序号' },
    { prop: 'pmdt020', label: '收货数量' }
  ]
  const data = filteredList.value.map(row => {
    const obj = {}
    columns.forEach(col => { obj[col.label] = row[col.prop] || '' })
    return obj
  })
  const ws = XLSX.utils.json_to_sheet(data)
  // set column widths
  ws['!cols'] = columns.map(c => ({ wch: Math.max(c.label.length * 2, 14) }))
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '送货明细')
  const now = new Date()
  const filename = `送货明细_${now.getFullYear()}${String(now.getMonth()+1).padStart(2,'0')}${String(now.getDate()).padStart(2,'0')}.xlsx`
  XLSX.writeFile(wb, filename)
}
</script>

<style scoped>
.delivery-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.query-bar {
  flex-shrink: 0;
  background: #fafafa;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 8px 12px 0 4px;
}
.query-form {
  margin-bottom: 0;
}
.query-form :deep(.el-form-item) {
  margin-bottom: 8px;
  margin-right: 12px;
}
.toolbar {
  flex-shrink: 0;
  display: flex;
  align-items: center;
}

.col-header {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
}
.filter-icon {
  cursor: pointer;
  color: #c0c4cc;
  font-size: 14px;
  transition: color 0.2s;
}
.filter-icon:hover {
  color: #409eff;
}
.filter-icon.active {
  color: #409eff;
}
:global(.filter-popover) {
  padding: 8px !important;
}
:global(.filter-popover .el-popper__arrow) {
  display: none;
}
</style>
