import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import type { RouteLocationNormalized } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: { requiresAuth: false },
    },
    {
      path: '/',
      name: 'home',
      component: () => import('../views/HomeView.vue'),
      meta: { requiresAuth: true },
      redirect: '/laboratory',
      children: [
        {
          path: '/laboratory',
          name: 'laboratory',
          component: () => import('../views/LaboratoryListView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: '/equipment',
          name: 'equipment',
          component: () => import('../views/EquipmentListView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: '/borrow/apply',
          name: 'borrowApply',
          component: () => import('../views/BorrowApplyView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: '/borrow/my',
          name: 'borrowMy',
          component: () => import('../views/BorrowMyListView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: '/borrow/approval',
          name: 'borrowApproval',
          component: () => import('../views/BorrowApprovalView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: '/reservation/apply',
          name: 'reservationApply',
          component: () => import('../views/ReservationApplyView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: '/reservation/my',
          name: 'reservationMy',
          component: () => import('../views/ReservationMyListView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: '/reservation/approval',
          name: 'reservationApproval',
          component: () => import('../views/ReservationApprovalView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: '/repair/apply',
          name: 'repairApply',
          component: () => import('../views/RepairApplyView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: '/repair/my',
          name: 'repairMy',
          component: () => import('../views/RepairMyListView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: '/repair/management',
          name: 'repairManagement',
          component: () => import('../views/RepairManagementView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: '/scrap/apply',
          name: 'scrapApply',
          component: () => import('../views/ScrapApplyView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: '/scrap/my',
          name: 'scrapMy',
          component: () => import('../views/ScrapMyListView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: '/scrap/approval',
          name: 'scrapApproval',
          component: () => import('../views/ScrapApprovalView.vue'),
          meta: { requiresAuth: true },
        },
      ],
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/AboutView.vue'),
      meta: { requiresAuth: true },
    },
  ],
})

// 路由守卫
router.beforeEach((to: RouteLocationNormalized, from, next) => {
  const userStore = useUserStore()

  // 如果路由需要认证
  if (to.meta.requiresAuth) {
    // 检查是否已登录
    if (userStore.isLoggedIn()) {
      next()
    } else {
      // 未登录，跳转到登录页
      next({ name: 'login', query: { redirect: to.fullPath } })
    }
  } else {
    // 如果访问登录页且已登录，跳转到首页
    if (to.name === 'login' && userStore.isLoggedIn()) {
      next({ name: 'home' })
    } else {
      next()
    }
  }
})

export default router
