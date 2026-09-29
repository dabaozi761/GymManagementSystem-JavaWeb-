<script setup lang="ts">
import { Activity, ArrowDownToLine, ArrowLeft, ArrowRight, CalendarDays, Check, ChevronDown, ChevronLeft, ChevronRight, CircleAlert, ClipboardList, CreditCard, Dumbbell, Eye, EyeOff, LayoutDashboard, Leaf, LoaderCircle, LogOut, Menu, Pencil, Plus, RefreshCw, Search, ShieldCheck, SlidersHorizontal, Target, Trash2, UserRound, UsersRound, X } from 'lucide-vue-next'
defineProps<{ name: string; size?: number }>()
const icons: Record<string, any> = { activity: Activity, download: ArrowDownToLine, back: ArrowLeft, arrow: ArrowRight, calendar: CalendarDays, check: Check, down: ChevronDown, left: ChevronLeft, right: ChevronRight, alert: CircleAlert, clipboard: ClipboardList, card: CreditCard, dumbbell: Dumbbell, eye: Eye, eyeOff: EyeOff, layout: LayoutDashboard, leaf: Leaf, loading: LoaderCircle, logout: LogOut, menu: Menu, edit: Pencil, plus: Plus, refresh: RefreshCw, search: Search, shield: ShieldCheck, filter: SlidersHorizontal, target: Target, trash: Trash2, user: UserRound, users: UsersRound, close: X }
</script>
<template><component :is="icons[name] || icons.activity" :size="size || 20" :stroke-width="1.7" aria-hidden="true" /></template>
