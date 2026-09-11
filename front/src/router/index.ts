import { createRouter, createWebHistory } from 'vue-router'
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
      component: () => import('../views/EmployeePortal.vue'),
      props: { portal: 'nepg' },
    },
    {
      path: '/nepm/portal',
      name: 'nepmPortal',
      component: () => import('../views/NepmWorkbench.vue'),
    },
    {
      path: '/nepv/portal',
      name: 'nepvPortal',
      component: () => import('../views/EmployeePortal.vue'),
      props: { portal: 'nepv' },
    },
    {
      path: '/aqiFeedback',
      name: 'aqiFeedback',
      component: () => import('../views/AqiFeedBackList.vue'),
    },
  ],
})

export default router
