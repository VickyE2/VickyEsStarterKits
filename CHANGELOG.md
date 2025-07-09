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