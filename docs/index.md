---
hide:
  - navigation
---

# Strata

Strata is a GUI library for Java games that does not depend on any engine. You describe a screen as a tree of components: labels, images, buttons, checkboxes, sliders, text boxes and the panels that lay them out. An adapter then draws that tree and feeds it input on a particular engine. The same screen code runs in a Swing window on your desktop and on any engine that has an adapter.

![A small form drawn by Strata](assets/images/quickstart.png)

```java
--8<-- "quickstart/SettingsScreen.java:screen"
```

## What you get

- Two-pass layout (measure, then arrange) with five containers: `AnchorPanel`, `StackPanel`, `GridPanel`, `AbsolutePanel` and `ScrollPanel`, plus a `Spacer`. Children can be weighted, spread, spanned across cells, and ordered by z index.
- Built-in controls: labels with word wrapping, images, buttons, checkboxes, sliders, text boxes and text areas with selection and the clipboard, lists, tables, trees, combo boxes, menus, tabs, date and time pickers, windows and message boxes. The password box keeps its text out of events, snapshots and the clipboard.
- Click counting, dragging, hover, keyboard focus and tab order, handled the same way on every engine.
- Popups, modal dialogs, toasts and tooltips, drawn in layers above the screen.
- Screens opened the same way on every engine, with `Strata.screens().open(screen)`, and screens opened above others.
- Typed events with priorities and bubbling to ancestors. A Pre event can be canceled, which skips the default behavior, or given another value; a Post event reports every change, whether the user or code made it.
- Lazy event snapshots, so a handler can compare a component's state at the moment an event fired with its state now.
- Data binding that keeps a control in step with a state in your model, one way or two.
- Themes that map style keys to colors, gradients, sprites and nine-slices. A missing key is reported before anything draws, along with every other missing key.
- Three ways to extend: decorate a built-in control, compose several, or write your own component or layout on the same base classes the built-ins use.
- A small SPI for engine adapters, with contract tests that hold every adapter to the same rules.

## Where to start

<div class="grid cards" markdown>

- [Installation](getting-started/installation.md): requirements, building from source, and adding Strata to a Swing app.
- [Your first screen](getting-started/first-screen.md): a step-by-step walk through the screen above, run in a Swing window.
- [Core concepts](getting-started/concepts.md): the handful of ideas everything else builds on.
- [Guides](guides/index.md): components, layout, controls, overlays, events, binding, screens, themes, text and extending, each in depth.
- [Platforms](platforms/index.md): the Swing adapter, how engines differ, and how to write an adapter of your own.
- [FAQ](help/faq.md) and [Troubleshooting](help/troubleshooting.md): short answers to common questions, and fixes for common errors.

</div>

## Project status

Strata is at version 0.1.0 and has not been released. The API will still change. [Status and limitations](help/status.md) lists what works, what is untested and what is planned.

Strata is released under the Apache License, Version 2.0.
