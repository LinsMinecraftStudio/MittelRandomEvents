# MittelRandomEvents

**Make every minute on your server worth looking forward to.**

## Links
**GitHub**: <https://github.com/LinsMinecraftStudio/MittelRandomEvents>  
**Discord**: <https://discord.gg/9YP2UaPZhE>

**简体中文链接**: <https://www.minebbs.com/resources/mittelrandomevents-folia.18461/>  
**QQ群**: 1107921027

## The Problem

You've built the map. You've designed the gameplay. You've configured the rewards. Players join, look around, and leave.

It's not that the content is bad — it's that it's *static*. Everything is hardcoded. Once players figure out the pattern, there's nothing left to anticipate.

**MRE exists to fix that.**

It won't build your maps or design your gameplay. It does exactly one thing:

**It lets your server run, shift, and surprise on its own — even when you're not watching.**

Event lines fire, weights roll, conditions gate. Players never know what's coming next.

## Not Just Another "Timed Reward" Plugin

Most random-event plugins on the market do one of two things: broadcast a scheduled message, or hand out a random reward. Players see through it after two uses.

MRE is built on two core concepts:

- **Event** — *what happens.* A sequence of actions, with optional conditions, weight, and duration.
- **Event Line** — *when it happens.* Runs on a fixed interval or a Cron schedule, then picks a weighted event from its pool.

Natural disasters, blessings, dungeon invasions, traveling merchants, random bosses, world mutations — if you can imagine it, you can slot it in.

In short: your server gets its own **fate system**.

## Timing, Your Way

- **Fixed interval** — every 30 seconds, every 5 minutes, whatever you need
- **Cron expressions** — 3 AM, Friday 8:30 PM, every day at noon — down to the minute
- **Manual trigger** — for testing or on-the-fly events, one click

## Random, But Sensible

- Each event has its **own weight** within a line — higher weight, more frequent
- **Empty event slots** are supported — "nothing happens" can be part of the pool
- **Condition filtering** runs before selection — ineligible events don't even roll

Random doesn't mean chaotic.

## Action Choreography

| Category | What it can do |
|---|---|
| Target Selection | Players / worlds (filter by permission, by world; shuffle; limit count) |
| Effects | Heal / damage / clear inventory / play sound / change weather |
| Messaging | Private message / server broadcast |
| Commands | Console or player commands, with `%placeholder%` interpolation |
| Flow Control | Stop remaining actions / skip the next action |

Actions run in order, and each one passes its result to the next. So you can chain things like:

> Select all VIP players → run a command for each → play a sound → broadcast an announcement

**A concrete example:**

> Every day at 20:00, there's a 30% chance a "Traveling Merchant" arrives. Server-wide broadcast + 3 random players receive a discount coupon. Lasts 10 minutes.

## Condition System

11 built-in condition types: online player count, total players, world count, server TPS, server version, plugin state, player permissions, player name / world name matching, and more.

Combine them with `all_of` / `any_of` / `not` logic gates to build arbitrarily complex rules.

For example: *"TPS above 15 **AND** more than 30 players online **AND** not during early morning hours."*

If the server isn't in the right state, nothing fires.

## A Beginning and an End

Events support a **duration**: a "start action" runs on trigger, the state persists for the duration, and an "end action" runs automatically when time is up.

Limited-time events, phased encounters, boss arrivals — events have a full lifecycle, not just a single instant.

> **Note:** If your event temporarily modifies values, remember to restore them in the "end action."

## Full GUI Configuration

Everything is configurable in-game:

A main menu leads into paginated lists for events and event lines. Create, rename, change icons, enable/disable, delete. Visually compose actions and conditions inside events; visually set weights, intervals, Cron schedules, and durations inside event lines. Start, stop, and trigger instantly.

Don't want to write YAML? You don't have to. The entire pipeline can be clicked together.

![A gui picture](https://cdn.modrinth.com/data/cached_images/d5d6c4e32237e89962d47f83ef31712aafe5e82b_0.webp)

## Compatibility

- **PlaceholderAPI + MiniPlaceholders support**
- More plugin integrations on the way

## Placeholders

MRE registers the following 9 global placeholders automatically when PlaceholderAPI and/or MiniPlaceholders is enabled. They do not require a player context.

Replace `EVENT_ID` and `LINE_ID` with an event ID and an event-line ID respectively, not their display names. For example: `%mittelrandomevents_event_enabled_merchant%` or `<mittelrandomevents_event_enabled:merchant>`.

### PlaceholderAPI

| Placeholder | Description | Missing ID / unavailable state |
|---|---|---|
| `%mittelrandomevents_event_count%` | Total number of registered events, including disabled events. | `0` when no events are registered. |
| `%mittelrandomevents_line_count%` | Total number of registered event lines. | `0` when no lines are registered. |
| `%mittelrandomevents_event_enabled_EVENT_ID%` | Whether the event is enabled: `true` or `false`. | `false` if the ID is omitted or unknown. |
| `%mittelrandomevents_event_display_name_EVENT_ID%` | The event's display name, serialized with its formatting. | Empty string if the ID is omitted or unknown. |
| `%mittelrandomevents_line_running_LINE_ID%` | Whether the line has an active, non-cancelled Cron task: `true` or `false`. | `false` if the ID is omitted or unknown, or the line is not scheduled. |
| `%mittelrandomevents_next_event_remaining_LINE_ID%` | Seconds until the line's next scheduled Cron execution, rounded up. | `0` if the ID is omitted or unknown, or the line is not scheduled. |
| `%mittelrandomevents_next_event_remaining_formatted_LINE_ID%` | The same countdown, formatted using MittelLib's countdown format. | Literal `0` if the ID is omitted or unknown; formatted zero if the line exists but is not scheduled. |
| `%mittelrandomevents_current_event_LINE_ID%` | ID of the event currently held by the line. | Empty string if the ID is omitted or unknown, or the line has no current event. |
| `%mittelrandomevents_current_event_display_name_LINE_ID%` | Display name of the line's current event, serialized with its formatting. | Empty string if the ID is omitted or unknown, or the line has no current event. |

With MittelLib 1.4.8, PlaceholderAPI arguments are split on underscores and MRE uses only the first argument as the ID. Use IDs without underscores (hyphens work), or use MiniPlaceholders for IDs containing underscores.

### MiniPlaceholders

| Placeholder | Description | Missing ID / unavailable state |
|---|---|---|
| `<mittelrandomevents_event_count>` | Total number of registered events, including disabled events. | `0` when no events are registered. |
| `<mittelrandomevents_line_count>` | Total number of registered event lines. | `0` when no lines are registered. |
| `<mittelrandomevents_event_enabled:EVENT_ID>` | Whether the event is enabled: `true` or `false`. | `false` if the ID is omitted or unknown. |
| `<mittelrandomevents_event_display_name:EVENT_ID>` | The event's formatted display name. | Empty text if the ID is omitted or unknown. |
| `<mittelrandomevents_line_running:LINE_ID>` | Whether the line has an active, non-cancelled Cron task: `true` or `false`. | `false` if the ID is omitted or unknown, or the line is not scheduled. |
| `<mittelrandomevents_next_event_remaining:LINE_ID>` | Seconds until the line's next scheduled Cron execution, rounded up. | `0` if the ID is omitted or unknown, or the line is not scheduled. |
| `<mittelrandomevents_next_event_remaining_formatted:LINE_ID>` | The same countdown, formatted using MittelLib's countdown format. | Literal `0` if the ID is omitted or unknown; formatted zero if the line exists but is not scheduled. |
| `<mittelrandomevents_current_event:LINE_ID>` | ID of the event currently held by the line. | Empty text if the ID is omitted or unknown, or the line has no current event. |
| `<mittelrandomevents_current_event_display_name:LINE_ID>` | Formatted display name of the line's current event. | Empty text if the ID is omitted or unknown, or the line has no current event. |

`line_running` describes scheduling, not whether an event is currently active. The `next_event_remaining` placeholders count down to the next Cron execution, not the end of the current event; that execution does not necessarily start a new event. Events with no duration are cleared immediately after their actions finish, so they normally do not appear in `current_event` placeholders afterward.

### Console Command Variables

The `command` parameter of `execute_console_command` also supports the following local variables. These use `%name%` syntax and do not require PlaceholderAPI or MiniPlaceholders.

| Variable | Description | Availability |
|---|---|---|
| `%player%` | Selected player's name. | When `runForEachPlayer: true`. |
| `%playerUuid%` | Selected player's UUID. | When `runForEachPlayer: true`. |
| `%playerWorld%` | Name of the selected player's current world. | When `runForEachPlayer: true`. |
| `%playerX%` | Selected player's X coordinate, including the fractional part. | When `runForEachPlayer: true`. |
| `%playerY%` | Selected player's Y coordinate, including the fractional part. | When `runForEachPlayer: true`. |
| `%playerZ%` | Selected player's Z coordinate, including the fractional part. | When `runForEachPlayer: true`. |
| `%parameterName%` | Value of the key `parameterName` in the action or event context, converted to text. Replace `parameterName` with the actual, case-sensitive key. | When that key is present in `action.parameters()` or `context.parameters()`. |

Action parameters override context parameters with the same key; injected per-player values override both. Undefined local variables are left unchanged by this interpolation step. For example, `command: "give %player% minecraft:diamond %rewardAmount%"` can use a custom action parameter `rewardAmount: 3` with `runForEachPlayer: true`.

`%player%` is also resolved by MittelLib when a player context is supplied, including `message_players` and `execute_player_command`. The other local variables above are expanded by MRE's console-command interpolation.

## For Developers: Three Lines of Code

MRE exposes a stable integration API. Other plugins can register their own action types, events, and event lines:

```java
MittelRandomEventsAPI.registerActionType(myAction); // register a custom action
MittelRandomEventsAPI.registerEvent(myEvent);       // register a custom event
MittelRandomEventsAPI.registerLine(myLine);         // register and schedule immediately
```

Custom contexts, custom actions, deep integration.

## What Can You Use It For?

If you want your server to *do something*, MRE can probably handle it:

- Survival server daily events
- RPG world random encounters
- Minigame server activities
- Seasonal and holiday events
- Dungeon and challenge mechanics
- Online player rewards
- Server announcement integration
- Cross-plugin automation pipelines
- Admin testing and ad-hoc events

You can make your server lively — or keep events restrained, rare, and mysterious. What matters isn't randomness itself; it's making every random moment fit the rhythm of *your* server.

## What's Next? Help Shape It

MRE is in its first-release stage. Next up: starter events, a short setup guide, and improvements based on real server feedback. See the [roadmap](ROADMAP.md) for the current priorities.

Try an event, share a configuration, suggest an idea, improve a translation, or send a small fix — **you do not need to be a developer to help**. Join us through the GitHub and Discord links above, or QQ group **1107921027**, and tell us what you want your server to do next.
