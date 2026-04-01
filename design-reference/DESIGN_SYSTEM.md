# T-Mark Design System Reference

This folder contains the complete design language for the T-Mark mobile app. Open `visual-reference.html` in a browser for the full interactive reference.

---

## Brand Assets

```
design-reference/
├── DESIGN_SYSTEM.md          # This file
├── visual-reference.html     # Interactive design system (open in browser)
├── app-icon/
│   ├── appicon.png           # App icon (transparent background)
│   ├── appicon-on-black.png  # App icon on dark background (used on login screen)
│   ├── logo.svg              # Vector logo
│   └── playstore-icon.png    # Play Store listing icon (512x512)
└── logo/
    ├── logo-on-black.png     # Full logo on dark background
    └── logo-on-white.png     # Full logo on light background
```

---

## Color Palette

### Primary
| Name | Hex | Compose | Usage |
|------|-----|---------|-------|
| **T-Mark Red** | `#D42B1E` | `TMarkRed` | Primary CTA, accents, active states |
| **Red Light** | `#F03528` | `TMarkRedDark` | Hover/press states |
| **Red Dim** | `rgba(212,43,30,0.08)` | — | Subtle red backgrounds |

### Neutrals
| Name | Hex | Compose | Usage |
|------|-----|---------|-------|
| **Black** | `#0A0908` | `TMarkBlack` | Headers, dark sections, primary text |
| **Black 2** | `#141310` | — | Elevated dark surfaces |
| **Black 3** | `#1E1C1A` | — | Floating dark surfaces |
| **Text** | `#111110` | — | Body text on light |
| **Text Muted** | `#888582` | `TMarkMuted` | Secondary text, labels, placeholders |
| **Off-White** | `#F5F4F2` | `TMarkOffWhite` | Page background |
| **Light Gray** | `#EFEFED` | — | Tag/chip backgrounds |
| **Border** | `#D8D6D2` | `TMarkBorder` | Borders, dividers |
| **Surface** | `#FFFFFF` | `TMarkSurface` | Card/section backgrounds |

### Status Colors
| Name | Hex | Compose | Usage |
|------|-----|---------|-------|
| Pending | `#F59E0B` | `StatusPending` | Amber |
| Confirmed | `#10B981` | `StatusConfirmed` | Green |
| Declined | `#EF4444` | `StatusDeclined` | Red |
| Converted | `#6366F1` | `StatusConverted` | Indigo |
| Paid | `#10B981` | `StatusPaid` | Green |
| Overdue | `#EF4444` | `StatusOverdue` | Red |
| Partial | `#F59E0B` | `StatusPartial` | Amber |
| Sent | `#3B82F6` | `StatusSent` | Blue |
| Draft | `#9CA3AF` | `StatusDraft` | Gray |

---

## Typography

### Font Families

| Font | Weight | Role | Android Compose |
|------|--------|------|----------------|
| **Bebas Neue** | Regular | Display, headings, prices | `BebasNeue` |
| **Barlow Condensed** | 300-700 | Labels, eyebrows, navigation, buttons | `BarlowCondensed` |
| **Barlow** | 300-600 | Body text, descriptions | `Barlow` |

### Type Scale (Compose)

| Role | Font | Size | Weight | Letter Spacing | Example |
|------|------|------|--------|---------------|---------|
| Hero | Bebas Neue | 64-120sp | — | 0.01em | Page titles |
| Section Title | Bebas Neue | 32-42sp | — | — | Section headings |
| Card Title | Bebas Neue | 22-26sp | — | — | Card names, sub-headings |
| Price | Bebas Neue | 22-42sp | — | — | ৳15,000 |
| Eyebrow / Label | Barlow Condensed | 9-11sp | SemiBold | 0.2-0.4em | BROWSE CATALOG, STATUS |
| Nav / Tab | Barlow Condensed | 10-13sp | Medium | 0.2em | HOME, BROWSE, ORDERS |
| Button Text | Barlow Condensed | 11-13sp | SemiBold | 0.2-0.25em | ADD TO BASKET |
| Body | Barlow | 13-16sp | 300-400 | — | Descriptions, content |
| Small / Meta | Barlow | 11-12sp | 300 | — | Timestamps, captions |

### Typography Rules
- Headings: Always `UPPERCASE` for Bebas Neue
- Labels/Eyebrows: Always `UPPERCASE` with wide letter-spacing (0.2-0.4em)
- Body text: Sentence case, font-weight 300 (light), line-height 1.7-1.8
- Prices: Bebas Neue, `"৳${"%,.0f".format(amount)}"`
- Never use the same font for headings and body

---

## Spacing System

| Token | Value | Usage |
|-------|-------|-------|
| `xs` | 4dp | Tight gaps between related elements |
| `sm` | 8dp | Small padding, icon gaps |
| `md` | 12dp | Default vertical spacing |
| `lg` | 16dp | Section padding, card padding |
| `xl` | 20dp | Horizontal page padding |
| `2xl` | 24dp | Section gaps |
| `3xl` | 32dp | Major section separators |

### Key spacing patterns
- **Page horizontal padding:** 20dp
- **Card internal padding:** 16dp
- **Section vertical gap:** 20dp
- **List item vertical padding:** 12-14dp
- **Bottom tab bar safe area:** `navigationBarsPadding()`
- **Status bar safe area:** `statusBarsPadding()`

---

## Component Patterns

### Dark Headers
- Background: `TMarkBlack` (#0A0908)
- Always use `statusBarsPadding()` at the top
- Embed back navigation directly into the dark section (no separate ScreenHeader + content band)
- Eyebrow label: Barlow Condensed 10sp, 0.28em spacing, `TMarkMuted`
- Title: Bebas Neue 24-32sp, white

### Red Accent Line
- Used before section labels: `Box(Modifier.width(3.dp).height(14.dp).background(TMarkRed))`
- Used in cards: 3-4px left border in red
- Used as top border on hover: 3px red bar

### Buttons
| Type | Style |
|------|-------|
| **Primary** | Red background (#D42B1E), white text, Barlow Condensed 13sp, uppercase, 0.25em spacing |
| **Outline Red** | Transparent, red border (40% opacity), red text, hover fills red |
| **Outline White** | Transparent, white border (20% opacity), white text (on dark bg) |
| **White Solid** | White background, red text (on red bg) |

### Cards
- White background on off-white page
- 1px border (`TMarkBorder`)
- On hover: subtle shadow + `translateY(-4px)` + red top bar reveal
- Code/label: Barlow Condensed 10sp, red, uppercase, 0.35em spacing
- Title: Bebas Neue 22sp
- Price: Bebas Neue 28sp

### Status Badges
- Outline style: 1px border matching status color, text in status color
- Font: Barlow Condensed 9sp, uppercase, 0.15em spacing
- Padding: 4-6dp horizontal, 2-3dp vertical

### Lists / Rows
- Dark background variant: `#141310` base, `#1E1C1A` on hover
- Left accent: 2px transparent border, turns red on hover
- Code: Barlow Condensed 11sp, red
- Name: Barlow 14sp, white at 80% opacity
- Price: Bebas Neue 22sp, white

### Bottom Tab Bar
- 5 tabs: HOME, BROWSE, REQUEST, ORDERS, PROFILE
- Active: Red dot + red text
- Inactive: `TMarkMuted` text
- Hidden on detail/form screens

---

## Section Layout Pattern

Most pages follow this structure:

```
┌─────────────────────────────────┐
│  Dark Header (TMarkBlack)       │  statusBarsPadding()
│  ← Back   EYEBROW LABEL        │  Barlow Condensed 10sp
│  Page Title                     │  Bebas Neue 24-32sp
│  [Optional subtitle/avatar]     │
├─────────────────────────────────┤
│  Content Area (TMarkOffWhite)   │
│                                 │
│  ┌──── SECTION LABEL ────┐      │  Red accent + Barlow Condensed 10sp
│  │ Content cards/lists   │      │  White cards with border
│  └───────────────────────┘      │
│                                 │
│  ┌──── SECTION LABEL ────┐      │
│  │ More content          │      │
│  └───────────────────────┘      │
│                                 │
├─────────────────────────────────┤
│  Bottom Tab Bar                 │  navigationBarsPadding()
└─────────────────────────────────┘
```

### Surface Depth System
| Level | Background | Usage |
|-------|-----------|-------|
| Base | `TMarkOffWhite` (#F5F4F2) | Page background |
| Elevated | `White` (#FFFFFF) | Cards, sections |
| Floating | White + shadow | Modals, bottom sheets, sticky CTA |

---

## Motion & Animation

### Allowed properties
- Only animate `transform` and `opacity`
- Never use `transition-all`

### Easing curves
- **Default:** `cubic-bezier(0.16, 1, 0.3, 1)` — spring-like
- **Duration:** 250-350ms for UI transitions, 600-800ms for reveals

### Interactive states
Every clickable element must have:
- Hover state (desktop/web)
- Focus-visible state
- Active/pressed state
- Disabled state (when applicable)

---

## Background Composition

Pages use a mix of surface types:

| Section | Background | Approx. Usage |
|---------|-----------|---------------|
| Dark header | `#0A0908` | 15-20% |
| Off-white body | `#F5F4F2` | 50-60% |
| White cards | `#FFFFFF` | 25-30% |
| Red accent CTA | `#D42B1E` | 5% |

### Dark sections
- Use subtle radial gradient glow: `radial-gradient(ellipse at 70% 50%, rgba(212,43,30,0.07), transparent)`
- Adds depth without being distracting

---

## Image Treatment

When displaying images:
- Add gradient overlay: `bg-gradient-to-t from-black/60`
- Add color treatment layer with `mix-blend-multiply`
- Use Coil for loading with crossfade
- Placeholder: gray gradient matching card visual

---

## Currency & Localization

- **Currency:** Bangladeshi Taka (৳)
- **Format:** `৳15,000` — Bebas Neue, comma-separated, no decimal
- **"per day":** Barlow 11sp, muted color, below the price
- **Phone format:** `01XXXXXXXXX` (Bangladeshi mobile)
