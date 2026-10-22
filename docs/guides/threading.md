# Threads

An application loads orders on a worker thread and puts them in a table. A client receives a price update on its network thread. Strata's components are not thread safe, and a change made from the wrong thread corrupts layout or event state without any sign at the time. Strata therefore names one thread for each host, the UI thread, tells you how to get work onto it, and throws when a call comes from anywhere else.

## The UI thread

The UI thread is the thread that dispatches a host's input, lays it out and renders it. On Swing it is the event dispatch thread. Components, overlays and handlers belong to it: once a component is attached to a host, you use it only there. `State.set` is the exception, and so is the hand-over below.

`UiThread` (`org.inspirenxe.strata.api`) is the way in. Get it from `ComponentFactory.uiThread()` for a host, or from `Strata.uiThread()` for the platform that `Strata.screens()` opens screens on. It is an `Executor`:

| Method | Does |
|---|---|
| `execute(task)` | Runs the task on the UI thread, after the tasks given before it |
| `submit(supplier)` | The same, and returns a `CompletableFuture` that completes on the UI thread with the result |
| `isCurrent()` | True when the calling thread is the UI thread |
| `checkCurrent()` | Throws `IllegalStateException` off the UI thread |

All of them may be called from any thread.

## Handing work over

A server update arrives on a network thread. The screen hands the change to the UI thread, and names the person who made it so handlers can tell it from the user's own edit:

```java
--8<-- "threading/ThreadingExamples.java:update"
```

The rules for tasks:

- Tasks run in the order they were given.
- Each task starts with no cause frame open, so what it changes reports `Origin.CODE` unless the task opens a frame of its own, as the example does. See [Who caused it](events.md#who-caused-it).
- `execute` called on the UI thread queues the task too, and it runs after the current dispatch. Code behaves the same from every thread. A caller that wants to run at once checks `isCurrent()` first.
- A task that throws is logged through `System.Logger` and the next one runs. A task given to `submit` completes its future exceptionally instead and logs nothing.
- Queuing a task asks the platform's hosts for a frame, so an idle host that renders on demand picks it up. See [Writing an adapter](../platforms/writing-an-adapter.md).

`submit` is for reading something back:

```java
--8<-- "threading/ThreadingExamples.java:submit"
```

## States need no hand-over

`State.set` is safe from any thread. The state records the change and bindings follow it on the UI thread, in the next frame. A model that a worker updates therefore exposes states, and the screen binds to them. See [Data binding](binding.md).

## What is checked

An attached component checks the calling thread in these calls:

- Anything that changes layout, which includes nearly every setter that changes size or content.
- Adding, inserting, moving and removing children.
- `onEvent`, `subscribe` and `bind`.
- `repaintAfter`.
- Opening and closing an overlay, and giving an open overlay a handler.
- Every method of the animator. See [Animation](animation.md).

A call from the wrong thread throws `IllegalStateException`. The message names the calling thread and the UI thread and says to hand the work over with `UiThread.execute` or to set a `State`. Each check costs one comparison with the recorded thread, or a call to the platform's test.

The check comes at the first step that needs the UI thread. A setter may already have changed its data by then, so do not trust a component that threw: put it right from the UI thread.

Only components attached to a host are checked. A tree built on a worker thread belongs to no one until it is added, so a worker may build rows and a UI task may attach them:

```java
--8<-- "threading/ThreadingExamples.java:worker-tree"
```

## Opening screens

`Strata.screens().open` is safe from any thread. Called off the UI thread it hands the opening over and returns a handle at once. See [Screens](screens.md).

## How Strata knows the thread

The adapter can name its thread. An engine with a main thread executor gives it to core, and core hands every task to it. An adapter that can test the thread cheaply gives a test, such as `SwingUtilities::isEventDispatchThread`. Both are optional:

- Without an executor, core queues tasks and runs them at the start of the next frame (the next `flush`, layout or render) of any host of the platform. A task queued by a task waits for the following frame.
- Without a test, the first thread that sends input to, lays out or renders a host of the platform becomes the UI thread, for good. Until a thread is known, `isCurrent()` is false and `checkCurrent()` lets every thread pass.
- All the hosts of one platform share one UI thread.

An adapter whose screen controller is its own, not core's, tells core which platform it belongs to so `Strata.uiThread()` can find the thread. See [Writing an adapter](../platforms/writing-an-adapter.md).

## In tests

The test kit's tester drives its screen from the thread that opens it, and that thread is the UI thread. Tasks given to `uiThread().execute` run when the test calls `frame()`, `advance` or any input method. See [Testing](testing.md).

## Defaults

| What | Default |
|---|---|
| Without a platform executor | Tasks run at the start of the next flush, layout or render of any host of the platform |
| Without a platform thread test | The first thread that drives a host is the UI thread |
| Cause frame inside a task | None, so changes report `Origin.CODE` |
