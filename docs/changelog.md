### HyUI Changelog

#### 0.6.1 - 3 Feb 2026
- Add getter for template processor on the UI context.
- Add "margin" aliases for anchor-*.
- Remove clip children.

#### 0.6.0 - 1 Feb 2026
- Add per-player image caching - see the docs for more info.
- Add async image loading (not the best).
- Add page refreshing at a set rate with full page and UI context access, similar to HUD refreshes.
- Add the ability to register template components from resources - `registerComponentFromFile(name, resourcePath)`

#### 0.5.10 - 31 Jan 2026
* Fixed layout mode being applied to buttons and not group surrounding button.
* Refactor: use supplier instead of direct value - thank you @Farrael!
* Feature: Allow negative anchors - thank you @WolverinDEV
* Changed from ARR to LGPLv3 license.

#### 0.5.10 - 30 Jan 2026
* Fix field access checks.

#### 0.5.9 - 30 Jan 2026
* Fix tab navigation builder not supporting template runtime interactions.
* Add scrollbar style support for divs, same way as the textarea.
* Add sub-components to Template Processor.
* Add "hot-reload" of HYUIML (html) from project resources. This assumes one of the candidate paths exists (src/main/resources, build/resources/main, ../src/main/resources, or ../build/resources/main).

#### 0.5.8 - 29 Jan 2026
- Rework internally how logging occurs, it is now enabled in releases, at Finest level only.
- Add helper methods to ItemGridBuilder to update/remove/get all slots, the tutorial has also been updated to showcase this.
- Add support for replacing button builders for tab navigation - this complements the existing selected/unselected styling. Use TabNavigationBuilder.addTab(id, label, contentId, buttonBuilder) , this means every tab's button can have its own builder. By default, tabs are built with secondary/primary button builders. You can of course update/remove tabs as well.
- Add support for CustomButtonBuilder for tab navigation.
- Add [tutorial](https://hyui.gitbook.io/docs/tutorials/tutorial-tab-navigation) for Tab Navigation.

#### 0.5.7 - 29 Jan 2026
- Add events for ItemGrid. See tutorial: [here](https://hyui.gitbook.io/docs/tutorial-working-with-item-grids). 
- Add Multi-Line Text Field (textarea). 
- Ensure store in HUDs is always called with a 'fresh' store. This means there is no need to pass a store to a HUD on creation, or opening. These are marked as deprecated. Thanks to Willem for this!

#### 0.5.6 - 28 Jan 2026
- Fix button text not being set.
- Fix progressbar texture paths not working as expected.

#### 0.5.6 - 28 Jan 2026
- Fix button text not being set.
- Fix progressbar texture paths not working as expected.

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
