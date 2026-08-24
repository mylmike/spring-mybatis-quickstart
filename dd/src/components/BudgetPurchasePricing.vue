<template>
  <div class="budget-page">
    <!-- 功能区 -->
    <div class="action-bar">
      <el-button type="primary" size="default" icon="Search" :disabled="isQueryMode || isEdit" @click="handleQuery">查询</el-button>
      <el-button v-if="isQueryMode" type="success" size="default" icon="Check" @click="handleConfirmQuery" :loading="loading">确认</el-button>
      <el-button type="success" size="default" icon="Plus" :disabled="isEdit || isQueryMode" @click="handleAdd">新增</el-button>
      <el-button type="success" size="default" icon="Edit" :disabled="isEdit || isQueryMode" @click="handleEdit">编辑</el-button>
      <el-button type="warning" size="default" icon="Document" :disabled="!isEdit" @click="handleSave">保存</el-button>
      <el-button type="danger" size="default" icon="Delete" :disabled="detailList.length === 0 || isQueryMode" @click="handleDelete">删除</el-button>
      <el-button size="default" icon="Download" @click="handleExport">导出模板</el-button>
      <el-button size="default" icon="Upload" @click="handleImport">导入Excel</el-button>
      <el-button size="default" icon="CircleClose" :disabled="!isEdit && !isQueryMode" @click="handleCancel">取消</el-button>
      <input ref="importFileRef" type="file" accept=".xlsx,.xls" style="display:none" @change="handleFileChange" />
    </div>

    <el-card class="header-card" shadow="never">
      <el-form :model="header" label-width="80px" class="header-form" inline>
        <el-form-item label="账套" required class="required-field">
          <el-input v-model="header.bgbtucent" :disabled="!isEdit && !isQueryMode" placeholder="企业代码" style="width: 140px;" />
        </el-form-item>
        <el-form-item label="账别" required class="required-field">
          <el-input v-model="header.bgbtucld" :disabled="!isEdit && !isQueryMode" placeholder="账别" style="width: 140px;" />
        </el-form-item>
        <el-form-item v-if="isQueryMode" label="物料编号">
          <el-input v-model="header.bgbtuc001" placeholder="物料编号（可空，按物料筛选）" clearable style="width: 220px;" />
        </el-form-item>
        <el-form-item label="年度" required class="required-field">
          <el-input-number
            v-model="header.bgbtuc002"
            :disabled="!isEdit && !isQueryMode"
            :min="2000"
            :precision="0"
            size="default"
            controls-position="right"
            style="width: 140px;"
          />
        </el-form-item>
        <el-form-item label="请求JSON" class="json-field">
          <el-input
            v-model="requestJson"
            type="textarea"
            :rows="2"
            readonly
            resize="none"
            placeholder="点击【保存】/【确认】/【删除】后显示发到后台的 JSON"
            class="json-textarea"
          />
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="detail-card" shadow="never">
      <template #header v-if="isEdit || isQueryMode">
        <div class="detail-header">
          <div class="detail-tools">
            <el-button type="primary" size="small" icon="Plus" :disabled="!canEditDetail" @click="addRow">新增行</el-button>
            <el-button type="danger" size="small" icon="Delete" :disabled="!canEditDetail || detailSelected.length === 0" @click="removeRows">删除选中</el-button>
          </div>
        </div>
      </template>

      <el-table
        ref="detailTable"
        :data="detailList"
        border
        height="100%"
        @selection-change="handleDetailSelection"
        :row-key="row => row._uid"
      >
        <el-table-column type="selection" width="50" :reserve-selection="true" :selectable="() => canEditDetail || isQueryMode" />
        <el-table-column type="index" label="序号" width="55" align="center" />

        <el-table-column prop="bgbtuc001" label="物料编号" min-width="150" align="center">
          <template #default="{ row }">
            <el-input
              v-model="row.bgbtuc001"
              :disabled="!(canEditDetail || isQueryMode)"
              size="small"
              style="width: 100%;"
              placeholder="物料编号"
            />
          </template>
        </el-table-column>

        <el-table-column prop="bgbtuc003" label="供应商/采购员" min-width="140" align="center">
          <template #default="{ row }">
            <el-input
              v-model="row.bgbtuc003"
              :disabled="!(canEditDetail || isQueryMode)"
              size="small"
              style="width: 100%;"
              placeholder="如 S001"
            />
          </template>
        </el-table-column>

        <el-table-column prop="bgbtuc005" label="本币金额" min-width="160" align="right">
          <template #default="{ row }">
            <el-input-number
              v-model="row.bgbtuc005"
              :disabled="!(canEditDetail || isQueryMode)"
              :precision="6"
              :step="0.000001"
              size="small"
              controls-position="right"
              style="width: 100%;"
            />
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { queryBgbtuc, saveBgbtuc, deleteBgbtuc } from '../api/budget.js'
import * as XLSX from 'xlsx'

/** 生成唯一行标识 */
let _uid = 0
const genUid = () => ++_uid

/** 单头区 */
const header = reactive({
  bgbtucent: '60',
  bgbtucld: 'NBYL',
  bgbtuc001: '',
  bgbtuc002: new Date().getFullYear()
})

/** 是否处于编辑模式 */
const isEdit = ref(false)
/** 是否处于查询输入模式 */
const isQueryMode = ref(false)
/** 单身是否可编辑：编辑模式 + 单头必填字段完整 */
const canEditDetail = computed(() => {
  return isEdit.value &&
    !!header.bgbtucent && String(header.bgbtucent).trim() !== '' &&
    !!header.bgbtucld && String(header.bgbtucld).trim() !== '' &&
    !!header.bgbtuc002 && Number(header.bgbtuc002) !== 0
})
/** 查询模式前的快照（供取消时还原） */
const preQuerySnapshot = ref(null)
/** 加载状态 */
const loading = ref(false)
/** 单身表格数据 */
const detailList = ref([])
/** 表格已选中的行 */
const detailSelected = ref([])
const detailTable = ref(null)
const importFileRef = ref(null)

/** Excel 列头 */
const EXCEL_COLS = ['企业代码/账套', '账别', '物料编号', '年度', '供应商/采购员', '本币金额']

/** 加载时的原始快照，用于判断是否有修改 */
const originalJson = ref('')
/** 最近一次发后台的 JSON（用于调试） */
const requestJson = ref('')

/** 把后端字段映射成表格数据 */
const mapDetail = item => ({
  _uid: genUid(),
  bgbtucent: item.bgbtucent ?? '',
  bgbtucld: item.bgbtucld ?? '',
  bgbtuc001: item.bgbtuc001 ?? '',
  bgbtuc002: typeof item.bgbtuc002 === 'number' ? item.bgbtuc002 : Number(item.bgbtuc002) || 0,
  bgbtuc003: item.bgbtuc003 ?? '',
  bgbtuc005: typeof item.bgbtuc005 === 'number' ? item.bgbtuc005 : Number(item.bgbtuc005) || 0
})

/** 新增单身空行，自动带入单头字段（物料编号在单身，留空待填） */
const buildEmptyRow = () => ({
  _uid: genUid(),
  bgbtucent: header.bgbtucent,
  bgbtucld: header.bgbtucld,
  bgbtuc001: '',
  bgbtuc002: header.bgbtuc002,
  bgbtuc003: '',
  bgbtuc005: 0
})

/** 检查是否已有同条件数据（账套/账别/年度），有则自动回填到单身 */
const autoQueryExist = async () => {
  if (!isEdit.value) return
  const { bgbtucent, bgbtucld, bgbtuc002 } = header
  if (!bgbtucent || String(bgbtucent).trim() === '') return
  if (!bgbtucld || String(bgbtucld).trim() === '') return
  if (!bgbtuc002 || Number(bgbtuc002) === 0) return
  try {
    const res = await queryBgbtuc({
      bgbtucent: String(bgbtucent),
      bgbtucld: String(bgbtucld),
      bgbtuc002: String(bgbtuc002)
    })
    const raw = res.data?.master ?? res.data?.data ?? []
    if (raw.length > 0) {
      ElMessage.warning('该账套、账别、年度下已存在采购核价数据，自动回填到单身')
      detailList.value = raw.map(mapDetail)
      originalJson.value = JSON.stringify(detailList.value.map(r => ({ ...r, _uid: undefined })))
    }
  } catch (err) {
    // 后端无数据或不返回 data，静默忽略
  }
}

/** 导出 Excel 模板 */
const handleExport = () => {
  const rows = detailList.value.length > 0
    ? detailList.value.map(r => [r.bgbtucent, r.bgbtucld, r.bgbtuc001, r.bgbtuc002, r.bgbtuc003, r.bgbtuc005])
    : [[header.bgbtucent, header.bgbtucld, '', header.bgbtuc002, '', 0]]

  const ws = XLSX.utils.aoa_to_sheet([EXCEL_COLS, ...rows])
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '预算采购核价录入')
  XLSX.writeFile(wb, '预算采购核价录入模板.xlsx')
  ElMessage.success('导出成功')
}

/** 触发导入文件选择 */
const handleImport = () => {
  importFileRef.value?.click()
}

/** Excel 行 -> 表格数据 */
const mapExcelRow = row => ({
  _uid: genUid(),
  bgbtucent: row[0] != null && row[0] !== '' ? String(row[0]) : header.bgbtucent,
  bgbtucld: row[1] != null && row[1] !== '' ? String(row[1]) : header.bgbtucld,
  bgbtuc001: row[2] != null && row[2] !== '' ? String(row[2]) : '',
  bgbtuc002: Number(row[3]) || 0,
  bgbtuc003: row[4] != null ? String(row[4]) : '',
  bgbtuc005: Number(row[5]) || 0
})

/** 文件选择变更 -> 解析 Excel */
const handleFileChange = e => {
  const file = e.target.files[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = evt => {
    try {
      const wb = XLSX.read(evt.target.result, { type: 'binary' })
      const ws = wb.Sheets[wb.SheetNames[0]]
      const rows = XLSX.utils.sheet_to_json(ws, { header: 1, defval: '' })
      if (rows.length < 2) { ElMessage.warning('Excel 无数据行'); return }
      // 校验列头
      const headerRow = rows[0].map(c => String(c ?? '').trim())
      if (headerRow.join(',') !== EXCEL_COLS.join(',')) {
        ElMessage.error(`Excel 列头不匹配！\n期望: ${EXCEL_COLS.join(', ')}\n实际: ${headerRow.join(', ')}`)
        return
      }
      const data = rows.slice(1).filter(r => r.some(c => c !== undefined && c !== '' && c !== null))
      if (data.length === 0) { ElMessage.warning('Excel 无有效数据'); return }
      detailList.value = data.map(mapExcelRow)
      // 导入后自动把第一行的年度填入单头（物料编号在单身，不覆盖单头）
      if (detailList.value.length > 0) {
        const first = detailList.value[0]
        if (first.bgbtuc002) header.bgbtuc002 = first.bgbtuc002
      }
      originalJson.value = ''
      isEdit.value = true
      detailSelected.value = []
      detailTable.value?.clearSelection()
      ElMessage.success(`导入成功，共 ${detailList.value.length} 条`)
    } catch (err) {
      ElMessage.error('解析 Excel 失败: ' + err.message)
    }
  }
  reader.readAsBinaryString(file)
  e.target.value = ''
}

/** 构建查询条件：只取单头有值的字段 */
const buildQuery = () => {
  const q = {}
  const raw = {
    bgbtucent: header.bgbtucent,
    bgbtucld: header.bgbtucld,
    bgbtuc001: header.bgbtuc001,
    bgbtuc002: header.bgbtuc002
  }
  for (const [k, v] of Object.entries(raw)) {
    if (v !== '' && v !== null && v !== undefined && v !== 0) {
      q[k] = String(v)
    }
  }
  return q
}

/** 查询：进入查询条件输入模式 */
const handleQuery = () => {
  // 保存当前状态快照，供取消时还原
  preQuerySnapshot.value = {
    header: { ...header },
    detailList: detailList.value.map(r => ({ ...r, _uid: undefined }))
  }
  isQueryMode.value = true
  header.bgbtuc001 = ''
  ElMessage.info('请输入查询条件，填写完毕后点击"确认"提交')
}

/** 确认查询：将查询条件提交到后台 */
const handleConfirmQuery = async () => {
  const query = buildQuery()
  if (Object.keys(query).length === 0) {
    ElMessage.warning('请至少填入一个查询条件')
    return
  }
  const fullJson = { token: 'e6338a4acxw502kmf5dwr316ss8u0ymb', ...query }
  requestJson.value = JSON.stringify(fullJson, null, 2)
  console.log('查询发送 JSON:', requestJson.value)
  loading.value = true
  try {
    const res = await queryBgbtuc(query)
    const raw = res.data?.master ?? res.data?.data ?? []
    detailList.value = raw.map(mapDetail)
    // 用返回的第一条数据回填单头的年度（物料编号在单身）
    if (raw.length > 0) {
      const first = raw[0]
      if (first.bgbtuc002 != null && first.bgbtuc002 !== '' && first.bgbtuc002 !== 0) {
        header.bgbtuc002 = Number(first.bgbtuc002) || 0
      }
    }
    originalJson.value = JSON.stringify(detailList.value.map(r => ({ ...r, _uid: undefined })))
    isQueryMode.value = false
    detailSelected.value = []
    preQuerySnapshot.value = null
    await nextTick()
    detailTable.value?.clearSelection()
    ElMessage.success(`查询成功，共 ${detailList.value.length} 条`)
  } catch (err) {
    ElMessage.error('查询失败: ' + (err.response?.data?.message || err.message))
  } finally {
    loading.value = false
  }
}

/** 校验单头必填 */
const validateHeader = () => {
  if (!header.bgbtucent || String(header.bgbtucent).trim() === '') {
    ElMessage.warning('请先填写账套')
    return false
  }
  if (!header.bgbtucld || String(header.bgbtucld).trim() === '') {
    ElMessage.warning('请先填写账别')
    return false
  }
  if (!header.bgbtuc002 || Number(header.bgbtuc002) === 0) {
    ElMessage.warning('请先填写年度')
    return false
  }
  return true
}

/** 校验单行数据 */
const validateRow = (row, i) => {
  if (!row.bgbtuc001 || String(row.bgbtuc001).trim() === '') {
    ElMessage.warning(`第 ${i + 1} 行：物料编号不能为空`)
    return false
  }
  if (!row.bgbtuc003 || String(row.bgbtuc003).trim() === '') {
    ElMessage.warning(`第 ${i + 1} 行：供应商/采购员编码不能为空`)
    return false
  }
  if (row.bgbtuc005 === '' || row.bgbtuc005 == null || Number(row.bgbtuc005) === 0) {
    ElMessage.warning(`第 ${i + 1} 行：金额不能为0`)
    return false
  }
  return true
}

/** 新增：清空单身，进入编辑模式，自动添加一行空白行 */
const handleAdd = async () => {
  detailList.value = [buildEmptyRow()]
  originalJson.value = ''
  isEdit.value = true
  detailSelected.value = []
  await nextTick()
  detailTable.value?.clearSelection()
  ElMessage.info('已进入新增模式')
}

/** 进入编辑模式，若单身暂无数据则尝试自动回填已有数据 */
const handleEdit = async () => {
  isEdit.value = true
  if (detailList.value.length === 0) {
    await autoQueryExist()
  }
  ElMessage.info('已进入编辑模式')
}

/** 取消编辑/查询模式，恢复原始数据 */
const handleCancel = async () => {
  if (isQueryMode.value) {
    // 查询模式取消：还原进入查询前保存的快照
    const snap = preQuerySnapshot.value
    if (snap) {
      Object.assign(header, snap.header)
      detailList.value = snap.detailList.map(item => ({ ...item, _uid: genUid() }))
    }
    isQueryMode.value = false
    preQuerySnapshot.value = null
  } else {
    // 编辑模式取消
    if (originalJson.value) {
      detailList.value = JSON.parse(originalJson.value).map(item => ({ ...item, _uid: genUid() }))
    } else {
      detailList.value = []
    }
    isEdit.value = false
  }
  detailSelected.value = []
  await nextTick()
  detailTable.value?.clearSelection()
  ElMessage.info('已取消')
}

/** 新增行 */
const addRow = () => {
  // 新增前校验单头
  if (!validateHeader()) return
  // 新增前校验已有行
  for (let i = 0; i < detailList.value.length; i++) {
    if (!validateRow(detailList.value[i], i)) return
  }
  detailList.value.push(buildEmptyRow())
}

/** 删除选中行（前端，带确认提示） */
const removeRows = async () => {
  if (detailSelected.value.length === 0) {
    ElMessage.warning('请选择要删除的行')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确认删除选中的 ${detailSelected.value.length} 行数据？`,
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '继续确认',
        cancelButtonText: '否',
        distinguishCancelAndClose: true
      }
    )
  } catch {
    // 用户点了「否」或关闭，不执行删除
    return
  }
  const selected = [...detailSelected.value]
  detailList.value = detailList.value.filter(item => !selected.includes(item))
  detailSelected.value = []
  detailTable.value?.clearSelection()
  ElMessage.success('已删除选中行')
}

/** 表格选中变化 */
const handleDetailSelection = rows => {
  detailSelected.value = rows
}

/** 校验数据 */
const validate = () => {
  const headerChecks = [
    { name: '账套', value: header.bgbtucent },
    { name: '账别', value: header.bgbtucld  },
    { name: '年度', value: header.bgbtuc002 }
  ]
  const isEmpty = v => v === '' || v === null || v === undefined || v === 0
  const trimEmpty = v => typeof v === 'string' ? v.trim() === '' : isEmpty(v)
  const emptyHeader = headerChecks.filter(c => trimEmpty(c.value)).map(c => c.name)
  if (emptyHeader.length > 0) {
    ElMessage.warning(`单头区为空：${emptyHeader.join('、')}`)
    return false
  }
  if (detailList.value.length === 0) {
    ElMessage.warning('单身区没有数据可保存')
    return false
  }
  for (let i = 0; i < detailList.value.length; i++) {
    if (!validateRow(detailList.value[i], i)) return false
  }
  return true
}

/** 保存：调用 saveBgbtuc */
const handleSave = async () => {
  if (!validate()) return
  const payload = detailList.value.map(item => ({
    bgbtucent: String(header.bgbtucent),
    bgbtucld: String(header.bgbtucld),
    bgbtuc001: String(item.bgbtuc001 || ''),
    bgbtuc002: Number(header.bgbtuc002) || 0,
    bgbtuc003: String(item.bgbtuc003 || ''),
    bgbtuc005: Number(item.bgbtuc005) || 0
  }))
  const saveBody = { token: 'e6338a4acxw502kmf5dwr316ss8u0ymb', list: payload }
  requestJson.value = JSON.stringify(saveBody, null, 2)
  console.log('保存发送 JSON:', requestJson.value)
  try {
    const res = await saveBgbtuc(payload)
    const data = res.data ?? {}
    const insertCount = data.insertCount ?? 0
    const updateCount = data.updateCount ?? 0
    const deleteCount = data.deleteCount ?? 0
    ElMessage.success(`保存成功（新增 ${insertCount} 条，更新 ${updateCount} 条，删除 ${deleteCount} 条）`)
    originalJson.value = JSON.stringify(detailList.value.map(r => ({ ...r, _uid: undefined })))
    isEdit.value = false
    detailSelected.value = []
    await nextTick()
    detailTable.value?.clearSelection()
  } catch (err) {
    ElMessage.error('保存失败: ' + (err.response?.data?.message || err.message) + '\n发送数据: ' + JSON.stringify({ token: 'e6338a4acxw502kmf5dwr316ss8u0ymb', list: payload }))
  }
}

/** 删除：按单头条件批量删除 */
const handleDelete = async () => {
  if (!header.bgbtucent || !header.bgbtucld) {
    ElMessage.warning('请先填写账套和账别')
    return
  }
  if (!header.bgbtuc002 || Number(header.bgbtuc002) === 0) {
    ElMessage.warning('年度必填')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确认删除当前条件（账套:${header.bgbtucent} 账别:${header.bgbtucld} 年度:${header.bgbtuc002}）下的所有采购核价？\n点击「继续确认」将立即提交删除命令，点击「否」则取消。`,
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '继续确认',
        cancelButtonText: '否',
        distinguishCancelAndClose: true
      }
    )
  } catch {
    // 用户点了「否」或关闭，不提交删除命令
    return
  }
  loading.value = true
  try {
    const params = {
      bgbtucent: String(header.bgbtucent),
      bgbtucld: String(header.bgbtucld),
      bgbtuc002: Number(header.bgbtuc002) || 0
    }
    requestJson.value = JSON.stringify({ token: 'e6338a4acxw502kmf5dwr316ss8u0ymb', ...params }, null, 2)
    console.log('删除发送 JSON:', requestJson.value)
    const res = await deleteBgbtuc(params)
    const count = res.data?.deleteCount ?? 0
    ElMessage.success(`删除成功，共 ${count} 条`)
    // 刷新列表
    detailList.value = []
    originalJson.value = ''
    isEdit.value = false
    detailSelected.value = []
    await nextTick()
    detailTable.value?.clearSelection()
  } catch (err) {
    ElMessage.error('删除失败: ' + (err.response?.data?.message || err.message))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.budget-page {
  height: 100%;
  padding: 16px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: #f5f7fa;
}

/* 功能区 */
.action-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

/* 单头区 */
.header-card {
  flex-shrink: 0;
}

.header-form {
  padding-right: 16px;
}

/* JSON 调试框：与单头同一行右侧，占满剩余空间 */
.json-field {
  flex: 1;
  min-width: 360px;
  margin-bottom: 0;
}

.json-textarea {
  width: 100%;
}

.json-textarea :deep(.el-textarea__inner) {
  font-family: 'Consolas', 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.5;
  background: #fafafa;
  color: #303133;
  white-space: pre;
  overflow-x: auto;
  min-height: 64px !important;
  padding: 6px 10px;
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
  padding: 12px 16px;
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.detail-tools {
  display: flex;
  gap: 8px;
}

/* 必填项粉色背景 */
.required-field :deep(.el-input__wrapper),
.required-field :deep(.el-select__wrapper) {
  background-color: #ffc0cb;
  box-shadow: 0 0 0 1px #ff69b4 inset;
}
</style>