<template>
  <n-config-provider :theme-overrides="themeOverrides">
    <n-dialog-provider>
      <n-message-provider>
        <n-notification-provider>
          <router-view />
        </n-notification-provider>
      </n-message-provider>
    </n-dialog-provider>
  </n-config-provider>
</template>

<script setup lang="ts">
import {
  type GlobalThemeOverrides,
  NConfigProvider,
  NDialogProvider,
  NMessageProvider,
  NNotificationProvider
} from 'naive-ui'

const themeOverrides: GlobalThemeOverrides = {
  common: {
    primaryColor: '#2563EB',
    primaryColorHover: '#1D4ED8',
    primaryColorPressed: '#1E40AF',
    primaryColorSuppl: '#3B82F6',
    borderRadius: '8px',
    borderRadiusSmall: '6px',
    fontWeightStrong: '700'
  },
  Button: {
    borderRadiusMedium: '8px',
    borderRadiusLarge: '8px',
    textColorHoverPrimary: '#FFFFFF'
  },
  Input: {
    borderRadius: '8px'
  },
  Select: {
    peers: {
      InternalSelection: {
        borderRadius: '8px'
      }
    }
  },
  Card: {
    borderRadius: '8px'
  }
}
</script>
