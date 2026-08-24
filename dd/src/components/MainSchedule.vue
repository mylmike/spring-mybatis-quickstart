<template>
  <div class="schedule-page">
    <el-card class="basic-card" shadow="never">
      <template #header>
        <span class="card-title">基本数据</span>
      </template>
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
          <el-col :span="24" style="text-align: right;">
            <el-button type="primary" size="large" icon="Search" @click="loadBomTree">查询</el-button>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card class="tree-card" shadow="never">
      <template #header>
        <div class="tree-header">
          <span class="card-title">BOM 结构树</span>
          <el-button size="small" @click="toggleExpand">
            {{ expandAll ? '全部收起' : '全部展开' }}
          </el-button>
        </div>
      </template>
      <el-table
        :key="expandAll ? 'expand' : 'collapse'"
        :data="bomTree"
        row-key="id"
        border
        height="420"
        :default-expand-all="expandAll"
        :tree-props="{ children: 'children' }"
        v-loading="treeLoading"
      >
        <!-- BOM 行作为树形目录列(带展开箭头与缩进),约 35 字符宽 -->
        <el-table-column label="BOM 行" width="280">
          <template #default="{ row }">
            <!-- 顶层主件（level === 1）显示主件品号 bmba001；其余层级显示元件品号 bmba003 -->
            <span class="item-no">{{ (row.level === 1 ? row.bmba001 : row.bmba003) || '-' }}</span>
            <span v-if="row.bmba009" class="item-sep">/</span>
            <span class="item-seq" v-if="row.bmba009">{{ row.bmba009 }}</span>
            <span v-if="row.bmba010" class="item-sep">/</span>
            <span class="item-unit" v-if="row.bmba010">{{ row.bmba010 }}</span>
          </template>
        </el-table-column>
        <!-- 订单号 / 订单序号 为普通列,不参与树形;订单号约 20 字符宽,订单序号约 5 字符宽 -->
        <el-table-column prop="订单号" label="订单号" width="160" />
        <el-table-column prop="订单序号" label="订单序号" width="100" align="center" />
        <el-table-column prop="BOM用量" label="BOM用量" width="130" align="right" :formatter="formatQty" />
        <el-table-column prop="实际用量" label="实际用量" width="130" align="right" :formatter="formatQty" />
        <el-table-column prop="订单需求用量" label="订单需求用量" width="160" align="right" :formatter="formatQty" />
        <el-table-column prop="level" label="层级" width="70" align="center" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchBomTree } from '../api/pendingOrders.js'

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
let treeSeq = 0

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

const toggleExpand = () => {
  expandAll.value = !expandAll.value
}

/** 根据订单号 + 品号查询 BOM 结构树 */
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
    }
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
  overflow: auto;
  background: #f5f7fa;
}

.basic-card,
.tree-card {
  flex-shrink: 0;
  margin-bottom: 16px;
}

.tree-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
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

/* 必填项粉色背景 */
.required-field :deep(.el-input__wrapper) {
  background-color: #ffc0cb;
  box-shadow: 0 0 0 1px #ff69b4 inset;
}
</style>
