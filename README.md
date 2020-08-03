[![GitHub license](https://img.shields.io/github/license/OpenSrcerer/thermostat?style=flat-square)](https://github.com/OpenSrcerer/thermostat/blob/master/LICENSE)
[![GitHub stars](https://img.shields.io/github/stars/OpenSrcerer/thermostat?style=flat-square)](https://github.com/OpenSrcerer/thermostat/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/OpenSrcerer/thermostat?style=flat-square)](https://github.com/OpenSrcerer/thermostat/network)
[![GitHub issues](https://img.shields.io/github/issues/OpenSrcerer/thermostat?style=flat-square)](https://github.com/OpenSrcerer/thermostat/issues) 

# Online Status

<a href="https://top.gg/bot/700341788136833065" >
  <img src="https://top.gg/api/widget/status/700341788136833065.svg" alt="Thermostat" />
</a>
<a href="https://top.gg/bot/700341788136833065" >
  <img src="https://top.gg/api/widget/servers/700341788136833065.svg" alt="Thermostat" />
</a>
<a href="https://top.gg/bot/700341788136833065" >
  <img src="https://top.gg/api/widget/upvotes/700341788136833065.svg" alt="Thermostat" />
</a>  

# Thermostat
Thermostat is a bot created for Discord servers, in order to help out server owners who do not have the time or simply want their channel slowmode to be adjusted automatically. Staff teams should be focused on moderating the server and the people in it. By getting Thermostat in your server, you get rid of all of the stress that keeping an adequate slowmode might bring.

# Motivation
This bot was created with larger servers in mind, in order to keep the chat's speed readable and under control, while also defending against large raids & spam attacks. 

# Features & How To Use
What makes Thermostat special is the fact that it controls Discord slowmode in a dynamic way, calculating the needed slowmode for a channel dependent on the number of incoming messages in a unit of time. <a href="https://github.com/OpenSrcerer/thermostat/wiki">It comes with easy set up, which is only a few commands.</a> That's it! You set it, and forget it. Thermostat does not require continuous changes or maintenance.   
All you have to do is get started with Thermostat is:

<ul>
  <li><a href="https://top.gg/bot/700341788136833065">Invite Thermostat to your server.</a></li>
  <li>Set up your server prefix by sending the <code>@Thermostat prefix</code> command.</li>
  <li>Monitor the channels you need with <code>`prefix`monitor `channels/categories`</code>.</li>
  <li>Set up the maximum slowmode upper bound for your channels using <code>@`prefix`setmaximum `channels/categories`</code>.</li>
</ul>

**Note:** <code>\`prefix\`</code>, and <code>\`channels/categories\`</code> are variables that should be replaced as per your need. For example, if your prefix is <code>th!</code> and you're trying to monitor a channel called "#general", you would type <code>th!monitor #general</code>.

# Troubleshooting
The most common issue with all bots on Discord is permission errors. If a bot is not working properly, the most likely cause is a permission issue. If you need a guide to take you through that, <a href="https://support.discord.com/hc/en-us/articles/206029707-How-do-I-set-up-Permissions-">Discord has a great article all about it</a>.

**Thermostat currently needs these permissions in a channel to function correctly:**
<ol>
  <li><code>Manage Channels</code></li>
  <li><code>Read Messages</code></li>
  <li><code>Send Messages</code></li>
  <li><code>Manage Messages</code></li>
  <li><code>Embed Links</code></li>
  <li><code>Read Message History</code></li>
  <li><code>Add Reactions</code></li>
</ol>

The bot is going to react accordingly to the permissions it is missing. Please make sure that before you submit an issue, you have validated that Thermostat has all needed permissions in the specific channel you're trying to use it in.
If you still have problems, <a href="https://discord.gg/FnPb4nM">join our support server</a>.

# Current Dependencies
<ul>
  <li><a href="https://github.com/DV8FromTheWorld/JDA">JDA</a></li>
  <li><a href="https://github.com/JDA-Applications/JDA-Utilities">JDA-Utilities</a></li>
  <li><a href="https://dev.mysql.com/downloads/connector/j/3.1.html">MySQL JDBC Connector</a></li>
  <li><a href="http://logback.qos.ch/">Logback</a></li>
  <li><a href="https://github.com/brettwooldridge/HikariCP">HikariCP</a></li>
  <li><a href="https://github.com/DiscordBotList/DBL-Java-Library">Discord Bot List Java Library</a></li>
</ul>

# License
**The license used allows:**   
<ul>
  <li>Using the code provided in this repository for commercial use.</li>
  <li>Modifying the code provided in this repo.</li>
  <li>Privately using the code.</li>
</ul>

**However, you must:**   
<ul>
  <li>Distribute the code under the same license.</li>
  <li>State which snippets you have modified.</li>
  <li>Disclose the source of the code.</li>
</ul>   

**GNU © OpenSrcerer**
