<template>
  <div class="schedule-page">
    <el-card class="basic-card" shadow="never">
      <el-form :model="form" label-width="90px" class="basic-form">
        <el-row class="compact-row no-gutter-row">
          <el-col class="tight-col" style="display: none">
            <el-form-item label="企业代码" required class="required-field">
              <el-input v-model="form.enterpriseCode" style="width: 40px" />
            </el-form-item>
          </el-col>
          <el-col class="tight-col" style="display: none">
            <el-form-item label="营运据点">
              <el-input v-model="form.site" clearable style="width: 78px" />
            </el-form-item>
          </el-col>
          <el-col class="tight-col">
            <el-form-item label="成本中心">
              <div class="dept-select">
                <el-select-v2
                  v-model="form.costCenter"
                  multiple
                  collapse-tags
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
              <el-input
                v-model="form.customerOrderNo"
                clearable
                placeholder="请输入订单号"
                style="width: 150px"
                @keyup.enter="loadPendingOrders"
              />
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
            <el-form-item label="查询目标">
              <el-select v-model="form.queryTarget" style="width: 132px">
                <el-option label="左右两表" value="both" />
                <el-option label="仅左表" value="left" />
                <el-option label="仅右表" value="right" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col class="tight-col">
            <el-form-item label="生产日期">
              <el-date-picker v-model="form.prodDateStart" type="date" placeholder="起" value-format="YYYY-MM-DD" style="width: 112px" />
              <span class="range-sep">~</span>
              <el-date-picker v-model="form.prodDateEnd" type="date" placeholder="止" value-format="YYYY-MM-DD" style="width: 112px" />
            </el-form-item>
          </el-col>
          <el-col class="tight-col">
            <el-form-item label="开单日期">
              <el-date-picker v-model="form.docDateStart" type="date" placeholder="起" value-format="YYYY-MM-DD" style="width: 112px" />
              <span class="range-sep">~</span>
              <el-date-picker v-model="form.docDateEnd" type="date" placeholder="止" value-format="YYYY-MM-DD" style="width: 112px" />
            </el-form-item>
          </el-col>
          <el-col class="tight-col">
            <el-form-item label="预计开工">
              <el-date-picker v-model="form.planStartStart" type="date" placeholder="起" value-format="YYYY-MM-DD" style="width: 112px" />
              <span class="range-sep">~</span>
              <el-date-picker v-model="form.planStartEnd" type="date" placeholder="止" value-format="YYYY-MM-DD" style="width: 112px" />
            </el-form-item>
          </el-col>
          <el-col class="tight-col">
            <el-form-item label="预计完工">
              <el-date-picker v-model="form.planEndStart" type="date" placeholder="起" value-format="YYYY-MM-DD" style="width: 112px" />
              <span class="range-sep">~</span>
              <el-date-picker v-model="form.planEndEnd" type="date" placeholder="止" value-format="YYYY-MM-DD" style="width: 112px" />
            </el-form-item>
          </el-col>
          <el-col class="tight-col">
            <el-form-item label="状态码">
              <el-select v-model="form.workOrderStatus" multiple clearable collapse-tags collapse-tags-tooltip placeholder="全部" class="status-select">
                <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col class="tight-col form-actions-col">
            <el-form-item label=" ">
              <div class="form-actions">
                <el-button type="primary" size="small" icon="Search" v-if="canFunc(MODULE.SHOP_SCHEDULE, FUNC.QUERY)" @click="loadPendingOrders">查询</el-button>
                <el-button type="primary" size="small" :disabled="leftSelected.length === 0" v-if="canFunc(MODULE.SHOP_SCHEDULE, FUNC.EDIT)" @click="moveToRight">&gt;</el-button>
                <el-button type="warning" size="small" :disabled="rightSelected.length === 0" v-if="canFunc(MODULE.SHOP_SCHEDULE, FUNC.EDIT)" @click="moveToLeft">&lt;</el-button>
                <el-button type="success" size="small" :disabled="scheduledList.length === 0" v-if="canFunc(MODULE.SHOP_SCHEDULE, FUNC.SAVE)" @click="handleSave">保存</el-button>
                <el-button type="danger" size="small" :disabled="rightSelected.length === 0" v-if="canFunc(MODULE.SHOP_SCHEDULE, FUNC.SAVE)" @click="handleAudit">审核</el-button>
                <el-button type="info" size="small" :disabled="rightSelected.length === 0" v-if="canFunc(MODULE.SHOP_SCHEDULE, FUNC.SAVE)" @click="handleCancelAudit">取消审核</el-button>
                <el-button size="small" @click="toggleLeft">{{ leftVisible ? '隐藏明细' : '显示明细' }}</el-button>
                <el-button size="small" @click="toggleRight">{{ rightVisible ? '隐藏排程' : '显示排程' }}</el-button>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 保存时展示发送到后端的 JSON（临时缩效隐藏，需要时去掉下方 json-mini 类/样式即可恢复） -->
        <el-row v-if="saveJson">
          <el-col :span="24">
            <el-input
              v-model="saveJson"
              type="textarea"
              readonly
              :rows="1"
              class="json-mini"
              style="width: 10px"
              placeholder="保存后显示发送到后端的 JSON"
            />
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <div class="table-area" ref="tableArea">
      <!-- 折叠态：左侧未排工单明细表窄条，点击展开 -->
      <div v-if="!leftVisible" class="collapsed-strip" @click="showLeft" title="展开未排工单明细表">
        <el-icon><ArrowRight /></el-icon>
        <span class="strip-text">未排工单</span>
      </div>
      <div class="left-panel" :style="{ width: leftPanelWidth }" v-show="leftVisible" @contextmenu="onTableContextMenu('left', $event)">
        <el-table
          ref="leftTable"
          :data="pendingList"
          v-loading="loading"
          border
          height="100%"
          @selection-change="handleLeftSelection"
          @row-contextmenu="(row, col, ev) => onRowContextMenu(row, col, ev, 'left')"
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

      <div class="table-resizer" title="拖拽调整左表宽度" @mousedown="startResize" v-show="leftVisible && rightVisible" />

      <div class="right-panel" v-show="rightVisible" @contextmenu="onTableContextMenu('right', $event)">
        <el-table
          ref="rightTable"
          :data="scheduledList"
          border
          height="100%"
          :row-class-name="rightRowClass"
          @selection-change="handleRightSelection"
          @row-contextmenu="(row, col, ev) => onRowContextMenu(row, col, ev, 'right')"
        >
          <el-table-column type="selection" width="26" label="选取" />
          <el-table-column prop="sfahucstus" label="审核码" width="40" align="center" />
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
                :disabled="row.sfahucstus === 'Y'"
                placeholder="选择产线"
                :class="['line-select', { 'line-required': !row.productionLine }]"
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
          <el-table-column prop="issuedQty" label="已发料套数" width="70" align="right" />
          <el-table-column prop="stockInQty" label="入库量" width="70" />
          <el-table-column prop="balanceQty" label="余量" width="70" />
          <el-table-column prop="upph" label="UPPH值" width="70" class-name="plan-cell">
            <template #default="{ row }">
              <el-input
                v-model="row.upph"
                size="small"
                type="number"
                class="plan-input"
                :disabled="row.sfahucstus === 'Y'"
                @input="recalcDailyOutput(row)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="headcount" label="人数" width="64" class-name="plan-cell">
            <template #default="{ row }">
              <el-input
                v-model="row.headcount"
                size="small"
                type="number"
                class="plan-input"
                :disabled="row.sfahucstus === 'Y'"
                @input="recalcDailyOutput(row)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="workHours" label="工作时长" width="80" class-name="plan-cell">
            <template #default="{ row }">
              <el-input
                v-model="row.workHours"
                size="small"
                type="number"
                class="plan-input"
                :disabled="row.sfahucstus === 'Y'"
                @input="recalcDailyOutput(row)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="dailyOutput" label="日产量" width="80" class-name="plan-cell">
            <template #default="{ row }">
              <el-input
                :model-value="row.dailyOutput"
                size="small"
                type="number"
                class="plan-input"
                readonly
              />
            </template>
          </el-table-column>
          <el-table-column prop="scheduleQty" label="排程数量" width="50" align="right" />
          <el-table-column label="预计开工" min-width="160">
            <template #default="{ row }">
              <el-date-picker
                v-model="row.estimatedStartDate"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                :disabled="row.sfahucstus === 'Y'"
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
                :disabled="row.sfahucstus === 'Y'"
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
                :disabled="row.sfahucstus === 'Y'"
                :class="['daily-input', 'center-input', `daily-status-${row.dailyStatus || 'none'}`]"
                @update:model-value="setDailyValue(row, date, $event)"
              />
            </template>
          </el-table-column>
        </el-table>
      </div>
      <!-- 折叠态：右侧排程表窄条，点击展开 -->
      <div v-if="!rightVisible" class="collapsed-strip" @click="rightVisible = true" title="展开排程表">
        <el-icon><ArrowLeft /></el-icon>
        <span class="strip-text">排程表</span>
      </div>
    </div>

    <!-- 右键菜单：左右两表通用（隐藏/显示 左表 未排工单明细表、右表 排程表） -->
    <div
      v-if="ctxMenu.visible"
      class="ctx-menu"
      :style="{ left: ctxMenu.x + 'px', top: ctxMenu.y + 'px' }"
      @contextmenu.prevent
    >
      <div class="ctx-menu-item" v-if="ctxMenu.target !== 'left'" @click="onHideLeft">隐藏左表 未排工单明细表</div>
      <div class="ctx-menu-item" @click="onShowLeft">显示左表 未排工单明细表</div>
      <div class="ctx-menu-item" v-if="ctxMenu.target !== 'right'" @click="onHideRight">隐藏右表 排程表</div>
      <div class="ctx-menu-item" @click="onShowRight">显示右表 排程表</div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { fetchPendingOrders, saveSchedule, fetchSavedSchedule, deleteSchedule, queryLine, saveDailyPlan, queryItemBasicInfo, querySfahucExists, queryWorkOrder, WORK_ORDER_STATUS_MAP } from '../api/pendingOrders.js'
import { queryDept } from '../api/budget.js'
import { canFunc, MODULE, FUNC } from '../permission.js'

/** 生成默认单号：年月日 + 001 */
const genDocNo = () => {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}${m}${day}001`
}

/** 今天 + n 天，返回 YYYY-MM-DD（用于“生产日期”范围默认：今天 ~ 今天+7 天，含头尾） */
function todayPlus(n) {
  const d = new Date()
  d.setDate(d.getDate() + n)
  const p = (x) => String(x).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}

const form = reactive({
  orderNo: genDocNo(),
  costCenter: [],
  customerOrderNo: '',
  orderSeq: '',
  site: 'NBYL',
  enterpriseCode: '60',
  workOrderNo: '',
  itemNo: '',
  // 查询目标：both=左右两表都发；left=只发左表 queryOrder；right=只发右表 querySfahuc
  queryTarget: 'both',
  // 生产日期范围（仅右表 querySfahuc 后端新增条件 prod_start/prod_end；默认今天 ~ 今天+7 天，含头尾）
  prodDateStart: todayPlus(0),
  prodDateEnd: todayPlus(7),
  // 开单日期（起/止 可单独填，空则不发）
  docDateStart: '',
  docDateEnd: '',
  // 预计开工日期（起/止）
  planStartStart: '',
  planStartEnd: '',
  // 预计完工日期（起/止）
  planEndStart: '',
  planEndEnd: '',
  // 工单状态码多选（M/C/F/N/X/Y，空数组=全部），发给后端以逗号分隔（如 F,N）
  // 默认选中：已发放(F) + 未审核(N)
  workOrderStatus: ['F', 'N']
})

/** 状态码下拉选项（来自权威映射 WORK_ORDER_STATUS_MAP） */
const statusOptions = Object.keys(WORK_ORDER_STATUS_MAP).map(k => ({ value: k, label: WORK_ORDER_STATUS_MAP[k] }))

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
/** 保存时提交给后端的请求 JSON（展示在单头文本框，便于核对） */
const saveJson = ref('')
/** 加载时保存的精简签名，用于判断是否有修改（只比较落库字段） */
const originalSignature = ref('')
const leftSelected = ref([])
const rightSelected = ref([])
/** 点击「<」时登记的待删除行（保存时才真正调用删除接口） */
const deletedRows = ref([])
const leftTable = ref(null)
const rightTable = ref(null)
/** 左明细表宽度（初始 5px，拖拽后变为像素值） */
const leftPanelWidth = ref('5px')
const tableArea = ref(null)
const resizing = ref(false)

/** 左/右表显隐：默认只显示右表(排程表)，左表(未排工单明细表)折叠为窄条，需要时再展开 */
const leftVisible = ref(false)
const rightVisible = ref(true)

/** 左表初始宽度为 5px，直接展开会看不见，故展开时若尚未调整过宽度就给一个可用默认值 */
const ensureLeftWidth = () => {
  const cur = parseInt(leftPanelWidth.value, 10) || 0
  if (cur >= 200) return // 用户已拖过，保留其宽度
  const area = tableArea.value
  if (!area) return
  const max = Math.max(5, area.clientWidth - 420) // 为右表保留 420px
  leftPanelWidth.value = `${Math.max(5, Math.min(420, max))}px`
}
const showLeft = () => { ensureLeftWidth(); leftVisible.value = true }
const hideLeft = () => { leftVisible.value = false }
const toggleLeft = () => { leftVisible.value = !leftVisible.value; if (leftVisible.value) ensureLeftWidth() }
const toggleRight = () => { rightVisible.value = !rightVisible.value }

/** 右键菜单（左右两表通用，不依赖具体行数据） */
const ctxMenu = reactive({ visible: false, x: 0, y: 0, target: null })
const onRowContextMenu = (row, column, event, target) => {
  event.preventDefault()
  ctxMenu.target = target || null
  ctxMenu.x = event.clientX
  ctxMenu.y = event.clientY
  ctxMenu.visible = true
}
/** 面板级右键：空白区域（非数据行）也弹菜单；数据行由 @row-contextmenu 负责，此处跳过 */
const onTableContextMenu = (target, event) => {
  if (event.target?.closest?.('.el-table__row')) return
  event.preventDefault()
  ctxMenu.target = target || null
  ctxMenu.x = event.clientX
  ctxMenu.y = event.clientY
  ctxMenu.visible = true
}
const closeCtxMenu = () => { ctxMenu.visible = false }
const onEscClose = (e) => { if (e.key === 'Escape') closeCtxMenu() }
const onHideLeft = () => { hideLeft(); closeCtxMenu() }
const onShowLeft = () => { showLeft(); closeCtxMenu() }
const onHideRight = () => { rightVisible.value = false; closeCtxMenu() }
const onShowRight = () => { rightVisible.value = true; closeCtxMenu() }

/** 拖拽调整左表宽度 */
const startResize = (e) => {
  if (e.button !== 0) return
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
  // 无任何宽度限制：左表可一直拖到最右（右表随之收窄），也可一直拖到最左
  const w = Math.min(Math.max(e.clientX - rect.left, 0), rect.width)
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
  // 防御：进入页面时把左表宽度复位为默认固定像素值，
  // 避免残留的拖拽状态（如松手在窗口外导致 mouseup 漏捕获）把宽度压到极小
  leftPanelWidth.value = '5px'
  fetchAllDepts()
  fetchLines()
  // 右键菜单：点击别处 / 滚动 / Esc 均关闭
  document.addEventListener('click', closeCtxMenu)
  window.addEventListener('scroll', closeCtxMenu, true)
  document.addEventListener('keydown', onEscClose)
})

/** 重新计算两张 el-table 的布局：首屏/数据就绪后调用，避免表格宽度为 0 或错乱 */
const relayoutTables = () => {
  nextTick(() => {
    leftTable.value?.doLayout?.()
    rightTable.value?.doLayout?.()
  })
}

onUnmounted(() => {
  // 清理拖拽残留的全局监听，防止跨实例/卸载后误触发改写宽度
  resizing.value = false
  document.removeEventListener('mousemove', onResizeMove)
  document.removeEventListener('mouseup', stopResize)
  // 清理右键菜单监听
  document.removeEventListener('click', closeCtxMenu)
  window.removeEventListener('scroll', closeCtxMenu, true)
  document.removeEventListener('keydown', onEscClose)
})

/** 成本中心值改变时回填名称（多选时拼接多个名称） */
const handleCostCenterChange = (val) => {
  const vals = Array.isArray(val) ? val : (val ? [val] : [])
  const names = vals.map(v => {
    const found = deptOptions.value.find(o => o.value === v)
    return found ? found.label.split(' - ').slice(1).join(' - ') : v
  }).filter(Boolean)
  deptName.value = names.join(' / ')
}

/** 下拉框展开时确保列表已加载（复用首次请求的 Promise，避免重复建连） */
const handleCostCenterVisible = (visible) => {
  if (visible) {
    fetchAllDepts()
  }
}

/**
 * 生成「是否修改」判定用的精简签名：只取会落库/影响结果的字段，
 * 忽略 upph / stockInQty / balanceQty / workOrderStatus / dailyStatus 等展示字段，
 * 并把 dailyPlan 中数值为 0 的项（渲染期自动补的空值）剔除，
 * 避免「没有任何实际改动」却误判为已修改而重复请求后端。
 */
const toChangeSignature = (list) => JSON.stringify((list || []).map(r => ({
  sfahucseq: r.sfahucseq || '',
  workOrderNo: r.workOrderNo || '',
  itemNo: r.itemNo || '',
  orderNo: r.orderNo || '',
  sourceOrderNo: r.sourceOrderNo || '',
  seq: r.seq || '',
  estimatedStartDate: toDate(r.estimatedStartDate),
  estimatedEndDate: toDate(r.estimatedEndDate),
  costCenterCode: r.costCenterCode || '',
  costCenter: r.costCenter || '',
  productionLine: r.productionLine || '',
  dailyPlan: Object.fromEntries(
    Object.entries(r.dailyPlan || {}).filter(([, v]) => Number(v) > 0)
  )
})))

/** 从后端加载待排程工单和已保存的排程明细 */
let loadRunSeq = 0
const loadPendingOrders = async () => {
  const runId = ++loadRunSeq
  loading.value = true
  const t0 = performance.now()
  const t = () => performance.now() - t0
  const L = (msg) => console.log(`[R${runId}] ${msg} (+${t().toFixed(0)}ms)`)
  const dur = (msg, ta) => console.log(`[R${runId}] ${msg}: ${(performance.now() - ta).toFixed(0)}ms`)
  try {
    // 0. 组装日期范围 / 状态码 扩展查询条件（起/止 可单独填，只发填了的）
    const target = form.queryTarget || 'both'
    const dateCond = {}
    if (form.docDateStart) dateCond.sfaadocdt_start = form.docDateStart
    if (form.docDateEnd) dateCond.sfaadocdt_end = form.docDateEnd
    if (form.planStartStart) dateCond.sfaa019_start = form.planStartStart
    if (form.planStartEnd) dateCond.sfaa019_end = form.planStartEnd
    if (form.planEndStart) dateCond.sfaa020_start = form.planEndStart
    if (form.planEndEnd) dateCond.sfaa020_end = form.planEndEnd
    if (form.workOrderStatus.length) dateCond.sfaastus = form.workOrderStatus.join(',')
    // 生产日期范围：仅右表 querySfahuc 的后端新增条件（prod_start/prod_end），左表 queryOrder 不发
    const prodCond = {}
    if (form.prodDateStart) prodCond.prod_start = form.prodDateStart
    if (form.prodDateEnd) prodCond.prod_end = form.prodDateEnd
    // 按「查询目标」决定这些扩展条件发给哪一侧
    const leftCond = (target === 'left' || target === 'both') ? dateCond : {}
    const rightCond = (target === 'right' || target === 'both') ? { ...dateCond, ...prodCond } : {}

    // 1. 左表 queryOrder：仅「仅左表 / 左右两表」时调用并刷新左表
    if (target === 'left' || target === 'both') {
      const reqBody = {
        token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
        sfaaent: form.enterpriseCode || '60',
        sfaasite: form.site || 'NBYL',
        orderNo: form.customerOrderNo || '',
        sfaa068: (form.costCenter || []).join(','),
        sfaadocno: form.workOrderNo || '',
        sfaa010: form.itemNo || '',
        rowMax: 100,
        ...leftCond
      }
      const [res] = await Promise.all([
        (async () => { const ta = performance.now(); const r = await fetchPendingOrders(reqBody); dur('queryOrder', ta); return r })()
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
        issuedQty: item.sfaa049 ?? '',         // 已发料套数（随左表工单信息带入）
        stockInQty: item.sfaa050 ?? '',
        balanceQty: item['余量'] ?? '',
        upph: item.UPPH ?? item.upph ?? '',
        workOrderStatus: mapWorkOrderStatus(item.sfaastus)
      }))
      L(`左表就绪（queryOrder），共 ${pendingList.value.length} 行`)
    } else {
      L('查询目标=仅右表，跳过左表 queryOrder，左表不刷新')
    }

    // 2. 右表 querySfahuc：仅「仅右表 / 左右两表」时调用并刷新右表
    if (target === 'right' || target === 'both') {
      try {
        const ta = performance.now()
        const savedRes = await fetchSavedSchedule({
          enterpriseCode: form.enterpriseCode,
          site: form.site,
          costCenter: (form.costCenter || []).join(','),
          customerOrderNo: form.customerOrderNo,
          orderSeq: form.orderSeq,
          ...rightCond
        })
        dur('fetchSavedSchedule', ta)
        const rawSaved = savedRes.data?.master ?? savedRes.data?.data ?? []
        scheduledList.value = rawSaved.map(item => ({
          sfahucseq: item.sfahucseq || '',
          sfahucstus: item.sfahucstus || '',   // 审核码（查询回写）
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
          scheduleQty: item.sfahuc016 != null && item.sfahuc016 !== '' ? Number(item.sfahuc016) : (Number(item.sfahuc003) || 0), // 排程数量（查询回写，缺省取生产数量）
          // UPPH / 工单状态 / 入库量 / 余量 已由后端随 querySfahuc 一并返回
          // UPPH 值：优先取 querySfahuc 的 sfahuc014；左表(添加)拖入时沿用 queryOrder 的 upph（item.upph）
          upph: item.sfahuc014 ?? item.upph ?? '',
          headcount: item.sfahuc012 || '',
          workHours: item.sfahuc013 || '',
          dailyOutput: (Number(item.sfahuc012) || 0) * (Number(item.sfahuc013) || 0) * (Number(item.sfahuc014) || 0),
          workOrderStatus: mapWorkOrderStatus(item.sfaastus),
          issuedQty: item.sfaa049 ?? '',       // 已发料套数（querySfahuc 返回）
          stockInQty: item.sfaa050 ?? '',
          balanceQty: item['余量'] ?? '',
          // 日计划已随 querySfahuc 以「排产子表」返回，这里行转列成 {日期:数量}
          dailyPlan: subToDailyPlan(item['排产子表'])
        }))
        // 「左右两表」时才用右表已排程项过滤左表（仅右表时左表不刷新，不过滤）
        if (target === 'both') {
          const scheduledKeys = new Set(scheduledList.value.map(r => `${r.sourceOrderNo}||${r.workOrderNo}`))
          pendingList.value = pendingList.value.filter(r => !scheduledKeys.has(`${r.sourceOrderNo}||${r.workOrderNo}`))
        }
        // 每日计划已由后端随 querySfahuc 一并返回（排产子表 -> dailyPlan），无需逐行调用 loadDailyPlans
        // 计算每行日计划合计 vs 生产数量的对比状态
        scheduledList.value.forEach(refreshDailyStatus)
        relayoutTables()
        L(`右表就绪，共 ${scheduledList.value.length} 行`)
      } catch (e) {
        console.error('[querySfahuc] 查询异常', e)
      }
    } else {
      L('查询目标=仅左表，跳过右表 querySfahuc，右表不刷新')
    }

    loading.value = false
  } catch (err) {
    loading.value = false
    ElMessage.error('获取待排程工单失败: ' + (err.response?.data?.message || err.message))
  }
}

/** 把后端随 querySfahuc 返回的「排产子表」([{排产日期,排产数量}]) 行转列成 {日期:数量}
 *  原 loadDailyPlans 逐行请求后端的方式已废弃：日计划现在由后端一次性随主表返回。 */
const subToDailyPlan = (sub) => {
  const plan = {}
  if (Array.isArray(sub)) {
    sub.forEach(s => {
      const date = String(s['排产日期'] ?? s.sfajuc007 ?? '').substring(0, 10)
      const qty = s['排产数量'] ?? s.sfajuc003
      if (date && qty !== undefined && qty !== null && qty !== '') {
        plan[date] = Number(qty)
      }
    })
  }
  return plan
}

/** 并发受限的 map：每次最多 limit 个 fn 在途，避免一次性涌出大量请求拖慢后端/网关 */
const mapWithConcurrency = async (items, fn, limit = 8) => {
  const ret = []
  let i = 0
  const workers = Array.from({ length: Math.max(1, Math.min(limit, items.length)) }, async () => {
    while (i < items.length) {
      const idx = i++
      ret[idx] = await fn(items[idx], idx)
    }
  })
  await Promise.all(workers)
  return ret
}

/** 工单状态码 sfaastus → 中文；未匹配时原样返回 */
const mapWorkOrderStatus = (sfaastus) => {
  const code = (sfaastus ?? '').toString().trim()
  if (!code) return ''
  return WORK_ORDER_STATUS_MAP[code] ?? code
}



const handleLeftSelection = (rows) => {
  leftSelected.value = rows
}

const handleRightSelection = (rows) => {
  rightSelected.value = rows
}

/** 行级 class：已审核（sfahucstus=Y）的行标记为只读，前端置灰 + 不可编辑/删除/保存 */
const rightRowClass = ({ row }) => (row.sfahucstus === 'Y' ? 'locked-row' : '')

/** 重算某行 日产量 = 人数 × 工作时长 × UPPH值 */
const recalcDailyOutput = (row) => {
  const h = Number(row.headcount) || 0
  const w = Number(row.workHours) || 0
  const u = Number(row.upph) || 0
  row.dailyOutput = h * w * u
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
    // 排程数量默认 = 生产数量（工单数量）；车间计划排程无独立订单需求量，按工单数量取
    scheduleQty: Number(item.orderNo) || 0,
    headcount: '',
    workHours: '',
    dailyOutput: 0,
    dailyPlan: {}
  })))
  scheduledList.value.push(...newRows)
  pendingList.value = pendingList.value.filter(item => !selectedKeys.has(`${item.sourceOrderNo}||${item.workOrderNo}`))
  leftSelected.value = []
  leftTable.value?.clearSelection()
  scheduledList.value.forEach(refreshDailyStatus)
  ElMessage.success('已加入排程')
}

const moveToLeft = () => {
  if (rightSelected.value.length === 0) {
    ElMessage.warning('请选择排程工单')
    return
  }
  // 已审核（Y）的行只读，禁止移回/删除
  const selectedAll = [...rightSelected.value]
  const selected = selectedAll.filter(item => item.sfahucstus !== 'Y')
  const lockedCount = selectedAll.length - selected.length
  if (selected.length === 0) {
    ElMessage.warning('所选行均为已审核状态，不能移回待排程')
    return
  }
  if (lockedCount > 0) {
    ElMessage.warning(`已跳过 ${lockedCount} 条已审核行（只读，不可移回）`)
  }
  // 先确认：选「确认删除」才继续，点「取消」或直接关闭对话框都不进行任何操作
  ElMessageBox.confirm(
    '确定将所选排程工单移回待排程吗？保存时会一并删除这些已保存的记录。',
    '确认移回',
    { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning' }
  ).then(() => {
    const selectedKeys = new Set(selected.map(item => `${item.sourceOrderNo}||${item.workOrderNo}`))

    // 点击「<」时不立即删除：把所选行移回左侧（待排程）展示，
    // 并登记为「待删除」，待点「保存」时才调用删除接口。
    // 仅对曾经保存过的行（有 sfahucseq）登记删除，新加且未保存的行无需删除。
    for (const item of selected) {
      if (item.sfahucseq) {
        deletedRows.value.push({
          sfahucent: form.enterpriseCode || '',
          sfahucsite: form.site || '',
          sfahuc001: item.workOrderNo || '',
          sfahuc002: item.itemNo || '',
          sfahuc008: item.costCenterCode || ''
        })
      }
    }

    pendingList.value = [...selected, ...pendingList.value]
    scheduledList.value = scheduledList.value.filter(item => !selectedKeys.has(`${item.sourceOrderNo}||${item.workOrderNo}`))
    rightSelected.value = []
    rightTable.value?.clearSelection()
    ElMessage.success('已移回待排程，点保存时一并删除')
  }).catch(() => {
    // 取消或关闭对话框：不执行任何操作
  })
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

/** 取当前登录账号（登录时写入 localStorage 的 userAccount） */
const currentUserAccount = () => localStorage.getItem('userAccount') || ''

/** 构造 /saveSfhac 的 list 项；forceStatus 非空时强制覆盖每行的 sfahucstus（用于审核置 'Y'） */
const buildSavePayload = (rows, forceStatus) => rows.map((item, index) => ({
  sfahucent: '60',                              // 企业代码
  sfahucsite: form.site || '',                  // 营运据点
  sfahucdocno: form.orderNo || '',              // 单号
  sfahucseq: item.sfahucseq || (index + 1),     // 项次（已保存行沿用原序号）
  sfahuc001: item.workOrderNo || '',            // 工单单号
  sfahuc008: item.costCenterCode || '',         // 成本中心编码
  sfahuc002: item.itemNo || '',                 // 品号
  sfahuc003: item.orderNo || '',                // 数量（生产数量）
  sfahuc004: item.sourceOrderNo || '',          // 订单号
  sfahuc005: item.seq || '',                    // 订单序号
  sfahuc006: toDate(item.estimatedStartDate),   // 开工日期
  sfahuc007: toDate(item.estimatedEndDate),     // 完工日期
  sfahuc009: '',                                // 已入库数量（暂无）
  sfahuc010: item.productionLine || '',         // 产线（OOELUC003）
  sfahuc012: item.headcount != null && item.headcount !== '' ? String(item.headcount) : '0',
  sfahuc013: item.workHours != null && item.workHours !== '' ? String(item.workHours) : '0',
  sfahuc014: item.upph != null && item.upph !== '' ? String(item.upph) : '0',
  sfahuc015: item.dailyOutput != null && item.dailyOutput !== '' ? String(item.dailyOutput) : '0',
  sfahuc016: item.scheduleQty != null && item.scheduleQty !== '' ? String(item.scheduleQty) : '', // 排程数量
  sfahucstus: forceStatus || item.sfahucstus || '',
  userid: currentUserAccount()
}))

/** 保存右侧排程明细 */
const handleSave = async () => {
  // 既无排程行也无待删除记录 → 无需保存
  if (scheduledList.value.length === 0 && deletedRows.value.length === 0) {
    ElMessage.warning('没有需要保存的排程数据')
    return
  }
  // 有排程行但全部已审核只读 → 无需保存
  if (scheduledList.value.length > 0 && scheduledList.value.every(r => r.sfahucstus === 'Y') && deletedRows.value.length === 0) {
    ElMessage.warning('所有排程行均已审核为只读，无需保存')
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
  // 有待删除记录未提交到后端 → 先让操作员确认（取消/关闭则中止本次保存）
  if (deletedRows.value.length > 0) {
    try {
      await ElMessageBox.confirm(
        `本次保存将同时删除 ${deletedRows.value.length} 条已保存的排程记录，是否继续？`,
        '确认删除',
        { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning' }
      )
    } catch (e) {
      ElMessage.info('已取消保存')
      return
    }
  }
  // 保存前按站点刷新成本中心名称，保证落库的是站点对应的名称
  await ensureDeptMap()
  for (const row of scheduledList.value) syncCostCenterName(row)
  // 与加载时的原始签名对比（只比较落库字段，忽略运行期填充的 0 与展示字段）；
  // 无任何改动且无待删除行时，不发送任何请求到后端
  const currentSig = toChangeSignature(scheduledList.value)
  if (deletedRows.value.length === 0 && currentSig === originalSignature.value) {
    ElMessage.info('排程数据无修改，无需保存')
    return
  }
  try {
    // 0) 处理点「<」时登记的待删除行：此时才真正调用删除接口
    if (deletedRows.value.length > 0) {
      const deleteResults = await Promise.allSettled(
        deletedRows.value.map(payload => deleteSchedule(payload))
      )
      const failed = deleteResults.filter(r =>
        r.status !== 'fulfilled' ||
        !(r.value?.data?.success === true && r.value?.data?.deleted === true)
      )
      if (failed.length > 0) {
        ElMessage.error(`有 ${failed.length} 条排程删除失败，已取消保存`)
        return
      }
      deletedRows.value = []
    }

    // 已审核（Y）的行只读，保存时排除，不发送给后端
    const payload = buildSavePayload(scheduledList.value.filter(r => r.sfahucstus !== 'Y'))
    // 实际提交给后端的完整请求体（/saveSfahuc）
    const requestBody = {
      token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
      userid: currentUserAccount(),
      list: payload
    }
    saveJson.value = JSON.stringify(requestBody, null, 2)
    await saveSchedule(payload, currentUserAccount())

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

    originalSignature.value = currentSig
    ElMessage.success('保存成功')
  } catch (err) {
    ElMessage.error('保存失败: ' + (err.response?.data?.message || err.message))
  }
}

/** 审核：将所选右表行的 sfahucstus 置为 'Y' 并调用 /saveSfhac 保存（未选行则报警不发送） */
const handleAudit = async () => {
  if (rightSelected.value.length === 0) {
    ElMessage.warning('请先勾选要审核的排程行')
    return
  }
  try {
    // 本地先标记审核码，列表立即刷新
    for (const row of rightSelected.value) row.sfahucstus = 'Y'
    const payload = buildSavePayload(rightSelected.value, 'Y')
    const requestBody = {
      token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
      userid: currentUserAccount(),
      list: payload
    }
    saveJson.value = JSON.stringify(requestBody, null, 2)
    await saveSchedule(payload, currentUserAccount())
    ElMessage.success(`已审核 ${payload.length} 行`)
  } catch (err) {
    ElMessage.error('审核失败: ' + (err.response?.data?.message || err.message))
  }
}

/** 取消审核：将所选右表行的 sfahucstus 置为 'N' 并调用 /saveSfahuc 保存 */
const handleCancelAudit = async () => {
  if (rightSelected.value.length === 0) {
    ElMessage.warning('请先勾选要取消审核的排程行')
    return
  }
  try {
    // 本地先标记审核码为 N，列表立即刷新（取消只读）
    for (const row of rightSelected.value) row.sfahucstus = 'N'
    const payload = buildSavePayload(rightSelected.value, 'N')
    const requestBody = {
      token: 'e6338a4acxw502kmf5dwr316ss8u0ymb',
      userid: currentUserAccount(),
      list: payload
    }
    saveJson.value = JSON.stringify(requestBody, null, 2)
    await saveSchedule(payload, currentUserAccount())
    ElMessage.success(`已取消审核 ${payload.length} 行`)
  } catch (err) {
    ElMessage.error('取消审核失败: ' + (err.response?.data?.message || err.message))
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
  border-color: #ebeef5;
}
.schedule-page :deep(.el-table th.el-table__cell),
.schedule-page :deep(.el-table td.el-table__cell) {
  border-color: #ebeef5;
}

.schedule-page :deep(.el-table th.el-table__cell) {
  text-align: center;
  background: #1e90ff !important;
  color: #fff;
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

/* 表头 hover 也保持蓝色 */
.schedule-page :deep(.el-table th.el-table__cell:hover) {
  background: #1e90ff !important;
}

.basic-card {
  margin-bottom: 8px;
  flex-shrink: 0;
  height: 130px;
}



/* 去掉 header 后，压缩卡片内边距让表单整体上移 */
.basic-card :deep(.el-card__body) {
  padding: 12px 16px;
}

.basic-form {
  margin-right:-10px ;
}

/* 单头各字段行间距 3px */
.basic-form .el-form-item {
  margin-bottom: 3px;
}

/* 状态码多选：宽度随已选文字自动调整，不固定 */
.status-select {
  width: auto !important;
  min-width: 96px;
}

/* 表头所有输入控件统一高度 24px */
.basic-form .el-input__wrapper,
.basic-form .el-select__wrapper,
.basic-form .el-select-v2__wrapper {
  min-height: 24px;
  height: 24px;
}
.basic-form .el-date-editor {
  --el-date-editor-width: auto;
  height: 24px;
}
.basic-form .el-button {
  height: 24px;
}

/* 日期范围起止之间的分隔符 */
.range-sep {
  margin: 0 4px;
  color: #909399;
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

/* 折叠态侧栏窄条：26px 竖条，竖排文字 + 箭头，点击展开 */
.table-area .collapsed-strip {
  flex: 0 0 26px;
  width: 26px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  background: #f5f7fa;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  color: #909399;
  transition: background-color 0.2s, color 0.2s;
}
.table-area .collapsed-strip:hover {
  background: #ecf5ff;
  color: #409eff;
}
.table-area .collapsed-strip .strip-text {
  writing-mode: vertical-rl;
  font-size: 12px;
  letter-spacing: 2px;
}

.left-panel {
  flex: 0 0 auto;
  min-width: 0;
  background: #fff;
  border: 1px solid #ebeef5;
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

/* 已审核（只读）行置灰标识 */
.schedule-page :deep(.el-table .locked-row) {
  background-color: #f5f7fa;
}
.schedule-page :deep(.el-table .locked-row:hover > td.el-table__cell) {
  background-color: #ebeef5;
}

.right-panel {
  flex: 1;
  min-width: 0;
  background: #fff;
  border: 1px solid #ebeef5;
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
/* 1) 整个外层 cell 与 .cell 容器内边距归零，避免上下左右留白；
      .cell 用纵向 flex，让输入框 flex:1 撑满整格高度 */
.schedule-page :deep(.el-table td.el-table__cell .cell:has(.daily-input.center-input)) {
  padding: 0 !important;
  display: flex;
  flex-direction: column;
}
.schedule-page :deep(.el-table td.el-table__cell:has(.daily-input.center-input)) {
  padding: 0 !important;
}

/* 2) el-input-number 自身撑满 cell（宽度 + 高度） */
.daily-input {
  width: 100% !important;
  flex: 1;
  display: block;
  background-color: #a8d8ff !important;
}
/* 3) input-number 内嵌的 el-input / wrapper 全部去掉内边距、阴影、圆角，并纵向撑满 */
.daily-input :deep(.el-input-number),
.daily-input :deep(.el-input),
.daily-input :deep(.el-input__wrapper) {
  width: 100%;
  height: 100%;
  padding: 0;
  border-radius: 0;
  box-shadow: none !important;
  background-color: #a8d8ff !important;
}
/* 4) 日期列输入框背景设为浅蓝色（铺满根 + 内部各层） */
.daily-input :deep(.el-input__inner) {
  height: 100%;
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

/* 人数 / 工作时长 / UPPH值 / 日产量：铺满单元格、四周无空隙（处理方式同日期列） */
.schedule-page :deep(.el-table td.el-table__cell.plan-cell),
.schedule-page :deep(.el-table td.el-table__cell.plan-cell .cell) {
  padding: 0 !important;
}
.plan-input {
  width: 100% !important;
  display: block;
}
.plan-input :deep(.el-input__wrapper) {
  padding: 0;
  border-radius: 0;
  box-shadow: none !important;
}
.plan-input :deep(.el-input__inner) {
  text-align: center;
}

/* 保存 JSON 文本框：临时缩效隐藏（需要时删除 json-mini 类及相关样式即可恢复） */
.json-mini :deep(.el-textarea__inner) {
  height: 5px !important;
  min-height: 5px !important;
  resize: none;
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
/* 产线为必填项：未选值时背景浅红标识（EP 2.5+ el-select 背景在 .el-select__wrapper） */
.schedule-page :deep(.el-table td.el-table__cell .line-select.line-required .el-select__wrapper) {
  background-color: #fde2e2;
}
.schedule-page :deep(.el-table td.el-table__cell .line-select.line-required .el-select__wrapper.is-focused),
.schedule-page :deep(.el-table td.el-table__cell .line-select.line-required .el-select__wrapper:hover) {
  background-color: #f9c6c6;
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

/* 右键菜单（左右两表通用） */
.ctx-menu {
  position: fixed;
  z-index: 3000;
  min-width: 170px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.12);
  padding: 5px 0;
  font-size: 12px;
}
.ctx-menu-item {
  padding: 7px 16px;
  cursor: pointer;
  white-space: nowrap;
  color: #303133;
}
.ctx-menu-item:hover {
  background: #ecf5ff;
  color: #409eff;
}
</style>
