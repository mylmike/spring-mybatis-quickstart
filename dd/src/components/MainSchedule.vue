<template>
  <div class="schedule-page">
    <el-card class="basic-card" shadow="never">
      <el-form :model="form" label-width="90px" class="basic-form">
        <el-row :gutter="24">
          <el-col :span="4">
            <el-form-item label="企业代码" required class="required-field">
              <el-input v-model="form.enterpriseCode" />
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-form-item label="单号" required class="required-field">
              <el-input v-model="form.orderNo" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-form-item label="成本中心">
              <el-input v-model="form.costCenter" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="订单号">
              <el-input v-model="form.customerOrderNo" clearable @keyup.enter="loadBomTree" />
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="订单序号">
              <el-input v-model="form.orderSeq" clearable style="width: 80px;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="6">
            <el-form-item label="预计日期">
              <el-date-picker v-model="form.estimatedDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="营运据点">
              <el-input v-model="form.site" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="品号">
              <el-input v-model="form.itemNo" clearable @keyup.enter="loadBomTree" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <div class="form-btn-row">
              <el-button type="primary" size="small" icon="Search" @click="loadBomTree" style="height: 16px; padding: 0 12px; font-size: 12px;">查询</el-button>
            </div>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card class="tree-card" shadow="never">
      <el-table
        ref="tableRef"
        :key="expandAll ? 'expand' : 'collapse'"
        :data="bomTree"
        row-key="id"
        border
        :height="tableHeight"
        :default-expand-all="expandAll"
        :row-class-name="rowClassName"
        highlight-current-row
        :tree-props="{ children: 'children' }"
        v-loading="treeLoading"
      >
        <!-- BOM 行作为树形目录列(带展开箭头与缩进),约 35 字符宽 -->
        <el-table-column label="BOM 行" width="280">
          <template #header>
            <el-button size="small" @click="toggleExpand">
              <el-icon :size="14" style="vertical-align: middle; margin-right: 4px;">
                <Expand v-if="!expandAll" />
                <Fold v-else />
              </el-icon>
              {{ expandAll ? '全部收起' : '全部展开' }}
            </el-button>
          </template>
          <template #default="{ row }">
            <!-- 顶层主件（level === 1）显示主件品号 bmba001；其余层级显示元件品号 bmba003 -->
            <span class="item-no">{{ (row.level === 1 ? row.bmba001 : row.bmba003) || '-' }}</span>
            <span v-if="row.bmba009" class="item-sep">/</span>
            <span class="item-seq" v-if="row.bmba009">{{ row.bmba009 }}</span>
            <span v-if="row.bmba010" class="item-sep">/</span>
            <span class="item-unit" v-if="row.bmba010">{{ row.bmba010 }}</span>
          </template>
        </el-table-column>
        <!-- 层级放在 BOM 行之后、订单号之前 -->
        <el-table-column prop="level" label="层级" width="40" align="center" />
        <el-table-column prop="imaal003" label="品名" width="160" show-overflow-tooltip />
        <el-table-column prop="imaal004" label="规格" width="120" show-overflow-tooltip />
        <!-- 订单号 / 订单序号 为普通列,不参与树形;订单号约 20 字符宽,订单序号约 5 字符宽 -->
        <el-table-column prop="订单号" label="订单号" width="120" />
        <el-table-column prop="订单序号" label="序号" width="40" align="center" />
        <el-table-column prop="BOM用量" label="BOM量" width="60" align="right" :formatter="formatQty" />
        <el-table-column prop="实际用量" label="实际量" width="60" align="right" :formatter="formatQty" />
        <el-table-column prop="订单需求用量" label="订单需求量" width="70" align="right" :formatter="formatQty" />
        <!-- 以下为 /queryOrderBom 直接返回的工单字段（来源：sfaa_t），后端在每个 BOM 节点上带出 -->
        <el-table-column prop="sfaadocno" label="工单号" width="120" />
        <el-table-column prop="imaf013" label="补货策略" width="80" align="center" :formatter="formatMakeType" />
        <el-table-column prop="sfaa068" label="成本中心" width="60" />
        <el-table-column prop="ooefl003" label="成本中心名称" width="100" show-overflow-tooltip />
        <el-table-column prop="xmdd011" label="出货日期" width="120" />
        <el-table-column prop="sfaa019" label="预计开工日期" width="150">
          <template #default="{ row }">
            <el-date-picker
              v-if="row.sfaadocno"
              v-model="row.sfaa019"
              type="date"
              placeholder="选择日期"
              value-format="YYYY-MM-DD"
              :disabled="isClosed(row)"
              style="width: 130px"
            />
          </template>
        </el-table-column>
        <el-table-column prop="sfaa020" label="预计完工日期" width="150">
          <template #default="{ row }">
            <el-date-picker
              v-if="row.sfaadocno"
              v-model="row.sfaa020"
              type="date"
              placeholder="选择日期"
              value-format="YYYY-MM-DD"
              :disabled="isClosed(row)"
              style="width: 130px"
            />
          </template>
        </el-table-column>
        <el-table-column prop="sfaa012" label="生产数量" width="100" align="right" />
        <el-table-column prop="sfaa050" label="入库数量" width="100" align="right" />
        <el-table-column prop="sfaastus" label="状态码" width="80" align="center" :formatter="formatStatus" />
        <el-table-column prop="upph" label="UPPH" width="80" align="right" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Expand, Fold } from '@element-plus/icons-vue'
import { fetchBomTree, queryItemBasicInfo, WORK_ORDER_STATUS_MAP } from '../api/pendingOrders.js'

const form = reactive({
  orderNo: '',
  costCenter: '',
  customerOrderNo: '',
  orderSeq: '',
  estimatedDate: '',
  site: 'NBYL',
  enterpriseCode: '60',
  itemNo: ''
})

/** BOM 结构树数据 */
const bomTree = ref([])
const treeLoading = ref(false)
const expandAll = ref(true)
const tableRef = ref(null)
const tableHeight = ref(300)
let treeSeq = 0

/** 测量 tree-card 实际高度，绑定给 el-table 的 height，
 *  这样横向滚动条固定在可视区底部、竖向滚动时也能横向滚动 */
let ro = null
const measureTableHeight = () => {
  const el = tableRef.value?.$el
  if (el && el.parentElement) {
    tableHeight.value = el.parentElement.clientHeight
  }
}
onMounted(async () => {
  await nextTick()
  measureTableHeight()
  ro = new ResizeObserver(() => measureTableHeight())
  if (tableRef.value?.$el?.parentElement) {
    ro.observe(tableRef.value.$el.parentElement)
  }
})
onBeforeUnmount(() => {
  if (ro) ro.disconnect()
})

/** 工单状态码 sfaastus → 中文；未匹配时原样返回 */
const mapWorkOrderStatus = (sfaastus) => {
  const code = (sfaastus ?? '').toString().trim()
  if (!code) return ''
  return WORK_ORDER_STATUS_MAP[code] ?? code
}

/** 品号 → UPPH 缓存，避免重复请求 */
const upphCache = {}
/** 按品号调 queryItemBasicInfo 取 UPPH */
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

/** 工单明细数据（单身） */
// 已移除独立工单明细卡：/queryOrderBom 已在每个 BOM 节点带出 sfaa* 字段。

/** 为树节点生成唯一 id,便于 el-table 树形展开 */
const addTreeIds = (nodes) => {
  return (nodes || []).map(node => {
    treeSeq += 1
    return { ...node, id: `tree-${treeSeq}`, children: addTreeIds(node.children) }
  })
}

/** 数值格式化:去除多余小数位与尾随零 */
const formatQty = (row, column, cellValue) => {
  if (cellValue === undefined || cellValue === null || cellValue === '') return ''
  return Number(Number(cellValue).toFixed(6)).toString()
}

/** 状态码 sfaastus → 中文（与车间排程一致：F=已发放 / C=已结案 等） */
const formatStatus = (row, column, cellValue) => {
  if (!cellValue) return ''
  return WORK_ORDER_STATUS_MAP[cellValue] ?? cellValue
}

/** 补货策略 imaf013：1=采购件 / 2=自制件 / 3=委外件 */
const MAKE_TYPE_MAP = { '1': '采购件', '2': '自制件', '3': '委外件' }
const formatMakeType = (row, column, cellValue) => {
  if (!cellValue) return ''
  return MAKE_TYPE_MAP[cellValue] ?? cellValue
}

/** 是否已结案（成本结案 / 已结案）：用于行高亮与日期只读。
 * 兼容后端返回字母代码 M/C 以及已映射过的中文"成本结案"/"已结案"。 */
const CLOSED_STATUS_CODES = new Set(['M', 'C'])
const CLOSED_STATUS_NAMES = new Set(['成本结案', '已结案'])
const isClosed = (row) => {
  const raw = (row?.sfaastus ?? '').toString().trim()
  if (!raw) return false
  if (CLOSED_STATUS_CODES.has(raw)) return true
  if (CLOSED_STATUS_NAMES.has(raw)) return true
  // 也兼容形如"成本结案(已审核)"这种带后缀的情况
  return /^(成本结案|已结案)/.test(raw)
}

/** 行 class：结案行加 .closed-row 用于蓝色背景 */
const rowClassName = ({ row }) => (isClosed(row) ? 'closed-row' : '')

/** 递归收集所有树节点 */
const flattenTree = (nodes, out = []) => {
  for (const node of nodes || []) {
    out.push(node)
    if (node.children?.length) flattenTree(node.children, out)
  }
  return out
}

const toggleExpand = () => {
  expandAll.value = !expandAll.value
}

/**
 * 查询：调 /queryOrderBom 一次性拿到 BOM 树 + 工单字段（sfaa* 由后端在节点上带出），
 * 再为每行按品号查 /queryItemBasicInfo 补 UPPH。
 */
const loadBomTree = async () => {
  treeLoading.value = true
  try {
    const res = await fetchBomTree({
      xmdddocno: form.customerOrderNo,
      xmdd001: form.itemNo,
      xmddseq: form.orderSeq
    })
    const rawTree = res.data?.tree ?? res.data?.data?.tree ?? []
    treeSeq = 0
    bomTree.value = addTreeIds(Array.isArray(rawTree) ? rawTree : [])
    if (bomTree.value.length === 0) {
      ElMessage.info('未查询到 BOM 结构数据')
      return
    }
    // UPPH：按节点品号（level=1 取 bmba001，其余取 bmba003）查 queryItemBasicInfo，
    // 结果直接回填到节点对象的 upph 字段
    const allNodes = flattenTree(bomTree.value)
    for (const n of allNodes) {
      n.itemNo = n.level === 1 ? (n.bmba001 || '') : (n.bmba003 || '')
      // 成本中心 / 成本中心名为空时，用默认成本中心（imae035 / imae035Name）填补
      if (!n.sfaa068 && n.imae035) n.sfaa068 = n.imae035
      if (!n.ooefl003 && n.imae035Name) n.ooefl003 = n.imae035Name
    }
    await fillUpph(allNodes)
  } catch (err) {
    ElMessage.error('获取 BOM 结构失败: ' + (err.response?.data?.message || err.message))
  } finally {
    treeLoading.value = false
  }
}
</script>

<style scoped>
.schedule-page {
  height: 100%;
  padding: 16px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #f5f7fa;
}

.basic-card,
.tree-card {
  flex-shrink: 0;
  margin-bottom: 0;
}

.basic-card {
  height: 110px;
}

/* 去除卡片内边距，第一行与卡片边框贴紧 */
.basic-card :deep(.el-card__body) {
  padding-top: 0;
  padding-bottom: 0;
}

/* 单身字体 12px */
.tree-card :deep(.el-table) {
  font-size: 12px;
}

.tree-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}

.tree-card :deep(.el-table) {
  flex: none;
}

/* 让 el-table 自带的横向滚动条始终显示，
   避免竖向滚动到底才看到横向滚动条。
   仅在列宽总和超过容器时生效。 */
.tree-card :deep(.el-table__body-wrapper),
.tree-card :deep(.el-table__inner-wrapper),
.tree-card :deep(.el-scrollbar__wrap) {
  overflow-x: auto;
}

/* 去除卡片内边距，单身表与卡片边框贴紧 */
.tree-card :deep(.el-card__body) {
  padding: 0;
}

/* 表单行高 30px：输入框 20px，行间距为 0 */
.basic-form :deep(.el-form-item) {
  margin-bottom: 0;
}

/* 单头字体 12px */
.basic-form :deep(.el-form-item__label),
.basic-form :deep(.el-input__inner),
.basic-form :deep(.el-input),
.basic-form :deep(.el-date-picker),
.basic-form :deep(.el-date-editor),
.basic-form :deep(.el-select-v2),
.basic-form :deep(.el-button) {
  font-size: 12px;
}

/* 文本框 / 选择框 高度 20px */
.basic-form :deep(.el-input__wrapper),
.basic-form :deep(.el-select-v2__wrapper),
.basic-form :deep(.el-date-editor.el-input__wrapper) {
  min-height: 20px;
  height: 20px;
  padding-top: 0;
  padding-bottom: 0;
}

/* 表格紧凑：减小单元格内边距 */
.tree-card :deep(.el-table .cell) {
  padding: 0 4px;
}

.tree-card :deep(.el-table th.el-table__cell) {
  padding: 0;
}

/* 标题行高 40px */
.tree-card :deep(.el-table th.el-table__cell > .cell) {
  height: 40px;
  line-height: 40px;
}

/* 结案行（成本结案 M / 生管结案 C）背景蓝色 */
.tree-card :deep(.el-table .closed-row),
.tree-card :deep(.el-table .closed-row td) {
  background-color: #cfe3ff !important;
}

/* 单击选中的当前行：灰色背景（优先级高于结案蓝，最后点击的为准） */
.tree-card :deep(.el-table__body tr.current-row > td.el-table__cell),
.tree-card :deep(.el-table .current-row),
.tree-card :deep(.el-table .current-row td) {
  background-color: #e0e0e0 !important;
}

/* BOM 行表头：按钮靠左 */
.tree-card :deep(.el-table th.el-table__cell > .cell > .el-button) {
  vertical-align: middle;
}

.tree-card :deep(.el-table td.el-table__cell) {
  padding: 2px 0;
}

.tree-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* 所有表头文字居中（单元格数据的对齐方式保持不变） */
.tree-card :deep(.el-table th.el-table__cell > .cell) {
  text-align: center;
}

.item-no {
  font-weight: 600;
}

.item-sep {
  color: #c0c4cc;
  margin: 0 6px;
}

.item-name {
  color: #303133;
}

.item-seq {
  color: #606266;
}

.item-unit {
  color: #67c23a;
  font-weight: 500;
}

.card-title {
  font-weight: 600;
  font-size: 16px;
}



.basic-form {
  padding-right: 16px;
}

.form-btn-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* 必填项粉色背景 */
.required-field :deep(.el-input__wrapper) {
  background-color: #ffc0cb;
  box-shadow: 0 0 0 1px #ff69b4 inset;
}
</style>
