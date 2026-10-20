<template>
  <div class="main-layout">
    <!-- 上：标题栏 -->
    <header class="layout-header">
      <button class="sidebar-toggle" type="button" :title="sidebarCollapsed ? '展开菜单' : '隐藏菜单'" @click="toggleSidebar">
        <svg v-if="!sidebarCollapsed" viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
          <path d="M3 6h18v2H3zM3 11h18v2H3zM3 16h18v2H3z" />
        </svg>
        <svg v-else viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
          <path d="M3 6h18v2H3zM3 11h18v2H3zM3 16h18v2H3z" />
          <path d="M15 6l-6 6 6 6z" />
        </svg>
      </button>
      <div class="header-title">亿林信息管理系统</div>
      <div class="header-right">
        <span v-if="deptInfo && (deptInfo.userName || deptInfo.deptName)" class="dept-name">
          <span v-if="deptInfo.userName" class="user-name">{{ deptInfo.userName }}</span>
          <span v-if="deptInfo.userName" class="sep"></span>
          <span v-if="deptInfo.deptName">{{ deptInfo.deptNo }} : {{ deptInfo.deptName }}</span>
        </span>
        <el-button class="logout-btn" size="small" @click="handleLogout">退出登录</el-button>
      </div>
    </header>

    <div class="layout-body">
      <!-- 左下：总菜单 -->
      <aside class="layout-sidebar" :class="{ collapsed: sidebarCollapsed }">
        <el-menu
          :default-active="activeMenu"
          :default-openeds="defaultOpenedSubMenus"
          class="main-menu"
          @select="handleMenuSelect"
        >
          <!-- 递归渲染：分类(有 children)为 el-sub-menu，叶子为 el-menu-item，支持任意层级 -->
          <MenuNodes :nodes="visibleMenu" />
        </el-menu>
      </aside>

      <!-- 下右：内容区 -->
      <main class="layout-content" :class="{ 'content-fullbleed': activeMenu === MODULE.SHOP_SCHEDULE }">
        <component :is="currentComponent" />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, h, resolveComponent } from 'vue'
import { canViewModule, MODULE } from '../permission.js'
import MainSchedule from './MainSchedule.vue'
import ShopSchedule from './ShopSchedule.vue'
import DeliveryStatus from './DeliveryStatus.vue'
import BudgetEntry from './BudgetEntry.vue'
import BudgetReport from './BudgetReport.vue'
import BudgetPurchasePricing from './BudgetPurchasePricing.vue'
import BudgetPurchasePricingReport from './BudgetPurchasePricingReport.vue'

/** 侧边栏折叠状态 */
const sidebarCollapsed = ref(false)
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

const props = defineProps({
  deptInfo: { type: Object, default: null },
  /** 后端 gzweuc_t 菜单树：[{gzweuc001, gzweuc002, gzweuc003, gzweuc004, children}]
   *  gzweuc001 上级编号，gzweuc002 自身编号（=MODULE.* 值），gzweuc003 顺序，gzweuc004 标题 */
  menuTree: { type: Array, default: () => [] }
})

const emit = defineEmits(['logout'])

const handleLogout = () => {
  emit('logout')
}

/** 模块编号 → Vue 组件映射：key 必须与 permission.js 的 MODULE 值一致（即 gzweuc002）。
 *  已注册的模块点击后渲染对应组件；未注册的（诸如模块显示为占位页） */
const componentsMap = {
  [MODULE.MAIN_SCHEDULE]: MainSchedule,
  [MODULE.SHOP_SCHEDULE]: ShopSchedule,
  [MODULE.DELIVERY]: DeliveryStatus,
  [MODULE.BUDGET_ENTRY]: BudgetEntry,
  [MODULE.BUDGET_REPORT]: BudgetReport,
  [MODULE.BUDGET_PURCHASE_PRICING]: BudgetPurchasePricing,
  [MODULE.BUDGET_PURCHASE_PRICING_REPORT]: BudgetPurchasePricingReport
}
const placeholderPage = { template: '<div class="placeholder-page">该模块暂未实现</div>' }

/** 递归渲染菜单节点（函数式组件）：
 *  有 children → el-sub-menu（title 为菜单名，default 递归渲染子级）
 *  无 children → el-menu-item
 *  支持任意层级，之前模板只平铺一层导致第三级菜单丢失 */
const MenuNodes = (props) => {
  const render = (nodes) =>
    (nodes || []).map(n =>
      n.children && n.children.length
        ? h(resolveComponent('el-sub-menu'), { key: n.gzweuc002, index: n.gzweuc002 }, {
            title: () => n.gzweuc004,
            default: () => render(n.children)
          })
        : h(resolveComponent('el-menu-item'), { key: n.gzweuc002, index: n.gzweuc002 }, () => n.gzweuc004)
    )
  return render(props.nodes)
}
MenuNodes.props = ['nodes']

/** 递归过滤：分类节点本身不查权限；叶子节点必须 canViewModule；空分类自动隐藏 */
const filterByPermission = (nodes) => {
  const out = []
  for (const n of nodes || []) {
    if (n.children && n.children.length) {
      const kids = filterByPermission(n.children)
      if (kids.length) out.push({ ...n, children: kids })
    } else {
      if (canViewModule(n.gzweuc002)) out.push(n)
    }
  }
  // 兜底按显示顺序排
  return out.sort((a, b) => Number(a.gzweuc003 || 0) - Number(b.gzweuc003 || 0))
}
const visibleMenu = computed(() => filterByPermission(props.menuTree))

/** 递归收集所有分类节点编号（含嵌套层级），默认全部展开 */
const collectCategoryIds = (nodes, acc = []) => {
  for (const n of nodes || []) {
    if (n.children && n.children.length) {
      acc.push(n.gzweuc002)
      collectCategoryIds(n.children, acc)
    }
  }
  return acc
}
const defaultOpenedSubMenus = computed(() => collectCategoryIds(visibleMenu.value))

/** 取第一个可见叶子节点：用于默认激活项与权限变化时的回退 */
const firstLeaf = (nodes) => {
  for (const n of nodes || []) {
    if (!n.children || !n.children.length) return n
    const inner = firstLeaf(n.children)
    if (inner) return inner
  }
  return null
}
const defaultLeafId = computed(() => firstLeaf(visibleMenu.value)?.gzweuc002 || '')

const activeMenu = ref('')
/** 菜单/权限变化时，若当前激活项不再可见则回退到第一个可见叶子 */
watch(visibleMenu, (list) => {
  if (!list.length) { activeMenu.value = ''; return }
  const found = (nodes) => nodes.some(n =>
    (!n.children || !n.children.length)
      ? n.gzweuc002 === activeMenu.value
      : found(n.children)
  )
  if (!found(list)) activeMenu.value = defaultLeafId.value
}, { immediate: true })

const currentComponent = computed(() => componentsMap[activeMenu.value] || placeholderPage)

const handleMenuSelect = (index) => {
  activeMenu.value = index
}
</script>

<style scoped>
.main-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.layout-header {
  height: 56px;
  background: #1890ff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 24px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
  position: relative;
}

.header-title {
  font-size: 20px;
  font-weight: bold;
}

.sidebar-toggle {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 4px;
  background: transparent;
  color: #fff;
  cursor: pointer;
  transition: background-color 0.2s;
}
.sidebar-toggle:hover {
  background: rgba(255, 255, 255, 0.2);
}

.header-right {
  position: absolute;
  right: 24px;
  display: flex;
  align-items: center;
  font-size: 14px;
}

.dept-name {
  background: rgba(255, 255, 255, 0.18);
  padding: 4px 14px;
  border-radius: 14px;
  letter-spacing: 1px;
  margin-right: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-name {
  font-weight: bold;
}

.sep {
  width: 1px;
  height: 14px;
  background: rgba(255, 255, 255, 0.45);
}

.logout-btn {
  color: #fff;
  background: transparent;
  border-color: rgba(255, 255, 255, 0.6);
}
.logout-btn:hover {
  color: #1890ff;
  background: #fff;
  border-color: #fff;
}

.layout-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.layout-sidebar {
  width: 200px;
  background: #f5f7fa;
  border-right: 1px solid #e4e7ed;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: width 0.25s ease;
}

.layout-sidebar.collapsed {
  width: 0;
  border-right: none;
}

.menu-title {
  padding: 16px;
  font-size: 14px;
  font-weight: bold;
  color: #606266;
  border-bottom: 1px solid #e4e7ed;
}

.main-menu {
  border-right: none;
  background: transparent;
  /* 覆盖 Element Plus 菜单字体（内部 .el-menu-item/.el-sub-menu__title 使用该变量） */
  --el-menu-font-size: 11px;
  font-size: 11px;
}
.main-menu :deep(.el-menu-item),
.main-menu :deep(.el-sub-menu__title) {
  font-size: 11px;
}
/* 鼠标悬停 / 已选中菜单行：背景改为蓝色 */
.main-menu :deep(.el-menu-item:hover),
.main-menu :deep(.el-menu-item.is-active) {
  background-color: #409eff;
  color: #fff;
}

.layout-content {
  flex: 1;
  padding: 16px;
  background: #fff;
  overflow: auto;
}

/* 车间计划排程：内容区无内边距，页面自行控制（右侧紧贴浏览器边框） */
.layout-content.content-fullbleed {
  padding: 0;
  overflow: hidden;
}

.placeholder-page {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  font-size: 18px;
  color: #909399;
}
</style>
