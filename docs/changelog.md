### HyUI Changelog

#### 0.5.5 - 27 Jan 2026
- Update validation rules according to schema.
- Add helper method to cast a value to a particular type (UIContext.getValueAs).
- Optimize jar size.
- Add custom button support.

#### 0.5.4 - 26 Jan 2026
- Add template runtime evaluation.
- Add helper method to get a builder as a casted type.
- CHANGE: Do not force clearing a page when reloading an image, allow caller to decide: `reloadImage(String dynamicImageElementId, boolean shouldClearPage)`

#### 0.5.3 - 25 Jan 2026
- Add decorated container.
- Add a per-player image limit of 10.
- Add Hyvatar.io component.
- Add small secondary and tertiary buttons.
- Fix text alignment issues.
- Remove support for font size on buttons (client crash).

#### 0.5.2 - 24 Jan 2026
- Add access to custom styles from HYUIML.
- Add dynamic images from remote sources.
- Add circular progress bar support.

#### 0.5.1 - 24 Jan 2026
- Add default events to all elements that handle value changes, this allows us to always capture data updates.
- Add numberfield support for MinValue, MaxValue, Step, MaxDecimalPlaces.
- Fix bug with padding not being applied.
- Add conditionals, logical operators and string comparison to template.
- Add loops to template processor.

#### 0.5.0 - 23 Jan 2026

- Added `ItemGrid`.
- Added `ItemSlot`.
- Added `TabNavigation`.
- Added timer label builder (multiple formats).
- Added `TemplateProcessor` for HYUIML, including reusable components with variables/default values.
- BREAKING CHANGE: `flex-weight` now applies to the outer wrapping group (all elements except `Group`/`Label` are wrapped). This can change layout behavior compared to earlier versions.
