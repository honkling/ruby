# ruby

a punishment plugin for paper 1.21.1<br />
it probably works on older and newer versions. not tested.

this plugin was built specifically for my own project(s) and
does not necessarily reflect the needs of the common server owner so beware

# commands

|                Usage                   | Permission                                          | Description                                                                                                                     |
|----------------------------------------|-----------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------|
| `/punish (player) [reason] [...notes]` | `ruby.punish`                                       | punish a player                                                                                                                 |
| `/punishments (player)`                | `ruby.punishments.other` (to view another person's) | view punishments. any player can view their own punishments, but only moderators can view specific details / revert punishments |

# api

the api isn't too consistent or nice to work with,
but look at `me.honkling.ruby.punishment.PunishmentsKt` and
`me.honkling.ruby.punishment.Punishment`.