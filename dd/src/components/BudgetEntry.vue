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
          <el-input v-model="header.bgbsucent" :disabled="!isEdit && !isQueryMode" placeholder="企业代码" style="width: 140px;" />
        </el-form-item>
        <el-form-item label="账别" required class="required-field">
          <el-input v-model="header.bgbsucld" :disabled="!isEdit && !isQueryMode" placeholder="账别" style="width: 140px;" />
        </el-form-item>
        <el-form-item label="部门" required class="required-field">
          <div class="dept-wrapper">
            <el-select
              v-model="header.bgbsuc001"
              :disabled="!isEdit && !isQueryMode"
              filterable
              allow-create
              default-first-option
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
        <el-form-item label="年度" required class="required-field">
          <el-input-number
            v-model="header.bgbsuc002"
            :disabled="!isEdit && !isQueryMode"
            :min="2000"
            :precision="0"
            size="default"
            controls-position="right"
            style="width: 140px;"
            @change="autoQueryExist"
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
        :default-sort="{prop: 'bgbsucseq', order: 'ascending'}"
        @selection-change="handleDetailSelection"
        :row-key="row => row._uid"
      >
        <el-table-column type="selection" width="50" :reserve-selection="true" :selectable="() => canEditDetail || isQueryMode" />
        <el-table-column prop="bgbsucseq" label="序号" width="55" align="center" sortable :sort-method="(a, b) => (Number(a) || 0) - (Number(b) || 0)">
          <template #default="{ row, $index }">
            {{ row.bgbsucseq || $index + 1 }}
          </template>
        </el-table-column>

        <el-table-column prop="bgbsuc004" label="科目编号" min-width="160" align="center">
          <template #default="{ row }">
            <el-select
              v-model="row.bgbsuc004"
              :disabled="!(canEditDetail || isQueryMode)"
              filterable
              allow-create
              default-first-option
              :loading="acctLoading"
              placeholder="请选择或输入"
              size="small"
              style="width: 100%;"
              @change="(val) => handleAcctChange(row, val)"
              @visible-change="handleAcctVisible"
            >
              <el-option
                v-for="item in acctOptions"
                :key="item.value"
                :label="item.value"
                :value="item.value"
              >
                <span>{{ item.label }}</span>
              </el-option>
            </el-select>
          </template>
        </el-table-column>

        <el-table-column prop="_acctName" label="科目名称" min-width="150" align="center">
          <template #default="{ row }">
            <span>{{ acctNameMap[row.bgbsuc004] || '' }}</span>
          </template>
        </el-table-column>

        <el-table-column v-for="m in 12" :key="'m' + m" :label="m + '月'" min-width="115" align="right">
          <template #default="{ row }">
            <el-input-number
              v-model="row['m' + m]"
              :disabled="!(canEditDetail || isQueryMode)"
              :precision="6"
              :step="0.000001"
              size="small"
              controls-position="right"
              style="width: 100%;"
            />
          </template>
        </el-table-column>

        <el-table-column label="合计" min-width="130" align="right" prop="_total" :sortable="true" :sort-method="(a, b) => rowTotal(a) - rowTotal(b)">
          <template #default="{ row }">
            <span class="row-total">{{ formatMoney(rowTotal(row)) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, computed, nextTick, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { queryBgbsuc, saveBgbsuc, deleteBgbsuc, queryDept, querySubject } from '../api/budget.js'
import * as XLSX from 'xlsx'

/** 生成唯一行标识 */
let _uid = 0
const genUid = () => ++_uid

/** 单头区 */
const header = reactive({
  bgbsucent: '60',
  bgbsucld: 'NBYL',
  bgbsuc001: '',
  bgbsuc002: new Date().getFullYear()
})

/** 是否处于编辑模式 */
const isEdit = ref(false)
/** 是否处于查询输入模式 */
const isQueryMode = ref(false)
/** 单身是否可编辑：编辑模式 + 部门年度必填 */
const canEditDetail = computed(() => {
  return isEdit.value &&
    !!header.bgbsuc001 && String(header.bgbsuc001).trim() !== '' &&
    !!header.bgbsuc002 && Number(header.bgbsuc002) !== 0
})
/** 查询模式前的快照（供取消时还原） */
const preQuerySnapshot = ref(null)
/** 部门下拉选项 */
const deptOptions = ref([])
/** 部门加载状态 */
const deptLoading = ref(false)
/** 部门名称 */
const deptName = ref('')
/** 科目下拉选项 */
const acctOptions = ref([])
/** 科目加载状态 */
const acctLoading = ref(false)
/** 科目编码->名称映射 */
const acctNameMap = reactive({})
/** 加载状态 */
const loading = ref(false)
/** 单身表格数据 */
const detailList = ref([])
/** 单身按序号升序排序 */
function sortDetail() {
  const arr = [...detailList.value]
  arr.sort((a, b) => (Number(a.bgbsucseq) || 0) - (Number(b.bgbsucseq) || 0))
  detailList.value = arr
}
/** 表格已选中的行 */
const detailSelected = ref([])
const detailTable = ref(null)
const importFileRef = ref(null)

/** Excel 列头：账套/账别/部门/年度/序号/科目编号 + 12 月 */
const EXCEL_COLS = [
  '企业代码/账套', '账别', '部门编号', '年度', '序号', '科目编号',
  '1月', '2月', '3月', '4月', '5月', '6月',
  '7月', '8月', '9月', '10月', '11月', '12月'
]

/** 加载时的原始快照，用于判断是否有修改 */
const originalJson = ref('')


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

/** 部门值改变时更新部门名称，并检查是否已有数据 */
const handleDeptChange = (val) => {
  const found = deptOptions.value.find(o => o.value === val)
  deptName.value = found ? found.label.split(' - ').slice(1).join(' - ') : ''
  autoQueryExist()
}

/** 下拉框展开时加载全部部门 */
const handleDeptVisible = (visible) => {
  if (visible) {
    fetchAllDepts()
  }
}

/** 检查是否已有同条件数据，有则自动回填 */
const autoQueryExist = async () => {
  if (!isEdit.value) return
  const { bgbsucent, bgbsucld, bgbsuc001, bgbsuc002 } = header
  if (!bgbsucent || String(bgbsucent).trim() === '') return
  if (!bgbsucld || String(bgbsucld).trim() === '') return
  if (!bgbsuc001 || String(bgbsuc001).trim() === '') return
  if (!bgbsuc002 || Number(bgbsuc002) === 0) return
  try {
    const res = await queryBgbsuc({ bgbsucent: String(bgbsucent), bgbsucld: String(bgbsucld), bgbsuc001: String(bgbsuc001), bgbsuc002: String(bgbsuc002) })
    const raw = res.data?.master ?? res.data?.data ?? []
    if (raw.length > 0) {
      ElMessage.warning('该部门、年度已存在预算数据，自动回填到单身')
      detailList.value = raw.map(mapDetail)
      sortDetail()
      originalJson.value = JSON.stringify(detailList.value.map(r => ({ ...r, _uid: undefined })))
      fetchAllAccounts()
    }
  } catch (err) {
    // 后端无数据或不返回 data，静默忽略
  }
}

/** 加载全部科目列表 */
const fetchAllAccounts = async () => {
  acctLoading.value = true
  try {
    const res = await querySubject({ glacl002: '', glacl004: '', glac003: '2', glac007: '6' })
    const list = res.data?.data ?? []
    const get = (obj, ...keys) => {
      for (const k of keys) {
        const hit = Object.keys(obj).find(x => x.toLowerCase() === k.toLowerCase())
        if (hit && obj[hit] != null && obj[hit] !== '') return obj[hit]
      }
      return ''
    }
    acctOptions.value = list.map(item => {
      const code = get(item, 'glacl002')
      const name = get(item, 'glacl004')
      if (code) acctNameMap[code] = name
      return { value: code, label: `${code} - ${name}` }
    })
    console.log('科目选项:', acctOptions.value, '原始数据:', list)
  } catch (err) {
    acctOptions.value = []
  } finally {
    acctLoading.value = false
  }
}

/** 科目值改变时更新名称映射（如手动输入非列表值时通过名称映射显示） */
const handleAcctChange = (row, val) => {
  // 下拉列表已有名称映射，额外检查手动输入的情况
  if (val && !acctNameMap[val]) {
    acctNameMap[val] = ''
  }
}

/** 下拉框展开时加载全部科目 */
const handleAcctVisible = (visible) => {
  if (visible) {
    fetchAllAccounts()
  }
}

/** 12 个月份字段常量 */
const MONTH_KEYS = Array.from({ length: 12 }, (_, i) => 'm' + (i + 1))

/** 创建一个空白的月份字段集（全部为 0） */
function emptyMonths() {
  const o = {}
  for (const k of MONTH_KEYS) o[k] = 0
  return o
}

/** 新增单身空行：按科目聚合，12 个月份作为字段 */
const buildEmptyRow = () => ({
  _uid: genUid(),
  bgbsucseq: '',
  bgbsucent: header.bgbsucent,
  bgbsucld: header.bgbsucld,
  bgbsuc001: header.bgbsuc001,
  bgbsuc002: header.bgbsuc002,
  bgbsuc004: '',
  ...emptyMonths()
})

/** 把后端多条记录（每条一个月）按 (序号, 科目) 聚合成单条表格数据 */
const mapDetail = items => {
  const groups = new Map()
  for (const item of items) {
    const seq = item.bgbsucseq != null ? String(item.bgbsucseq) : ''
    const subj = item.bgbsuc004 != null ? String(item.bgbsuc004) : ''
    const key = seq + '||' + subj
    if (!groups.has(key)) {
      groups.set(key, {
        _uid: genUid(),
        bgbsucent: item.bgbsucent ?? '',
        bgbsucld: item.bgbsucld ?? '',
        bgbsuc001: item.bgbsuc001 ?? '',
        bgbsuc002: typeof item.bgbsuc002 === 'number' ? item.bgbsuc002 : Number(item.bgbsuc002) || 0,
        bgbsuc004: subj,
        bgbsucseq: seq,
        ...emptyMonths()
      })
    }
    const row = groups.get(key)
    const m = typeof item.bgbsuc003 === 'number' ? item.bgbsuc003 : Number(item.bgbsuc003) || 0
    if (m >= 1 && m <= 12) {
      const k = 'm' + m
      row[k] = typeof item.bgbsuc005 === 'number' ? item.bgbsuc005 : Number(item.bgbsuc005) || 0
    }
  }
  return Array.from(groups.values())
}

/** 导出 Excel 模板：科目为行，12 个月份为列 */
const handleExport = () => {
  const monthCells = MONTH_KEYS
  const rows = detailList.value.length > 0
    ? detailList.value.map(r => {
        const row = [r.bgbsucent, r.bgbsucld, r.bgbsuc001, r.bgbsuc002, r.bgbsucseq, r.bgbsuc004]
        for (const k of monthCells) row.push(r[k] ?? 0)
        return row
      })
    : [(() => {
        const row = [header.bgbsucent, header.bgbsucld, header.bgbsuc001, header.bgbsuc002, '', '']
        for (let i = 0; i < 12; i++) row.push(0)
        return row
      })()]

  const ws = XLSX.utils.aoa_to_sheet([EXCEL_COLS, ...rows])
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '预算录入')
  XLSX.writeFile(wb, '预算录入模板.xlsx')
  ElMessage.success('导出成功')
}

/** 触发导入文件选择 */
const handleImport = () => {
  importFileRef.value?.click()
}

/** Excel 行 -> 表格数据：列顺序为 账套/账别/部门/年度/序号/科目编号 + 12 月 */
const mapExcelRow = row => {
  const r = {
    _uid: genUid(),
    bgbsucent: row[0] != null ? String(row[0]) : header.bgbsucent,
    bgbsucld: row[1] != null ? String(row[1]) : header.bgbsucld,
    bgbsuc001: row[2] != null ? String(row[2]) : '',
    bgbsuc002: Number(row[3]) || 0,
    bgbsucseq: row[4] != null ? String(row[4]) : '',
    bgbsuc004: row[5] != null ? String(row[5]) : ''
  }
  for (let i = 0; i < 12; i++) {
    r['m' + (i + 1)] = Number(row[6 + i]) || 0
  }
  return r
}

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
      sortDetail()
      // 导入后自动把第一行的部门填入单头
      if (detailList.value.length > 0 && detailList.value[0].bgbsuc001) {
        header.bgbsuc001 = detailList.value[0].bgbsuc001
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
    bgbsucent: header.bgbsucent,
    bgbsucld: header.bgbsucld,
    bgbsuc001: header.bgbsuc001,
    bgbsuc002: header.bgbsuc002
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
  header.bgbsuc001 = ''
  deptName.value = ''
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
  console.log('查询发送 JSON:', JSON.stringify(fullJson, null, 2))
  loading.value = true
  try {
    const res = await queryBgbsuc(query)
    const raw = res.data?.master ?? res.data?.data ?? []
    detailList.value = raw.map(mapDetail)
    sortDetail()
    // 用返回的第一条数据回填单头的部门、年度
    if (raw.length > 0) {
      const first = raw[0]
      if (first.bgbsuc001 != null && first.bgbsuc001 !== '') {
        header.bgbsuc001 = String(first.bgbsuc001)
        fetchAllDepts().then(() => handleDeptChange(header.bgbsuc001))
      }
      if (first.bgbsuc002 != null && first.bgbsuc002 !== '' && first.bgbsuc002 !== 0) {
        header.bgbsuc002 = Number(first.bgbsuc002) || 0
      }
    }
    originalJson.value = JSON.stringify(detailList.value.map(r => ({ ...r, _uid: undefined })))
    // 查询成功后预加载科目名称映射
    fetchAllAccounts()
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
  if (!header.bgbsuc001 || String(header.bgbsuc001).trim() === '') {
    ElMessage.warning('请先填写部门')
    return false
  }
  if (!header.bgbsuc002 || Number(header.bgbsuc002) === 0) {
    ElMessage.warning('请先填写年度')
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

/** 进入编辑模式，自动带入账套/账别 */
const handleEdit = () => {
  isEdit.value = true
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
      if (snap.header.bgbsuc001) {
        fetchAllDepts().then(() => handleDeptChange(snap.header.bgbsuc001))
      } else {
        deptName.value = ''
        deptOptions.value = []
      }
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

/** 计算一行 12 个月金额合计 */
const rowTotal = (row) => {
  if (!row) return 0
  let sum = 0
  for (const k of MONTH_KEYS) sum += Number(row[k]) || 0
  return sum
}

/** 金额格式化：千分位 + 保留 2 位 */
const formatMoney = (v) => {
  const n = Number(v) || 0
  return n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 6 })
}

/** 校验单行数据：科目必填、12 月至少一月非零 */
const validateRow = (row, i) => {
  if (!row.bgbsuc004 || String(row.bgbsuc004).trim() === '') {
    ElMessage.warning(`第 ${i + 1} 行：科目编码不能为空`)
    return false
  }
  let hasAmount = false
  for (const k of MONTH_KEYS) {
    if (Number(row[k]) !== 0) { hasAmount = true; break }
  }
  if (!hasAmount) {
    ElMessage.warning(`第 ${i + 1} 行：12 个月金额不能全部为 0`)
    return false
  }
  return true
}

const addRow = () => {
  // 新增前校验单头
  if (!validateHeader()) return
  // 新增前校验已有行
  for (let i = 0; i < detailList.value.length; i++) {
    if (!validateRow(detailList.value[i], i)) return
  }
  detailList.value.push(buildEmptyRow())
  sortDetail()
}

/** 删除选中行（前端） */
const removeRows = () => {
  if (detailSelected.value.length === 0) {
    ElMessage.warning('请选择要删除的行')
    return
  }
  const selected = [...detailSelected.value]
  detailList.value = detailList.value.filter(item => !selected.includes(item))
  sortDetail()
  detailSelected.value = []
  detailTable.value?.clearSelection()
}

/** 表格选中变化 */
const handleDetailSelection = rows => {
  detailSelected.value = rows
}

/** 校验数据 */
const isEmpty = v => v === '' || v === null || v === undefined || v === 0
const trimEmpty = v => typeof v === 'string' ? v.trim() === '' : isEmpty(v)

const validate = () => {
  const headerChecks = [
    { name: '账套',     value: header.bgbsucent },
    { name: '账别',     value: header.bgbsucld  },
    { name: '部门编号', value: header.bgbsuc001 },
    { name: '年度',     value: header.bgbsuc002 }
  ]
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
  // 校验同一行内科目不能重复
  const subjKeys = new Map()
  for (let i = 0; i < detailList.value.length; i++) {
    const r = detailList.value[i]
    const subj = String(r.bgbsuc004 || '').trim()
    if (subjKeys.has(subj)) {
      ElMessage.warning(`第 ${i + 1} 行：科目 ${subj} 与第 ${subjKeys.get(subj) + 1} 行重复`)
      return false
    }
    subjKeys.set(subj, i)
  }
  return true
}

/** 保存：把每一行（12 个月）拆成 12 条记录提交 */
const handleSave = async () => {
  if (!validate()) return
  const payload = []
  detailList.value.forEach((item, idx) => {
    const seq = String(item.bgbsucseq || idx + 1)
    const subj = String(item.bgbsuc004 || '')
    for (let m = 1; m <= 12; m++) {
      payload.push({
        bgbsucent: String(header.bgbsucent),
        bgbsucld: String(header.bgbsucld),
        bgbsucseq: seq,
        bgbsuc001: String(header.bgbsuc001),
        bgbsuc002: Number(header.bgbsuc002) || 0,
        bgbsuc003: m,
        bgbsuc004: subj,
        bgbsuc005: Number(item['m' + m]) || 0
      })
    }
  })
  const saveBody = { token: 'e6338a4acxw502kmf5dwr316ss8u0ymb', list: payload }
  console.log('保存发送 JSON:', JSON.stringify(saveBody, null, 2))
  try {
    await saveBgbsuc(payload)
    originalJson.value = JSON.stringify(detailList.value.map(r => ({ ...r, _uid: undefined })))
    ElMessage.success('保存成功')
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
  if (!header.bgbsucent || !header.bgbsucld) {
    ElMessage.warning('请先填写账套和账别')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确认删除当前条件（账套:${header.bgbsucent} 账别:${header.bgbsucld} 部门:${header.bgbsuc001} 年度:${header.bgbsuc002}）下的所有预算明细？`,
      '删除确认',
      { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }
    )
  } catch {
    return
  }
  loading.value = true
  try {
    const params = {
      bgbsucent: String(header.bgbsucent),
      bgbsucld: String(header.bgbsucld),
      bgbsuc001: String(header.bgbsuc001),
      bgbsuc002: Number(header.bgbsuc002) || 0
    }
    console.log('删除发送 JSON:', JSON.stringify({ token: 'e6338a4acxw502kmf5dwr316ss8u0ymb', ...params }, null, 2))
    const res = await deleteBgbsuc(params)
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

/* 合计列样式 */
.row-total {
  display: inline-block;
  font-weight: 600;
  color: #409eff;
  font-family: 'Courier New', Consolas, monospace;
}

/* 必填项粉色背景 */
.required-field :deep(.el-input__wrapper),
.required-field :deep(.el-select__wrapper) {
  background-color: #ffc0cb;
  box-shadow: 0 0 0 1px #ff69b4 inset;
}

/* 部门下拉 + 名称标签 */
.dept-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
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
</style>
