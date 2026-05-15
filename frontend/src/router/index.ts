import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  scrollBehavior() {
    return { top: 0 }
  },
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/auth/LoginView.vue'),
      meta: { requiresGuest: true }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/auth/RegisterView.vue'),
      meta: { requiresGuest: true }
    },
    {
      path: '/',
      component: () => import('@/layouts/MainLayout.vue'),
      children: [
        {
          path: '',
          name: 'post-list',
          component: () => import('@/views/post/PostListView.vue')
        },
        {
          path: 'posts/create',
          name: 'post-create',
          component: () => import('@/views/post/PostEditView.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'posts/:id',
          name: 'post-detail',
          component: () => import('@/views/post/PostDetailView.vue')
        },
        {
          path: 'posts/:id/edit',
          name: 'post-edit',
          component: () => import('@/views/post/PostEditView.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'me',
          name: 'my-profile',
          component: () => import('@/views/profile/MyProfileView.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'profile/edit',
          name: 'edit-profile',
          component: () => import('@/views/profile/EditProfileView.vue'),
          meta: { requiresAuth: true }
        },
        {
          path: 'users/:id',
          name: 'user-home',
          component: () => import('@/views/profile/UserHomeView.vue')
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/views/not-found/NotFoundView.vue')
    }
  ]
})

export default router
