import { createRouter, createWebHistory } from 'vue-router'
import { getSessionStatus, type PortalRole } from '@/api/session'
import NepgTaskSuccessView from '../views/nepg/NepgTaskSuccessView.vue'

declare module 'vue-router' {
  interface RouteMeta { requiredRole?: PortalRole }
}
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'systemEntry',
      component: () => import('../views/SystemEntry.vue'),
    },
    {
      path: '/neps/login',
      name: 'nepsLogin',
      component: () => import('../views/EmployeeLogin.vue'),
      props: { portal: 'neps' },
    },
    {
      path: '/neps/register',
      name: 'nepsRegister',
      component: () => import('../views/EmployeeLogin.vue'),
      props: { portal: 'neps' },
    },
    {
      path: '/nepg/login',
      name: 'nepgLogin',
      component: () => import('../views/EmployeeLogin.vue'),
      props: { portal: 'nepg' },
    },
    {
      path: '/nepm/login',
      name: 'nepmLogin',
      component: () => import('../views/EmployeeLogin.vue'),
      props: { portal: 'nepm' },
    },
    {
      path: '/nepv/login',
      name: 'nepvLogin',
      component: () => import('../views/EmployeeLogin.vue'),
      props: { portal: 'nepv' },
    },
    {
      path: '/nepg/portal',
      name: 'nepgPortal',
      redirect: '/nepg/tasks',
    },
    {
      path: '/nepg/tasks',
      name: 'nepgTasks',
      component: () => import('../views/nepg/NepgTaskListView.vue'),
      meta: { requiredRole: 'NEPG_GRID_MEMBER' },
    },
    {
      path: '/nepg/tasks/:afId/success',
      name: 'nepgTaskSuccess',
      component: NepgTaskSuccessView,
      props: true,
      meta: { requiredRole: 'NEPG_GRID_MEMBER' },
    },
    {
      path: '/nepg/tasks/:afId',
      name: 'nepgTaskDetail',
      component: () => import('../views/nepg/NepgTaskDetailView.vue'),
      props: true,
      meta: { requiredRole: 'NEPG_GRID_MEMBER' },
    },
    {
      path: '/nepm/portal',
      name: 'nepmPortal',
      component: () => import('../views/NepmWorkbench.vue'),
      meta: { requiredRole: 'NEPM_ADMIN' },
    },
    {
      path: '/nepv/portal',
      name: 'nepvPortal',
      component: () => import('../views/EmployeePortal.vue'),
      props: { portal: 'nepv' },
      meta: { requiredRole: 'NEPV_DECISION_MAKER' },
    },
    {
      path: '/aqiFeedback',
      name: 'aqiFeedback',
      component: () => import('../views/AqiFeedBackList.vue'),
      meta: { requiredRole: 'NEPS_SUPERVISOR' },
    },
  ],
})

router.beforeEach(async (to) => {
  if (!to.meta.requiredRole) return true
  try {
    const response = await getSessionStatus()
    if (response.data.data.portal === to.meta.requiredRole) return true
  } catch {
    // Missing or expired sessions are redirected below.
  }
  const loginByRole: Record<PortalRole, string> = {
    NEPS_SUPERVISOR: '/neps/login',
    NEPG_GRID_MEMBER: '/nepg/login',
    NEPM_ADMIN: '/nepm/login',
    NEPV_DECISION_MAKER: '/nepv/login',
  }
  return { path: loginByRole[to.meta.requiredRole] }
})

export default router
