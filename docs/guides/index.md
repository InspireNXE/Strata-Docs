# Guides

Each guide covers one part of Strata in depth: the rules, the defaults, the edge cases and working code. Every Java example on these pages is compiled and tested against Strata by this site's build, so it matches the current API.

| Guide | Read it to learn |
|---|---|
| [Components](components.md) | What every component has: the tree, parents and ancestors, drawing order and z index, state flags, focus, cursors, tooltips, bounds |
| [Layout](layout.md) | How sizes and positions are decided, margins and padding, every container, the spacer, and scrolling |
| [Controls](controls.md) | What each control does, key by key: labels and images, buttons, checkboxes, sliders, text fields, lists, tables, trees, drop-downs, menus, pickers, windows, message boxes, tabs, progress bars and separators |
| [Appearances](appearances.md) | How values of one type show their icon, name and tooltip in every list, table and combo box |
| [Overlays and tooltips](overlays.md) | Popups, modal dialogs, toasts, how overlays are layered, tooltips and held modifiers |
| [Events](events.md) | Handlers, priorities, Pre and Post events, cancellation, bubbling, clicks and drags |
| [Data binding](binding.md) | Keeping controls in step with states in a model, one way or two |
| [Event snapshots](snapshots.md) | Comparing a component's state when an event fired with its state now |
| [Input and focus](input-focus.md) | Keys, modifiers, focus rules, tab order, text input, what a key meets, devices |
| [Commands and shortcuts](commands.md) | Commands shared by buttons, menu items and switches, shortcuts and their scopes, mnemonics, default and cancel buttons |
| [Screens](screens.md) | Opening screens with `Strata.screens()`, the lifecycle, handles, closing and titles, rebuilding, screens above screens, stacks and theme reloads |
| [Threads](threading.md) | The UI thread, handing work over from other threads, and what is checked |
| [Themes](theming.md) | Style keys, paints, sprites, nine-slices, themes by id and theme files |
| [Text](text.md) | Text styles, spans, measurement and wrapping |
| [Internationalization](internationalization.md) | Right to left screens, mixed direction text, plurals, numbers in the user's language and grapheme clusters |
| [Animation](animation.md) | Moving properties over time, easings, fading, the built-in motion and testing it |
| [Drag and drop](drag-and-drop.md) | Transfers between lists, fields and other applications, drop targets, effects and edge scrolling |
| [Clipboard](clipboard.md) | Copying and pasting text, files, images and objects of your own |
| [File dialogs](files.md) | Open, save and folder dialogs the platform provides |
| [Testing](testing.md) | Driving screens without an engine: finding components, input, the clock and checking the result |
| [Extending Strata](extending.md) | Changing built-ins through their events, composition, composites of private parts, custom components with their own events, every hook and when it runs, and custom layouts |
