### Template Processor

The Template Processor lets you pre-process HYUIML with variables, filters, and reusable components before HyUI parses it into builders. The showcase command is a good reference: `src/main/java/au/ellie/hyui/commands/HyUIShowcaseCommand.java`.

#### Variable Interpolation

Use `{{$variable}}` in HYUIML and set values in Java:

```java
TemplateProcessor template = new TemplateProcessor()
    .setVariable("playerName", playerRef.getUsername())
    .setVariable("playerLevel", 42);
```

```html
<p>Player: {{$playerName}}</p>
<p>Level: {{$playerLevel}}</p>
```

Defaults and filters are supported:

```html
<p>Missing value: {{$missing|Not Set}}</p>
<p>Uppercase: {{$playerName|upper}}</p>
<p>Number: {{$playerGold|number}}</p>
```

Dot paths are supported for nested data:

```html
<p>Tier: {{$meta.tier}}</p>
<p>First item name: {{$items.0.name}}</p>
```

#### Each Loops

Iterate over lists, arrays, or any `Iterable` with `{{#each}}`.
Inside the loop, item fields and getters are available as variables, and the full item is available as `item`.

```java
TemplateProcessor template = new TemplateProcessor()
    .setVariable("items", items);
```

```html
{{#each items}}
  <p>{{$name}} ({{$meta.tier}})</p>
{{/each}}
```

#### If Conditionals

Conditionals support truthy checks, comparisons, logical operators, and `contains`.

```html
{{#if isAdmin}}
  <p>Admin mode</p>
{{else}}
  <p>Standard mode</p>
{{/if}}

{{#if power >= minPower && rarity != Common}}
  <p>Strong item</p>
{{/if}}

{{#if meta.source contains "Craft" || rarity == Epic}}
  <p>Highlight</p>
{{/if}}
```

Supported operators:
- Equality: `==`, `!=`
- Numeric: `>`, `<`, `>=`, `<=`
- Logical: `&&`, `||`, `!`
- Contains: `contains` (strings, arrays, iterables, map keys)

#### Components (Reusable Blocks)

Register a component template and inject parameters when you use it.

```java
TemplateProcessor template = new TemplateProcessor()
    .registerComponent("statCard", """
        <div style="background-color: #2a2a3e; padding: 10; anchor-width: 120; anchor-height: 60;">
            <p style="color: #888888; font-size: 11;">{{$label}}</p>
            <p style="color: #ffffff; font-size: 18; font-weight: bold;">{{$value}}</p>
        </div>
        """);
```

```html
{{@statCard:label=Blocks Placed,value=12.847}}
{{@statCard:label=Creatures Found,value=23}}
```

Notes:
- Component parameters replace `{{$paramName}}` placeholders inside the component template.
- Component templates can include normal `{{$variable}}` placeholders, which are processed after component inclusion.

#### Combining Components with Loops + Models

Components can be used inside `{{#each}}` blocks, and they can also include their own `{{#if}}` blocks.
Items in a loop expose fields and getters directly, so models are straightforward to use.

```java
TemplateProcessor template = new TemplateProcessor()
    .setVariable("items", items)
    .setVariable("minPower", 10)
    .registerComponent("showcaseItem", """
        <div style="background-color: #2a2a3e; padding: 8; anchor-height: 40; flex-direction: row;">
            <p style="color: #ffffff; flex-weight: 2;">{{$name}} (Tier: {{$meta.tier}})</p>
            {{#if power >= minPower && rarity != Common}}
            <p style="color: #4CAF50; flex-weight: 1;">Power {{$power}}</p>
            {{else}}
            <p style="color: #888888; flex-weight: 1;">Power {{$power}}</p>
            {{/if}}
        </div>
        """);
```

```html
{{#each items}}
  {{@showcaseItem:name={{$name}},meta.tier={{$meta.tier}},power={{$power}},rarity={{$rarity}}}}
{{/each}}
```

Model support:
- Public fields and getters (`getX()` / `isX()`).
- Nested models via dot paths (`{{$meta.tier}}`).
- Lists/arrays via index (`{{$items.0.name}}`).

#### How the Showcase Uses It

In `HyUIShowcaseCommand`, the `TemplateProcessor` is used to:

- Define player stats (variables) like `playerName`, `playerLevel`, and `playerGold`.
- Register reusable blocks such as `statCard` and `featureItem`.
- Render the final HYUIML string with `PageBuilder.fromTemplate(...)`.

This keeps the HYUIML readable while avoiding duplicated markup for repeated UI patterns.
