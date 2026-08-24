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
        <div class="menu-title">导航菜单</div>
        <el-menu
          :default-active="activeMenu"
          :default-openeds="['schedule', 'finance']"
          class="main-menu"
          @select="handleMenuSelect"
        >
          <el-sub-menu index="schedule">
            <template #title>计划排程</template>
            <el-menu-item index="main-schedule">主计划排程</el-menu-item>
            <el-menu-item index="shop-schedule">车间计划排程</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="attendance">人事考勤</el-menu-item>
          <el-menu-item index="delivery">供应商送货待收货</el-menu-item>
          <el-sub-menu index="finance">
            <template #title>财务核算</template>
            <el-menu-item index="budget-entry">预算录入</el-menu-item>
            <el-menu-item index="budget-report">预算报表</el-menu-item>
            <el-menu-item index="budget-purchase-pricing">预算采购核价录入</el-menu-item>
            <el-menu-item index="budget-purchase-pricing-report">预算采购核价分析报表</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </aside>

      <!-- 下右：内容区 -->
      <main class="layout-content" :class="{ 'content-fullbleed': activeMenu === 'shop-schedule' }">
        <component :is="currentComponent" />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

/** 侧边栏折叠状态 */
const sidebarCollapsed = ref(false)
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}
import MainSchedule from './MainSchedule.vue'
import ShopSchedule from './ShopSchedule.vue'
import DeliveryStatus from './DeliveryStatus.vue'
import BudgetEntry from './BudgetEntry.vue'
import BudgetReport from './BudgetReport.vue'
import BudgetPurchasePricing from './BudgetPurchasePricing.vue'
import BudgetPurchasePricingReport from './BudgetPurchasePricingReport.vue'

defineProps({
  deptInfo: { type: Object, default: null }
})

const emit = defineEmits(['logout'])

const handleLogout = () => {
  emit('logout')
}

const componentsMap = {
  'main-schedule': MainSchedule,
  'shop-schedule': ShopSchedule,
  attendance: { template: '<div class="placeholder-page">人事考勤（待开发）</div>' },
  delivery: DeliveryStatus,
  'budget-entry': BudgetEntry,
  'budget-report': BudgetReport,
  'budget-purchase-pricing': BudgetPurchasePricing,
  'budget-purchase-pricing-report': BudgetPurchasePricingReport
}

const activeMenu = ref('main-schedule')
const currentComponent = computed(() => componentsMap[activeMenu.value])

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
