<template>
  <div class="schedule-page">
    <el-card class="basic-card" shadow="never">
      <el-form :model="form" label-width="auto" class="basic-form">
        <el-row :gutter="0" class="tight-row" style="justify-content: flex-start;">
          <el-col :span="3" style="display: none;">
            <el-form-item label="企业代码" required class="required-field">
              <el-input v-model="form.enterpriseCode" style="width: 50px;" />
            </el-form-item>
          </el-col>
          <el-col :span="3" style="display: none;">
            <el-form-item label="营运据点">
              <el-input v-model="form.site" clearable style="width: 70px;" />
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="订单号">
              <el-input v-model="form.customerOrderNo" clearable @keyup.enter="loadBomTree" style="width: 160px;" />
            </el-form-item>
          </el-col>
          <el-col :span="3">
            <el-form-item label="订单序号">
              <el-input v-model="form.orderSeq" clearable style="width: 50px;" />
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="品号">
              <el-input v-model="form.itemNo" clearable @keyup.enter="loadBomTree" style="width: 100px;" />
            </el-form-item>
          </el-col>
          <el-col :span="3">
            <el-form-item label="品名">
              <el-input v-model="form.productName" clearable style="width: 120px;" />
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="业务员">
              <el-input v-model="form.salesman" clearable style="width: 130px;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="0" class="tight-row" style="margin-top: 5px;">
          <el-col :span="5">
            <el-form-item label="订单日期">
              <el-date-picker v-model="form.orderDateStart" type="date" placeholder="起" value-format="YYYY-MM-DD" style="width: 130px;" />
              <span class="range-sep">~</span>
              <el-date-picker v-model="form.orderDateEnd" type="date" placeholder="止" value-format="YYYY-MM-DD" style="width: 130px;" />
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-form-item label="交货日期">
              <el-date-picker v-model="form.deliveryDateStart" type="date" placeholder="起" value-format="YYYY-MM-DD" style="width: 130px;" />
              <span class="range-sep">~</span>
              <el-date-picker v-model="form.deliveryDateEnd" type="date" placeholder="止" value-format="YYYY-MM-DD" style="width: 130px;" />
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-form-item label="补货策略">
              <el-select v-model="form.replenishStrategy" multiple collapse-tags clearable placeholder="全部" style="width: 160px;">
                <el-option label="采购件" value="1" />
                <el-option label="自制件" value="2" />
                <el-option label="委外件" value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="不展示虚拟件">
              <el-checkbox v-model="form.hidePhantom" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <div class="form-btn-row">
              <span class="op-btns">
                <el-button type="primary" size="small" icon="Search" v-if="canFunc(MODULE.MAIN_SCHEDULE, FUNC.QUERY)" @click="loadBomTree" style="height: 20px; padding: 0 14px; font-size: 13px;">查询</el-button>
                <el-button color="#a0cfff" size="small" style="height: 20px; padding: 0 14px; font-size: 13px; color: #fff;" v-if="canFunc(MODULE.MAIN_SCHEDULE, FUNC.EDIT)" @click="onMoveToRight">&gt;</el-button>
                <el-button color="#f0d2a0" size="small" style="height: 20px; padding: 0 14px; font-size: 13px; color: #fff;" v-if="canFunc(MODULE.MAIN_SCHEDULE, FUNC.EDIT)" @click="onMoveToLeft">&lt;</el-button>
                <el-button color="#95d3af" size="small" style="height: 20px; padding: 0 14px; font-size: 13px; color: #fff;" v-if="canFunc(MODULE.MAIN_SCHEDULE, FUNC.SAVE)" @click="onSaveSchedule">保存</el-button>
                <el-button size="small" style="height: 20px; padding: 0 10px; font-size: 13px;" @click="toggleUnfinished">{{ unfinishedCollapsed ? '显示订单' : '隐藏订单' }}</el-button>
                <el-button size="small" style="height: 20px; padding: 0 10px; font-size: 13px;" @click="toggleMiddle">{{ middleCollapsed ? '显示BOM' : '隐藏BOM' }}</el-button>
                <el-button size="small" style="height: 20px; padding: 0 10px; font-size: 13px;" @click="toggleRight">{{ rightCollapsed ? '显示排程' : '隐藏排程' }}</el-button>
              </span>
              <!-- 工单绑定 JSON：展示 /querySchedulableWorkOrders 的请求与返回 -->
              <el-input
                v-model="woBindJson"
                type="textarea"
                readonly
                :rows="2"
                class="wo-bind-json"
                style="flex: 0 0 30px; width: 30px;"
                placeholder="工单绑定 JSON（/querySchedulableWorkOrders）"
              />
            </div>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card class="tree-card" shadow="never">
      <div class="tables-wrap" ref="tablesWrapRef">
        <!-- 折叠态：最左侧窄条，点击展开 -->
        <div v-if="unfinishedCollapsed" class="collapsed-strip" @click="toggleUnfinished" title="展开未交订单">
          <el-icon><ArrowRight /></el-icon>
          <span class="strip-text">未交订单</span>
        </div>
        <!-- 最左侧：未出货订单明细表（/queryUnfinishedOrders） -->
        <div v-show="!unfinishedCollapsed" class="unfinished-panel" :style="{ width: unfinishedWidth + 'px' }" @contextmenu="onTableContextMenu">
          <el-table
            ref="unfinishedTableRef"
            :data="unfinishedList"
            border
            :height="unfinishedTableHeight"
            highlight-current-row
            :row-class-name="unfinishedRowClassName"
            v-loading="unfinishedLoading"
            @row-contextmenu="onRowContextMenu"
            @row-click="onUnfinishedRowClick"
          >
            <el-table-column prop="xmdddocno" label="订单号" width="120" sortable show-overflow-tooltip />
            <el-table-column prop="xmddseq" label="订单序号" width="50" align="center" sortable />
            <el-table-column prop="xmdd001" label="品号" width="100" sortable show-overflow-tooltip />
            <el-table-column prop="gzcbl004" label="补货策略" width="40" align="center" sortable :formatter="formatMakeType" />
            <el-table-column prop="imaal003" label="品名" width="160" sortable show-overflow-tooltip />
            <el-table-column prop="xmdd006" label="订单量" width="70" align="right" sortable :formatter="formatQty" />
            <el-table-column prop="xmdd014" label="已出货量" width="70" align="right" sortable :formatter="formatQty" />
            <el-table-column prop="undqty" label="未交量" width="70" align="right" sortable :formatter="formatQty" />
            <el-table-column prop="xmdd011" label="订单交期" width="80" sortable :formatter="formatDate" />
            <el-table-column prop="customerName" label="客户" width="80" sortable show-overflow-tooltip />
            <el-table-column prop="ooag011" label="业务员" width="50" sortable show-overflow-tooltip />
          </el-table>
        </div>
        <div v-if="!unfinishedCollapsed && !middleCollapsed" class="splitter" @mousedown="startDragUnfinished"></div>
        <!-- 折叠态：中间 BOM 工单表窄条，点击展开 -->
        <div v-if="middleCollapsed" class="collapsed-strip" @click="toggleMiddle" title="展开BOM工单表">
          <el-icon><ArrowRight /></el-icon>
          <span class="strip-text">BOM工单</span>
        </div>
        <div v-show="!middleCollapsed" class="left-panel" :style="leftPanelStyle" @contextmenu="onTableContextMenu">
          <el-table
            ref="tableRef"
            :key="(expandAll ? 'expand' : 'collapse') + (treeMode ? '-tree' : '-flat')"
            :data="displayData"
            row-key="id"
            border
            :height="tableHeight"
            :default-expand-all="expandAll"
            highlight-current-row
            :tree-props="treeMode ? { children: 'children' } : {}"
            :cell-class-name="leftCellClassName"
            :row-class-name="bomRowClassName"
            @current-change="onBomCurrentChange"
            v-loading="treeLoading"
            @row-contextmenu="onRowContextMenu"
          >
            <!-- BOM 行作为树形目录列(带展开箭头与缩进),约 35 字符宽 -->
            <el-table-column label="BOM 行" width="280" fixed="left">
              <template #header>
                <el-button size="small" @click="toggleExpand">
                  <el-icon :size="14" style="vertical-align: middle; margin-right: 4px;">
                    <Expand v-if="!expandAll" />
                    <Fold v-else />
                  </el-icon>
                  {{ expandAll ? '全部收起' : '全部展开' }}
                </el-button>
                <el-button size="small" @click="toggleTreeMode">
                  {{ treeMode ? '平铺显示' : '按树型目录展开' }}
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
            <el-table-column prop="level" label="层级" width="35" align="center" fixed="left" />
            <!-- 自定义勾选列（第 3 列）：每行独立勾选，不随树形级联 -->
            <el-table-column width="20" align="center" fixed="left">
              <template #header>
                <el-checkbox
                  :model-value="allLeftChecked"
                  :indeterminate="someLeftChecked && !allLeftChecked"
                  @change="onToggleAllLeft"
                />
              </template>
              <template #default="{ row }">
                <el-checkbox
                  :model-value="leftCheckedIds.has(row.id)"
                  @change="(val) => onToggleLeftRow(row, val)"
                />
              </template>
            </el-table-column>
            <!-- 行号：按展开顺序从 1 依次递增 -->
            <el-table-column label="行号" width="30" align="center" fixed="left">
              <template #default="{ row }">{{ rowNoOf(row) }}</template>
            </el-table-column>
            <el-table-column prop="ooefl003" label="成本中心名称" width="100" sortable show-overflow-tooltip fixed="left" />
            <el-table-column prop="imaal003" label="品名" width="160" show-overflow-tooltip />
            <el-table-column prop="imaal004" label="规格" width="120" show-overflow-tooltip />
            <!-- 订单号 / 订单序号 为普通列,不参与树形;订单号约 20 字符宽,订单序号约 5 字符宽 -->
            <el-table-column prop="订单号" label="订单号" width="120" />
            <el-table-column prop="订单序号" label="序号" width="40" align="center" />
        <el-table-column prop="BOM用量" label="BOM量" width="60" align="right" :formatter="formatQty" />
        <el-table-column prop="实际用量" label="实际量" width="60" align="right" :formatter="formatQty" />
        <el-table-column prop="订单需求用量" label="订单需求量" width="70" align="right" :formatter="formatQty" />
        <!-- 以下为 /queryOrderBom 直接返回的工单字段（来源：sfaa_t），后端在每个 BOM 节点上带出 -->
        <el-table-column prop="sfaadocno" label="工单号" width="140" />
        <el-table-column prop="imaf013" label="补货策略" width="80" align="center" :formatter="formatMakeType" />
        <el-table-column prop="sfaa068" label="成本中心" width="60" />
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
        <el-table-column prop="sfaa049" label="已发料套数" width="100" align="right" />
        <el-table-column prop="sfaa050" label="入库数量" width="100" align="right" />
        <el-table-column prop="sfaastus" label="状态码" width="80" align="center" :formatter="formatStatus" />
        <el-table-column prop="upph" label="UPPH" width="80" align="right" />
        <!-- 动态日期列：每行根据自身预计开工~预计完工展开为多列，文本框可编辑（蓝色背景） -->
        <el-table-column
          v-for="date in bomDateColumns"
          :key="date"
          :label="formatDateColLabel(date)"
          min-width="78"
          align="center"
        >
          <template #header>
            <div class="date-col-header">
              <div class="date-col-date">{{ formatDateColLabel(date) }}</div>
              <div class="date-col-week">{{ weekdayOf(date) }}</div>
            </div>
          </template>
          <template #default="{ row }">
            <el-input
              v-if="isBomInDateRange(row, date)"
              :model-value="getBomDateValue(row, date)"
              size="small"
              class="daily-input center-input bom-date-cell"
              :disabled="isClosed(row)"
              @update:model-value="setBomDateValue(row, date, $event)"
            />
          </template>
        </el-table-column>
          </el-table>
        </div>
        <div v-if="!middleCollapsed && !rightCollapsed" class="splitter" @mousedown="startDrag"></div>
        <div v-show="!rightCollapsed" class="right-panel" @contextmenu="onTableContextMenu">
          <el-table
            ref="rightTableRef"
            :data="scheduledList"
            border
            :height="rightTableHeight"
            highlight-current-row
            :row-class-name="rightRowClassName"
            :default-sort="{ prop: 'sfahuc002', order: 'ascending' }"
            v-loading="rightLoading"
            @selection-change="onRightSelectionChange"
            @current-change="onRightCurrentChange"
            @row-contextmenu="onRowContextMenu"
          >
            <el-table-column type="selection" width="26" align="center" />
            <el-table-column prop="sfahucseq" label="项次" width="35" align="center" sortable :sort-method="numSort" />
            <el-table-column prop="sfahuc004" label="订单号" width="125" sortable />
            <el-table-column prop="sfahuc005" label="订单序号" width="40" align="center" sortable :sort-method="numSort" />
            <el-table-column prop="sfahuc002" label="品号" width="95" sortable show-overflow-tooltip />
            <el-table-column prop="itemName" label="品名" width="160" sortable show-overflow-tooltip>
              <template #default="{ row }">{{ row.itemName || '' }}</template>
            </el-table-column>
            <el-table-column prop="sfahuc011" label="订单需求量" width="70" align="right" sortable :sort-method="numSort" />
            <el-table-column prop="orderSchedulableQty" label="订单可排数量" width="70" align="right" sortable :sort-method="numSort">
              <template #default="{ row }">{{ fmtNum(row.orderSchedulableQty) }}</template>
            </el-table-column>
            <el-table-column prop="costCenterName" label="成本中心名称" width="100" sortable show-overflow-tooltip>
              <template #default="{ row }">{{ row.costCenterName || '' }}</template>
            </el-table-column>
            <el-table-column prop="sfahuc001" label="工单号" width="165" sortable>
              <template #default="{ row }">
                <el-input
                  v-if="row._woLocked"
                  v-model="row.sfahuc001"
                  size="small"
                  disabled
                  placeholder="关联带回"
                  style="width: 100%"
                />
                <el-select
                  v-else
                  v-model="row.sfahuc001"
                  size="small"
                  filterable
                  clearable
                  placeholder="请选择"
                  :loading="row._woLoading"
                  popper-class="wo-popper"
                  style="width: 100%"
                  @visible-change="(v) => v && ensureWorkOrderOptions(row)"
                  @change="onWorkOrderChange(row)"
                >
                  <el-option
                    v-for="opt in (row._woOptions || [])"
                    :key="opt.sfaadocno"
                    :label="opt.sfaadocno"
                    :value="opt.sfaadocno"
                  >
                    <span class="wo-opt-docno">{{ opt.sfaadocno }}</span>
                    <span class="wo-opt-qty">可排 {{ opt.kpsl ?? opt.sfaa012 }}</span>
                    <span class="wo-opt-start">开工 {{ opt.sfaa019 }}</span>
                    <span class="wo-opt-end">完工 {{ opt.sfaa020 }}</span>
                    <span class="wo-opt-srcno">来源单号 {{ opt.sfaa022 }}</span>
                    <span class="wo-opt-srcseq">来源序号 {{ opt.sfaa023 }}</span>
                    <span class="wo-opt-orderqty">订单数量 {{ opt.xmdd005 }}</span>
                    <span class="wo-opt-flag" :class="{ 'is-default': opt.row_flag === 'Y' || opt.row_flag === '1' }">默认 {{ opt.row_flag }}</span>
                    <span class="wo-opt-status">{{ formatStatus(null, null, opt.sfaastus) }}</span>
                  </el-option>
                </el-select>
              </template>
            </el-table-column>
            <el-table-column prop="sfahuc016" label="排程数量" width="80" align="right" sortable :sort-method="numSort">
              <template #default="{ row }">
                <el-input-number
                  v-model="row.sfahuc016"
                  :controls="false"
                  :min="0"
                  size="small"
                  style="width: 100%"
                />
              </template>
            </el-table-column>
            <el-table-column prop="sfahuc006" label="预计开工日期" width="110" sortable>
              <template #default="{ row }">
                <el-date-picker
                  v-model="row.sfahuc006"
                  type="date"
                  placeholder="选择日期"
                  value-format="YYYY-MM-DD"
                  size="small"
                  style="width: 100%"
                />
              </template>
            </el-table-column>
            <el-table-column prop="sfahuc007" label="预计完工日期" width="110" sortable>
              <template #default="{ row }">
                <el-date-picker
                  v-model="row.sfahuc007"
                  type="date"
                  placeholder="选择日期"
                  value-format="YYYY-MM-DD"
                  size="small"
                  style="width: 100%"
                />
              </template>
            </el-table-column>
            <el-table-column prop="sfahuc008" label="成本中心" width="40" sortable />
            <el-table-column prop="sfahuc009" label="工单数量" width="50" align="right" sortable :sort-method="numSort" />
            <el-table-column prop="sfaa049" label="已发料套数" width="50" align="right" sortable :sort-method="numSort" />
            <el-table-column prop="sfaa050" label="已入库数量" width="50" align="right" sortable :sort-method="numSort" />

          </el-table>
        </div>
        <!-- 折叠态：右侧窄条，点击展开 -->
        <div v-if="rightCollapsed" class="collapsed-strip right" @click="toggleRight" title="展开排程明细">
          <el-icon><ArrowLeft /></el-icon>
          <span class="strip-text">排程明细</span>
        </div>
      </div>
    </el-card>

    <!-- 右键菜单 -->
    <div
      v-if="ctxMenu.visible"
      class="ctx-menu"
      :style="{ left: ctxMenu.x + 'px', top: ctxMenu.y + 'px' }"
      @contextmenu.prevent
    >
      <div class="ctx-menu-item" @click="onCtxOrderDetail">显示订单展开详情</div>
      <div class="ctx-menu-item" @click="onCtxOrderSchedule">显示订单排程</div>
      <div class="ctx-menu-item" @click="onHideUnfinished">隐藏未出货订单</div>
      <div class="ctx-menu-item" @click="onShowUnfinished">显示未出货订单</div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Expand, Fold, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { fetchBomTree, queryUnfinishedOrders, querySfahucSchedule, saveSfahucSchedule, deleteSfahucSchedule, fetchSchedulableWorkOrders, queryWorkOrderBySource, queryOrderScheduledQty, searchItemDesc, queryItemBasicInfo, WORK_ORDER_STATUS_MAP, TOKEN } from '../api/pendingOrders.js'
import { queryDept } from '../api/budget.js'
import { canFunc, MODULE, FUNC } from '../permission.js'

const form = reactive({
  orderNo: '',
  costCenter: '',
  customerOrderNo: '',
  orderSeq: '',
  estimatedDate: '',
  site: 'NBYL',
  enterpriseCode: '60',
  itemNo: '',
  // 未出货订单明细查询条件（单头没有的，补充到单头，空值不传后端）
  deliveryDateStart: '',   // 交货日期下限 xmdd011_start
  deliveryDateEnd: '',     // 交货日期上限 xmdd011_end
  orderDateStart: '',      // 订单日期下限 xmdadocdt_start
  orderDateEnd: '',        // 订单日期上限 xmdadocdt_end
  productName: '',         // 品名 pmaal004
  salesman: '',            // 业务员 ooag011
  replenishStrategy: ['2'], // 补货策略 imaf013（1采购件/2自制件/3委外件，多选 → 以逗号拼接，如 '1,2,3'；默认选自制件）
  hidePhantom: true         // 不展示虚拟件（true → x_flag='N'，虚拟件的下阶上移到父级）
})

/** BOM 结构树数据 */
const bomTree = ref([])
const treeLoading = ref(false)
const expandAll = ref(true)
const treeMode = ref(true)        // true=树形目录，false=平铺
const tableRef = ref(null)
const tableHeight = ref(300)
let treeSeq = 0

/** 右侧已排程明细（/querySfahuc 返回） */
const scheduledList = ref([])
const rightLoading = ref(false)
const rightTableRef = ref(null)
const rightTableHeight = ref(300)
/** 命中排程的 订单号||订单序号 集合，用于左侧高亮 */
const scheduledKeys = ref(new Set())

/** 未出货订单明细表（最左侧 /queryUnfinishedOrders） */
const unfinishedList = ref([])
const unfinishedLoading = ref(false)
const unfinishedTableRef = ref(null)
const unfinishedTableHeight = ref(300)
/** 最左侧面板宽度像素值 */
const unfinishedWidth = ref(420)
const draggingUnfinished = ref(false)
/** 当前在左侧未出货订单表选中的订单（仅用于查询与保存后回查，不回填到单头） */
const selectedOrderNo = ref('')
const selectedOrderSeq = ref('')

/** 左右面板可拖动分隔条：左侧宽度像素值 + 容器 ref */
const leftWidth = ref(700)
const tablesWrapRef = ref(null)
const dragging = ref(false)
/** 用户是否手动拖过 中间/右表 分隔条：拖过则展开右表时保留其调好的宽度 */
let leftWidthDragged = false

/** 三表折叠：用 v-show 而非 v-if，表格实例始终挂载，保证三表联动、选中态、高度测量不受影响。
 *  默认只显示中间 BOM 工单表，左右两侧折叠为窄条 */
const unfinishedCollapsed = ref(true)
const middleCollapsed = ref(false)
const rightCollapsed = ref(true)
/** 中间 BOM 表宽度策略：
 *  - 右表可见：固定宽度 leftWidth，右表 flex:1 吸收剩余 → 拖分隔条可精确调整两表边界
 *  - 右表隐藏：铺满全部剩余空间 */
const leftPanelStyle = computed(() => {
  if (!rightCollapsed.value) return { width: leftWidth.value + 'px' }
  return { flex: '1 1 0%', width: 'auto' }
})
const toggleUnfinished = () => { unfinishedCollapsed.value = !unfinishedCollapsed.value }
const toggleMiddle = () => { middleCollapsed.value = !middleCollapsed.value }
/** 展开右表：首次给中间 2/3、右表 1/3；用户拖过分隔条则保留其拖好的宽度 */
const toggleRight = () => {
  if (rightCollapsed.value) {
    const wrap = tablesWrapRef.value
    if (wrap) {
      const leftUsed = unfinishedCollapsed.value ? 26 : unfinishedWidth.value
      const leftSplit = unfinishedCollapsed.value ? 0 : 6
      const avail = Math.max(0, wrap.clientWidth - leftUsed - leftSplit - 6)
      if (leftWidthDragged) {
        // 保留用户拖好的宽度，仅限制在容器可用范围内（不再为右表保留最小宽度）
        leftWidth.value = Math.max(0, Math.min(leftWidth.value, avail))
      } else {
        leftWidth.value = Math.round(avail * 2 / 3)
      }
    }
    rightCollapsed.value = false
  } else {
    rightCollapsed.value = true
  }
}

const startDrag = (e) => {
  dragging.value = true
  document.body.style.cursor = 'col-resize'
  document.body.style.userSelect = 'none'
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
  e.preventDefault()
}
const onDrag = (e) => {
  if (!dragging.value) return
  // 以中间 BOM 表自身的左边缘为基准计算新宽度，分隔条才能精确跟随鼠标。
  // （若以容器左边缘为基准会偏移「最左侧面板宽+分隔条」，表现为拖不动）
  const midEl = tableRef.value?.$el?.parentElement
  if (!midEl) return
  // 下限 0：可一直拖到最左或最右，不做任何宽度限制
  const w = Math.min(Math.max(e.clientX - midEl.getBoundingClientRect().left, 0), maxLeftWidth())
  leftWidth.value = w
  leftWidthDragged = true
}
const stopDrag = () => {
  dragging.value = false
  document.body.style.cursor = ''
  document.body.style.userSelect = ''
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
}

/** 组件卸载时清理拖拽监听，避免内存泄漏 */
onBeforeUnmount(() => {
  stopDrag()
  stopDragUnfinished()
})

/** 测量左右两个 table 容器高度，绑定给 el-table 的 height，
 *  这样横向滚动条固定在可视区底部、竖向滚动时也能横向滚动 */
let ro = null
const measureTableHeight = () => {
  // 面板折叠(display:none)时容器 clientHeight 为 0，此时沿用上一次高度，
  // 避免展开瞬间表格塌陷（展开后 ResizeObserver 会再次触发并恢复正确高度）
  const apply = (targetRef, el) => {
    if (!el || !el.parentElement) return
    const h = el.parentElement.clientHeight
    if (h > 0) targetRef.value = h
  }
  apply(tableHeight, tableRef.value?.$el)
  apply(rightTableHeight, rightTableRef.value?.$el)
  apply(unfinishedTableHeight, unfinishedTableRef.value?.$el)
}
/** 中间 BOM 表允许拖到的最大宽度：容器剩余空间。
 *  不再为右表保留最小宽度，向右可一直拖到底（右表随之收窄） */
const maxLeftWidth = () => {
  const wrap = tablesWrapRef.value
  if (!wrap) return leftWidth.value
  const leftUsed = unfinishedCollapsed.value ? 26 : unfinishedWidth.value
  const leftSplit = unfinishedCollapsed.value ? 0 : 6
  return Math.max(0, wrap.clientWidth - 6 - leftUsed - leftSplit)
}

/** 最左侧面板拖拽：调整其宽度（unfinishedWidth） */
const startDragUnfinished = (e) => {
  draggingUnfinished.value = true
  document.body.style.cursor = 'col-resize'
  document.body.style.userSelect = 'none'
  document.addEventListener('mousemove', onDragUnfinished)
  document.addEventListener('mouseup', stopDragUnfinished)
  e.preventDefault()
}
const onDragUnfinished = (e) => {
  if (!draggingUnfinished.value) return
  const wrap = tablesWrapRef.value
  if (!wrap) return
  const rect = wrap.getBoundingClientRect()
  // 下限 0：可一直拖到最左，不做任何宽度限制
  const w = Math.min(Math.max(e.clientX - rect.left, 0), maxUnfinishedWidth())
  unfinishedWidth.value = w
}
const stopDragUnfinished = () => {
  draggingUnfinished.value = false
  document.body.style.cursor = ''
  document.body.style.userSelect = ''
  document.removeEventListener('mousemove', onDragUnfinished)
  document.removeEventListener('mouseup', stopDragUnfinished)
}
/** 最左侧面板宽度允许的最大值：容器剩余空间。
 *  不再为中间表/右表保留最小宽度，可一直向右拖到底 */
const maxUnfinishedWidth = () => {
  const wrap = tablesWrapRef.value
  if (!wrap) return unfinishedWidth.value
  return Math.max(0, wrap.clientWidth - 6)
}

/** 首次加载：左明细表直接设为最大宽度 */
const initLeftWidth = () => {
  leftWidth.value = maxLeftWidth()
}

onMounted(async () => {
  await nextTick()
  initLeftWidth()
  measureTableHeight()
  ro = new ResizeObserver(() => measureTableHeight())
  const leftParent = tableRef.value?.$el?.parentElement
  const rightParent = rightTableRef.value?.$el?.parentElement
  const unfParent = unfinishedTableRef.value?.$el?.parentElement
  if (leftParent) ro.observe(leftParent)
  if (rightParent) ro.observe(rightParent)
  if (unfParent) ro.observe(unfParent)
  // 右键菜单：点击别处 / 滚动 / Esc 均关闭
  document.addEventListener('click', closeCtxMenu)
  window.addEventListener('scroll', closeCtxMenu, true)
  document.addEventListener('keydown', onEscClose)
})
onBeforeUnmount(() => {
  if (ro) ro.disconnect()
  document.removeEventListener('click', closeCtxMenu)
  window.removeEventListener('scroll', closeCtxMenu, true)
  document.removeEventListener('keydown', onEscClose)
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

/** 右侧明细表数值列排序：按数字比较（空值排最后） */
const numSort = (a, b) => {
  const na = Number(a)
  const nb = Number(b)
  const ea = a === '' || a === null || a === undefined || isNaN(na)
  const eb = b === '' || b === null || b === undefined || isNaN(nb)
  if (ea && eb) return 0
  if (ea) return 1
  if (eb) return -1
  return na - nb
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

/** 日期格式化 YYYY-MM-DD（空则留空） */
const formatDate = (row, column, cellValue) => {
  if (!cellValue) return ''
  const s = String(cellValue).trim()
  // 后端可能是 datetime，截取前 10 位
  return s.length >= 10 ? s.slice(0, 10) : s
}

/** 把后端 datetime（2026-12-25 00:00:00）截取为 YYYY-MM-DD，空值返回空串 */
const toDateStr = (v) => {
  if (!v) return ''
  const s = String(v).trim()
  return s.length >= 10 ? s.slice(0, 10) : s
}

/** 归一化为日期时间型 YYYY-MM-DD HH:mm:ss（sfahuc006/007 后端为 datetime）：
 *  已是完整 datetime 取前 19 位；只有日期则补 00:00:00 */
const toDateTimeStr = (v) => {
  if (!v) return ''
  const s = String(v).trim()
  if (s.length >= 19) return s.slice(0, 19)
  if (s.length === 10) return `${s} 00:00:00`
  return s
}

/** 解析 YYYY-MM-DD 为本地 Date */
const parseYMDDate = (s) => {
  if (!s) return null
  const [y, m, d] = String(s).split('-').map(Number)
  if (!y || !m || !d) return null
  return new Date(y, m - 1, d)
}
/** Date → YYYY-MM-DD */
const formatYMDDate = (d) => {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}
/** 显示列头用 YYYY/M/D（与车间排程示例图一致） */
const formatDateColLabel = (date) => {
  if (!date) return ''
  const [y, m, d] = String(date).split('-')
  return `${parseInt(y, 10)}/${parseInt(m, 10)}/${parseInt(d, 10)}`
}
/** 周几中文（一/二/.../日） */
const weekdayOf = (date) => {
  if (!date) return ''
  const d = parseYMDDate(date)
  if (!d) return ''
  return ['日', '一', '二', '三', '四', '五', '六'][d.getDay()]
}

/** 遍历整棵树（含子节点）收集所有节点 */
const walkBomTree = (rows, cb) => {
  if (!Array.isArray(rows)) return
  for (const r of rows) {
    cb(r)
    if (Array.isArray(r?.children) && r.children.length) walkBomTree(r.children, cb)
  }
}

/** 左侧 BOM 树动态日期列：取整棵树中所有节点的 预计开工~预计完工 的并集 */
const bomDateColumns = computed(() => {
  if (!bomTree.value?.length) return []
  let minDate = ''
  let maxDate = ''
  walkBomTree(bomTree.value, (r) => {
    const start = toDateStr(r?.sfaa019)
    const end = toDateStr(r?.sfaa020)
    if (start && (!minDate || start < minDate)) minDate = start
    if (end && (!maxDate || end > maxDate)) maxDate = end
  })
  if (!minDate || !maxDate) return []
  const cols = []
  const cur = parseYMDDate(minDate)
  const last = parseYMDDate(maxDate)
  while (cur && last && cur.getTime() <= last.getTime()) {
    cols.push(formatYMDDate(cur))
    cur.setDate(cur.getDate() + 1)
  }
  return cols
})

/** 判断某行某日期是否处于该行的预计开工~预计完工区间内（仅含工单节点） */
const isBomInDateRange = (row, date) => {
  if (!row?.sfaadocno) return false
  const start = toDateStr(row.sfaa019)
  const end = toDateStr(row.sfaa020)
  return !!(start && end && date >= start && date <= end)
}

/** 获取某行某天计划数量，未填写时显示 0 */
const getBomDateValue = (row, date) => {
  if (!row.bomDailyPlan) row.bomDailyPlan = {}
  if (row.bomDailyPlan[date] === undefined || row.bomDailyPlan[date] === null || row.bomDailyPlan[date] === '') {
    row.bomDailyPlan[date] = 0
  }
  return row.bomDailyPlan[date]
}

/** 写入某行某天计划数量（输入为数字或数字字符串） */
const setBomDateValue = (row, date, v) => {
  if (!row.bomDailyPlan) row.bomDailyPlan = {}
  const num = Number(v)
  row.bomDailyPlan[date] = isNaN(num) ? 0 : num
}

/** 左侧某行是否已在右侧排程表中存在
 *  匹配条件：订单号 + 订单序号 + 品号（左侧树形目录品号 == 右侧 sfahuc002） */
const isScheduledRow = (row) => {
  const key = `${row?.['订单号'] ?? ''}||${row?.['订单序号'] ?? ''}||${row?.itemNo ?? ''}`
  return scheduledKeys.value.has(key)
}
/** 品号列（BOM 行）：queryOrderBom 返回的 ddkpsj（订单可排数量）<= 0 表示已排完 → 单元格变浅蓝 */
const cellClassForBomDone = ({ row }) => {
  const raw = row?.ddkpsj
  if (raw === null || raw === undefined || raw === '') return ''
  const v = Number(raw)
  return !isNaN(v) && v <= 0 ? 'bom-done-cell' : ''
}

/** 左侧 BOM 表：表格级 cell-class-name
 *  - 第 1 列（品号/树形目录列）ddkpsj <= 0 → bom-done-cell 浅蓝 */
const leftCellClassName = ({ row, columnIndex }) => {
  if (columnIndex === 0 && cellClassForBomDone({ row })) return 'bom-done-cell'
  return ''
}

/** 中间 BOM 表当前选中行的品号（用于右表同行品号高亮） */
const currentBomItemNo = ref('')
const bomRowItemNo = (row) => row?.itemNo || (row?.level === 1 ? row?.bmba001 : row?.bmba003) || ''
const onBomCurrentChange = (currentRow) => {
  currentBomItemNo.value = currentRow ? bomRowItemNo(currentRow) : ''
}

/** 右侧明细表：品号与中间表当前选中行品号相同的行 → 浅蓝背景；
 *  订单可排数量为 0 → 字体变淡；未排完（>0）→ 字体加深 */
const rightRowClassName = ({ row }) => {
  const cls = []
  if (currentBomItemNo.value && row?.sfahuc002 === currentBomItemNo.value) cls.push('right-hit-row')
  const q = Number(row?.orderSchedulableQty)
  if (q === 0) cls.push('right-zero-row')
  else if (q > 0) cls.push('right-active-row')
  return cls.join(' ')
}

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

/** 中间表数据：树形模式用原始结构，平铺模式把子节点展开成扁平列表 */
const displayData = computed(() => {
  if (treeMode.value) return bomTree.value
  const out = []
  const walk = (list) => {
    list.forEach((n) => {
      const { children, ...rest } = n
      out.push(children && children.length ? rest : n)
      if (children && children.length) walk(children)
    })
  }
  walk(bomTree.value)
  return out
})

/** 在“树形目录”与“平铺”之间切换 */
const toggleTreeMode = () => {
  treeMode.value = !treeMode.value
}

/** 右侧明细表当前勾选的行（仅在 < 撤销时使用） */
const rightSelectedRows = ref([])
/** 点 < 移除的行，暂存待保存时调 /deleteSfahuc 从数据库删除 */
const pendingDeleteList = ref([])

/** 左侧勾选用：以 row.id 独立记录，不随树形级联 */
const leftCheckedIds = ref(new Set())
/** 把整棵树（含子节点）拍平成数组 */
const flattenBom = (rows, acc = []) => {
  if (!Array.isArray(rows)) return acc
  for (const r of rows) {
    acc.push(r)
    if (Array.isArray(r.children) && r.children.length) flattenBom(r.children, acc)
  }
  return acc
}
/** 当前勾选的行对象（供 > 按钮使用） */
const leftSelectedRows = computed(() =>
  flattenBom(bomTree.value).filter((r) => leftCheckedIds.value.has(r.id))
)
/** 行号：按树的展开顺序从 1 依次递增，id -> 行号 */
const bomRowNoMap = computed(() => {
  const map = new Map()
  flattenBom(bomTree.value).forEach((r, i) => map.set(r.id, i + 1))
  return map
})
const rowNoOf = (row) => bomRowNoMap.value.get(row?.id) ?? ''
/** 左侧表头全选：选中/取消所有行（不含级联逻辑） */
const allLeftChecked = computed(() => {
  const all = flattenBom(bomTree.value)
  return all.length > 0 && all.every((r) => leftCheckedIds.value.has(r.id))
})
const someLeftChecked = computed(() => leftCheckedIds.value.size > 0)

const syncLeftChecked = (nextSet) => {
  leftCheckedIds.value = nextSet
}
const onToggleLeftRow = (row, val) => {
  const s = new Set(leftCheckedIds.value)
  if (val) s.add(row.id)
  else s.delete(row.id)
  syncLeftChecked(s)
}
const onToggleAllLeft = (val) => {
  const s = new Set()
  if (val) for (const r of flattenBom(bomTree.value)) s.add(r.id)
  syncLeftChecked(s)
}
const onRightSelectionChange = (rows) => {
  rightSelectedRows.value = rows
}

/** 右表当前点击行（highlight-current-row） */
const currentRightRow = ref(null)
const onRightCurrentChange = (row) => {
  currentRightRow.value = row
}

/** 最左侧未出货订单表行级 class：wpsl（未排数量）<= 0 → 浅蓝背景 */
const unfinishedRowClassName = ({ row }) => {
  const v = Number(row?.wpsl)
  if (row?.wpsl !== '' && row?.wpsl != null && !Number.isNaN(v) && v <= 0) return 'wpsl-zero-row'
  return ''
}

/** 中间 BOM 表行级 class：订单号+订单序号+品号 与右表选中行（点击或勾选）相同的行 → 浅蓝背景 */
const bomRowClassName = ({ row }) => {
  const keys = new Set()
  if (currentRightRow.value) {
    keys.add(`${currentRightRow.value.sfahuc004 ?? ''}||${currentRightRow.value.sfahuc005 ?? ''}||${currentRightRow.value.sfahuc002 ?? ''}`)
  }
  for (const r of rightSelectedRows.value) {
    keys.add(`${r.sfahuc004 ?? ''}||${r.sfahuc005 ?? ''}||${r.sfahuc002 ?? ''}`)
  }
  if (keys.size && keys.has(`${row['订单号'] ?? ''}||${row['订单序号'] ?? ''}||${bomRowItemNo(row) ?? ''}`)) return 'bom-hit-row'
  return ''
}

/**
 * 左侧勾选的行 → 加入右侧明细表。
 * 按品号(sfaa010) 调 /querySchedulableWorkOrders 取可排工单，
 * 用返回中的 工单号(sfaadocno) / 预计开工(sfaa019) / 预计完工(sfaa020) /
 * 成本中心(sfaa068) / 可排数量(kpsl) 填入对应字段（取不到则留空）。
 * 再自动计算 sfahucseq = 现有最大序号 + 1，并把品号写入命中集合使左侧变蓝。
 */
/** 把某条可排程工单信息回填到右表行：
 *  工单号 sfahuc001←sfaadocno，开工 sfahuc006←sfaa019，完工 sfahuc007←sfaa020，
 *  成本中心 sfahuc008←sfaa068，工单数量 sfahuc009/sfahuc003←kpsl(可排数量) */
const applyWorkOrderInfo = (row, info) => {
  if (!row || !info) return
  row.sfahuc001 = info.sfaadocno ?? ''
  row.sfahuc006 = toDateStr(info.sfaa019)
  row.sfahuc007 = toDateStr(info.sfaa020)
  row.sfahuc008 = info.sfaa068 ?? ''
  if (info.ooefl003) row.costCenterName = info.ooefl003
  const qty = info.kpsl ?? info.sfaa012 ?? '' // 可排数量优先
  row.sfahuc009 = qty   // 工单数量（显示列）
  row.sfahuc003 = qty   // 工单数量（保存字段）
  // 选择工单后：排程数量默认取该工单的“可排数量”
  row.sfahuc016 = info.kpsl ?? info.sfaa012 ?? ''
}

/** 默认工单号：下拉选项中 可排数量(kpsl) == 订单需求量(sfahuc011) 的那条 */
const applyWorkOrderDefault = (row) => {
  const list = row._woOptions || []
  if (!list.length) return
  // 已由 > 插入时带入工单号的行不覆盖，保留多条返回的逐行结果
  if (row.sfahuc001) return
  const need = Number(row.sfahuc011)
  const hit = isNaN(need)
    ? null
    : list.find((o) => Number(o.kpsl ?? o.sfaa012) === need)
  if (hit) applyWorkOrderInfo(row, hit)
}

/** 品号 -> 品名 缓存（/searchItemDesc 查询结果，同品号只查一次） */
const itemNameCache = ref({})
/** 为右表某行补品名：优先用缓存，否则按品号(sfahuc002)调 /searchItemDesc */
const ensureItemName = async (row) => {
  if (!row) return
  const itemNo = String(row.sfahuc002 ?? '').trim()
  if (!itemNo) return
  if (row.itemName) return
  if (itemNameCache.value[itemNo]) {
    row.itemName = itemNameCache.value[itemNo]
    return
  }
  try {
    const res = await searchItemDesc({ imaal001: itemNo })
    const list = res.data?.list ?? res.data?.data?.list ?? []
    const name = Array.isArray(list) && list.length ? (list[0]?.imaal003 ?? '') : ''
    itemNameCache.value = { ...itemNameCache.value, [itemNo]: name }
    row.itemName = name
  } catch (e) {
    // 查询失败留空，不影响主流程
  }
}

/** 单头文本框：展示各接口提交的 JSON（仅请求，不含返回） */
const woBindJson = ref('')
const reqJsonMap = ref({})
const setReqJson = (apiName, body) => {
  reqJsonMap.value = { ...reqJsonMap.value, [apiName]: body }
  woBindJson.value = JSON.stringify(reqJsonMap.value, null, 2)
}

/** 数值展示：空值原样返回，数字去掉多余小数 */
const fmtNum = (v) => {
  if (v === '' || v === null || v === undefined) return ''
  const n = Number(v)
  return isNaN(n) ? String(v) : Number(n.toFixed(6)).toString()
}
/** 转数字，无法转换时返回 null */
const toNumOrNull = (v) => {
  if (v === '' || v === null || v === undefined) return null
  const n = Number(v)
  return isNaN(n) ? null : n
}

/** 订单可排数量 = 订单需求量 - 已排数量；结果为负时按 0 显示 */
const calcOrderSchedulableQty = (needQty, doneQty) => {
  if (needQty === null) return ''
  const v = needQty - (doneQty ?? 0)
  return v < 0 ? 0 : v
}

/** 为右表某行加载工单下拉选项：
 *  调 /querySchedulableWorkOrders，sfaa010 = 该行品号(sfahuc002)，sfaastus 默认 M,F,N,C */
const ensureWorkOrderOptions = async (row) => {
  if (!row) return
  if (row._woLocked) return // 关联带回工单号的行只读，无需加载下拉
  if (Array.isArray(row._woOptions) && row._woOptions.length) return
  if (row._woLoading) return
  const itemNo = row.sfahuc002 ?? ''
  if (!itemNo) { row._woOptions = []; return }
  const reqBody = {
    token: TOKEN,
    sfaaent: form.enterpriseCode || '60',
    sfaasite: form.site || 'NBYL',
    sfaa010: itemNo,
    // 订单号/订单序号（来源单号/来源序号）一并下发，后端据此匹配并返回默认行
    sfaa022: row.sfahuc004 ?? '',
    sfaa023: row.sfahuc005 ?? '',
    // 绑定工单下拉框：仅允许 sfaastus in ('F','N') 两种状态（关联仍用 'M,F,N,C'）
    sfaastus: 'F,N'
  }
  row._woLoading = true
  setReqJson('querySchedulableWorkOrders', reqBody)
  try {
    const res = await fetchSchedulableWorkOrders(reqBody)
    const master = res.data?.master ?? res.data?.data ?? []
    row._woOptions = Array.isArray(master) ? master : []
  } catch (e) {
    row._woOptions = []
  } finally {
    row._woLoading = false
  }
  applyWorkOrderDefault(row)
}

/** 手动选择工单号后：把对应日期/成本中心/工单数量填入 */
const onWorkOrderChange = (row) => {
  const info = (row._woOptions || []).find((o) => o.sfaadocno === row.sfahuc001)
  if (info) applyWorkOrderInfo(row, info)
}

const onMoveToRight = async () => {
  if (!leftSelectedRows.value.length) {
    ElMessage.warning('请先在左侧 BOM 表勾选要加入排程的行')
    return
  }
  // 计算当前最大序号
  const seqs = (scheduledList.value || [])
    .map(r => parseInt(r.sfahucseq, 10))
    .filter(n => !isNaN(n))
  let nextSeq = seqs.length ? Math.max(...seqs) + 1 : 1

  const newKeys = new Set(scheduledKeys.value)
  const allRows = leftSelectedRows.value
  // ddkpsj（订单可排数量）<= 0 表示已排完，不加入右侧排程
  const rows = []
  const skippedDone = []
  for (const row of allRows) {
    const raw = row.ddkpsj
    const v = (raw === null || raw === undefined || raw === '') ? NaN : Number(raw)
    if (!isNaN(v) && v <= 0) skippedDone.push(row)
    else rows.push(row)
  }
  if (!rows.length) {
    ElMessage.warning('所选行订单可排数量均已排完（ddkpsj<=0），未加入排程')
    return
  }
  if (skippedDone.length) {
    ElMessage.info(`已跳过 ${skippedDone.length} 行（订单可排数量<=0，已排完）`)
  }

  // 并发查 /querySchedulableWorkOrders（一批 4 条）
  // 关联条件：品号 sfaa010 + 订单号 sfaa022 + 订单序号 sfaa023 + 状态码 sfaastus，按条件组合去重
  const orderInfoMap = new Map() // 条件组合 key -> master[]
  const CONCURRENCY = 4
  const condList = []
  const condKeySet = new Set()
  for (const row of rows) {
    const itemNo = row.itemNo || (row.level === 1 ? row.bmba001 : row.bmba003) || ''
    const cond = {
      sfaaent: form.enterpriseCode || '60',
      sfaasite: form.site || 'NBYL',
      sfaa010: itemNo,
      sfaastus: 'M,F,N,C',
      sfaa022: row['订单号'] ?? '',   // 来源单号（订单号）：关联按订单精确过滤
      sfaa023: row['订单序号'] ?? ''  // 来源序号（订单序号）
    }
    const key = `${cond.sfaa010}||${cond.sfaa022}||${cond.sfaa023}`
    if (condKeySet.has(key)) continue
    condKeySet.add(key)
    condList.push({ key, cond })
  }
  for (let i = 0; i < condList.length; i += CONCURRENCY) {
    const batch = condList.slice(i, i + CONCURRENCY)
    await Promise.all(batch.map(async ({ key, cond }) => {
      try {
        const res = await queryWorkOrderBySource(cond)
        const master = res.data?.master ?? res.data?.data ?? []
        orderInfoMap.set(key, Array.isArray(master) ? master : [])
      } catch (e) {
        // 查询失败不阻断，该组合相关字段留空
        orderInfoMap.set(key, [])
      }
    }))
  }

  // 并发查 /queryOrderScheduledQty（按 订单号+订单序号+品号 去重）取已排数量 ypgds
  const scheduledQtyMap = new Map() // 订单号||订单序号||品号 -> 已排数量(数字，查不到按 0)
  const orderKeySet = new Set()
  for (const row of rows) {
    const itemNo = row.itemNo || (row.level === 1 ? row.bmba001 : row.bmba003) || ''
    const k = `${row['订单号'] ?? ''}||${row['订单序号'] ?? ''}||${itemNo}`
    if (!orderKeySet.has(k)) orderKeySet.add(k)
  }
  const orderKeys = [...orderKeySet]
  for (let i = 0; i < orderKeys.length; i += CONCURRENCY) {
    const batch = orderKeys.slice(i, i + CONCURRENCY)
    await Promise.all(batch.map(async (k) => {
      const [oNo, oSeq, itemNo] = k.split('||')
      const qtyReq = {
        token: TOKEN,
        sfahucent: form.enterpriseCode || '60',
        sfahucsite: form.site || 'NBYL',
        sfahuc004: oNo,
        sfahuc005: oSeq,
        sfahuc002: itemNo
      }
      setReqJson('queryOrderScheduledQty', qtyReq)
      try {
        const res = await queryOrderScheduledQty(qtyReq)
        const ypgds = toNumOrNull(res.data?.ypgds)
        scheduledQtyMap.set(k, ypgds === null ? 0 : ypgds)
      } catch (e) {
        // 查询失败按 0 处理，不阻断
        scheduledQtyMap.set(k, 0)
      }
    }))
  }

  let inserted = 0
  const blockedRows = []
  const addedRows = []
  for (const row of rows) {
    const itemNo = row.itemNo || (row.level === 1 ? row.bmba001 : row.bmba003) || ''
    const orderNo = row['订单号'] ?? ''
    const orderSeq = row['订单序号'] ?? ''
    const planQty = row['订单需求用量'] ?? ''
    // 已排数量（ypgds，无返回/NULL 按 0）；订单可排数量 = 订单需求量 - 已排数量
    const doneQty = scheduledQtyMap.get(`${orderNo}||${orderSeq}||${itemNo}`) ?? 0
    const needQty = toNumOrNull(planQty)
    const remainQty = needQty === null ? '' : needQty - doneQty
    // 订单需求量 - 已排数量 <= 0 说明已排完，禁止继续排程
    if (needQty !== null && remainQty <= 0) {
      blockedRows.push(`${orderNo}-${orderSeq}`)
      continue
    }
    // 成本中心：优先取左侧 BOM 行的 sfaa068，左侧为空时再用接口返回的 sfaa068 兜底
    const leftCostCenter = row.sfaa068 ?? ''
    // 成本中心名称：优先取左侧 BOM 行的 ooefl003
    const leftCostCenterName = row.ooefl003 ?? ''
    // /querySchedulableWorkOrders 返回多少条就插入多少行；无返回时插入一条空明细
    const qKey = `${itemNo}||${orderNo}||${orderSeq}`
    const infos = orderInfoMap.get(qKey) || []
    const list = infos.length ? infos : [{}]
    for (const info of list) {
      // 工单数量：优先取关联返回 sfaa012，取不到再回退可排数量 kpsl
      const qty = info.sfaa012 ?? info.kpsl ?? ''
      const newRow = {
        sfahucdocno: form.orderNo,
        sfahucseq: String(nextSeq),
        sfahuc004: orderNo,
        sfahuc005: orderSeq,
        sfahuc002: itemNo,
        sfahuc011: planQty,
        // 以下字段取自 /querySchedulableWorkOrders 返回
        sfahuc001: info.sfaadocno ?? '',    // 工单号
        sfahuc006: toDateStr(info.sfaa019), // 预计开工日期
        sfahuc007: toDateStr(info.sfaa020), // 预计完工日期
        sfahuc008: leftCostCenter || info.sfaa068 || '', // 成本中心（左侧优先）
        costCenterName: leftCostCenterName || info.ooefl003 || '', // 成本中心名称（仅展示，不提交）
        sfahuc003: qty,                     // 工单数量（= 可排数量 kpsl）
        sfahuc009: qty,                     // 工单数量（显示列）
        // 排程数量默认 = min(订单需求量, 工单数量)：订单需求量>=工单数量时取工单数量，否则取订单需求量
        sfahuc016: (() => {
          const demand = Number(planQty) || 0
          const work = Number(qty) || 0
          return demand >= work ? work : demand
        })(),
        sfaa049: info.sfaa049 ?? '',        // 已发料套数（显示列，随工单信息带入）
        sfaa050: info.sfaa050 ?? '',        // 已入库数量（显示列，随工单信息带入）
        sfahuc010: '',
        itemName: row.imaal003 ?? '',   // 品名（左侧 BOM 行直接带出，缺省时再查 /searchItemDesc）
        orderSchedulableQty: calcOrderSchedulableQty(needQty, doneQty), // 订单可排数量（<0 显示 0）
        _woOptions: null,
        _woLoading: false,
        // 关联带回工单号 → 工单号只读；未带回（空明细）则允许手动绑定
        _woLocked: !!info.sfaadocno
      }
      scheduledList.value.push(newRow)
      addedRows.push(newRow)
      nextSeq++
      inserted++
    }
    newKeys.add(`${orderNo}||${orderSeq}||${itemNo}`)
  }
  scheduledKeys.value = newKeys
  // 取消勾选
  leftCheckedIds.value = new Set()
  // 逐行加载工单下拉（sfaa010 = 品号）并回填默认值（不阻塞提示）
  Promise.all(addedRows.map((r) => ensureWorkOrderOptions(r)))
  // 品名缺失时按品号查 /searchItemDesc 补齐（不阻塞提示）
  Promise.all(addedRows.map((r) => ensureItemName(r)))
  if (blockedRows.length) {
    ElMessage.warning(`订单项 ${blockedRows.join('、')} 已排完，未加入排程`)
  }
  if (inserted) {
    ElMessage.success(`已加入 ${inserted} 行到排程明细`)
  }
}

/**
 * 右侧勾选的行 ← 移出（仅前端移除，不调接口）。
 */
const onMoveToLeft = async () => {
  if (!rightSelectedRows.value.length) {
    ElMessage.warning('请先在右侧明细表勾选要撤销的行')
    return
  }
  try {
    await ElMessageBox.confirm('确认删除选择的记录', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch (e) {
    // 取消/关闭：不执行移除
    return
  }
  // 只删除当前勾选的那几行（按行对象比对，避免误删同 订单号+订单序号+品号 的其它行）
  const removeSet = new Set(rightSelectedRows.value)
  const removedCount = removeSet.size
  // 记录待删除的明细（保存时调 /deleteSfahuc 真正从数据库删除）
  for (const r of rightSelectedRows.value) {
    pendingDeleteList.value.push({
      sfahucent: form.enterpriseCode || '60',
      sfahucsite: form.site || 'NBYL',
      sfahuc001: r.sfahuc001 ?? '', // 工单号
      sfahuc002: r.sfahuc002 ?? '', // 品号
      sfahuc008: r.sfahuc008 ?? '', // 成本中心
      sfahuc004: r.sfahuc004 ?? '', // 订单号
      sfahuc005: r.sfahuc005 ?? ''  // 订单序号
    })
  }
  scheduledList.value = scheduledList.value.filter(r => !removeSet.has(r))
  // 重新计算命中集合
  const keys = new Set()
  for (const r of scheduledList.value) {
    keys.add(`${r.sfahuc004 ?? ''}||${r.sfahuc005 ?? ''}||${r.sfahuc002 ?? ''}`)
  }
  scheduledKeys.value = keys
  rightSelectedRows.value = []
  rightTableRef.value?.clearSelection?.()
  ElMessage.success(`已撤销 ${removedCount} 行`)
}

/**
 * 仅提交“有变化”的行：加载到右侧表时为每行存一份保存字段的快照 _origin；
 * 新插入（点 > ）的行没有 _origin，恒视为脏行；编辑后与快照不同的行也视为脏行。
 */
const SAVE_FIELDS = ['sfahuc001', 'sfahuc002', 'sfahucdocno', 'sfahucseq', 'sfahuc003', 'sfahuc004', 'sfahuc005', 'sfahuc006', 'sfahuc007', 'sfahuc008', 'sfahuc009', 'sfahuc010', 'sfahuc011', 'sfahuc016']
const saveFieldsOf = (r) => {
  const o = {}
  for (const k of SAVE_FIELDS) o[k] = r?.[k] ?? ''
  return o
}
const isRowDirty = (r) => {
  if (!r || !r._origin) return true // 新插入的行（未从库加载）一律提交
  const cur = saveFieldsOf(r)
  for (const k of SAVE_FIELDS) {
    if (String(cur[k] ?? '') !== String(r._origin[k] ?? '')) return true
  }
  return false
}

/**
 * 保存：将右侧明细表“有变化”的行提交到后端 /saveSfahuc，未变化的行不发请求。
 * 请求体：{ sfahucent, sfahucsite, list:[{ sfahuc001+sfahuc002 为主键, ... }] }
 */
const onSaveSchedule = async () => {
  // 仅提交“有变化”的行（与加载快照比对），新插入行无快照恒为脏行
  const changed = scheduledList.value.filter(isRowDirty)
  if (!changed.length && !pendingDeleteList.value.length) {
    ElMessage.warning('没有发生变化的行，无需保存')
    return
  }
  try {
    // 先处理点 < 移除的记录：调 /deleteSfahuc 从数据库删除
    if (pendingDeleteList.value.length) {
      const delList = pendingDeleteList.value
      pendingDeleteList.value = []
      const results = await Promise.all(delList.map((cond) => deleteSfahucSchedule(cond)))
      const failed = results.filter(r => r.data?.success === false || r.data?.code === 'E')
      if (failed.length) {
        ElMessage.error(`删除已移除明细失败 ${failed.length} 条，其余继续保存`)
      }
    }
    // 仅把有变化的行发给后端；未变化行不进入请求体
    const payload = changed.map(r => ({
      sfahuc001: r.sfahuc001,
      sfahuc002: r.sfahuc002,
      sfahucdocno: r.sfahucdocno ?? '',
      sfahucseq: r.sfahucseq ?? '',
      sfahuc003: r.sfahuc003 ?? '',
      sfahuc004: r.sfahuc004 ?? '',
      sfahuc005: r.sfahuc005 ?? '',
      sfahuc006: toDateTimeStr(r.sfahuc006), // 日期时间型 YYYY-MM-DD HH:mm:ss
      sfahuc007: toDateTimeStr(r.sfahuc007), // 日期时间型 YYYY-MM-DD HH:mm:ss
      sfahuc008: r.sfahuc008 ?? '',
      sfahuc009: r.sfahuc009 ?? '',
      sfahuc010: r.sfahuc010 ?? '',
      sfahuc011: r.sfahuc011 ?? '',
      sfahuc016: r.sfahuc016 ?? ''
    }))
    if (payload.length) {
      const res = await saveSfahucSchedule(payload)
      if (res.data?.success === false || res.data?.code === 'E') {
        ElMessage.error('保存失败: ' + (res.data?.message || '未知错误'))
        return
      }
      ElMessage.success(`已保存 ${payload.length} 行排程明细（共 ${scheduledList.value.length} 行，跳过 ${scheduledList.value.length - payload.length} 行未变化）`)
    } else {
      ElMessage.success('已删除撤销的明细')
    }
    // 保存后自动重新查询：中间 BOM 表（含 ddkpsj 等最新状态）+ 右侧明细表
    if (selectedOrderNo.value) {
      await loadBomAndSchedule(selectedOrderNo.value, selectedOrderSeq.value)
    } else {
      await loadScheduled()
    }
  } catch (err) {
    ElMessage.error('保存失败: ' + (err.response?.data?.message || err.message))
  }
}

/**
 * 右键菜单：在左右明细表任意行上点右键弹出，包含两个命令
 *   1. 显示订单展开详情  2. 显示订单排程
 */
const ctxMenu = reactive({ visible: false, x: 0, y: 0, row: null })

/** el-table 的 row-contextmenu：(row, column, event) */
const onRowContextMenu = (row, column, event) => {
  event.preventDefault()
  ctxMenu.row = row
  ctxMenu.x = event.clientX
  ctxMenu.y = event.clientY
  ctxMenu.visible = true
}

/** 面板级右键：点到空白区域（非数据行）也弹菜单；数据行由 @row-contextmenu 负责，此处跳过 */
const onTableContextMenu = (event) => {
  // 点到数据行时交给 row-contextmenu 处理，避免重复打开
  if (event.target?.closest?.('.el-table__row')) return
  event.preventDefault()
  ctxMenu.row = null // 空白区无关联行，菜单命令均为面板布局类（不依赖行数据）
  ctxMenu.x = event.clientX
  ctxMenu.y = event.clientY
  ctxMenu.visible = true
}

const closeCtxMenu = () => {
  ctxMenu.visible = false
}

/** Esc 关闭右键菜单 */
const onEscClose = (e) => {
  if (e.key === 'Escape') closeCtxMenu()
}

/** 从左右任一侧的行中取出 订单号 / 订单序号 / 品号 */
const pickRowKeys = (row) => {
  if (!row) return { orderNo: '', orderSeq: '', itemNo: '' }
  const orderNo = row['订单号'] ?? row.sfahuc004 ?? ''
  const orderSeq = row['订单序号'] ?? row.sfahuc005 ?? ''
  const itemNo = row.itemNo ?? row.sfahuc002 ?? (row.level === 1 ? row.bmba001 : row.bmba003) ?? ''
  return { orderNo, orderSeq, itemNo }
}

/* ---- 命令 1：显示订单展开详情 ----
 *  收起右侧排程明细表、展开中间 BOM 工单表并铺满整个区域（不留 5px 残留）。 */
const onCtxOrderDetail = () => {
  middleCollapsed.value = false
  rightCollapsed.value = true
  ElMessage.success('已切换：BOM 工单表最大化')
}

/* ---- 命令 2：显示订单排程 ----
 *  收起中间 BOM 工单表、展开右侧排程明细表占满其余宽度（不留 5px 残留）。 */
const onCtxOrderSchedule = () => {
  middleCollapsed.value = true
  rightCollapsed.value = false
  ElMessage.success('已切换：BOM 表已收起 / 右表最大化')
}

/* ---- 命令 3：隐藏未出货订单（折叠为侧栏窄条，与工具栏按钮同一套状态） ---- */
const onHideUnfinished = () => {
  unfinishedCollapsed.value = true
  ElMessage.success('已隐藏未出货订单')
}

/* ---- 命令 4：显示未出货订单（强制 700px，中间/右侧按比例同步收缩） ---- */
const onShowUnfinished = () => {
  const wrap = tablesWrapRef.value
  const oldLeft = leftWidth.value
  // 折叠态时最左侧只占 26px 窄条且无分隔条，据此推算当前右侧实际宽度
  const oldUnfinished = unfinishedCollapsed.value ? 26 : unfinishedWidth.value
  const oldSplit = unfinishedCollapsed.value ? 6 : 12
  unfinishedWidth.value = 700
  unfinishedCollapsed.value = false
  if (!wrap) {
    ElMessage.success('已显示未出货订单（700px）')
    return
  }
  const W = wrap.clientWidth
  const oldRight = Math.max(0, W - oldUnfinished - oldLeft - oldSplit)
  const available = Math.max(0, W - 700 - 12)
  // 保留当前 中间BOM表 : 右侧排程表 的宽度比例，两者同步收窄以腾出 700px
  if (oldLeft + oldRight > 0) {
    leftWidth.value = Math.max(5, Math.round(available * oldLeft / (oldLeft + oldRight)))
  } else {
    leftWidth.value = Math.max(5, available - 50)
  }
  ElMessage.success('已显示未出货订单（700px）')
}

/**
 * 调 /querySfahuc，返回结果填入右侧明细表，并构建 订单号||订单序号 命中集合用于左侧高亮。
 * orderNo / orderSeq 不传时回退到单头表单值。
 */
const loadScheduled = async (orderNo, orderSeq) => {
  rightLoading.value = true
  try {
    const res = await querySfahucSchedule({
      sfahucent: '60',
      sfahucsite: form.site,
      sfahuc004: orderNo ?? selectedOrderNo.value,
      sfahuc005: orderSeq ?? selectedOrderSeq.value
    })
    const master = res.data?.master ?? res.data?.data?.master ?? []
    // 后端返回的是 datetime（2026-09-01 00:00:00），统一截取为日期型 YYYY-MM-DD，否则保存会出错
    scheduledList.value = (Array.isArray(master) ? master : []).map((r) => {
      const row = {
        ...r,
        sfahuc006: toDateStr(r.sfahuc006),
        sfahuc007: toDateStr(r.sfahuc007),
        _woOptions: null,
        _woLoading: false
      }
      // 从库加载的行：存一份保存字段快照，供“仅提交有变化的行”比对
      row._origin = saveFieldsOf(row)
      return row
    })
    const keys = new Set()
    for (const r of scheduledList.value) {
      keys.add(`${r.sfahuc004 ?? ''}||${r.sfahuc005 ?? ''}||${r.sfahuc002 ?? ''}`)
    }
    scheduledKeys.value = keys
    // 补 订单可排数量 与 成本中心名称（/querySfahuc 不返回这两项）
    await fillLoadedRowExtras()
  } catch (err) {
    ElMessage.error('获取排程明细失败: ' + (err.response?.data?.message || err.message))
    scheduledList.value = []
    scheduledKeys.value = new Set()
  } finally {
    rightLoading.value = false
  }
}

/** 成本中心编码 -> 名称 映射（懒加载一次，/queryDept） */
const deptNameMap = ref({})
let deptMapLoaded = false
const ensureDeptNameMap = async () => {
  if (deptMapLoaded) return
  try {
    // ooeg003='3' → 责任中心类型为成本中心（与车间计划排程一致）
    const res = await queryDept({ ooefl001: '', ooefl003: '', ooeg003: '3' })
    const list = res.data?.master ?? res.data?.data ?? []
    // 大小写不敏感取值（与车间计划排程一致）
    const get = (obj, ...keys) => {
      for (const k of keys) {
        const hit = Object.keys(obj || {}).find((x) => x.toLowerCase() === k.toLowerCase())
        if (hit && obj[hit] != null && obj[hit] !== '') return obj[hit]
      }
      return ''
    }
    const map = {}
    for (const item of list) {
      const code = String(get(item, 'ooefl001')).trim()
      const name = String(get(item, 'ooefl003')).trim()
      if (code) map[code] = name
    }
    if (Object.keys(map).length) {
      deptNameMap.value = map
      deptMapLoaded = true
    }
  } catch (e) {
    // 查询失败则名称留空，不影响主流程
  }
}

/** 查询回显的行补 订单可排数量 与 成本中心名称 */
const fillLoadedRowExtras = async () => {
  if (!scheduledList.value.length) return
  // 1) 订单可排数量 = 订单需求量(sfahuc011) - 已排数量(ypgds，按 订单号+订单序号+品号 查)
  const keySet = new Set()
  for (const r of scheduledList.value) {
    const k = `${r.sfahuc004 ?? ''}||${r.sfahuc005 ?? ''}||${r.sfahuc002 ?? ''}`
    if (!keySet.has(k)) keySet.add(k)
  }
  const keys = [...keySet]
  const CONCURRENCY = 4
  for (let i = 0; i < keys.length; i += CONCURRENCY) {
    const batch = keys.slice(i, i + CONCURRENCY)
    await Promise.all(batch.map(async (k) => {
      const [oNo, oSeq, itemNo] = k.split('||')
      const qtyReq = {
        token: TOKEN,
        sfahucent: form.enterpriseCode || '60',
        sfahucsite: form.site || 'NBYL',
        sfahuc004: oNo,
        sfahuc005: oSeq,
        sfahuc002: itemNo
      }
      setReqJson('queryOrderScheduledQty', qtyReq)
      try {
        const res = await queryOrderScheduledQty(qtyReq)
        const ypgds = toNumOrNull(res.data?.ypgds) ?? 0
        for (const r of scheduledList.value) {
          if ((r.sfahuc004 ?? '') === oNo && (r.sfahuc005 ?? '') === oSeq && (r.sfahuc002 ?? '') === itemNo) {
            r.orderSchedulableQty = calcOrderSchedulableQty(toNumOrNull(r.sfahuc011), ypgds)
          }
        }
      } catch (e) {
        // 查询失败留空，不阻断
      }
    }))
  }
  // 2) 品名（按品号查 /searchItemDesc）
  await Promise.all(scheduledList.value.map((r) => ensureItemName(r)))
  // 3) 成本中心名称（按 sfahuc008 从 /queryDept 映射补齐）
  await ensureDeptNameMap()
  const map = deptNameMap.value
  for (const r of scheduledList.value) {
    if (r.costCenterName) continue
    const code = String(r.sfahuc008 ?? '').trim()
    if (!code) continue
    // 先精确匹配，再退化为大小写不敏感匹配
    let name = map[code]
    if (!name) {
      const hit = Object.keys(map).find((k) => k.toLowerCase() === code.toLowerCase())
      name = hit ? map[hit] : ''
    }
    if (name) r.costCenterName = name
  }
}

/**
 * 查询未出货订单明细（最左侧明细表）：调 /queryUnfinishedOrders。
 * 仅发送单头中已填写的条件（订单号/订单序号/品号 来自单头已有字段；
 * 交货日期/订单日期上下限、品名、业务员 来自新增的单头条件），空值不传后端。
 */
const loadUnfinishedOrders = async () => {
  unfinishedLoading.value = true
  try {
    const body = {
      token: TOKEN,
      xmddent: form.enterpriseCode || '60',
      xmddsite: form.site || 'NBYL'
    }
    if (form.customerOrderNo) body.xmdddocno = form.customerOrderNo
    if (form.orderSeq) body.xmddseq = form.orderSeq
    if (form.itemNo) body.xmdd001 = form.itemNo
    if (form.deliveryDateStart) body.xmdd011_start = form.deliveryDateStart
    if (form.deliveryDateEnd) body.xmdd011_end = form.deliveryDateEnd
    if (form.orderDateStart) body.xmdadocdt_start = form.orderDateStart
    if (form.orderDateEnd) body.xmdadocdt_end = form.orderDateEnd
    if (form.productName) body.imaal003 = form.productName
    if (form.salesman) body.ooag011 = form.salesman
    if (form.replenishStrategy?.length) body.imaf013 = form.replenishStrategy.join(',')
    setReqJson('queryUnfinishedOrders', body)
    const res = await queryUnfinishedOrders(body)
    const list = res.data?.list ?? res.data?.data?.list ?? []
    unfinishedList.value = Array.isArray(list) ? list : []
  } catch (err) {
    ElMessage.error('获取未出货订单失败: ' + (err.response?.data?.message || err.message))
    unfinishedList.value = []
  } finally {
    unfinishedLoading.value = false
  }
}

/**
 * 查询按钮：仅刷新最左侧未出货订单明细表（/queryUnfinishedOrders）。
 * 中间 BOM 树表与右侧排程明细不再由查询按钮直接获取，
 * 而是由点击最左侧未出货订单行触发（onUnfinishedRowClick）。
 */
const loadBomTree = async () => {
  await loadUnfinishedOrders()
  // 清空中间/右侧与当前选中订单，等待用户选择具体订单行
  bomTree.value = []
  scheduledList.value = []
  scheduledKeys.value = new Set()
  selectedOrderNo.value = ''
  selectedOrderSeq.value = ''
}

/**
 * 点击最左侧未出货订单明细行：用该行的 订单号(xmdddocno) + 订单序号(xmddseq)
 * 调 /queryOrderBom，结果填入中间 BOM 树表；同时按该订单号+订单序号 载入右侧已排程明细。
 */
const onUnfinishedRowClick = async (row) => {
  if (!row) return
  const orderNo = row.xmdddocno || ''
  const orderSeq = row.xmddseq || ''
  if (!orderNo) {
    ElMessage.warning('该行缺少订单号，无法查询 BOM')
    return
  }
  // 记录当前选中的订单（仅用于查询与保存后回查，不回填到单头 订单号/订单序号）
  selectedOrderNo.value = orderNo
  selectedOrderSeq.value = orderSeq
  await loadBomAndSchedule(orderNo, orderSeq)
}

/**
 * 按 订单号(xmdddocno) + 订单序号(xmddseq) 调 /queryOrderBom 载入中间 BOM 树，
 * 再为每行按品号查 /queryItemBasicInfo 补 UPPH，并同步右侧已排程明细。
 */
const loadBomAndSchedule = async (orderNo, orderSeq) => {
  treeLoading.value = true
  try {
    const res = await fetchBomTree({
      xmdddocno: orderNo,
      xmddseq: orderSeq,
      x_flag: form.hidePhantom ? 'N' : ''
    })
    const rawTree = res.data?.tree ?? res.data?.data?.tree ?? []
    treeSeq = 0
    currentBomItemNo.value = '' // 重新载入 BOM 后清空选中品号，右表高亮随之取消
    bomTree.value = addTreeIds(Array.isArray(rawTree) ? rawTree : [])
    if (bomTree.value.length === 0) {
      ElMessage.info('未查询到 BOM 结构数据')
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
    // BOM 查询之后，按当前订单号 + 订单序号 查已排程明细，填入右侧表
    await loadScheduled(orderNo, orderSeq)
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
  padding: 0px;
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
  padding: 0px;
}

.basic-card {
  /* 高度随表头字段自动增长（现已包含未出货订单查询条件） */
  height: auto;
  border: none;
  box-shadow: none;
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

/* 左右双表并排：左 BOM 树 + 右排程明细 */
.tables-wrap {
  display: flex;
  flex: 1;
  min-height: 0;
  width: 100%;
}

.tables-wrap .left-panel {
  flex: 0 0 auto;
  min-width: 0;
  overflow: hidden;
  background: #fff;
  display: flex;
  flex-direction: column;
}

/* 最左侧未出货订单明细表面板 */
.tables-wrap .unfinished-panel {
  flex: 0 0 auto;
  min-width: 0;
  overflow: hidden;
  background: #fff;
  display: flex;
  flex-direction: column;
}

/* 约束表格不溢出所在面板，避免遮挡中间的拖拽把手 */
.tables-wrap .left-panel > :deep(.el-table),
.tables-wrap .right-panel > :deep(.el-table) {
  width: 100% !important;
  max-width: 100% !important;
}

/* 拖拽把手：位于两表之间，拖动调整左表宽度（与车间计划排程同款） */
.tables-wrap .splitter {
  flex: 0 0 6px;
  cursor: col-resize;
  background: transparent;
  transition: background-color 0.2s;
}
.tables-wrap .splitter:hover,
.tables-wrap .splitter:active {
  background: #409eff;
}

/* 折叠态侧栏窄条：26px 竖条，竖排文字 + 箭头，点击展开 */
.tables-wrap .collapsed-strip {
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
  color: #909399;
  transition: background-color 0.2s, color 0.2s;
}
.tables-wrap .collapsed-strip:hover {
  background: #ecf5ff;
  color: #409eff;
}
.tables-wrap .collapsed-strip .strip-text {
  writing-mode: vertical-rl;
  font-size: 12px;
  letter-spacing: 2px;
}

.tables-wrap .right-panel {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  background: #fff;
  display: flex;
  flex-direction: column;
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
  display: flex;
  flex: 1;
  min-height: 0;
}

/* 右侧排程明细表标题行也居中、40px；表头文字过长自动换行 */
.tables-wrap .right-panel :deep(.el-table th.el-table__cell > .cell) {
  text-align: center;
  min-height: 40px;
  line-height: 20px;
  white-space: normal;
  word-break: break-all;
}
.tables-wrap .right-panel :deep(.el-table th.el-table__cell) {
  height: auto;
}

/* 明细表（未出货/BOM/排程）表头背景蓝色 */
.tables-wrap :deep(.el-table th.el-table__cell) {
  background-color: #1e90ff !important;
  color: #fff;
}

/* 表单行高 30px：输入框 20px，行间距为 0 */
.basic-form :deep(.el-form-item) {
  margin-bottom: 0;
}

/* 单头字段紧凑：列宽收缩到内容宽度，字段之间零空隙 */
.basic-form :deep(.el-row.tight-row) {
  flex-wrap: wrap;
  margin: 0;
  padding: 0;
}
.basic-form :deep(.el-row.tight-row) > .el-col {
  flex: 0 0 auto;
  width: auto;
  max-width: none;
  margin: 0;
  padding: 0;
}
.basic-form :deep(.el-row.tight-row) .el-form-item {
  margin: 0;
  padding: 0;
  align-items: center;
}
.basic-form :deep(.el-row.tight-row) .el-form-item__label {
  margin: 0;
  padding: 0;
  height: 25px;
  line-height: 25px;
  display: inline-flex;
  align-items: center;
  justify-content: flex-start;
  /* 覆盖 Element 按最长标签统一设的 label-width:auto 内联宽度，
     让每个标签宽度随自身文字收缩，消除标签与文本框之间的空隙 */
  width: auto !important;
  max-width: none !important;
}
.basic-form :deep(.el-row.tight-row) .el-form-item__content {
  margin: 0;
  padding: 0;
  align-items: center;
  line-height: 25px;
}
/* 日期选择器外层容器高度与输入框保持一致，避免标签与文本框上下错位 */
.basic-form :deep(.el-row.tight-row) .el-date-editor {
  height: 25px;
  min-height: 25px;
  line-height: 25px;
}

/* 单头字体 12px */
.basic-form :deep(.el-form-item__label),
.basic-form :deep(.el-input__inner),
.basic-form :deep(.el-input),
.basic-form :deep(.el-date-picker),
.basic-form :deep(.el-date-editor),
.basic-form :deep(.el-select),
.basic-form :deep(.el-select__wrapper),
.basic-form :deep(.el-select-v2),
.basic-form :deep(.el-button) {
  font-size: 12px;
}

/* 日期起止之间的分隔符 */
.range-sep {
  margin: 0 4px;
  color: #909399;
}

/* 文本框 / 选择框 高度 25px */
.basic-form :deep(.el-input__wrapper),
.basic-form :deep(.el-select__wrapper),
.basic-form :deep(.el-select-v2__wrapper),
.basic-form :deep(.el-date-editor.el-input__wrapper) {
  min-height: 25px;
  height: 25px;
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
/* 左侧 BOM 表动态日期列：表头（日期+周几） */
.schedule-page :deep(.el-table .date-col-header) {
  display: flex;
  flex-direction: column;
  align-items: center;
  line-height: 1.2;
}
.schedule-page :deep(.el-table .date-col-date) {
  font-weight: 600;
}
.schedule-page :deep(.el-table .date-col-week) {
  font-size: 12px;
  color: #fff;
}

/* 左侧 BOM 表动态日期列单元格：蓝色背景文本框 */
.schedule-page :deep(.el-table .bom-date-cell .el-input__wrapper),
.schedule-page :deep(.el-table .bom-date-cell .el-input__inner) {
  background-color: #cfe3ff !important;
  text-align: center;
  font-weight: 600;
  padding: 0 4px;
}
.schedule-page :deep(.el-table .bom-date-cell .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #6aa8ff inset;
}

/* 左侧 BOM 表中：品号列（树形目录列）ddkpsj（订单可排数量）<= 0 已排完 → 浅蓝背景 */
.tables-wrap .left-panel :deep(.el-table .bom-done-cell) {
  background-color: #cfe3ff !important;
}

/* 右侧明细表：品号与中间表当前选中行相同的行 → 浅蓝背景 */
.tables-wrap .right-panel :deep(.el-table .right-hit-row > td.el-table__cell) {
  background-color: #cfe3ff !important;
}

/* 中间 BOM 表：订单号+订单序号 与右表选中行相同的行 → 浅蓝背景 */
.tables-wrap .left-panel :deep(.el-table .bom-hit-row > td.el-table__cell) {
  background-color: #cfe3ff !important;
}

/* 最左侧未出货订单表：wpsl <= 0 的行 → 浅蓝背景 */
.tables-wrap .unfinished-panel :deep(.el-table .wpsl-zero-row > td.el-table__cell) {
  background-color: #cfe3ff !important;
}

/* 右侧明细表：订单可排数量为 0 的行 → 字体颜色变淡 */
.tables-wrap .right-panel :deep(.el-table .right-zero-row > td.el-table__cell) {
  color: #a8a8a8 !important;
}
/* 右侧明细表：未排完（订单可排数量 > 0）的行 → 字体加深 */
.tables-wrap .right-panel :deep(.el-table .right-active-row > td.el-table__cell) {
  color: #000000 !important;
  font-weight: 600;
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

/* 右表工单号下拉：选项右侧的工单数量 */
.wo-opt-qty {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}

/* 右表工单号下拉：选项右侧的工单状态码（中文） */
.wo-opt-status {
  margin-left: 10px;
  color: #409eff;
  font-size: 12px;
}

/* 右表工单号下拉：加宽弹层以容纳多列；各列样式 */
.wo-popper {
  min-width: 940px !important;
}
.wo-opt-docno {
  font-weight: 600;
  color: #303133;
}
.wo-opt-start,
.wo-opt-end {
  margin-left: 10px;
  color: #606266;
  font-size: 12px;
}
.wo-opt-srcno,
.wo-opt-srcseq,
.wo-opt-orderqty {
  margin-left: 10px;
  color: #606266;
  font-size: 12px;
}
.wo-opt-flag {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}
.wo-opt-flag.is-default {
  color: #67c23a;
  font-weight: 600;
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
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
}

/* 四个操作按钮成组并居中 */
.form-btn-row .op-btns {
  grid-column: 2;
  display: inline-flex;
  align-items: center;
  justify-self: center;
}

/* 单头工单绑定 JSON 文本框 */
.form-btn-row .wo-bind-json {
  grid-column: 3;
  justify-self: end;
  min-width: 0;
}
.form-btn-row .wo-bind-json :deep(.el-textarea__inner) {
  font-size: 12px;
  line-height: 1.4;
}

/* 必填项粉色背景 */
.required-field :deep(.el-input__wrapper) {
  background-color: #ffc0cb;
  box-shadow: 0 0 0 1px #ff69b4 inset;
}

/* 右键菜单 */
.ctx-menu {
  position: fixed;
  z-index: 3000;
  min-width: 170px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.12);
  padding: 4px 0;
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

<!-- 补货策略等下拉框弹出层会被 teleport 到 body，scoped 样式无法命中，需全局规则 -->
<style>
.el-select-dropdown__item {
  font-size: 12px;
}
</style>
