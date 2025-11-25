# Version 0.0.5 - AKARI
- Fixed the capabilities and claimed kits not saving when a player dies, rejoins the world or changes dimensions. This was mainly because the onCLone logic was wrong. It's fixed now tho... (major bug)
- Added support for 1.20.1 (major addition)
- Added `autoRollFR` means auto-roll from right-click in config. This makes it so that instead of opening a whole gui to roll it just auto rolls for you and gives you the items. (minor addition)

# Version 0.0.4 - ARI
- Destroy starter kit on death if "give type" is `ALWAYS` (minor addition)
- Fixed discriminator packet error on dedicated server (minor affix)
- Added the ability to reward starter kit `ON_DEATH` in GiveSelectorMode : `giveSelectorMode` in config while also allowing `ALWAYS` to give on death (minor addition)
- Updated kit list command to be more informative (minor addition)
- Fixed kit usages not updating (minor affix)
- Deletes kit from existence if its on `ALWAYS` or `ON_DEATH` or `ON_DEATH_NOT_CLAIMED` (minor addition)
- `ON_DEATH_NOT_CLAIMED` only give on death if has never claimed or spun (minor addition)

# Version 0.0.3 - ARI
- Fixed Random kit selection item not breaking kit-selector when usages had been exhausted due to packet mis alignment. (major bug)
- Added config option `breakKitSelector` to toggle the kit-selector item breaking feature when usages exhausted. (minor affix)
- Added the ability to specify in config `breakSelectorOnRandomConfirm` to break the selector when a random roll is confirmed. (minor affix)
- Added tooltip position adjustment of items in kit selection screen as they sometimes go below the screen for items with long tooltips. (minor affix)
- Added hover indicator for unclaimable kits removing what was at the left edge of the kit previously. (minor affix)
- Added the option to enable infinite claiming of kits. (minor affix)
- Fixed bug where in random selection result user could click kits. (minor bug)
- Fixed server bug when trying to open starter kit item (minor bug)
- Fixed syncing of config from client to server. (forgotten implementation)

# Version 0.0.2 - ARI
- Fixed mis-rendered selected object in kit creation screen (minor fix)
- Made default selected color white to make default test color (minor fix)
- Added attribute `weight` to kits for random Kit Functionality. This uses the kit usage to try your "gambling" luck to get better kits. DEVN: Only the kits you can access will be chosen to spin from (major addition)
- Added slot-ables attribute to kits (with curious integration)
- Added visual indicator to un-selectable kits and Reason why (minor addition)
- Added permission-based kits with compat of luck-perms and craft-tweaker (minor affix)
- Added the ability for players to roll random kit even when not enforced (can put this off in config)
- Added three commands `starterkits reload` to force reload config, `starterkits list` for a list of all registered kits and kits and `starterkits get_selector` to get the kit selector
- Output received on error or success of creating kits