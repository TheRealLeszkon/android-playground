# Design System Document: The Kinetic Minimalist

## 1. Overview & Creative North Star
The "Kinetic Minimalist" is a high-end editorial interpretation of Material Design 3. While M3 provides the logic, this system provides the soul. Our Creative North Star is **"The Digital Gallery"**—an approach that treats mobile real estate as a curated exhibition space. 

We move beyond the "template" look by rejecting rigid boxes and heavy outlines. Instead, we use intentional asymmetry, expansive negative space (breathing room), and high-contrast typography scales. The goal is to make the user feel they are interacting with a premium, bespoke digital object rather than a generic app.

---

## 2. Colors & Tonal Depth
We utilize a sophisticated palette where `#3CDA84` (Primary) acts as a high-energy pulse against a muted, multi-tonal grayscale foundation.

### The "No-Line" Rule
**Explicit Instruction:** Designers are prohibited from using 1px solid borders for sectioning or layout containment. Structural boundaries must be defined solely through background color shifts or subtle tonal transitions. For example, a `surface-container-low` section sitting on a `surface` background creates a sophisticated boundary that feels integrated, not "caged."

### Surface Hierarchy & Nesting
Treat the UI as a series of physical layers. Depth is created by stacking:
*   **Base:** `surface` (#f9f9f9)
*   **Primary Content Area:** `surface-container-lowest` (#ffffff) for maximum "pop."
*   **Secondary/Support Areas:** `surface-container` (#eeeeee).

### The "Glass & Gradient" Rule
To elevate the experience, use **Glassmorphism** for floating elements (e.g., bottom navigation or sticky headers). Use `surface` colors at 70% opacity with a `20px` backdrop-blur. 
*   **Signature Texture:** Main CTAs should not be flat. Apply a subtle linear gradient from `primary` (#006d3b) to `primary-container` (#3cda84) at a 135-degree angle to add a "liquid" professional polish.

---

## 3. Typography: Editorial Authority
We pair the geometric precision of **Manrope** for headers with the high-legibility of **Inter** for utility and body text.

*   **Display (Manrope):** Use `display-lg` (3.5rem) with `-0.04em` letter spacing. This creates an "Editorial Impact" that dominates the hero section.
*   **Headlines (Manrope):** `headline-lg` (2rem) should be used for section titles, often offset asymmetrically to the left to break the standard center-align grid.
*   **Body (Inter):** `body-lg` (1rem) is our workhorse. Ensure a line-height of `1.6` to maintain a premium, airy feel.
*   **Labels (Inter):** `label-md` (0.75rem) should be all-caps with `0.1em` tracking when used for category tags to provide a functional, "technical" contrast to the organic headers.

---

## 4. Elevation & Depth
Traditional drop shadows are too "heavy" for this system. We achieve hierarchy through **Tonal Layering**.

*   **The Layering Principle:** Place a `surface-container-lowest` card on a `surface-container-low` section. The contrast in hex values creates a soft "lift" naturally.
*   **Ambient Shadows:** If an element must float (like a FAB), use an "Ambient Shadow": `0px 12px 32px` with a 6% opacity of the `on-surface` color. It should feel like a soft glow, not a dark stain.
*   **The "Ghost Border" Fallback:** If accessibility requires a stroke, use a "Ghost Border": the `outline-variant` token (#bbcabc) at **15% opacity**. This provides a hint of a edge without cluttering the visual field.

---

## 5. Components

### Buttons (The M3 Signature)
*   **Primary:** High-pill shape (`rounded-full`). Background: Signature Gradient. Text: `on-primary` (#ffffff). No shadow; use a slight scale-up (1.02x) on hover/active states.
*   **Secondary:** `outline` variant but using the "Ghost Border" rule. Text: `primary` (#006d3b).
*   **Tertiary:** Pure text with `label-md` styling. No container.

### Input Fields
*   **Style:** Minimalist underline or soft-tonal block (using `surface-container-high`). 
*   **Interaction:** On focus, the label should animate to a `primary` color, and the bottom "Ghost Border" should transform into a 2px `primary` solid line.

### Cards & Lists
*   **Rule:** Forbid the use of divider lines. 
*   **Execution:** Use `spacing-8` (2rem) of vertical white space to separate list items. For cards, use a shift from `surface` to `surface-container-low` to define the clickable area.

### Featured Component: The "Hero Carousel Indicator"
Instead of standard dots, use expanding slugs. The active state is a `primary` colored pill; inactive states are `outline-variant` circles. This reinforces the "Kinetic" nature of the system.

---

## 6. Do's and Don'ts

### Do:
*   **Embrace Asymmetry:** Align your headline to the left and your body text to a slightly narrower column on the right.
*   **Use Generous Padding:** Use `spacing-12` (3rem) or `spacing-16` (4rem) for section padding to let the "Green" accents breathe.
*   **Color as Signal:** Use `#3CDA84` only for things that are interactive or essential.

### Don't:
*   **No "Box-in-Box" Design:** Avoid putting a card inside a container that also has a border. Use background color shifts instead.
*   **No Pure Black Shadows:** Never use `#000000` for shadows. Use a tinted `on-surface` or `on-secondary-container` value.
*   **No Standard M3 Borders:** Do not use the default 100% opaque `outline` token for cards; it creates a "cheap" UI feel. Use tonal shifts or Ghost Borders only.