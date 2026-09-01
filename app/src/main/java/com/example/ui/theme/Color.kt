package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// High-contrast Light Theme Colors (WCAG AA & AAA compliant)
val BluePrimary = Color(0xFF026AA7) // High contrast rich ocean blue
val BlueOnPrimary = Color(0xFFFFFFFF)
val BluePrimaryContainer = Color(0xFFE0F2FE) // Sky-100
val BlueOnPrimaryContainer = Color(0xFF034164) // Deep navy (high contrast on container)

val SlateSecondary = Color(0xFF334155) // Slate-700
val SlateOnSecondary = Color(0xFFFFFFFF)
val SlateSecondaryContainer = Color(0xFFE2E8F0) // Slate-200
val SlateOnSecondaryContainer = Color(0xFF0F172A) // Slate-900

val SlateTertiary = Color(0xFF0F172A)
val SlateOnTertiary = Color(0xFFFFFFFF)

val ArtisticBackground = Color(0xFFF8FAFC) // Slate-50 clean background
val ArtisticOnBackground = Color(0xFF0F172A) // Slate-900 ultra-crisp dark text
val ArtisticSurface = Color(0xFFFFFFFF) // Pure white card surface
val ArtisticOnSurface = Color(0xFF0F172A) // Slate-900 high contrast text on surface
val ArtisticSurfaceVariant = Color(0xFFF1F5F9) // Slate-100 soft container
val ArtisticOnSurfaceVariant = Color(0xFF334155) // Slate-700 readable secondary text
val ArtisticOutline = Color(0xFF94A3B8) // Slate-400 clear borders
val ArtisticOutlineVariant = Color(0xFFE2E8F0) // Slate-200 soft dividers

// High-contrast Dark Theme Colors (WCAG AA & AAA compliant)
val DarkPrimary = Color(0xFF38BDF8) // Sky-400 vivid bright blue for dark surfaces
val DarkOnPrimary = Color(0xFF041E3A) // Very dark navy text on bright blue button
val DarkPrimaryContainer = Color(0xFF0C4A6E) // Sky-900 container
val DarkOnPrimaryContainer = Color(0xFFE0F2FE) // Sky-100 high contrast text on container

val DarkSecondary = Color(0xFF94A3B8) // Slate-400
val DarkOnSecondary = Color(0xFF0F172A) // Slate-900
val DarkSecondaryContainer = Color(0xFF1E293B) // Slate-800
val DarkOnSecondaryContainer = Color(0xFFF1F5F9) // Slate-100

val DarkBackground = Color(0xFF090D16) // Deep dark canvas
val DarkOnBackground = Color(0xFFF8FAFC) // Crisp white-slate text
val DarkSurface = Color(0xFF131B2E) // Elevated card surface
val DarkOnSurface = Color(0xFFF8FAFC) // Pure crisp readable text on dark surface
val DarkSurfaceVariant = Color(0xFF1E293B) // Slate-800 distinguishable container
val DarkOnSurfaceVariant = Color(0xFFCBD5E1) // Slate-300 high contrast secondary text
val DarkOutline = Color(0xFF475569) // Slate-600
val DarkOutlineVariant = Color(0xFF334155) // Slate-700

// Status Colors with Light & Dark contrast
val StatusDraftLight = Color(0xFF475569)
val StatusDraftDark = Color(0xFF94A3B8)
val StatusSentLight = Color(0xFF0284C7)
val StatusSentDark = Color(0xFF38BDF8)
val StatusAcceptedLight = Color(0xFF16A34A)
val StatusAcceptedDark = Color(0xFF4ADE80)
val StatusRejectedLight = Color(0xFFDC2626)
val StatusRejectedDark = Color(0xFFF87171)

// Backward compatibility references
val StatusDraft = StatusDraftLight
val StatusSent = StatusSentLight
val StatusAccepted = StatusAcceptedLight
val StatusRejected = StatusRejectedLight

