---
hide:
  - navigation
---

# Strata

Strata is a GUI library for Java that does not depend on any engine, whether you are writing a game, a business application or a tool. You describe a screen as a tree of components: labels, images, buttons, checkboxes, sliders, text boxes and the panels that lay them out. An adapter then draws that tree and feeds it input on a particular engine. The same screen code runs in a Swing window on your desktop and on any engine that has an adapter.

![A small form drawn by Strata](assets/images/quickstart.png)

```java
--8<-- "quickstart/SettingsScreen.java:screen"
```

## What you get

- Two-pass layout (measure, then arrange) with six containers: `AnchorPanel`, `StackPanel`, `GridPanel`, `WrapPanel`, `AbsolutePanel` and `ScrollPanel`, plus a `Spacer`. Children can be weighted, spread, spanned across cells, ordered by z index, and lined up by the baseline of their text. Screens mirror for right to left languages.
- Built-in controls: labels with word wrapping, images, buttons and toggle buttons, checkboxes, sliders, text boxes and text areas with selection, undo and the clipboard, lists, tables, trees, grids, combo boxes, menus, tabs, date and time pickers, windows and message boxes. The password box keeps its text out of events, snapshots and the clipboard.
- Click counting, dragging, hover, keyboard focus and tab order, handled the same way on every engine. Drag and drop carries text, files and your own data between controls, and in from other applications where the engine allows it.
- Commands with shortcuts that a button, a menu item and a toolbar share, mnemonics, default and cancel buttons, and context menus.
- Popups, modal dialogs, toasts and tooltips, drawn in layers above the screen.
- Screens opened the same way on every engine, with `Strata.screens().open(screen)`, and screens opened above others.
- Typed events with priorities and bubbling to ancestors. A Pre event can be canceled, which skips the default behavior, or given another value; a Post event reports every change, whether the user or code made it.
- Lazy event snapshots, so a handler can compare a component's state at the moment an event fired with its state now.
- Data binding that keeps a control in step with a state in your model, one way or two, with converters and validation that show an invalid field and say why.
- Themes that map style keys to colors, gradients, rounded corners, sprites and nine-slices. A missing key is reported before anything draws, along with every other missing key.
- Drawing for your own components: lines, shapes, arcs, shadows, gradients at any angle and computed pixels, on every engine, and animation of any property.
- Three ways to extend: decorate a built-in control, compose several, or write your own component or layout on the same base classes the built-ins use.
- A small SPI for engine adapters, with contract tests that hold every adapter to the same rules. An engine that draws only when asked can render on demand.
- A test kit that opens your screens without a window, finds components, clicks and types, and advances the clock.

## Where to start

<div class="grid cards" markdown>

- [Installation](getting-started/installation.md): requirements, the Maven setup, adding Strata to a Swing app, and building from source.
- [Your first screen](getting-started/first-screen.md): a step-by-step walk through the screen above, run in a Swing window.
- [Core concepts](getting-started/concepts.md): the handful of ideas everything else builds on.
- [Guides](guides/index.md): components, layout, controls, overlays, events, binding, commands, screens, threading, themes, text, animation, drag and drop, testing and extending, each in depth.
- [Platforms](platforms/index.md): the Swing, Vulkan and Minecraft adapters, how engines differ, and how to write an adapter of your own.
- [Minecraft](platforms/minecraft/index.md): Strata screens in mods on NeoForge, Forge, Fabric and SpongeVanilla, in vanilla's look and following resource packs.
- [FAQ](help/faq.md) and [Troubleshooting](help/troubleshooting.md): short answers to common questions, and fixes for common errors.

</div>

## Project status

Strata is at version 0.1.0 and has not been released. The API will still change. [Status and limitations](help/status.md) lists what works, what is untested and what is not supported.

Strata is released under the Apache License, Version 2.0.
